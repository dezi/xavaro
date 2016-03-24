package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

@SuppressWarnings("unused")
public class WebAppActivity
{
    private static final String LOGTAG = WebAppActivity.class.getSimpleName();

    @JavascriptInterface
    public void recordActivity(String message)
    {
        ActivityManager.recordActivity(message);
    }
}
