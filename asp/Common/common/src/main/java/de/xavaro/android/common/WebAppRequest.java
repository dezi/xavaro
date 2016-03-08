package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.util.Log;
import android.webkit.WebView;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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

    private final ArrayList<String> asyncRequests = new ArrayList<>();

    private final Runnable loadAsyncRunner = new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                String src = null;

                synchronized (asyncRequests)
                {
                    if (asyncRequests.size() > 0)
                    {
                        src = asyncRequests.remove(0);
                    }
                }

                if (src == null) break;

                byte[] content = webapploader.getRequestData(src);

                if (content == null)
                {
                    Log.d(LOGTAG, "loadAsync: FAIL " + webappname + "=" + src);
                    continue;
                }

                Log.d(LOGTAG, "loadAsync: LOAD " + webappname + "=" + src + "=" + content.length);

                final String cbscript = "WebAppRequest.onLoadAsyncJSON(\"" + src + "\","
                        + new String(content) + ");";

                /*
                Runnable callback = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        webview.evaluateJavascript(cbscript, null);
                    }
                };

                Simple.makePost(callback);
                */

                webview.evaluateJavascript(cbscript, null);
            }
        }
    };

    @JavascriptInterface
    public void loadAsyncJSON(String src)
    {
        Simple.removePost(loadAsyncRunner);

        synchronized (asyncRequests)
        {
            asyncRequests.add(src);
        }

        Simple.makePost(loadAsyncRunner, 40);
    }

    @JavascriptInterface
    public String loadSync(String src)
    {
        byte[] content = webapploader.getRequestData(src);
        if (content == null) Log.d(LOGTAG, "loadSync: FAIL " + webappname + "=" + src);
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
