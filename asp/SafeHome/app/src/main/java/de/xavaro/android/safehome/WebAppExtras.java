package de.xavaro.android.safehome;

import android.webkit.JavascriptInterface;

public class WebAppExtras
{
    @JavascriptInterface
    public String loadSync(String src)
    {
        return WebApp.getContent(src);
    }
}
