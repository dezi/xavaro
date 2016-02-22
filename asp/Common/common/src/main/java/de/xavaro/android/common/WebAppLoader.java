package de.xavaro.android.common;

import android.annotation.SuppressLint;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.acl.LastOwnerException;

import de.xavaro.android.common.Json;

public class WebAppLoader extends WebViewClient
{
    private static final String LOGTAG = WebAppLoader.class.getSimpleName();

    private final String webappname;
    private final String rootUrl;
    private final JSONArray cachedefs;

    public WebAppLoader(String webappname)
    {
        this.webappname = webappname;

        rootUrl = WebApp.getHTTPRoot(webappname);
        cachedefs = Json.getArray(WebApp.getManifest(webappname), "cachedefs");
    }

    //region Overridden methods

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "shouldOverrideUrlLoading=" + url);

        return true;
    }

    @Override
    @SuppressLint("NewApi")
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request)
    {
        return getRequest(request.getUrl().toString());
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        return getRequest(url);
    }

    //endregion Overridden methods

    public byte[] getRequestData(String url)
    {
        int interval = getIntervalForUrl(url);

        if (interval < 0)
        {
            //
            // Unknown cache policy for url in manifest, deny anyway.
            //

            return null;
        }

        if (url.startsWith(rootUrl))
        {
            if (Simple.getSharedPrefBoolean("developer.webapps.httpbypass"))
            {
                //
                // Developer mode. Always load webapp components w/o cache.
                //

                interval = 0;
            }
        }
        else
        {
            Log.d(LOGTAG, "==========>" + interval + "=" + url);

            if (Simple.getSharedPrefBoolean("developer.webapps.datacachedisable"))
            {
                //
                // Developer mode. Always load data components w/o cache.
                //

                interval = 0;
            }
        }

        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile(webappname, url, interval);
        if (wcr.content == null) return null;

        return wcr.content;
    }

    public WebResourceResponse getRequest(String url)
    {
        if (url.equals(rootUrl)) return loadRootHTML();
        if (url.endsWith("favicon.ico")) return denyLoad();

        Log.d(LOGTAG, "getRequest: " + url);

        int interval = getIntervalForUrl(url);

        if (interval < 0)
        {
            //
            // Unknown cache policy for url in manifest, deny anyway.
            //

            return denyLoad();
        }

        if (url.startsWith(rootUrl))
        {
            if (Simple.getSharedPrefBoolean("developer.webapps.httpbypass"))
            {
                //
                // Developer mode. Always load webapp components w/o cache.
                //

                interval = 0;
            }
        }
        else
        {
            Log.d(LOGTAG, "==========>" + interval + "=" + url);

            if (Simple.getSharedPrefBoolean("developer.webapps.datacachedisable"))
            {
                //
                // Developer mode. Always load data components w/o cache.
                //

                interval = 0;
            }
        }

        WebAppCache.WebAppCacheResponse wcr = WebAppCache.getCacheFile(webappname, url, interval);
        if (wcr.content == null) return denyLoad();

        return new WebResourceResponse(wcr.mimetype, wcr.encoding,
                new ByteArrayInputStream(wcr.content));
    }

    private int getIntervalForUrl(String url)
    {
        if (cachedefs != null)
        {
            for (int inx = 0; inx < cachedefs.length(); inx++)
            {
                JSONObject cachedef = Json.getObject(cachedefs, inx);

                String pattern = Json.getString(cachedef, "pattern");
                int interval = Json.getInt(cachedef, "interval");

                if ((pattern != null) && url.matches(pattern)) return interval;
            }
        }

        return -1;
    }

    private WebResourceResponse denyLoad()
    {
        return new WebResourceResponse("text/plain", "utf-8", null);
    }

    private WebResourceResponse loadRootHTML()
    {
        String preloadjava = "";
        String preloadmeta = "";

        preloadjava = "<script>" + webappname + " = {};</script>\n";

        JSONObject manifest = WebApp.getManifest(webappname);

        if (manifest != null)
        {
            preloadjava += "<script>"
                    + webappname + ".manifest = \n"
                    + Json.toPretty(manifest) + ";\n"
                    + "</script>\n";
        }

        JSONArray preloads = WebApp.getPreloads(webappname);

        if (preloads != null)
        {
            for (int inx = 0; inx < preloads.length(); inx++)
            {
                preloadmeta += "<script src=\""
                        + Json.getString(preloads, inx)
                        + "\"></script>\n";
            }
        }

        String initialHTML = "<!doctype html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>" + webappname + "</title>\n"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + preloadjava
                + preloadmeta
                + "</head>\n"
                + "<body><script src=\"main.js\"></script></body>\n"
                + "</html>\n";

        //Log.d(LOGTAG, initialHTML);

        ByteArrayInputStream bais = new ByteArrayInputStream(initialHTML.getBytes());
        return new WebResourceResponse("text/html", "UTF-8", bais);
    }
}
