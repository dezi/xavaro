package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;

public class WebAppView extends WebView
{
    public WebAppView(Context context)
    {
        super(context);
    }

    public WebAppNine nine;
    public WebAppPrefs prefs;
    public WebAppSpeak speak;
    public WebAppMedia media;
    public WebAppEvents events;
    public WebAppHealth health;
    public WebAppRequest request;
    public WebAppUtility utility;
    public WebAppIntercept intercept;
    public WebAppAssistance assistance;

    @SuppressLint("SetJavaScriptEnabled")
    public void loadWebView(String webappname, String mode)
    {
        setWebChromeClient(new WebChromeClient());

        //
        // Remove "Chrome" form user agent string because
        // Google will not go into mobile style if set.
        //

        String agent = getSettings().getUserAgentString().replace("Chrome","");

        WebAppLoader webapploader = new WebAppLoader(webappname, agent, mode);
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

        ArrayList<String> permissions = WebApp.getPermissions(webappname);

        if (permissions.contains("domstorage"))
        {
            getSettings().setDomStorageEnabled(false);
        }

        if (permissions.contains("database"))
        {
            getSettings().setDatabaseEnabled(false);
        }

        //
        // Native add ons via permissions.
        //

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

        if (permissions.contains("prefs"))
        {
            prefs = new WebAppPrefs(webappname);
            addJavascriptInterface(prefs, "WebAppPrefs");
        }

        if (permissions.contains("speak"))
        {
            speak = new WebAppSpeak();
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

        loadUrl(WebApp.getHTTPAppRoot(webappname));
    }
}
