package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;


public class WebFrameLayout extends FrameLayout
{
    private final String LOGTAG = "WebFrameLayout";

    private Context context;

    private WebView webview;

    public WebFrameLayout(Context context)
    {
        super(context);

        myInit(context);
    }

    public WebFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public WebFrameLayout(Context context, AttributeSet attrs, int defStyle)
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

        webview = new WebView(context);
        webview.setWebViewClient(new WebClient());

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(false);
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);
        webview.getSettings().setSupportZoom(true);

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webview.loadUrl("http://m.bild.de/");

        addView(webview);
    }
}