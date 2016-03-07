package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

@SuppressWarnings("unused")
public class WebAppIntercept
{
    private final String webappname;

    public WebAppIntercept(String webappname)
    {
        this.webappname = webappname;
    }

    @JavascriptInterface
    public void requestUnload(int resultcode)
    {
        WebApp.requestUnload(webappname, resultcode);
    }
}
