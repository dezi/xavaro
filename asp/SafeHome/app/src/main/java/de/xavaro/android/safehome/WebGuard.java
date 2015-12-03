package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebGuard extends WebViewClient
{
    private final String LOGTAG = "WebGuard";

    private Context context;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "URL=" + url);

        Toast.makeText(context,url,Toast.LENGTH_LONG).show();

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
                url.contains("shop.mopo.de/") ||
                url.contains("alster.mopo.de/") ||
                url.contains("www.express.de/") ||
                url.contains("praeludium_mopo.js") ||
                url.contains("doubleclick.net/") ||
                url.contains("adition.com/") ||
                url.contains("yoc.com/") ||
                url.contains("m6r.eu/") ||
                url.contains("71i.de/") ||
                url.contains("ioam.de/") ||
                url.contains("criteo.com/") ||
                url.contains("amobee.com/") ||
                url.contains("yieldlab.net/") ||
                url.contains("meetrics.net/") ||
                url.contains("revsci.net/") ||
                url.contains("tfag.de/") ||
                url.contains("movad.de/") ||
                url.contains("outbrain.com/") ||
                url.contains("nuggad.net/") ||
                url.contains("wt-eu02.net/") ||
                url.contains("veeseo.com/") ||
                url.contains("addthis.com/") ||
                url.contains("ligatus.com/") ||
                url.contains("emetriq.de/") ||
                url.contains("laterpay.net/") ||
                url.contains("optimizely.com/") ||
                url.contains("edelight.biz/") ||
                url.contains("emsservice.de/") ||
                url.contains("adobedtm.com/") ||
                url.contains("stroeerdigitalmedia.de/") ||
                url.contains("plista.com/") ||
                url.contains("stickyadstv.com/") ||
                url.contains("elasticbeanstalk.com/") ||
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

        if ((! url.endsWith(".png")) && (! url.endsWith(".jpg")) && (! url.endsWith(".ico")))
        {
            Log.d(LOGTAG, "Load URL=" + url);
        }

        return null;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }
}
