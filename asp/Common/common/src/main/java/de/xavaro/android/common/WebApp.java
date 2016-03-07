package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WebApp
{
    private static final String LOGTAG = WebApp.class.getSimpleName();

    public static String getHTTPRoot()
    {
        String httpserver = CommonConfigs.WebappsServerName;
        String httpport = "" + CommonConfigs.WebappsServerPort;
        String wifiname = Simple.getWifiName();

        if (Simple.getSharedPrefBoolean("developer.webapps.httpbypass." + wifiname))
        {
            httpserver = Simple.getSharedPrefString("developer.webapps.httpserver." + wifiname);
            httpport = Simple.getSharedPrefString("developer.webapps.httpport." + wifiname);
        }

        if ((httpport == null) || httpport.equals("80"))
        {
            return "http://" + httpserver;
        }

        return "http://" + httpserver + ":" + httpport;
    }

    public static String getHTTPAppRoot(String webappname)
    {
        return getHTTPRoot() + "/webapps/" + webappname + "/";
    }

    public static String getHTTPLibRoot()
    {
        return getHTTPRoot() + "/weblibs/";
    }

    @Nullable
    public static JSONObject getManifest(String webappname)
    {
        String manifestsrc = getHTTPAppRoot(webappname) + "manifest.json";
        String manifest = WebApp.getStringContent(webappname, manifestsrc);
        JSONObject jmanifest = Json.fromString(manifest);
        jmanifest = Json.getObject(jmanifest, "manifest");

        return jmanifest;
    }

    @Nullable
    public static String getLabel(String webappname)
    {
        return Json.getString(getManifest(webappname), "label");
    }

    @Nullable
    public static Drawable getAppIcon(String webappname)
    {
        String appiconpng = Json.getString(getManifest(webappname), "appicon");
        String appiconsrc = getHTTPAppRoot(webappname) + appiconpng;

        return getImage(webappname, appiconsrc);
    }

    public static ArrayList<String> getPermissions(String webappname)
    {
        ArrayList<String> list = new ArrayList<>();

        JSONArray array = Json.getArray(getManifest(webappname), "permissions");

        if (array != null)
        {
            for (int inx = 0; inx < array.length(); inx++)
            {
                list.add(Json.getString(array, inx));
            }
        }

        return list;
    }

    @Nullable
    private static Drawable getImage(String webappname, String src)
    {
        int interval = Simple.getSharedPrefBoolean("developer.webapps.httpbypass") ? 0 : 24;
        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile(webappname, src, interval);

        if (wcr.content != null)
        {
            Bitmap bitmap = BitmapFactory.decodeByteArray(wcr.content, 0, wcr.content.length);
            return new BitmapDrawable(Simple.getResources(), bitmap);
        }

        return null;
    }

    @Nullable
    public static String getStringContent(String webappname, String src)
    {
        int interval = Simple.getSharedPrefBoolean("developer.webapps.httpbypass") ? 0 : 24;
        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile(webappname, src, interval);

        return (wcr.content == null) ? null : new String(wcr.content);
    }

    public static boolean hasEvents(String webappname)
    {
        return Json.getBoolean(getManifest(webappname), "events");
    }

    public static boolean hasPreferences(String webappname)
    {
        return Json.getBoolean(getManifest(webappname), "preferences");
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void loadWebView(WebView webview, String webappname, String mode)
    {
        webview.setWebChromeClient(new WebChromeClient());

        //
        // Remove "Chrome" form user agent string because
        // Google will not go into mobile style if set.
        //

        String agent = webview.getSettings().getUserAgentString().replace("Chrome","");

        WebAppLoader webapploader = new WebAppLoader(webappname, agent, mode);
        webview.setWebViewClient(webapploader);

        //
        // Default settings in webview,
        //

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.getSettings().setSupportZoom(false);
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        //
        // Permission setting in webview.
        //

        ArrayList<String> permissions = getPermissions(webappname);

        if (permissions.contains("domstorage"))
        {
            webview.getSettings().setDomStorageEnabled(false);
        }

        if (permissions.contains("database"))
        {
            webview.getSettings().setDatabaseEnabled(false);
        }

        //
        // Native add ons via permissions.
        //

        if (permissions.contains("request"))
        {
            Object request = new WebAppRequest(webappname, webview, webapploader);
            webview.addJavascriptInterface(request, "WebAppRequest");
        }

        if (permissions.contains("intercept"))
        {
            Object intercept = new WebAppIntercept();
            webview.addJavascriptInterface(intercept, "WebAppIntercept");
        }

        if (permissions.contains("utility"))
        {
            Object utility = new WebAppUtility();
            webview.addJavascriptInterface(utility, "WebAppUtility");
        }

        if (permissions.contains("prefs"))
        {
            Object prefs = new WebAppPrefs(webappname);
            webview.addJavascriptInterface(prefs, "WebAppPrefs");
        }

        if (permissions.contains("events"))
        {
            Object events = new WebAppEvents(webappname);
            webview.addJavascriptInterface(events, "WebAppEvents");
        }

        webview.loadUrl(WebApp.getHTTPAppRoot(webappname));
    }
}
