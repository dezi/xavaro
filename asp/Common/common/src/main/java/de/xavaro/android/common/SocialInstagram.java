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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class SocialInstagram
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

    private static boolean verbose = true;
    private static JSONObject user;
    private static String locale;
    private static String token;

    public static void initialize(Application app)
    {
        //cachedir = new File(Simple.getExternalCacheDir(), "instagram");
        locale = Simple.getLocaleLanguage() + "_" + Simple.getLocaleCountry();
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

        token = null;
        Simple.removeSharedPref(tokenpref);
        CookieManager.getInstance().setCookie("https://www.instagram.com", "sessionid=");
    }

    public static boolean isLoggedIn()
    {
        return (Simple.getSharedPrefString(tokenpref) != null);
    }

    public static void setVerbose(boolean yesno)
    {
        verbose = yesno;
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
    private static JSONObject getGraphRequest(String path)
    {
        return getGraphRequest(path, null);
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

                String fbid = Json.getString(friend, "id");
                String name = Json.getString(friend, "full_name");
                if ((fbid == null) || (name == null)) continue;

                String fnamepref = "social.instagram.friend.name." + fbid;
                String fmodepref = "social.instagram.friend.mode." + fbid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.getFacebookLoadProfileImage(fbid);

                if (oldfriends.containsKey(fnamepref)) oldfriends.remove(fnamepref);
                if (oldfriends.containsKey(fmodepref)) oldfriends.remove(fmodepref);
            }

            for (Map.Entry<String, ?> entry : oldfriends.entrySet())
            {
                Simple.removeSharedPref(entry.getKey());
            }
        }
    }
}
