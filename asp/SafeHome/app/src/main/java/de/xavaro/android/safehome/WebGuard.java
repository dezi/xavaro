package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
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
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        if (url.contains("smartadserver.com") && (! url.contains("diff/251")))
        {
            Log.d(LOGTAG, "Kill URL=" + url);

            return new WebResourceResponse("text/plain", "utf-8", null);
        }

        if (url.contains("krxd.net/"))
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
}
