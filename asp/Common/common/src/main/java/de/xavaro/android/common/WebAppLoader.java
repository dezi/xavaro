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
import java.io.File;

public class WebAppLoader extends WebViewClient
{
    private static final String LOGTAG = WebAppLoader.class.getSimpleName();

    public static final int LOADER_CACHE_WRITEONLY = 0;
    public static final int LOADER_CACHE_STRIPHEADERS = -1;
    public static final int LOADER_NATIVE = -2;
    public static final int LOADER_INTERCEPT = -3;
    public static final int LOADER_DENY = -4;
    public static final int LOADER_UNKNOWN = -5;

    private final String webappname;
    private final String agent;
    private final String mode;
    private final String rootUrl;
    private final String appRootUrl;
    private final JSONArray cachedefs;

    public WebAppLoader(String webappname, String agent, String mode)
    {
        this.webappname = webappname;
        this.agent = agent;
        this.mode = mode;

        rootUrl = WebApp.getHTTPRoot();
        appRootUrl = WebApp.getHTTPAppRoot(webappname);
        cachedefs = Json.getArray(WebApp.getManifest(webappname), "cachedefs");
    }

    //region Overridden methods

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "shouldOverrideUrlLoading url=" + url);

        return true;
    }

    @Override
    @SuppressLint("NewApi")
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request)
    {
        return getRequest(view, request.getUrl().toString());
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        return getRequest(view, url);
    }

    //endregion Overridden methods

    public byte[] getRequestData(String url)
    {
        if ((! url.startsWith("http://")) && (! url.startsWith("http://")))
        {
            if (url.startsWith("/weblibs/"))
            {
                url = rootUrl + url;
            }
            else
            {
                url = appRootUrl + url;
            }
        }

        int interval = getCacheIntervalForUrl(url);

        if (interval == LOADER_UNKNOWN)
        {
            //
            // Unknown cache policy for url in manifest, deny anyway.
            //

            return null;
        }

        Log.d(LOGTAG, "getRequestData:" + url);

        WebAppCache.WebAppCacheResponse wcr;
        wcr = WebAppCache.getCacheFile(webappname, url, interval, agent);
        if (wcr.content == null) return null;

        return wcr.content;
    }

    private WebResourceResponse getLocalFile(WebView view, String url)
    {
        if (! url.startsWith("local://")) return denyLoad();

        byte[] bytes = Simple.getFileBytes(new File(url.substring(8)));
        if (bytes == null) return denyLoad();

        String mime = "image/jpeg";
        String enco = "UTF-8";

        return new WebResourceResponse(mime, enco, new ByteArrayInputStream(bytes));
    }

    private WebResourceResponse getRequest(WebView view, String url)
    {
        if (url.equals(appRootUrl)) return loadRootHTML();
        if (url.endsWith("favicon.ico")) return denyLoad();

        if (url.startsWith("local://")) return getLocalFile(view, url);

        int interval = getCacheIntervalForUrl(url);

        if (interval <= LOADER_DENY) return denyLoad();

        if (interval == LOADER_NATIVE) return null;

        if (interval == LOADER_INTERCEPT)
        {
            final WebView rview = view;
            final String rurl = url;

            view.post(new Runnable()
            {
                @Override
                public void run()
                {
                    String script = "WebAppIntercept.onUserHrefClick(\"" + rurl + "\");";
                    rview.evaluateJavascript(script, null);
                }
            });

            return denyLoad();
        }

        WebAppCache.WebAppCacheResponse wcr;
        wcr = WebAppCache.getCacheFile(webappname, url, interval, agent);
        if (wcr.content == null) return denyLoad();

        return new WebResourceResponse(wcr.mimetype, wcr.encoding,
                new ByteArrayInputStream(wcr.content));
    }

    private int getCacheIntervalForUrl(String url)
    {
        int interval = LOADER_UNKNOWN;

        String wifiname = Simple.getWifiName();

        if (url.startsWith(rootUrl))
        {
            if (Simple.getSharedPrefBoolean("developer.webapps.httpbypass." + wifiname))
            {
                //
                // Developer mode. Always load webapp components w/o cache.
                //

                interval = LOADER_CACHE_WRITEONLY;
            }
            else
            {
                interval = 24;
            }

            return interval;
        }

        if (cachedefs != null)
        {
            for (int inx = 0; inx < cachedefs.length(); inx++)
            {
                JSONObject cachedef = Json.getObject(cachedefs, inx);

                String pattern = Json.getString(cachedef, "pattern");

                if (pattern != null)
                {
                    pattern = pattern.replace("%%appserver%%", WebApp.getHTTPRoot());
                }

                if ((pattern != null) && url.matches(pattern))
                {
                    interval = Json.getInt(cachedef, "interval");
                    break;
                }
            }
        }

        if (interval >= 0)
        {
            if (Simple.getSharedPrefBoolean("developer.webapps.datacachedisable." + wifiname))
            {
                //
                // Developer mode. Always load data components w/o cache.
                //

                interval = LOADER_CACHE_WRITEONLY;
            }
        }

        if (interval == LOADER_UNKNOWN)
        {
            Log.e(LOGTAG, "getCacheIntervalForUrl: unknown cache policy:" + url);
        }

        return interval;
    }

    private WebResourceResponse denyLoad()
    {
        return new WebResourceResponse("text/plain", "utf-8", null);
    }

    private WebResourceResponse loadRootHTML()
    {
        String preloadmeta = "";
        String preloadjava = "<script>" + webappname + " = {};</script>\n";

        JSONObject manifest = WebApp.getManifest(webappname);

        if (manifest != null)
        {
            preloadjava += "<script>"
                    + "WebApp = {};\n"
                    + "WebApp.manifest = \n"
                    + Json.toPretty(manifest) + ";\n"
                    + "</script>\n";
        }

        JSONArray weblibs = Json.getArray(manifest, "weblibs");

        if (weblibs != null)
        {
            for (int inx = 0; inx < weblibs.length(); inx++)
            {
                preloadmeta += "<script src=\""
                        + WebApp.getHTTPLibRoot() + Json.getString(weblibs, inx) + ".js"
                        + "\"></script>\n";
            }
        }

        JSONArray preloads = Json.getArray(manifest, "preload");

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
                + "<body>\n"
                + "<script>\n"
                + "document.body.style.margin = '0px';\n"
                + "document.body.style.padding = '0px';\n"
                + "</script>\n"
                + "<script src=\"" + mode + ".js\"></script>\n"
                + "</body>\n"
                + "</html>\n";

        //Log.d(LOGTAG, initialHTML);

        ByteArrayInputStream bais = new ByteArrayInputStream(initialHTML.getBytes());
        return new WebResourceResponse("text/html", "UTF-8", bais);
    }

    private Runnable onPageFinishedRunner;

    public void setOnPageFinishedRunner(Runnable runner)
    {
        onPageFinishedRunner = runner;
    }

    @Override
    public void onPageFinished(WebView view, String url)
    {
        Log.d(LOGTAG, "onPageFinished:" + url);

        if (onPageFinishedRunner != null)
        {
            Simple.makePost(onPageFinishedRunner);
            onPageFinishedRunner = null;
        }
    }
}
