package de.xavaro.android.safehome;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebClient extends WebViewClient
{
    private final String LOGTAG = "WebClient";

    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "URL=" + url);

        view.loadUrl(url);

        return true;
    }
}
