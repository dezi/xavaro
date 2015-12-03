package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;

public class WebGuard extends WebViewClient
{
    private final String LOGTAG = "WebGuard";
    private Context context;
    private ArrayList backStack = new ArrayList();

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "URL=" + url);

        Toast.makeText(context,url,Toast.LENGTH_LONG).show();

        backStack.add(url);

        view.loadUrl(url);

        return true;
    }

    @Override
    @SuppressLint("NewApi")
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request)
    {
        return checkUrlResource(request.getUrl().toString());
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        return checkUrlResource(url);
    }

    private WebResourceResponse checkUrlResource(String url)
    {
        if (url.contains("smartadserver.com") && (! url.contains("diff/251")))
        {
            Log.d(LOGTAG, "Kill URL=" + url);

            return new WebResourceResponse("text/plain", "utf-8", null);
        }

        if (url.contains("krxd.net/") ||
                url.contains("doubleclick.net/") ||
                url.contains("adition.com/") ||
                url.contains("yoc.com/") ||
                url.contains("71i.de/") ||
                url.contains("ioam.de/") ||
                url.contains("amobee.com/") ||
                url.contains("yieldlab.net/") ||
                url.contains("meetrics.net/") ||
                url.contains("revsci.net/") ||
                url.contains("tfag.de/") ||
                url.contains("movad.de/") ||
                url.contains("outbrain.com/") ||
                url.contains("nuggad.net/") ||
                url.contains("nuggad.net/") ||
                url.contains("emetriq.de/") ||
                url.contains("optimizely.com/") ||
                url.contains("edelight.biz/") ||
                url.contains("emsservice.de/") ||
                url.contains("stickyadstv.com/") ||
                url.contains("amazon-adsystem.com/") ||
                url.contains("google-analytics.com/") ||
                url.contains("googletagmanager.com/") ||
                url.contains("googlesyndication.com/") ||
                url.contains("googletagservices.com/") ||
                url.contains("googleadservices.com/"))
        {
            Log.d(LOGTAG, "Kill URL=" + url);

            return new WebResourceResponse("text/plain", "utf-8", null);
        }

        Log.d(LOGTAG, "Load URL=" + url);

        return null;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public int getBackStackSize()
    {
        return backStack.size();
    }

    public void popBackStack()
    {
        if (backStack.size() > 0)
        {
            backStack.remove(backStack.size() - 1);
        }
    }
}
