package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

@SuppressWarnings("unused")
public class WebAppSpeak implements Speak.SpeakDoneCallback
{
    private static final String LOGTAG = WebAppSpeak.class.getSimpleName();

    private final WebView webview;

    public WebAppSpeak(WebView webview)
    {
        this.webview = webview;
    }

    @JavascriptInterface
    public void speak(String text)
    {
        Speak.speak(text);
    }

    @JavascriptInterface
    public void speak(String text, boolean callback)
    {
        if (callback)
        {
            Speak.speak(text, this);
        }
        else
        {
            Speak.speak(text);
        }
    }

    @JavascriptInterface
    public void unmute()
    {
        Simple.unmuteSpeech();
    }

    @JavascriptInterface
    public void vibrate()
    {
        Simple.makeVibration();
    }

    @Override
    public void OnSpeakDone(String text)
    {
        final String cbscript = ""
                + "if (! WebAppSpeak.onSpeakDone)"
                + "{"
                + "    WebAppSpeak.onSpeakDone = function()"
                + "    {"
                + "    }"
                + "}"
                + "WebAppSpeak.onSpeakDone(\"" + text + "\");";

        Simple.makePost(new Runnable()
        {
            @Override
            public void run()
            {
                webview.evaluateJavascript(cbscript, null);
            }
        });
    }
}
