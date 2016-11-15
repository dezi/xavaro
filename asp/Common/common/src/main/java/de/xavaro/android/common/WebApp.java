package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

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

        if (httpport.equals("80"))
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
    public static String getCategory(String webappname)
    {
        return Json.getString(getManifest(webappname), "category");
    }

    @Nullable
    public static byte[] getIconData(String webappname, String iconurl)
    {
        int interval = WebApp.getWebAppInterval();

        String src = WebApp.getHTTPAppRoot(webappname) + "/" + iconurl;
        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile(webappname, src, interval);
        return wcr.content;
    }


    @Nullable
    public static JSONArray getVoiceIntents(String webappname)
    {
        JSONObject manifest = getManifest(webappname);
        if ((manifest == null) || ! Json.getBoolean(manifest, "voice")) return null;

        JSONArray locales = Json.getArray(manifest, "locale");
        if (locales == null) return null;

        String target = Simple.getLocale() + ".json";

        for (int inx = 0; inx < locales.length(); inx++)
        {
            String locale = Json.getString(locales, inx);
            if ((locale == null) || ! locale.endsWith(target)) continue;

            String localesrc = getHTTPAppRoot(webappname) + locale;
            JSONObject strings = Json.fromString(getStringContent(webappname, localesrc));
            if (strings == null) continue;

            JSONArray intents = null;

            if (Json.has(strings, "intents"))
            {
                intents = Json.getArray(strings, "intents");
            }

            if (Json.has(strings, "intent"))
            {
                if (intents == null) intents = new JSONArray();
                Json.put(intents, Json.getObject(strings, "intent"));
            }

            return ((intents != null) && (intents.length() > 0)) ? intents : null;
        }

        return null;
    }


    @Nullable
    public static byte[] getAppIconData(String webappname)
    {
        String appiconpng = Json.getString(getManifest(webappname), "appicon");
        if (appiconpng == null) return null;

        String appiconsrc = getHTTPAppRoot(webappname) + appiconpng;

        int interval = getWebAppInterval();

        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile(webappname, appiconsrc, interval);

        return wcr.content;
    }

    @Nullable
    public static Drawable getAppIcon(String webappname)
    {
        byte[] imgdata = getAppIconData(webappname);
        if (imgdata == null) return null;

        Bitmap bitmap = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length);
        return new BitmapDrawable(Simple.getResources(), bitmap);
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

    public static int getWebAppInterval()
    {
        boolean devel = Simple.getSharedPrefBoolean("developer.enable");
        String bypass = "developer.webapps.httpbypass." + Simple.getWifiName();

        return (devel && Simple.getSharedPrefBoolean(bypass)) ? 0 : 24;
    }

    @Nullable
    private static String getStringContent(String webappname, String src)
    {
        int interval = getWebAppInterval();

        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile(webappname, src, interval);

        return (wcr.content == null) ? null : new String(wcr.content);
    }

    private static boolean hasEvents(String webappname)
    {
        return Json.getBoolean(getManifest(webappname), "events");
    }

    public static boolean hasPreferences(String webappname)
    {
        return Json.getBoolean(getManifest(webappname), "preferences");
    }

    public static boolean hasVoice(String webappname)
    {
        return Json.getBoolean(getManifest(webappname), "voice");
    }

    private static final Map<String, WebAppView> eventWebApps = new HashMap<>();

    private static void handleEventUI(String webappname, JSONArray events)
    {
        Log.d(LOGTAG, "handleEventUI:" + webappname + "=" + events.length());

        if (! hasEvents(webappname))
        {
            OopsService.log(LOGTAG, "handleEventUI: no event.js:" + webappname);

            return;
        }

        WebAppView eventWebApp;

        synchronized (eventWebApps)
        {
            if (eventWebApps.containsKey(webappname))
            {
                eventWebApp = eventWebApps.get(webappname);
            }
            else
            {
                Log.d(LOGTAG, "handleEventUI: request event webapp start:" + webappname);

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
        Log.d(LOGTAG, "handleEvent:" + webappname + "=" + events.length());

        int postdelay = 0;

        if (! CommonStatic.focused)
        {
            Log.d(LOGTAG, "handleEvent: Application is offline");

            //
            // Create launch intent for Safehome.
            //

            try
            {
                Intent intent = new Intent("de.xavaro.android.safehome.LAUNCHITEM");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", "webapp");
                intent.putExtra("subtype", webappname);

                Simple.getAnyContext().startActivity(intent);

                postdelay = 5000;
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);

                return;
            }
        }

        final String fwebappname = webappname;
        final JSONArray fevents = events;

        final Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                handleEventUI(fwebappname, fevents);
            }
        };

        Simple.makePost(runner, postdelay);
    }

    public static void requestUnload(String webappname, int resultcode)
    {
        synchronized (eventWebApps)
        {
            Log.d(LOGTAG, "requestUnload:" + webappname + "=" + resultcode);

            if (eventWebApps.containsKey(webappname))
            {
                eventWebApps.remove(webappname);
            }
        }
    }
}
