package de.xavaro.android.common;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

public class WebLib
{
    private static final String LOGTAG = WebLib.class.getSimpleName();

    @Nullable
    public static JSONObject getConfig(String weblib)
    {
        boolean devel = Simple.getSharedPrefBoolean("developer.enable");
        String bypass = "developer.webapps.httpbypass." + Simple.getWifiName();
        int interval = (devel && Simple.getSharedPrefBoolean(bypass)) ? 0 : 24;

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
        boolean devel = Simple.getSharedPrefBoolean("developer.enable");
        String bypass = "developer.webapps.httpbypass." + Simple.getWifiName();
        int interval =  (devel && Simple.getSharedPrefBoolean(bypass)) ? 0 : 24;

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
}
