package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

public class WebAppExtras
{
    public final String webappname;

    public WebAppExtras(String webappname)
    {
        this.webappname = webappname;
    }

    @JavascriptInterface
    public String loadSync(String src)
    {
        return WebApp.getContent(webappname, src);
    }
}
