package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import java.io.File;

@SuppressWarnings("unused")
public class WebAppStorage
{
    private static final String LOGTAG = WebAppStorage.class.getSimpleName();

    private final String webappname;

    public WebAppStorage(String webappname)
    {
        this.webappname = webappname;
    }

    @JavascriptInterface
    public String getAppStorage(String filename)
    {
        String name = "webapps." + webappname + "." + filename + ".json";
        File file = new File(Simple.getExternalFilesDir(), name);
        String json = Simple.getFileContent(file);

        return (json != null) ? json : "{}";
    }

    @JavascriptInterface
    public void putAppStorage(String filename, String json)
    {
        putAppStorage(filename, json, true);
    }

    @JavascriptInterface
    public void putAppStorage(String filename, String json, boolean pretty)
    {
        if (pretty && (json != null))
        {
            if (json.startsWith("[")) json = Json.toPretty(Json.fromStringArray(json));
            if (json.startsWith("{")) json = Json.toPretty(Json.fromStringObject(json));
        }

        String name = "webapps." + webappname + "." + filename + ".json";
        File file = new File(Simple.getExternalFilesDir(), name);
        Simple.putFileContent(file, json);
    }
}
