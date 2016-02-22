package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;

public class WebApp
{
    private static final String LOGTAG = WebApp.class.getSimpleName();

    public static String getHTTPRoot(String webappname)
    {
        //
        // http://192.168.2.101/webapps/tvguide/manifest.json
        //

        String httpserver = CommonConfigs.WebappsServerName;
        String httpport = "" + CommonConfigs.WebappsServerPort;

        if (Simple.getSharedPrefBoolean("developer.webapps.httpbypass"))
        {
            httpserver = Simple.getSharedPrefString("developer.webapps.httpserver");
            httpport = Simple.getSharedPrefString("developer.webapps.httpport");
        }

        if ((httpport == null) || httpport.equals("80"))
        {
            return "http://" + httpserver + "/webapps/" + webappname + "/";
        }

        return "http://" + httpserver + ":" + httpport + "/webapps/" + webappname + "/";
    }

    @Nullable
    public static JSONObject getManifest(String webappname)
    {
        String manifestsrc = getHTTPRoot(webappname) + "manifest.json";
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
        String appiconsrc = getHTTPRoot(webappname) + appiconpng;

        return getImage(webappname, appiconsrc);
    }

    @Nullable
    public static JSONArray getPreloads(String webappname)
    {
        return Json.getArray(getManifest(webappname), "preload");
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
}
