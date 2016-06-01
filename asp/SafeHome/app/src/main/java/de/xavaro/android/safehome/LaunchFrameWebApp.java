package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.WebAppView;

public class LaunchFrameWebApp extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameWebApp.class.getSimpleName();

    private WebAppView webview;

    public LaunchFrameWebApp(Context context, LaunchItem parent)
    {
        super(context, parent);
    }

    public void setWebAppName(String webappname)
    {
        webview = new WebAppView(getContext());
        webview.setBackgroundColor(0xffffffff);
        webview.loadWebView(webappname, "main");

        addView(webview);
    }

    public WebAppView getWebAppView()
    {
        return webview;
    }

    public void doDataCallback(String function, String data)
    {
        webview.request.doDataCallback(function, data);
    }

    public void onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        Log.d(LOGTAG, "onExecuteVoiceIntent");

        if (webview.request != null)
        {
            webview.request.doVoiceIntent(voiceintent, index);
        }
    }

    @Override
    public boolean onBackKeyWanted()
    {
        Log.d(LOGTAG, "onBackKeyWanted");

        return (webview.request != null) && webview.request.onBackKeyWanted();
    }

    @Override
    public void onBackKeyExecuted()
    {
        if (parent != null)
        {
            //
            // Inform launch item that we have been removed.
            //

            parent.onBackKeyExecuted();
        }
    }
}
