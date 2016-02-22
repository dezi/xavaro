package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;

public class WebAppExtras
{
    private final String webappname;
    private final WebAppLoader webapploader;

    public WebAppExtras(String webappname, WebAppLoader webapploader)
    {
        this.webappname = webappname;
        this.webapploader = webapploader;
    }

    @JavascriptInterface
    public String loadSync(String src)
    {
        byte[] content = webapploader.getRequestData(src);
        return (content == null) ? null : new String(content);
    }
}
