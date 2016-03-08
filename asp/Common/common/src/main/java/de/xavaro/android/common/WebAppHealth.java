package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import org.json.JSONObject;

import java.util.Locale;

@SuppressWarnings("unused")
public class WebAppHealth
{
    private static final String LOGTAG = WebAppHealth.class.getSimpleName();

    @JavascriptInterface
    public void addRecord(String datatype, String record)
    {
        HealthData.addRecord(datatype, Json.fromStringObject(record));
    }
}
