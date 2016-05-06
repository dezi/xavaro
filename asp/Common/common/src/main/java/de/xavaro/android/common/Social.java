package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.view.MotionEvent;
import android.view.View;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public abstract class Social
{
    private static final String LOGTAG = Social.class.getSimpleName();

    protected final String platform;
    protected final String locale;

    protected final String expirationpref;
    protected final String accesstokenpref;
    protected final String refreshtokenpref;

    protected String appurl;
    protected String appsecret;
    protected String appkey;

    protected String oauthurl;
    protected String tokenurl;

    protected String[] scopes;

    protected String apiurl;
    protected String apiextraparam;

    protected JSONObject user;
    protected boolean verbose;
    protected File cachedir;

    public Social(String platform)
    {
        this.platform = platform;

        expirationpref = "social." + platform + ".expiration";
        accesstokenpref = "social." + platform + ".accesstoken";
        refreshtokenpref = "social." + platform + ".refreshtoken";

        locale = Simple.getLocaleLanguage() + "_" + Simple.getLocaleCountry();

        cachedir = new File(Simple.getExternalCacheDir(), platform);

        if (! cachedir.exists())
        {
            if (cachedir.mkdirs()) Log.d(LOGTAG, "Constructor: created cache:" + cachedir);
        }
    }

    public boolean isEnabled()
    {
        return Simple.getSharedPrefBoolean("social." + platform + ".enable");
    }

    public boolean isLoggedIn()
    {
        return (Simple.getSharedPrefString(accesstokenpref) != null);
    }

    public boolean isReady()
    {
        return isEnabled() && isLoggedIn();
    }

    public void setVerbose(boolean yesno)
    {
        verbose = yesno;
    }

    @Nullable
    public String getAccessExpiration()
    {
        return isLoggedIn() ? Simple.timeStampAsISO(0) : null;
    }

    @Nullable
    public String[] getAccessScope()
    {
        return scopes;
    }

    @Nullable
    protected String getAccessToken()
    {
        return Simple.getSharedPrefString(accesstokenpref);
    }

    @Nullable
    public Drawable getProfileDrawable(String pfid, boolean circle)
    {
        return ProfileImages.getSocialProfileDrawable(platform, pfid, circle);
    }

    //region Login and logout

    @SuppressLint("SetJavaScriptEnabled")
    public void login()
    {
        //
        // Make sure, session cookies are invalidated
        // to be able to switch account.
        //

        logout();

        //
        // Precreate dialog.
        //

        AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getActContext());

        builder.setNegativeButton("Abbrechen", null);

        final AlertDialog dialog = builder.create();

        //
        // Load control webview client will intercept
        // redirect url with access code.
        //

        WebViewClient webclient = new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.d(LOGTAG, "shouldOverrideUrlLoading=" + url);

                if (url.startsWith(appurl))
                {
                    Uri uri = Uri.parse(url);
                    String code = uri.getQueryParameter("code");

                    Log.d(LOGTAG, "shouldOverrideUrlLoading: code=" + code);

                    JSONObject postdata = new JSONObject();

                    Json.put(postdata, "code", code);
                    Json.put(postdata, "client_id", appkey);
                    Json.put(postdata, "client_secret", appsecret);
                    Json.put(postdata, "redirect_uri", appurl);
                    Json.put(postdata, "grant_type", "authorization_code");

                    String content = SimpleRequest.readContent(tokenurl, postdata);

                    Log.d(LOGTAG, "=====>" + content);

                    if (content != null)
                    {
                        JSONObject jcontent = Json.fromString(content);

                        String accessToken = Json.getString(jcontent, "access_token");
                        Simple.setSharedPrefString(accesstokenpref, accessToken);

                        if (Json.has(jcontent, "expires_in"))
                        {
                            int expiseconds = Json.getInt(jcontent, "expires_in");
                            long expiration = Simple.nowAsTimeStamp() + (expiseconds - 10) * 1000;
                            Simple.setSharedPrefString(expirationpref, Simple.timeStampAsISO(expiration));
                        }

                        if (Json.has(jcontent, "refresh_token"))
                        {
                            String refreshToken = Json.getString(jcontent, "refresh_token");
                            Simple.setSharedPrefString(refreshtokenpref, refreshToken);

                            File socialdir = Simple.getMediaPath("social");
                            File oauthfile = new File(socialdir, platform + ".oauth.json");
                            Simple.putFileContent(oauthfile, content);
                        }
                    }

                    dialog.cancel();

                    return true;
                }

                return false;
            }
        };

        //
        // Upgrade dialog with nice web view.
        //

        FrameLayout dummy = new FrameLayout(Simple.getActContext());
        dummy.setLayoutParams(new FrameLayout.LayoutParams(Simple.MP, Simple.WC));
        dummy.setBackgroundColor(0x88008800);
        dummy.setPadding(0, 0, 0, 0);

        FrameLayout frame = new FrameLayout(Simple.getActContext());
        frame.setLayoutParams(new FrameLayout.LayoutParams(Simple.MP, 500));
        frame.setBackgroundColor(0x88880000);
        frame.setPadding(0, 0, 0, 0);

        WebView webview = new WebView(Simple.getActContext())
        {
            @Override
            public boolean onCheckIsTextEditor()
            {
                return true;
            }
        };

        webview.setWebViewClient(webclient);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(false);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        frame.addView(webview);
        dummy.addView(frame);
        dialog.setView(dummy);
        dialog.show();

        //
        // Fire up auth url in dialog.
        //

        String url = oauthurl
                + "?client_id=" + appkey
                + "&redirect_uri=" + appurl
                + "&scope=" + getScopeParameter()
                + "&response_type=code"
                + "&access_type=offline"
                + "&prompt=consent";

        Log.d(LOGTAG, "====>" + url);

        webview.loadUrl(url);
    }

    public void logout()
    {
        Uri uri = Uri.parse(oauthurl);

        clearCookies(uri.getScheme() + "://" + uri.getHost());

        File socialdir = Simple.getMediaPath("social");

        File oauthfile = new File(socialdir, platform + ".oauth.json");
        Simple.removeFile(oauthfile);

        File userfile = new File(socialdir, platform + ".user.json");
        Simple.removeFile(userfile);

        Simple.removeSharedPref(expirationpref);
        Simple.removeSharedPref(accesstokenpref);
        Simple.removeSharedPref(refreshtokenpref);

        user = null;
    }

    protected abstract String getScopeParameter();

    protected void clearCookies(String domain)
    {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookiestring = cookieManager.getCookie(domain);

        if (cookiestring != null)
        {
            String expire = "=; Expires=Wed, 31 Dec 2000 23:59:59 GMT";

            String[] cookies = cookiestring.split(";");

            for (String cookie : cookies)
            {
                Log.d(LOGTAG, "clearCookies:" + cookie);

                String[] cookieparts = cookie.split("=");
                cookieManager.setCookie(domain, cookieparts[ 0 ].trim() + expire);
            }
        }
    }

    //endregion Login and logout

    //region Cache driven publics

    @Nullable
    public JSONObject getUserProfile()
    {
        if (! isReady()) return null;

        if (user == null)
        {
            File socialdir = Simple.getMediaPath("social");
            File userfile = new File(socialdir, platform + ".user.json");

            user = Simple.getFileJSONObject(userfile);

            if (user == null)
            {
                user = getGraphUserProfile();
                Simple.putFileJSON(userfile, user);
            }
        }

        return user;
    }

    public JSONArray getUserFeeds(boolean feedonly)
    {
        JSONArray data = new JSONArray();

        getOwnerFeed(data);

        getUserFeeds(data, "friend", feedonly);
        getUserFeeds(data, "like", feedonly);

        return data;
    }

    protected JSONArray getOwnerFeed(JSONArray data)
    {
        //
        // Add account owner as an owner feed.
        //

        JSONObject owner = new JSONObject();

        String pfid = Simple.getSharedPrefString("social." + platform + ".pfid");
        String name = Simple.getSharedPrefString("social." + platform + ".name");

        if ((pfid != null) && (name != null))
        {
            Json.put(owner, "id", pfid);
            Json.put(owner, "name", name);
            Json.put(owner, "type", "owner");
            Json.put(owner, "plat", platform);

            File icon = ProfileImages.getSocialUserImageFile(platform, pfid);
            if (icon != null) Json.put(owner, "icon", icon.toString());

            Json.put(data, owner);
        }

        return data;
    }

    protected JSONArray getUserFeeds(JSONArray data, String type, boolean feedonly)
    {
        String modeprefix = "social." + platform + "." + type + ".mode.";
        String nameprefix = "social." + platform + "." + type + ".name.";

        Map<String, Object> friends = Simple.getAllPreferences(modeprefix);

        for (Map.Entry<String, Object> entry : friends.entrySet())
        {
            Object fmode = entry.getValue();
            if (!(fmode instanceof String)) continue;

            String mode = (String) fmode;
            if (feedonly && !mode.contains("feed")) continue;
            if (mode.equals("inactive")) continue;

            String pfid = entry.getKey().substring(modeprefix.length());
            String name = Simple.getSharedPrefString(nameprefix + pfid);
            if (name == null) continue;

            JSONObject item = new JSONObject();

            Json.put(item, "id", pfid);
            Json.put(item, "name", name);
            Json.put(item, "type", type);
            Json.put(item, "plat", platform);

            File icon = ProfileImages.getSocialUserImageFile(platform, pfid);
            if (icon != null) Json.put(item, "icon", icon.toString());

            Json.put(data, item);
        }

        return data;
    }

    @Nullable
    public JSONObject getPost(String postid)
    {
        if (postid == null) return null;

        File postfile = new File(cachedir, postid + ".post.json");

        if (postfile.exists())
        {
            return Json.fromString(Simple.getFileContent(postfile));
        }

        return getGraphPost(postid);
    }

    @Nullable
    public JSONArray getFeed(String userid)
    {
        if (userid == null) return null;

        File feedfile = new File(cachedir, userid + ".feed.json");

        if (feedfile.exists())
        {
            return Json.fromStringArray(Simple.getFileContent(feedfile));
        }

        return getGraphFeed(userid);
    }

    //endregion Cache driven publics

    //region Graph accessing methods

    @Nullable
    protected abstract JSONObject getGraphUserProfile();

    @Nullable
    protected abstract JSONArray getGraphUserFriendlist();

    @Nullable
    protected abstract JSONArray getGraphUserLikeslist();

    @Nullable
    protected abstract JSONArray getGraphFeed(String userid);

    @Nullable
    protected abstract JSONObject getGraphPost(String postid);

    @Nullable
    public JSONObject getGraphRequest(String path)
    {
        return getGraphRequest(path, new Bundle());
    }

    @Nullable
    public JSONObject getGraphRequest(String path, JSONObject parameters)
    {
        return getGraphRequest(path, getParameters(parameters));
    }

    @Nullable
    public JSONObject getGraphRequest(String path, Bundle parameters)
    {
        return getGraphRequest(apiurl, path, parameters);
    }

    @Nullable
    public JSONObject getGraphRequest(String requesturl, String path)
    {
        return getGraphRequest(requesturl, path, null);
    }

    @Nullable
    public JSONObject getGraphRequest(String requesturl, String path, Bundle parameters)
    {
        String token = getAccessToken();

        if ((path == null) || (token == null) || (requesturl == null)) return null;

        maintainStatistic(path);

        String url = requesturl + path + "?access_token=" + token;
        if (apiextraparam != null) url += apiextraparam;

        String content = SimpleRequest.readContent(url);

        if (content == null)
        {
            Log.d(LOGTAG, "getGraphRequest: failed=" + url);
        }
        else
        {
            Log.d(LOGTAG, "getGraphRequest: success=" + url);

            if (verbose) Log.d(LOGTAG, "getGraphRequest: " + content);
        }

        return Json.fromString(content);
    }

    private Bundle getParameters(JSONObject jparams)
    {
        Bundle bparams = new Bundle();

        if (jparams != null)
        {
            Iterator<String> keysIterator = jparams.keys();

            while (keysIterator.hasNext())
            {
                String key = keysIterator.next();
                Object val = Json.get(jparams, key);

                if (val instanceof Boolean)
                {
                    bparams.putBoolean(key, (Boolean) val);
                    continue;
                }

                if (val instanceof Integer)
                {
                    bparams.putInt(key, (Integer) val);
                    continue;
                }

                if (val instanceof Long)
                {
                    bparams.putLong(key, (Long) val);
                    continue;
                }

                if (val instanceof String)
                {
                    bparams.putString(key, (String) val);
                    continue;
                }

                if (val instanceof JSONArray)
                {
                    JSONArray array = (JSONArray) val;
                    String imploded = "";

                    for (int inx = 0; inx < array.length(); inx++)
                    {
                        Object jobj = Json.get(array, inx);
                        if (!(jobj instanceof String)) continue;

                        if (imploded.length() > 0) imploded += ",";
                        imploded += (String) jobj;
                    }

                    bparams.putString(key, imploded);
                }
            }
        }

        return bparams;
    }

    //endregion Graph accessing methods

    //region Graph call statistic

    public int getHourStatistic()
    {
        getStorage();

        synchronized (LOGTAG)
        {
            JSONArray hour = Json.getArray(statData, "hour");
            return (hour == null) ? 0 : hour.length();
        }
    }

    public int getTodayStatistic()
    {
        getStorage();

        synchronized (LOGTAG)
        {
            int today = Json.getInt(statData, Simple.getLocalDate(Simple.nowAsTimeStamp()));
            JSONArray hour = Json.getArray(statData, "hour");
            return (hour == null) ? today : (today  + hour.length());
        }
    }

    protected JSONObject statData;
    protected boolean statDirty;

    protected void maintainStatistic(String path)
    {
        getStorage();

        JSONArray hour = Json.getArray(statData, "hour");
        JSONObject stat = new JSONObject();

        Json.put(stat, "dst", Simple.nowAsISO());
        Json.put(stat, "api", path);

        Json.put(hour, stat);

        statDirty = true;
    }

    private final Runnable freeMemory = new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (LOGTAG)
            {
                if (statData != null)
                {
                    if (statDirty) putStorage();

                    statData = null;
                }
            }
        }
    };

    protected void removeOutdated()
    {
        long now = Simple.nowAsTimeStamp();
        String out = Simple.timeStampAsISO(now - (3600 * 1000));

        if (! Json.has(statData, "hour")) Json.put(statData, "hour", new JSONArray());

        JSONArray hour = Json.getArray(statData, "hour");

        String today = Simple.getLocalDate(Simple.nowAsTimeStamp());

        while ((hour != null) && (hour.length() > 0))
        {
            JSONObject old = Json.getObject(hour, 0);
            String dst = Json.getString(old, "dst");

            if ((dst == null) || (dst.compareTo(out) < 0))
            {
                hour.remove(0);

                Json.put(statData, today, Json.getInt(statData, today) + 1);
            }
            else
            {
                break;
            }
        }
    }

    private void getStorage()
    {
        Simple.removePost(freeMemory);
        Simple.makePost(freeMemory, 10 * 1000);

        if (statData != null) return;

        synchronized (LOGTAG)
        {
            try
            {
                File statfile = new File(cachedir, platform + ".statistic.json");
                statData = Json.fromStringObject(Simple.getFileContent(statfile));
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            if (statData == null) statData = new JSONObject();

            removeOutdated();
        }
    }

    private void putStorage()
    {
        if (statData == null) return;

        synchronized (LOGTAG)
        {
            try
            {
                File statfile = new File(cachedir, platform + ".statistic.json");
                Simple.putFileContent(statfile, Json.defuck(Json.toPretty(statData)));
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }
    }

    //endregion Graph call statistic

    //region Reconfiguration

    public void reconfigureFriendsAndLikes()
    {
        if (!isLoggedIn()) return;

        JSONArray friends = getGraphUserFriendlist();

        if (friends != null)
        {
            Map<String, Object> oldfriends = Simple.getAllPreferences("social." + platform + ".friend.");

            String dfmode = Simple.getSharedPrefString("social." + platform + ".newfriends.default");
            if (dfmode == null) dfmode = "feed+folder";

            for (int inx = 0; inx < friends.length(); inx++)
            {
                JSONObject friend = Json.getObject(friends, inx);
                if (friend == null) continue;

                String pfid = Json.getString(friend, "id");
                String name = Json.getString(friend, "full_name");
                if (name == null) name = Json.getString(friend, "displayName");
                if (name == null) name = Json.getString(friend, "name");
                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social." + platform + ".friend.name." + pfid;
                String fmodepref = "social." + platform + ".friend.mode." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.loadSocialUserImageFile(platform, pfid);

                if (oldfriends.containsKey(fnamepref)) oldfriends.remove(fnamepref);
                if (oldfriends.containsKey(fmodepref)) oldfriends.remove(fmodepref);
            }

            for (Map.Entry<String, ?> entry : oldfriends.entrySet())
            {
                Simple.removeSharedPref(entry.getKey());
            }
        }

        JSONArray likes = getGraphUserLikeslist();

        if (likes != null)
        {
            Map<String, Object> oldlikes = Simple.getAllPreferences("social." + platform + ".like.");

            String dfmode = Simple.getSharedPrefString("social." + platform + ".newlikes.default");
            if (dfmode == null) dfmode = "folder";

            for (int inx = 0; inx < likes.length(); inx++)
            {
                JSONObject like = Json.getObject(likes, inx);
                if (like == null) continue;

                String pfid = Json.getString(like, "id");
                String name = Json.getString(like, "full_name");
                if (name == null) name = Json.getString(like, "displayName");
                if (name == null) name = Json.getString(like, "name");
                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social." + platform + ".like.name." + pfid;
                String fmodepref = "social." + platform + ".like.mode." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.loadSocialUserImageFile(platform, pfid);

                if (oldlikes.containsKey(fnamepref)) oldlikes.remove(fnamepref);
                if (oldlikes.containsKey(fmodepref)) oldlikes.remove(fmodepref);
            }

            for (Map.Entry<String, ?> entry : oldlikes.entrySet())
            {
                Simple.removeSharedPref(entry.getKey());
            }
        }
    }

    //endregion Reconfiguration

    //region Cache maintenance

    private final long totalInterval = 3600;
    private long lastReconfigure;
    private long nextInterval;
    private long nextAction;

    private JSONArray feedList;
    private ArrayList<String> postFiles;

    public void commTick()
    {
        long now = Simple.nowAsTimeStamp();

        if ((now - lastReconfigure) > 24 * 3600 * 1000)
        {
            cachedir = new File(Simple.getExternalCacheDir(), platform);

            if (! cachedir.exists())
            {
                if (cachedir.mkdirs()) Log.d(LOGTAG, "commTick: created cache:" + cachedir);
            }

            Log.d(LOGTAG, "commTick: reconfigureFriendsAndLikes");

            reconfigureFriendsAndLikes();
            lastReconfigure = now;
            nextAction = now;

            return;
        }

        if (now < nextAction) return;

        if ((feedList == null) || feedList.length() == 0)
        {
            if ((! cachedir.exists()) && cachedir.mkdirs()) Log.d(LOGTAG, "commtick: created cache");

            //
            // Remove outdated posts.
            //

            if (postFiles != null)
            {
                while (postFiles.size() > 0)
                {
                    File obsolete = new File(cachedir, postFiles.remove(0));
                    if (obsolete.delete()) Log.d(LOGTAG, "commTick: deleted:" + obsolete);
                }
            }

            //
            // Get all user feeds.
            //

            feedList = getUserFeeds(false);

            if (feedList.length() == 0)
            {
                nextAction = now + (totalInterval * 1000);
            }
            else
            {
                nextInterval = (totalInterval * 1000) / feedList.length();

                //
                // Impose a rate limit on too many feeds.
                //

                if (now < (60 * 1000)) now = 60 * 1000;

                nextAction = now;

                //
                // Collect all existing post files for this run.
                //

                postFiles = Simple.getDirectoryAsList(cachedir, postsfilter);
            }

            return;
        }

        nextAction += nextInterval;

        //
        // Load one feed.
        //

        JSONObject feed = Json.getObject(feedList, 0);
        Json.remove(feedList, 0);
        if (feed == null) return;

        final String feedpfid = Json.getString(feed, "id");
        String feedname = Json.getString(feed, "full_name");
        if (feedname == null) feedname = Json.getString(feed, "name");

        Log.d(LOGTAG, "commTick: feed:" + feedpfid + " => " + feedname);

        JSONArray feeddata = getGraphFeed(feedpfid);
        if (feeddata == null) return;

        File feedfile = new File(cachedir, feedpfid + ".feed.json");
        Simple.putFileContent(feedfile, Json.toPretty(feeddata));

        //
        // Check feed stories.
        //

        for (int inx = 0; inx < feeddata.length(); inx++)
        {
            JSONObject post = Json.getObject(feeddata, inx);
            String postid = Json.getString(post, "id");
            if (postid == null) continue;

            String postname = postid + ".post.json";
            File postfile = new File(cachedir, postname);

            if (postFiles.contains(postname))
            {
                postFiles.remove(postname);
                continue;
            }

            JSONObject postdata = getGraphPost(postid);
            if (postdata == null) continue;

            Simple.putFileContent(postfile, Json.toPretty(postdata));
        }
    }

    private final FilenameFilter postsfilter = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String filename)
        {
            return filename.endsWith(".post.json");
        }
    };

    //endregion Cache maintenance

    //region Social interface

    public interface SocialInterface
    {
        boolean isEnabled();
        boolean isLoggedIn();
        boolean isReady();

        boolean hasFriends();
        boolean hasLikes();

        void login();
        void logout();

        String getAccessExpiration();
        String[] getAccessScope();

        String getUserId();
        String getUserDisplayName();

        Drawable getProfileDrawable(String pfid, boolean circle);

        void reconfigureFriendsAndLikes();

        int getHourStatistic();
        int getTodayStatistic();

        void getTest();
    }

    //endregion Social interface
}
