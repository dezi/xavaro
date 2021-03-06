package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

@SuppressWarnings("unused")
public class WebAppActivity
{
    private static final String LOGTAG = WebAppActivity.class.getSimpleName();

    @JavascriptInterface
    public String recordActivity(String message)
    {
        String uuid = ActivityOldManager.recordActivity(message);

        return (uuid != null) ? uuid : "";
    }
}
