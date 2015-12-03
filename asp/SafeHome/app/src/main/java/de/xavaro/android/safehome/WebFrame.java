package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class WebFrame extends FrameLayout
{
    private final String LOGTAG = "WebFrameLayout";

    private Context context;

    private WebView webview;
    private WebGuard webguard;

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

        webguard = new WebGuard();
        webguard.setContext(context);

        webview = new WebView(context);
        webview.setWebViewClient(webguard);

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

    public boolean doBackPressed()
    {
        Log.d(LOGTAG, "doBackPressed");

        if (webview.canGoBack())
        {
            webview.goBack();

            return false;
        }

        return true;
    }
}