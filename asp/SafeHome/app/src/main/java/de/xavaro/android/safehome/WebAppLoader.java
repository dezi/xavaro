package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

import java.io.ByteArrayInputStream;

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
        String url = request.getUrl().toString();

        Log.d(LOGTAG, "shouldInterceptRequest newapi=" + url);
        Log.d(LOGTAG, "shouldInterceptRequest   root=" + rootUrl);

        if (url.equals(rootUrl))
        {
            String initialHTML = "<!doctype html>\n"
                    + "<html>\n"
                    + "<head>\n"
                    + "\t<title>" + webappname + "</title>\n"
                    + "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                    + "</head>\n"
                    + "<body><script src=\"main.js\"></script></body>\n"
                    + "</html>\n";

            ByteArrayInputStream bais = new ByteArrayInputStream(initialHTML.getBytes());
            return new WebResourceResponse("text/html", "UTF-8", bais);
        }

        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        Log.d(LOGTAG, "shouldInterceptRequest oldapi=" + url);

        return null;
    }

    //endregion
}
