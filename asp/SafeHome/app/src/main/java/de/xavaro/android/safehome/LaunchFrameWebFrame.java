package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.WebGuard;

public class LaunchFrameWebFrame extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameWebFrame.class.getSimpleName();

    private WebView webview;
    private WebGuard webguard;

    public LaunchFrameWebFrame(Context context)
    {
        super(context);

        myInit(context);
    }

    public LaunchFrameWebFrame(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public LaunchFrameWebFrame(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void myInit(Context context)
    {
        setBackgroundColor(0xffffffee);

        webguard = new WebGuard();
        webguard.setContext(context);

        webview = new WebView(context);
        webview.setWebViewClient(webguard);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(false);

        webview.getSettings().setSupportZoom(true);

        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        addView(webview);
    }

    public void setLoadURL(JSONObject config, String website, String url)
    {
        webguard.setCurrent(config, url, website);
        webguard.setFeatures(webview);

        webview.loadUrl(url);
    }

    public boolean doBackPressed()
    {
        Log.d(LOGTAG, "doBackPressed");

        if (webview.canGoBack())
        {
            webview.goBack();
            webguard.setWasBackAction();

            return false;
        }

        return true;
    }
}