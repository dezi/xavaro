package de.xavaro.android.common;

import android.annotation.SuppressLint;

import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.util.Log;

import java.util.ArrayList;

public class WebAppView extends WebView
{
    private static final String LOGTAG = WebAppView.class.getSimpleName();

    public WebAppView(Context context)
    {
        super(context);
    }

    public WebAppBeta beta;
    public WebAppNine nine;
    public WebAppVoice voice;
    public WebAppPrefs prefs;
    public WebAppSpeak speak;
    public WebAppMedia media;
    public WebAppSocial social;
    public WebAppEvents events;
    public WebAppHealth health;
    public WebAppPrices prices;
    public WebAppNotify notify;
    public WebAppWeather weather;
    public WebAppRequest request;
    public WebAppUtility utility;
    public WebAppStorage storage;
    public WebAppActivity activity;
    public WebAppIntercept intercept;
    public WebAppAssistance assistance;

    public WebAppLoader webapploader;
    public ArrayList<String> permissions;

    @SuppressLint("SetJavaScriptEnabled")
    public void loadWebView(String webappname, String mode)
    {
        Log.d(LOGTAG, "loadWebView: " + webappname + " mode=" + mode);

        setWebChromeClient(new WebChromeClient());

        //
        // Remove "Chrome" form user agent string because
        // Google will not go into mobile style if set.
        //

        String agent = getSettings().getUserAgentString().replace("Chrome","");

        webapploader = new WebAppLoader(webappname, agent, mode);
        setWebViewClient(webapploader);

        //
        // Default settings in webview,
        //

        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setSupportMultipleWindows(true);
        getSettings().setSupportZoom(false);
        getSettings().setAppCacheEnabled(false);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        //
        // Permission setting in webview.
        //

        permissions = WebApp.getPermissions(webappname);

        getSettings().setDatabaseEnabled(permissions.contains("database"));
        getSettings().setDomStorageEnabled(permissions.contains("domstorage"));

        //
        // Native add ons via permissions.
        //

        if (permissions.contains("prices"))
        {
            prices = new WebAppPrices(webappname, this, webapploader);
            addJavascriptInterface(prices, "WebAppPrices");
        }

        if (permissions.contains("notify"))
        {
            notify = new WebAppNotify(webappname);
            addJavascriptInterface(notify, "WebAppNotify");
        }

        if (permissions.contains("health"))
        {
            health = new WebAppHealth();
            addJavascriptInterface(health, "WebAppHealth");
        }

        if (permissions.contains("assistance"))
        {
            assistance = new WebAppAssistance();
            addJavascriptInterface(assistance, "WebAppAssistance");
        }

        if (permissions.contains("intercept"))
        {
            intercept = new WebAppIntercept(webappname);
            addJavascriptInterface(intercept, "WebAppIntercept");
        }

        if (permissions.contains("request"))
        {
            request = new WebAppRequest(webappname, this, webapploader);
            addJavascriptInterface(request, "WebAppRequest");
        }

        if (permissions.contains("utility"))
        {
            utility = new WebAppUtility();
            addJavascriptInterface(utility, "WebAppUtility");
        }

        if (permissions.contains("storage"))
        {
            storage = new WebAppStorage(webappname);
            addJavascriptInterface(storage, "WebAppStorage");
        }

        if (permissions.contains("activity"))
        {
            activity = new WebAppActivity();
            addJavascriptInterface(activity, "WebAppActivity");
        }

        if (permissions.contains("social"))
        {
            social = new WebAppSocial();
            addJavascriptInterface(social, "WebAppSocial");
        }

        if (permissions.contains("weather"))
        {
            weather = new WebAppWeather(webappname, this, webapploader);
            addJavascriptInterface(weather, "WebAppWeather");
        }

        if (permissions.contains("prefs"))
        {
            prefs = new WebAppPrefs(webappname);
            addJavascriptInterface(prefs, "WebAppPrefs");
        }

        if (permissions.contains("voice"))
        {
            voice = new WebAppVoice(this);
            addJavascriptInterface(voice, "WebAppVoice");
        }

        if (permissions.contains("speak"))
        {
            speak = new WebAppSpeak(this);
            addJavascriptInterface(speak, "WebAppSpeak");
        }

        if (permissions.contains("events"))
        {
            events = new WebAppEvents(webappname);
            addJavascriptInterface(events, "WebAppEvents");
        }

        if (permissions.contains("media"))
        {
            media = new WebAppMedia();
            addJavascriptInterface(media, "WebAppMedia");
        }

        if (permissions.contains("nine"))
        {
            nine = new WebAppNine(webapploader);
            addJavascriptInterface(nine, "WebAppNine");
        }

        if (permissions.contains("beta"))
        {
            beta = new WebAppBeta();
            addJavascriptInterface(beta, "WebAppBeta");
        }

        loadUrl(WebApp.getHTTPAppRoot(webappname));
    }
}
