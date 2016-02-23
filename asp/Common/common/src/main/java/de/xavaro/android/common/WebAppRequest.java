package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.util.Log;
import android.webkit.WebView;

@SuppressWarnings("unused")
public class WebAppRequest
{
    private static final String LOGTAG = WebAppRequest.class.getSimpleName();

    private final String webappname;
    private final WebView webview;
    private final WebAppLoader webapploader;

    public WebAppRequest(String webappname, WebView webview, WebAppLoader webapploader)
    {
        this.webappname = webappname;
        this.webview = webview;
        this.webapploader = webapploader;
    }

    @JavascriptInterface
    public String loadSync(String src)
    {
        byte[] content = webapploader.getRequestData(src);
        if (content == null) Log.d(LOGTAG, "loadSync: FAILED " + webappname + "=" + src);
        return (content == null) ? null : new String(content);
    }

    @JavascriptInterface
    public void jsonfunz(String json)
    {
        Log.d(LOGTAG, "=====================> " + json.toString());
    }

    @JavascriptInterface
    public void callback(String src)
    {
        String text = "pupsi=" + src;
        final String js = "console.log(\"" + text + "\");";

        webview.post(new Runnable()
        {
            @Override
            public void run()
            {
                webview.evaluateJavascript(js, null);
            }
        });
    }
}
