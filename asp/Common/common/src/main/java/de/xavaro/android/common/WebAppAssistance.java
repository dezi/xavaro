package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import android.util.Log;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class WebAppAssistance
{
    private static final String LOGTAG = WebAppAssistance.class.getSimpleName();

    @JavascriptInterface
    public boolean hasAssistance()
    {
        return AssistanceMessage.hasAssistance();
    }

    @JavascriptInterface
    public void informAssistance(String text)
    {
        AssistanceMessage.informAssistance(text);
    }
}
