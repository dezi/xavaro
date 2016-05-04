package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.annotation.SuppressLint;

import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.app.AlertDialog;
import android.app.Application;
import android.text.TextUtils;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class SocialInstagram extends Social
{
    private static final String LOGTAG = SocialInstagram.class.getSimpleName();

    private static final String appsecret = "ab19aa46d88f4fb9a433f6d6cc1d4df3";
    private static final String appkey = "63afb3307ec24f4886230f44d2fda884";
    private static final String appurl = "http://www.xavaro.de/instagram";
    private static final String apiurl = "https://api.instagram.com/v1";
    private static final String tokenpref = "social.instagram.token";

    private static final Collection<String> permissions = Arrays.asList
            (
                    "basic",
                    "public_content",
                    "follower_list",
                    "likes"
            );

    private static JSONObject user;
    private static String token;

    public static void initialize(Application app)
    {
        //cachedir = new File(Simple.getExternalCacheDir(), "instagram");
        token = getAccessToken();

        getCurrentUser();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void login()
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

                if (url.contains("www.xavaro.de/instagram?code="))
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

                    String tokenurl = "https://api.instagram.com/oauth/access_token";
                    String content = SimpleRequest.readContent(tokenurl, postdata);

                    Log.d(LOGTAG, "=====>" + content);

                    if (content != null)
                    {
                        user = Json.fromString(content);
                        token = Json.getString(user, "access_token");
                        Simple.setSharedPrefString(tokenpref, token);
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
        frame.setLayoutParams(new FrameLayout.LayoutParams(Simple.MP, 400));
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

        webview.requestFocus(View.FOCUS_DOWN);

        webview.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_DOWN:
                        if (!view.hasFocus()) view.requestFocus();
                        break;
                }
                return false;
            }
        });

        frame.addView(webview);
        dummy.addView(frame);
        dialog.setView(dummy);
        dialog.show();

        //
        // Fire up auth url in dialog.
        //

        String url = "https://www.instagram.com/oauth/authorize/"
                + "?client_id=" + appkey
                + "&redirect_uri=" + appurl
                + "&scope=" + TextUtils.join("+", permissions)
                + "&response_type=code";

        webview.loadUrl(url);
    }

    public static void logout()
    {
        //
        // Nuke preference and session id.
        //

        user = null;
        token = null;
        Simple.removeSharedPref(tokenpref);
        CookieManager.getInstance().setCookie("https://www.instagram.com", "sessionid=");
    }

    public static boolean isEnabled()
    {
        return Simple.getSharedPrefBoolean("social.instagram.enable");
    }

    public static boolean isLoggedIn()
    {
        return (Simple.getSharedPrefString(tokenpref) != null);
    }

    @Nullable
    public static String getUserId()
    {
        JSONObject userdata = getCurrentUser();
        return Json.getString(userdata, "id");
    }

    @Nullable
    public static String getUserTokenExpiration()
    {
        if (isLoggedIn())
        {
            long now = Simple.nowAsTimeStamp();
            now += 60L * 86400 * 1000;

            return Simple.timeStampAsISO(now);
        }

        return null;
    }

    @Nullable
    public static String getUserDisplayName()
    {
        JSONObject userdata = getCurrentUser();
        return Json.getString(userdata, "full_name");
    }

    public static JSONArray getUserFeeds(boolean feedonly)
    {
        JSONArray data = new JSONArray();

        getOwnerFeed(data, "instagram");

        getUserFeeds(data, "instagram", "friend", feedonly);

        return data;
    }

    @Nullable
    public static JSONArray getUserFriendlist()
    {
        JSONObject response = getGraphRequest("/users/self/follows");
        return Json.getArray(response, "data");
    }

    @Nullable
    public static byte[] getUserIconData(String igid)
    {
        if (igid == null) return null;

        JSONObject userdata = getCurrentUser();

        if ((userdata == null) || ! Simple.equals(igid, Json.getString(userdata, "username")))
        {
            userdata = getUser(igid);
        }

        String iconurl = Json.getString(userdata, "profile_picture");
        return SimpleRequest.readData(iconurl);
    }

    @Nullable
    public static String getAccessToken()
    {
        return Simple.getSharedPrefString(tokenpref);
    }

    @Nullable
    public static JSONObject getCurrentUser()
    {
        if (user == null)
        {
            JSONObject response = getGraphRequest("/users/self");
            user = Json.getObject(response, "data");

            if (user != null) Log.d(LOGTAG, "=================>" + user.toString());
        }

        return user;
    }

    @Nullable
    public static JSONObject getUser(String igid)
    {
        JSONObject response = getGraphRequest("/users/" + igid);
        return Json.getObject(response, "data");
    }

    @Nullable
    public static JSONObject getPost(String postid)
    {
        if (postid == null) return null;

        File postfile = new File(cachedir, postid + ".post.json");

        if (postfile.exists())
        {
            return Json.fromString(Simple.getFileContent(postfile));
        }

        return getGraphPost(postid);
    }

    private static JSONObject getGraphPost(String postid)
    {
        JSONObject response = getGraphRequest("/media/" + postid);
        return Json.getObject(response, "data");
    }

    @Nullable
    public static JSONArray getFeed(String userid)
    {
        if (userid == null) return null;

        File feedfile = new File(cachedir, userid + ".feed.json");

        if (feedfile.exists())
        {
            return Json.fromStringArray(Simple.getFileContent(feedfile));
        }

        return getGraphFeed(userid);
    }

    private static JSONArray getGraphFeed(String userid)
    {
        if (userid == null) return null;

        JSONObject response = getGraphRequest("/users/" + userid  + "/media/recent");
        return Json.getArray(response, "data");
    }

    @Nullable
    public static JSONObject getGraphRequest(String path)
    {
        return getGraphRequest(path, new Bundle());
    }

    @Nullable
    public static JSONObject getGraphRequest(String path, JSONObject parameters)
    {
        return getGraphRequest(path, getParameters(parameters));
    }

    @Nullable
    public static JSONObject getGraphRequest(String path, Bundle parameters)
    {
        if (path == null) return null;
        if (token == null) token = getAccessToken();
        if (token == null) return null;

        String url = apiurl + path + "?access_token=" + token;
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

    public static void reconfigureFriends()
    {
        if (! isLoggedIn()) return;

        JSONArray friends = getUserFriendlist();

        if (friends != null)
        {
            Map<String, Object> oldfriends = Simple.getAllPreferences("social.instagram.friend.");

            String dfmode = Simple.getSharedPrefString("social.instagram.newfriends.default");
            if (dfmode == null) dfmode = "feed+folder";

            for (int inx = 0; inx < friends.length(); inx++)
            {
                JSONObject friend = Json.getObject(friends, inx);
                if (friend == null) continue;

                String pfid = Json.getString(friend, "id");
                String name = Json.getString(friend, "full_name");
                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social.instagram.friend.name." + pfid;
                String fmodepref = "social.instagram.friend.mode." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.getFacebookLoadProfileImage(pfid);

                if (oldfriends.containsKey(fnamepref)) oldfriends.remove(fnamepref);
                if (oldfriends.containsKey(fmodepref)) oldfriends.remove(fmodepref);
            }

            for (Map.Entry<String, ?> entry : oldfriends.entrySet())
            {
                Simple.removeSharedPref(entry.getKey());
            }
        }
    }

    //region Cache maintenance

    private static long totalInterval = 3600;
    private static long lastReconfigure;
    private static long nextInterval;
    private static long nextAction;
    private static File cachedir;

    private static JSONArray feedList;

    public static void commTick()
    {
        long now = Simple.nowAsTimeStamp();

        if ((now - lastReconfigure) > 24 * 3600 * 1000)
        {
            cachedir = new File(Simple.getExternalCacheDir(), "instagram");

            if (! cachedir.exists())
            {
                if (cachedir.mkdirs()) Log.d(LOGTAG, "commTick: created cache:" + cachedir);
            }

            Log.d(LOGTAG, "commTick: reconfigureFriends");

            reconfigureFriends();
            lastReconfigure = now;
            nextAction = now;

            return;
        }

        if (now < nextAction) return;

        if ((feedList == null) || feedList.length() == 0)
        {
            feedList = getUserFeeds(false);

            if (feedList.length() == 0)
            {
                nextAction = now + (totalInterval * 1000);
            }
            else
            {
                nextInterval = (totalInterval * 1000) / feedList.length();
                nextAction = now;
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
        final String feedname = Json.getString(feed, "name");

        Log.d(LOGTAG, "commTick: feed:" + feedpfid + " => " + feedname);

        JSONArray feeddata = getGraphFeed(feedpfid);
        if (feeddata == null) return;

        File feedfile = new File(cachedir, feedpfid + ".feed.json");
        Simple.putFileContent(feedfile, Json.toPretty(feeddata));

        //
        // Check feed stories.
        //

        FilenameFilter postsfilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                return filename.startsWith(feedpfid + "_") && filename.endsWith(".post.json");
            }
        };

        if ((! cachedir.exists()) && cachedir.mkdirs()) Log.d(LOGTAG, "commtick: created cache");
        ArrayList<String> postfiles = Simple.getDirectoryAsList(cachedir, postsfilter);

        for (int inx = 0; inx < feeddata.length(); inx++)
        {
            JSONObject post = Json.getObject(feeddata, inx);
            String postid = Json.getString(post, "id");
            if (postid == null) continue;

            String postname = postid + ".post.json";
            File postfile = new File(cachedir, postname);

            if (postfiles.contains(postname))
            {
                postfiles.remove(postname);
                continue;
            }

            JSONObject postdata = getGraphPost(postid);
            if (postdata == null) continue;

            Simple.putFileContent(postfile, Json.toPretty(postdata));
        }

        //
        // Remove outdated posts.
        //

        while (postfiles.size() > 0)
        {
            File obsolete = new File(cachedir, postfiles.remove(0));
            if (obsolete.delete()) Log.d(LOGTAG, "commTick: deleted:" + obsolete);
        }
    }

    //endregion Cache maintenance
}
