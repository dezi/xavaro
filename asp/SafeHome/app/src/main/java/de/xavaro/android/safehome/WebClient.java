package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebClient extends WebViewClient
{
    private final String LOGTAG = "WebClient";
    private Context context;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "URL=" + url);

        Toast.makeText(context,url,Toast.LENGTH_LONG).show();

        view.loadUrl(url);

        return true;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }
}
