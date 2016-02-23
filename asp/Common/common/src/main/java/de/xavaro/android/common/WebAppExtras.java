package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.util.Log;

@SuppressWarnings("unused")
public class WebAppExtras
{
    private static final String LOGTAG = WebAppCache.class.getSimpleName();

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

        if (content == null) Log.d(LOGTAG, "loadSync: FAILED " + webappname + "=" + src);

        return (content == null) ? null : new String(content);
    }
}
