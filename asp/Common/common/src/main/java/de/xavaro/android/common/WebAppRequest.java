package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import android.content.Context;
import android.webkit.WebView;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("unused")
public class WebAppRequest
{
    private static final String LOGTAG = WebAppRequest.class.getSimpleName();

    private final String webappname;
    private final WebView webview;
    private final WebAppLoader webapploader;

    private VoiceIntentRequester requester;

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
        // Remember voice requesting instance for
        // a possible callback from client web app.
        //

        requester = intent.getRequester();

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

    private ArrayList<String> backKeyRequests = new ArrayList<>();

    public boolean onBackKeyWanted()
    {
        if (backKeyRequests.size() == 0) return false;

        String method = backKeyRequests.get(backKeyRequests.size() - 1);
        String cbscript = method + "();";

        webview.evaluateJavascript(cbscript, null);

        return true;
    }

    @JavascriptInterface
    public void requestBackKey(String method)
    {
        if (! backKeyRequests.contains(method)) backKeyRequests.add(method);
    }

    @JavascriptInterface
    public void releaseBackKey(String method)
    {
        if (backKeyRequests.contains(method)) backKeyRequests.remove(method);
    }

    @JavascriptInterface
    public void requestKeyboard()
    {
        Log.d(LOGTAG, "requestKeyboard:" + webview);

        webview.requestFocus();
    }

    @JavascriptInterface
    public void requestVoiceIntent()
    {
        //
        // Callback from javascript evalution.
        //

        if (requester != null)
        {
            requester.onRequestVoiceIntent();
            requester = null;
        }
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
        Context activity = Simple.getActContext();

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
            int size = raw.available();

            if (size < (64 * 1024))
            {
                byte[] ba = new byte[ size ];
                int xfer = raw.read(ba, 0, size);

                return "data:image/png;base64," + Base64.encodeToString(ba, 0, xfer, 0);
            }
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
