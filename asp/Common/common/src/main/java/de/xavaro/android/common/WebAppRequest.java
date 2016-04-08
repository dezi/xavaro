package de.xavaro.android.common;

import android.content.Context;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONObject;

import java.io.InputStream;
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

    public void doDataCallback(String function, String data)
    {
        final String cbscript = function + "(" + data + ");";
        webview.evaluateJavascript(cbscript, null);
    }

    public void doVoiceIntent(VoiceIntent intent, int index)
    {
        //
        // Register fake callback function in case
        // webapp did not define one itself.
        //

        JSONObject matchobj = intent.getMatch(index);
        Json.put(matchobj, "command", intent.getCommand());
        Json.put(matchobj, "triggers", intent.getActionKeywords(index));
        String match = (matchobj == null) ? "{}" : matchobj.toString();

        final String cbscript = ""
                + "if (! WebAppRequest.onVoiceIntent)"
                + "{"
                + "    WebAppRequest.onVoiceIntent = function()"
                + "    {"
                + "    }"
                + "}"
                + "WebAppRequest.onVoiceIntent(" + match + ");";

        webview.evaluateJavascript(cbscript, null);
    }

    //region Back key handling

    public void doBackPressed()
    {
        //
        // Register fake callback function in case
        // webapp did not define one itself.
        //

        final String cbscript = ""
                + "if (! WebAppRequest.onBackkeyPressed)"
                + "{"
                + "    WebAppRequest.onBackkeyPressed = function()"
                + "    {"
                + "        WebAppRequest.haveBackkeyPressed(false);"
                + "    }"
                + "}"
                + "WebAppRequest.onBackkeyPressed();";

        webview.evaluateJavascript(cbscript, null);
    }

    @JavascriptInterface
    public void haveBackkeyPressed(boolean pressed)
    {
        //
        // Callback from javascript evalution.
        //

        if (! pressed) doBackkeyPressed();
    }

    @JavascriptInterface
    public void doBackkeyPressed()
    {
        Context activity = Simple.getAppContext();

        if (activity instanceof BackKeyMaster)
        {
            ((BackKeyMaster) activity).onBackKeyExecuteNow();
        }
    }

    //endregion Back key handling

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
                    Log.e(LOGTAG, "loadAsync: FAIL " + webappname + "=" + src);

                    content = (src.endsWith(".json") || src.endsWith(".json.gz"))
                            ? "{}".getBytes()
                            : "".getBytes();
                }

                String contstr = new String(content);

                if ((src.endsWith(".json") || src.endsWith(".json.gz")) &&
                        ! (contstr.startsWith("{") || contstr.startsWith("[")))
                {
                    Log.e(LOGTAG, "loadAsync: NO JSON " + webappname + "=" + src);

                    contstr = "{}";
                }
                else
                {
                    Log.d(LOGTAG, "loadAsync: LOAD " + webappname + "=" + src + "=" + content.length);
                }

                final String cbscript = "WebAppRequest.onLoadAsyncJSON"
                        + "(\"" + src + "\"," + contstr + ");";

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

        if (content == null)
        {
            Log.e(LOGTAG, "loadSync: FAIL " + webappname + "=" + src);

            content = (src.endsWith(".json") || src.endsWith(".json.gz"))
                    ? "{}".getBytes()
                    : "".getBytes();
        }

        String contstr = new String(content);

        if ((src.endsWith(".json") || src.endsWith(".json.gz")) &&
                ! (contstr.startsWith("{") || contstr.startsWith("[")))
        {
            Log.e(LOGTAG, "loadSync: NO JSON " + webappname + "=" + src);

            contstr = "{}";
        }

        return contstr;
    }

    @JavascriptInterface
    public String loadResourceImage(int resourceid)
    {
        try
        {
            InputStream raw = Simple.getAnyContext().getResources().openRawResource(resourceid);

            byte[] ba = new byte[ 32 * 1024 ];
            int xfer = raw.read(ba, 0, ba.length);

            return "data:image/png;base64," + Base64.encodeToString(ba, 0, xfer, 0);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return "";
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
