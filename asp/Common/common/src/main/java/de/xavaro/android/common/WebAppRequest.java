package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.util.Log;
import android.webkit.WebView;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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
    public boolean saveSync(String src, String json)
    {
        try
        {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);

            OutputStream out = connection.getOutputStream();
            out.write(json.getBytes());
            out.close();

            int result = connection.getResponseCode();

            Log.d(LOGTAG, "================>" + result + "=" + src);

            return (result == 200);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }
}
