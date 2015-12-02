package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;


public class WebFrame extends FrameLayout
{
    private final String LOGTAG = "WebFrameLayout";

    private Context context;

    private WebView webview;
    private WebGuard webclient;

    public WebFrame(Context context)
    {
        super(context);

        myInit(context);
    }

    public WebFrame(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public WebFrame(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    private void myInit(Context context)
    {
        this.context = context;

        setBackgroundColor(0xffffffee);

        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        setLayoutParams(layout);

        webclient = new WebGuard();
        webclient.setContext(context);

        webview = new WebView(context);
        webview.setWebViewClient(webclient);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(false);
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);
        webview.getSettings().setSupportZoom(true);

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        addView(webview);
    }

    public void setLoadURL(String url)
    {
        webview.loadUrl(url);
    }
}