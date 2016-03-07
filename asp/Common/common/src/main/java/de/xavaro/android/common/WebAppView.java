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

    public WebAppPrefs prefs;
    public WebAppSpeak speak;
    public WebAppEvents events;
    public WebAppRequest request;
    public WebAppUtility utility;
    public WebAppIntercept intercept;

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

        if (permissions.contains("request"))
        {
            request = new WebAppRequest(webappname, this, webapploader);
            addJavascriptInterface(request, "WebAppRequest");
        }

        if (permissions.contains("intercept"))
        {
            intercept = new WebAppIntercept(webappname);
            addJavascriptInterface(intercept, "WebAppIntercept");
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

        loadUrl(WebApp.getHTTPAppRoot(webappname));
    }
}
