package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import android.app.AlertDialog;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.view.View;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SocialGoogleplus extends Social implements Social.SocialInterface
{
    private static final String LOGTAG = SocialGoogleplus.class.getSimpleName();
    private static SocialGoogleplus instance;

    private static final String appsecret = "D65banXZZN55o3Cn1qNc3bSy";
    private static final String appkey = "404416935707-b7o1okeho0c3s2oj9qipksprnn8bshoi.apps.googleusercontent.com";
    private static final String appurl = "http://www.xavaro.de/googleplus";
    private static final String apiurl = "https://www.googleapis.com/plus/v1";
    private static final String tokenpref = "social.googleplus.token";

    private static final Set<String> permissions = new HashSet<String>(Arrays.asList
            (
                    "plus.login",
                    "plus.me",
                    "userinfo.profile"
            ));

    private JSONObject user;
    private String token;

    public static void initialize(Application app)
    {
        if (instance != null) return;
        instance = new SocialGoogleplus();
    }

    public static SocialGoogleplus getInstance()
    {
        return instance;
    }

    public SocialGoogleplus()
    {
        super("googleplus");
    }

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

                if (url.contains(appurl))
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

                    String tokenurl = "https://www.googleapis.com/oauth2/v4/token";
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

        String scope = "";

        for (String permission : permissions)
        {
            if (scope.length() > 0) scope += "%20";

            scope += "https://www.googleapis.com/auth/" + permission;
        }

        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + appkey
                + "&redirect_uri=" + appurl
                + "&scope=" + scope
                + "&response_type=code";

        Log.d(LOGTAG, "====>" + url);

        webview.loadUrl(url);
    }

    public void logout()
    {
        Simple.removeSharedPref(tokenpref);

        token = null;
        user = null;
    }

    @Override
    public boolean isLoggedIn()
    {
        return (Simple.getSharedPrefString(tokenpref) != null);
    }

    @Nullable
    public String getUserId()
    {
        return null;
    }

    @Nullable
    public Set<String> getUserPermissions()
    {
        return permissions;
    }

    @Nullable
    public String getUserTokenExpiration()
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
    public String getUserDisplayName()
    {
        JSONObject userdata = getCurrentUser();
        return Json.getString(userdata, "full_name");
    }

    @Nullable
    public byte[] getUserIconData(String pfid)
    {
        if (pfid == null) return null;

        return null;
    }

    @Nullable
    public String getAccessToken()
    {
        return Simple.getSharedPrefString(tokenpref);
    }

    @Nullable
    public JSONObject getCurrentUser()
    {
        return null;
    }

    @Nullable
    public JSONObject getUser(String pfid)
    {
        JSONObject response = getGraphRequest("/users/" + pfid);
        return Json.getObject(response, "data");
    }

    @Override
    public JSONArray getUserFriendlist()
    {
        JSONObject response = getGraphRequest("/users/self/follows");
        return Json.getArray(response, "data");
    }

    @Override
    public JSONArray getUserLikeslist()
    {
        return null;
    }

    @Nullable
    public Drawable getProfileDrawable(String pfid, boolean circle)
    {
        return ProfileImages.getInstagramProfileDrawable(pfid, true);
    }

}
