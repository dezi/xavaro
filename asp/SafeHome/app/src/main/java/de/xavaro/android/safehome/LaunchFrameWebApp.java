package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import de.xavaro.android.common.WebApp;
import de.xavaro.android.common.WebAppRequest;
import de.xavaro.android.common.WebAppLoader;
import de.xavaro.android.common.WebAppView;

public class LaunchFrameWebApp extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameWebApp.class.getSimpleName();

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

    public void setWebAppName(String webappname)
    {
        WebAppView webview = new WebAppView(getContext());
        webview.setBackgroundColor(0xffffffff);
        webview.loadWebView(webappname, "main");

        addView(webview);
    }

    @Override
    public void onBackKeyExecuted()
    {
        if (parent != null) parent.onBackKeyExecuted();
    }
}
