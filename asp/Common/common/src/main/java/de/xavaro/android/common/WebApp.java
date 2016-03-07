package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<String, WebAppView> eventWebApps = new HashMap<>();

    public static void handleEventUI(String webappname, JSONArray events)
    {
        WebAppView eventWebApp = null;

        synchronized (eventWebApps)
        {
            if (eventWebApps.containsKey(webappname))
            {
                eventWebApp = eventWebApps.get(webappname);
            }
            else
            {
                eventWebApp = new WebAppView(Simple.getAppContext());
                eventWebApp.loadWebView(webappname, "event");

                eventWebApps.put(webappname, eventWebApp);
            }
        }

        if (eventWebApp.events != null)
        {
            eventWebApp.events.scheduleEventsNotification(events);
        }
    }

    public static void handleEvent(String webappname, JSONArray events)
    {
        final String fwebappname = webappname;
        final JSONArray fevents = events;

        Simple.makePost(new Runnable()
        {
            @Override
            public void run()
            {
                handleEventUI(fwebappname, fevents);
            }
        });
    }
}
