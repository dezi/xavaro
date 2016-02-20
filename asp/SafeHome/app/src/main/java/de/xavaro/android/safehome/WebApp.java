package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

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
        String manifest = WebApp.getContent(manifestsrc);
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

        return getImage(appiconsrc);
    }

    @Nullable
    public static Drawable getImage(String src)
    {
        try
        {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            int length = connection.getContentLength();
            byte[] buffer = new byte[ length ];

            InputStream input = connection.getInputStream();

            int xfer;
            int total = 0;

            while (total < length)
            {
                xfer = input.read(buffer, total, length - total);
                total += xfer;
            }

            input.close();

            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            return new BitmapDrawable(Simple.getResources(), bitmap);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static String getContent(String src)
    {
        try
        {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            int length = connection.getContentLength();
            byte[] buffer = new byte[ length ];

            InputStream input = connection.getInputStream();

            int xfer;
            int total = 0;

            while (total < length)
            {
                xfer = input.read(buffer, total, length - total);
                total += xfer;
            }

            input.close();

            return new String(buffer);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }
}
