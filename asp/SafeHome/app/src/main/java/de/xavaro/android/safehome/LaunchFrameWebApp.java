package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class LaunchFrameWebApp extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameWebApp.class.getSimpleName();

    private String webappname;
    private WebAppLoader webAppLoader;
    private WebView webview;
    private String rootUrl;

    public LaunchFrameWebApp(Context context)
    {
        this(context, null, 0);
    }

    public LaunchFrameWebApp(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public LaunchFrameWebApp(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setWebAppName(String webappname)
    {
        this.webappname = webappname;

        rootUrl = WebApp.getHTTPRoot(webappname);

        webAppLoader = new WebAppLoader(rootUrl, webappname);

        webview = new WebView(getContext());

        webview.setBackgroundColor(0xff8888ff);
        webview.setWebViewClient(webAppLoader);
        webview.setWebChromeClient(new WebChromeClient());

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.getSettings().setDomStorageEnabled(false);
        webview.getSettings().setSupportZoom(false);
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webview.addJavascriptInterface(new WebAppExtras(), "extras");

        addView(webview);

        webview.loadUrl(rootUrl);
    }

    @Override
    public void onBackKeyExecuted()
    {
        if (parent != null) parent.onBackKeyExecuted();
    }
}
