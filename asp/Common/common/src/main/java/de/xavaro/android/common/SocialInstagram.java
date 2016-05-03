package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SocialInstagram
{
    private static final String LOGTAG = SocialInstagram.class.getSimpleName();

    private static final String appkey = "63afb3307ec24f4886230f44d2fda884";
    private static final String appurl = "http://www.xavaro.de/instagram";

    private static final Collection<String> permissions = Arrays.asList
            (
                    "basic",
                    "public_content",
                    "follower_list",
                    "likes"
            );

    private static boolean verbose = true;
    private static String locale;

    public static void initialize(Application app)
    {
        //cachedir = new File(Simple.getExternalCacheDir(), "instagram");
        locale = Simple.getLocaleLanguage() + "_" + Simple.getLocaleCountry();
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

                    Log.d(LOGTAG, "=====================code=" + code);
                    Simple.setSharedPrefString("social.instagram.code", code);

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

        Simple.removeSharedPref("social.instagram.code");

        CookieManager.getInstance().setCookie("https://www.instagram.com", "sessionid=");
    }

    public static boolean isLoggedIn()
    {
        return (Simple.getSharedPrefString("social.instagram.code") != null);
    }

    public static void setVerbose(boolean yesno)
    {
        verbose = yesno;
    }

    @Nullable
    public static String getUserId()
    {
        return null;
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
        return null;
    }
}
