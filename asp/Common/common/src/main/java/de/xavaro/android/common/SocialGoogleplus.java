package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import android.app.AlertDialog;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.view.View;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
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
    private static final String oauthurl = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String[] scopes = { "plus.login", "plus.me", "userinfo.profile" };

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

        apiurl = "https://www.googleapis.com/plus/v1";
        apiextraparam = "&prettyPrint=true";
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

                    String tokenurl = "https://www.googleapis.com/oauth2/v4/token";
                    String content = SimpleRequest.readContent(tokenurl, postdata);

                    Log.d(LOGTAG, "=====>" + content);

                    if (content != null)
                    {
                        JSONObject jcontent = Json.fromString(content);

                        accessToken = Json.getString(jcontent, "access_token");
                        refreshToken = Json.getString(jcontent, "refresh_token");

                        int expiseconds = Json.getInt(jcontent, "expires_in");
                        expiration = Simple.nowAsTimeStamp() + (expiseconds - 10) * 1000;

                        Simple.setSharedPrefString(expirationpref, Simple.timeStampAsISO(expiration));
                        Simple.setSharedPrefString(accesstokenpref, accessToken);
                        Simple.setSharedPrefString(refreshtokenpref, refreshToken);

                        File socialdir = Simple.getMediaPath("social");
                        File oauthfile = new File(socialdir, platform + ".oauth.json");
                        Simple.putFileContent(oauthfile, content);
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

        String url = oauthurl
                + "?client_id=" + appkey
                + "&redirect_uri=" + appurl
                + "&scope=" + getScopeParameter()
                + "&response_type=code"
                + "&access_type=offline"
                + "&approval_prompt=force";

        Log.d(LOGTAG, "====>" + url);

        webview.loadUrl(url);
    }

    public void logout()
    {
        clearCookies("https://accounts.google.com");

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

    @Override
    public boolean isLoggedIn()
    {
        return (Simple.getSharedPrefString(accesstokenpref) != null);
    }

    @Nullable
    public Set<String> getUserPermissions()
    {
        return null;
    }

    @Override
    protected String getScopeParameter()
    {
        String scopeval = "";

        for (String scope : scopes)
        {
            if (scopeval.length() > 0) scopeval += "%20";

            scopeval += "https://www.googleapis.com/auth/" + scope;
        }

        return scopeval;
    }


    @Nullable
    public String getUserTokenExpiration()
    {
        return isLoggedIn() ? Simple.timeStampAsISO(0) : null;
    }

    @Nullable
    public String getUserId()
    {
        JSONObject userdata = getCurrentUser();
        return Json.getString(userdata, "id");
    }

    @Nullable
    public String getUserDisplayName()
    {
        JSONObject userdata = getCurrentUser();
        return Json.getString(userdata, "displayName");
    }

    @Nullable
    public byte[] getUserIconData(String pfid)
    {
        if (pfid == null) return null;

        JSONObject userdata = getCurrentUser();

        if ((userdata == null) || !Simple.equals(pfid, Json.getString(userdata, "id")))
        {
            userdata = getGraphUser(pfid);
        }

        JSONObject image = Json.getObject(userdata, "image");
        String iconurl = Json.getString(image, "url");
        if (iconurl != null) iconurl = iconurl.replace("?sz=50", "?sz=400");
        return SimpleRequest.readData(iconurl);
    }

    @Nullable
    public String getAccessToken()
    {
        // todo check expiration and refresh.

        String validdate = Simple.getSharedPrefString(expirationpref);
        if (validdate == null) return null;

        if (validdate.compareTo(Simple.nowAsISO()) < 0)
        {
            //
            // Token is expired.
            //

            Log.d(LOGTAG,"getAccessToken: expired...");
        }

        return Simple.getSharedPrefString(accesstokenpref);
    }

    @Nullable
    public JSONObject getGraphCurrentUser()
    {
        return isLoggedIn() ? getGraphRequest("/people/me") : null;
    }

    @Nullable
    public JSONObject getGraphUser(String pfid)
    {
        return isLoggedIn() ? getGraphRequest("/people/" + pfid) : null;
    }

    @Override
    public JSONArray getUserFriendlist()
    {
        return null;
    }

    @Override
    public JSONArray getUserLikeslist()
    {
        return null;
    }

    @Nullable
    public Drawable getProfileDrawable(String pfid, boolean circle)
    {
        return ProfileImages.getGoogleplusProfileDrawable(pfid, true);
    }
}
