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

import de.xavaro.android.common.Json;

public class WebAppLoader extends WebViewClient
{
    private static final String LOGTAG = WebAppLoader.class.getSimpleName();

    private final String rootUrl;
    private final String webappname;

    public WebAppLoader(String rootUrl, String webappname)
    {
        this.rootUrl = rootUrl;
        this.webappname = webappname;
    }

    //region Overridden methods.

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
        return shouldInterceptRequest(view, request.getUrl().toString());
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        if (url.equals(rootUrl)) return loadRootHTML();
        if (url.endsWith("favicon.ico")) return denyLoad();

        Log.d(LOGTAG, "shouldInterceptRequest: " + url);

        return null;
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

    //endregion
}
