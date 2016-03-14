package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

@SuppressWarnings("unused")
public class WebAppSpeak
{
    private static final String LOGTAG = WebAppSpeak.class.getSimpleName();

    @JavascriptInterface
    public void speak(String text)
    {
        Speak.speak(text);
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
}
