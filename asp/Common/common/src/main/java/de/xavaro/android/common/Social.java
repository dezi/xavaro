package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class Social
{
    private static final String LOGTAG = Social.class.getSimpleName();

    protected final String platform;
    protected final String locale;

    protected final String expirationpref;
    protected final String accesstokenpref;
    protected final String accesssecretpref;
    protected final String refreshtokenpref;

    protected String appurl;
    protected String appfox;
    protected String appfix;

    protected String oauthurl;
    protected String tokenurl;
    protected String requesturl;

    protected String[] scopes;

    protected String apiurl;
    protected Bundle apiextraparam;
    protected boolean apisigned;
    protected boolean apifeedhasposts;

    protected JSONObject user;
    protected boolean verbose;
    protected File cachedir;
    protected long cacheInterval = 3600;

    public Social(String platform)
    {
        this.platform = platform;

        expirationpref = "social." + platform + ".expiration";
        accesstokenpref = "social." + platform + ".accesstoken";
        accesssecretpref = "social." + platform + ".accesssecret";
        refreshtokenpref = "social." + platform + ".refreshtoken";

        locale = Simple.getLocaleLanguage() + "_" + Simple.getLocaleCountry();

        cachedir = new File(Simple.getExternalCacheDir(), platform);

        if ((! cachedir.exists()) && cachedir.mkdirs())
        {
            Log.d(LOGTAG, "Constructor: created cache:" + cachedir);
        }
    }

    /*
    protected String checkDat(String value)
    {
        value = Simple.getBase64Encoded(Simple.dezify(value));
        Log.d(LOGTAG, "checkDat: platform=" + platform + "=" + value);
        return value;
    }
    */

    protected String check(String value)
    {
        return Simple.dezify(Simple.getBase64Decoded(value));
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
        if ((Simple.getSharedPrefString(expirationpref) != null) &&
                Simple.getSharedPrefString(refreshtokenpref) == null)
        {
            return Simple.getSharedPrefString(expirationpref);
        }

        return isLoggedIn() ? Simple.timeStampAsISO(0) : null;
    }

    @Nullable
    public String getPlatform()
    {
        return platform;
    }

    @Nullable
    protected String getAccessToken()
    {
        return Simple.getSharedPrefString(accesstokenpref);
    }

    @Nullable
    protected String getAccessSecret()
    {
        return Simple.getSharedPrefString(accesssecretpref);
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
                    exchangeCodeforToken(url);

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
        // Get base token.
        //

        String oauth_token = null;

        if (requesturl != null)
        {
            //
            // Platform desires an oauth token for authorization.
            //

            Bundle params = new Bundle();
            params.putString("oauth_callback", appurl);

            params = getSignedOAuthParams("POST", requesturl, params);
            String oauth = getSignedOAuthHeader(params);
            String content = SimpleRequest.readContent(requesturl, oauth, new JSONObject());

            if (content != null)
            {
                JSONObject query = getQuery(content);
                oauth_token = Json.getString(query, "oauth_token");
            }
        }

        //
        // Fire up authorization url in dialog.
        //

        String scopes = getScopeParameter();

        String url = oauthurl
                + "?client_id=" + appfix
                + "&redirect_uri=" + appurl
                + ((scopes != null) ? "&scope=" + scopes : "")
                + ((oauth_token != null) ? "&oauth_token=" + oauth_token : "")
                + "&response_type=code"
                + "&access_type=offline"
                + "&prompt=consent";

        Log.d(LOGTAG, "login: " + url);

        webview.loadUrl(url);
    }

    private void exchangeCodeforToken(String url)
    {
        //
        // Final oauth step. The user has accepted access or not
        // and now we try to exchange our code from authorization
        // against a valid access token and eventually access token
        // secret for Twitter.
        //

        Uri uri = Uri.parse(url);

        String code = uri.getQueryParameter("code");
        String oauth_token = uri.getQueryParameter("oauth_token");
        String oauth_verifier = uri.getQueryParameter("oauth_verifier");

        if (code != null)
        {
            Log.d(LOGTAG, "exchangeCodeforToken: code=" + code);

            JSONObject postdata = new JSONObject();

            Json.put(postdata, "code", code);
            Json.put(postdata, "client_id", appfix);
            Json.put(postdata, "client_secret", check(appfox));
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
        }

        if ((oauth_token != null) && (oauth_verifier != null))
        {
            Log.d(LOGTAG, "exchangeCodeforToken: oauth_token="
                    + oauth_token + " => " + oauth_verifier);

            Bundle params = new Bundle();
            params.putString("oauth_token", oauth_token);
            params.putString("oauth_verifier", oauth_verifier);

            params = getSignedOAuthParams("POST", tokenurl, params);
            String oauth = getSignedOAuthHeader(params);

            Log.d(LOGTAG, "==============oauth=" + oauth);
            String content = SimpleRequest.readContent(tokenurl, oauth, new JSONObject());
            Log.d(LOGTAG, "==============content=" + content);

            if (content != null)
            {
                JSONObject jcontent = getQuery(content);

                File socialdir = Simple.getMediaPath("social");
                File oauthfile = new File(socialdir, platform + ".oauth.json");
                Simple.putFileJSON(oauthfile, jcontent);

                String accessToken = Json.getString(jcontent, "oauth_token");
                Simple.setSharedPrefString(accesstokenpref, accessToken);

                String accessSecret = Json.getString(jcontent, "oauth_token_secret");
                Simple.setSharedPrefString(accesssecretpref, accessSecret);
            }
        }
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
            Log.d(LOGTAG, "clearCookies: vorher=" + cookiestring);

            String expire = "=x;"; // Expires=Wed, 31 Dec 2000 23:59:59 GMT";

            String[] cookies = cookiestring.split(";");

            for (String cookie : cookies)
            {
                Log.d(LOGTAG, "clearCookies: " + domain + "=" + cookie.trim());

                String[] cookieparts = cookie.trim().split("=");
                cookieManager.setCookie(domain, cookieparts[ 0 ] + expire);
            }

            cookiestring = cookieManager.getCookie(domain);
            Log.d(LOGTAG, "clearCookies: nachher=" + cookiestring);
        }
    }

    protected JSONObject getQuery(String querystr)
    {
        JSONObject jquery = new JSONObject();

        if (querystr != null)
        {
            String[] parts = querystr.split("&");

            for (String part : parts)
            {
                String[] pparts = part.split("=");
                if (pparts.length != 2) continue;

                Json.put(jquery,
                        Simple.getUrlDecoded(pparts[ 0 ]),
                        Simple.getUrlDecoded(pparts[ 1 ]));
            }
        }

        return jquery;
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

    protected void getOwnerFeed(JSONArray data)
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
    }

    protected void getUserFeeds(JSONArray data, String type, boolean feedonly)
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
    }

    public abstract boolean getPostSuitable(JSONObject post);

    @Nullable
    public JSONObject getPost(String postid)
    {
        if (postid == null) return null;

        File postfile = new File(cachedir, postid + ".post.json");

        if (postfile.exists())
        {
            return Json.fromString(Simple.getFileContent(postfile));
        }

        JSONObject post = getGraphPost(postid);
        Simple.putFileJSON(postfile, post);

        return post;
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

        if (parameters == null) parameters = new Bundle();
        if (apiextraparam != null) parameters.putAll(apiextraparam);

        String url;
        String content;

        if (apisigned)
        {
            parameters.putString("oauth_token", token);

            url = requesturl + path;

            parameters = getSignedOAuthParams("GET", url, parameters);
            String oauth = getSignedOAuthHeader(parameters);

            url += getQueryString(parameters);

            content = SimpleRequest.readContent(url, oauth);
        }
        else
        {
            parameters.putString("access_token", token);

            url = requesturl + path + getQueryString(parameters);

            content = SimpleRequest.readContent(url);
        }

        if (content == null)
        {
            Log.d(LOGTAG, "getGraphRequest: failed=" + url);
        }
        else
        {
            Log.d(LOGTAG, "getGraphRequest: success=" + url);

            if (verbose) Log.d(LOGTAG, "getGraphRequest: " + content);

            if (content.trim().startsWith("["))
            {
                JSONArray array = Json.fromStringArray(content);
                JSONObject object = new JSONObject();
                Json.put(object, "data", array);
                return object;
            }
        }

        return Json.fromString(content);
    }

    private String getQueryString(Bundle parameters)
    {
        String query = "";

        if (parameters != null)
        {
            Set<String> keys = parameters.keySet();

            for (String key : keys)
            {
                Object obj = parameters.get(key);
                if (obj == null) continue;

                query += (query.length() == 0) ? "?" : "&";
                query += Simple.getUrlEncoded(key) + "=" + Simple.getUrlEncoded(obj.toString());
            }
        }

        return query;
    }

    protected Bundle getParameters(JSONObject jparams)
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

    @Nullable
    protected Bundle getSignedOAuthParams(String method, String url, Bundle parameters)
    {
        if (parameters == null) parameters = new Bundle();

        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timst = "" + (Simple.nowAsTimeStamp() / 1000);

        parameters.putString("oauth_version", "1.0");
        parameters.putString("oauth_consumer_key", appfix);
        parameters.putString("oauth_signature_method", "HMAC-SHA1");
        parameters.putString("oauth_timestamp", timst);
        parameters.putString("oauth_nonce", nonce);

        //
        // Sort shit for checksum.
        //

        List<String> sorted = new ArrayList<>(parameters.keySet());
        Collections.sort(sorted);

        //
        // Build sorted query string.
        //

        String querystring = "";

        for (String key : sorted)
        {
            if (querystring.length() > 0) querystring += "&";

            querystring += Simple.getUrlEncoded(key) + "=";
            querystring += Simple.getUrlEncoded("" + parameters.get(key));
        }

        //
        // Build signature string.
        //

        String basestring = method.toUpperCase();
        basestring += "&" + Simple.getUrlEncoded(url);
        basestring += "&" + Simple.getUrlEncoded(querystring);

        String keystring = Simple.getUrlEncoded(check(appfox)) + "&";
        if (getAccessToken() != null) keystring += Simple.getUrlEncoded(getAccessSecret());

        String signature = computeSignature(basestring, keystring);

        Log.d(LOGTAG,"getSignedOAuth: base=" + basestring);
        Log.d(LOGTAG,"getSignedOAuth: keys=" + keystring);
        Log.d(LOGTAG,"getSignedOAuth: sign=" + signature);

        parameters.putString("oauth_signature", signature);

        return parameters;
    }

    @Nullable
    protected String getSignedOAuthHeader(Bundle parameters)
    {
        String header = "OAuth ";

        List<String> sorted = new ArrayList<>(parameters.keySet());
        Collections.sort(sorted);

        for (String key : sorted)
        {
            if (! key.startsWith("oauth_")) continue;

            if (! header.equals("OAuth ")) header += ", ";

            header += key + "=" + "\"" + Simple.getUrlEncoded("" + parameters.get(key)) + "\"";

            parameters.remove(key);
        }

        return header;
    }

    @Nullable
    private String computeSignature(String baseString, String keyString)
    {
        try
        {
            byte[] keyBytes = keyString.getBytes();
            SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);

            byte[] text = baseString.getBytes();
            return Base64.encodeToString(mac.doFinal(text), 0).trim();
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    //endregion Graph accessing methods

    //region Graph call statistic

    public int getWindowStatistic()
    {
        getStorage();

        synchronized (LOGTAG)
        {
            JSONArray hour = Json.getArray(statData, "window");
            return (hour == null) ? 0 : hour.length();
        }
    }

    public int getTodayStatistic()
    {
        getStorage();

        synchronized (LOGTAG)
        {
            int today = Json.getInt(statData, Simple.getLocalDateInternal(Simple.nowAsTimeStamp()));
            JSONArray hour = Json.getArray(statData, "window");
            return (hour == null) ? today : (today  + hour.length());
        }
    }

    protected int windowSecs = 3600;
    protected JSONObject statData;
    protected boolean statDirty;

    protected void maintainStatistic(String path)
    {
        getStorage();

        JSONArray hour = Json.getArray(statData, "window");
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
        String out = Simple.timeStampAsISO(now - (windowSecs * 1000));

        if (! Json.has(statData, "window")) Json.put(statData, "window", new JSONArray());

        JSONArray window = Json.getArray(statData, "window");

        String today = Simple.getLocalDateInternal(Simple.nowAsTimeStamp());

        while ((window != null) && (window.length() > 0))
        {
            JSONObject old = Json.getObject(window, 0);
            String dst = Json.getString(old, "dst");

            if ((dst == null) || (dst.compareTo(out) < 0))
            {
                window.remove(0);

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
                if (pfid == null) pfid = Json.getString(friend, "id_str");

                String name = Json.getString(friend, "full_name");
                if (Simple.isEmpty(name)) name = Json.getString(friend, "displayName");
                if (Simple.isEmpty(name)) name = Json.getString(friend, "name");
                if (Simple.isEmpty(name)) name = Json.getString(friend, "username");

                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social." + platform + ".friend.name." + pfid;
                String fmodepref = "social." + platform + ".friend.mode." + pfid;
                String ficonpref = "social." + platform + ".friend.icon." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.loadSocialUserImageFile(platform, pfid);

                if (oldfriends.containsKey(fnamepref)) oldfriends.remove(fnamepref);
                if (oldfriends.containsKey(fmodepref)) oldfriends.remove(fmodepref);
                if (oldfriends.containsKey(ficonpref)) oldfriends.remove(ficonpref);
            }

            for (Map.Entry<String, ?> entry : oldfriends.entrySet())
            {
                //
                // Pre check manually added friends or likes
                // by search not contained in lists. Manually
                // entries are identified by having an icon
                // preference.
                //

                String key = entry.getKey();
                String[] parts = key.split("\\.");

                if (parts.length >= 5)
                {
                    parts[ 3 ] = "icon";

                    String checkpref = TextUtils.join(".", parts);
                    String checkvalue = Simple.getSharedPrefString(checkpref);
                    if (checkvalue != null) continue;
                }

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
                if (pfid == null) pfid = Json.getString(like, "id_str");

                String name = Json.getString(like, "full_name");
                if (Simple.isEmpty(name)) name = Json.getString(like, "displayName");
                if (Simple.isEmpty(name)) name = Json.getString(like, "name");
                if (Simple.isEmpty(name)) name = Json.getString(like, "username");

                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social." + platform + ".like.name." + pfid;
                String fmodepref = "social." + platform + ".like.mode." + pfid;
                String ficonpref = "social." + platform + ".like.icon." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.loadSocialUserImageFile(platform, pfid);

                if (oldlikes.containsKey(fnamepref)) oldlikes.remove(fnamepref);
                if (oldlikes.containsKey(fmodepref)) oldlikes.remove(fmodepref);
                if (oldlikes.containsKey(ficonpref)) oldlikes.remove(ficonpref);
            }

            for (Map.Entry<String, ?> entry : oldlikes.entrySet())
            {
                //
                // Pre check manually added friends or likes
                // by search not contained in lists. Manually
                // entries are identified by having an icon
                // preference.
                //

                String key = entry.getKey();
                String[] parts = key.split("\\.");

                if (parts.length >= 5)
                {
                    parts[ 3 ] = "icon";

                    String checkpref = TextUtils.join(".", parts);
                    String checkvalue = Simple.getSharedPrefString(checkpref);
                    if (checkvalue != null) continue;
                }

                Simple.removeSharedPref(key);
            }
        }
    }

    //endregion Reconfiguration

    //region Searching

    public boolean canSearchUsers()
    {
        return false;
    }

    public JSONArray getSearchUsers(String query, int max)
    {
        return null;
    }

    //endregion Searching

    //region Cache maintenance
    
    private long lastReconfigure;
    private long nextInterval;
    private long nextAction;

    private final JSONObject feedSync = new JSONObject();
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
            if ((! cachedir.exists()) && cachedir.mkdirs())
            {
                Log.d(LOGTAG, "commtick: created cache");
            }

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
                nextAction = now + (cacheInterval * 1000);
            }
            else
            {
                nextInterval = (cacheInterval * 1000) / feedList.length();

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

        if (! Simple.isInternetConnected())
        {
            Log.d(LOGTAG, "commTick: no internet.");

            return;
        }

        //
        // Load one random feed.
        //

        int random = (int) Math.floor(Math.random() * feedList.length());
        JSONObject feed = Json.getObject(feedList, random);
        Json.remove(feedList, random);
        if (feed == null) return;

        final String feedpfid = Json.getString(feed, "id");
        String feedname = Json.getString(feed, "full_name");
        if (feedname == null) feedname = Json.getString(feed, "name");
        File feedfile = new File(cachedir, feedpfid + ".feed.json");

        Log.d(LOGTAG, "commTick: feed:" + feedpfid + " => " + feedname);

        JSONArray feeddata = getGraphFeed(feedpfid);

        if (feeddata == null)
        {
            //
            // Get recent stored feed to avoid cache file deletion.
            //

            feeddata = Simple.getFileJSONArray(feedfile);
        }

        //
        // Check and cache feed stories and store feed to cache.
        //

        if (feeddata != null)
        {
            cacheFeedStories(feedpfid, feeddata);

            Simple.putFileContent(feedfile, Json.toPretty(feeddata));
        }
    }

    protected void cacheFeedStories(String feedpfid, JSONArray feeddata)
    {
        if (feeddata != null)
        {
            int newposts = 0;

            synchronized (feedSync)
            {
                for (int inx = 0; inx < feeddata.length(); inx++)
                {
                    JSONObject post = Json.getObject(feeddata, inx);

                    String postid = Json.getString(post, "id");
                    if (postid == null) postid = Json.getString(post, "id_str");
                    if (postid == null) continue;

                    String postname = postid + ".post.json";
                    File postfile = new File(cachedir, postname);

                    if ((postFiles != null) && postFiles.contains(postname))
                    {
                        postFiles.remove(postname);
                        continue;
                    }

                    if (postfile.exists()) continue;

                    JSONObject postdata = apifeedhasposts ? post : getGraphPost(postid);
                    if (postdata == null) continue;

                    Simple.putFileContent(postfile, Json.toPretty(postdata));

                    if (getPostSuitable(postdata)) newposts++;

                    Log.d(LOGTAG, "cacheFeedStories: " + postfile.toString());
                }
            }

            SimpleStorage.addInt("socialfeednews", platform + ".count." + feedpfid, newposts);
            SimpleStorage.put("socialfeednews", platform + ".stamp." + feedpfid, Simple.nowAsISO());
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

        void setVerbose(boolean yesno);

        String getAccessExpiration();

        String getPlatform();
        String getUserId();
        String getUserDisplayName();

        JSONArray getUserFeeds(boolean feedonly);

        JSONObject getPost(String postid);
        JSONArray getFeed(String userid);
        JSONObject getGraphRequest(String path, JSONObject params);

        Drawable getProfileDrawable(String pfid, boolean circle);
        boolean getPostSuitable(JSONObject post);

        void reconfigureFriendsAndLikes();

        boolean canSearchUsers();
        JSONArray getSearchUsers(String query, int max);

        int getWindowStatistic();
        int getTodayStatistic();

        void getTest();
    }

    //endregion Social interface
}
