package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.json.JSONObject;

//
// User agent: Mozilla/5.0 (Linux; Android 5.0.2; SM-T555 Build/LRX22G; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/46.0.2490.76 Safari/537.36
//

public class LaunchFrameWebFrame extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameWebFrame.class.getSimpleName();

    private JSONObject config;
    private WebView webview;
    private WebGuard webguard;

    //region Region: Constructor logic.

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

        webview.getSettings().setSupportZoom(true);

        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        addView(webview);
    }

    //endregion

    public void setLoadURL(JSONObject config, String website, String url)
    {
        webguard.setCurrent(config, url, website);
        webguard.setFeatures(webview);

        webview.loadUrl(url);
    }

    @Override
    public void onBackKeyExecuted()
    {
        Log.d(LOGTAG, "onBackKeyExecuted");

        //
        // Bubble event up to launchitem
        // to get deallocated.
        //

        this.parent.onBackKeyExecuted();
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