package de.xavaro.android.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

public class WebLib
{
    private static final String LOGTAG = WebLib.class.getSimpleName();

    @Nullable
    public static JSONObject getConfig(String weblib)
    {
        int interval = WebApp.getWebAppInterval();

        String src = WebApp.getHTTPLibRoot() + weblib + ".json";
        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile("weblibs", src, interval);
        if (wcr.content == null) return null;

        return Json.fromStringObject(new String(wcr.content));
    }

    @Nullable
    public static JSONObject getLocaleConfig(String weblib, String subtype)
    {
        JSONObject config = getLocaleConfig(weblib);

        if (subtype != null) config = Json.getObject(config, subtype);

        return config;
    }

    @Nullable
    public static JSONObject getLocaleConfig(String weblib)
    {
        int interval = WebApp.getWebAppInterval();

        String src = WebApp.getHTTPLibRoot() + weblib + ".json";
        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile("weblibs", src, interval);
        if (wcr.content == null) return null;

        JSONObject base = Json.fromStringObject(new String(wcr.content));
        if (base == null) return null;

        JSONArray locales = Json.getArray(base, "locales");
        if (locales == null) return null;

        String localesuffix = "." + Simple.getLocale();

        for (int inx = 0; inx < locales.length(); inx++)
        {
            String localeitem = Json.getString(locales, inx);
            if (localeitem == null) continue;

            if (localeitem.endsWith(localesuffix))
            {
                src = WebApp.getHTTPLibRoot() + localeitem + ".json";
                wcr = WebAppCache.getCacheFile("weblibs", src, interval);
                if (wcr.content == null) return null;

                return Json.fromStringObject(new String(wcr.content));
            }
        }

        return null;
    }

    @Nullable
    public static Bitmap getIconBitmap(String weblib, String iconurl)
    {
        int interval = WebApp.getWebAppInterval();

        String src = WebApp.getHTTPLibRoot() + weblib + "/" + iconurl;
        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile("weblibs", src, interval);
        if (wcr.content == null) return null;

        return BitmapFactory.decodeByteArray(wcr.content, 0, wcr.content.length);
    }

    @Nullable
    public static Drawable getIconDrawable(String weblib, String iconurl)
    {
        return Simple.getDrawable(getIconBitmap(weblib, iconurl));
    }
}