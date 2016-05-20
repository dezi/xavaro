package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class WebAppNotify
{
    private static final String LOGTAG = WebAppNotify.class.getSimpleName();

    private final String webappname;

    public WebAppNotify(String webappname)
    {
        this.webappname = webappname;
    }

    @JavascriptInterface
    public void addNotification(String intent)
    {
        JSONObject json = Json.fromStringObject(intent);

        NotifyIntent notification = new NotifyIntent();

        notification.key = Json.getString(json, "key");
        notification.title = Json.getString(json, "title");

        if (Json.has(json, "icon"))
        {
            notification.iconres = "webapp|" + webappname + "|" + Json.getString(json, "icon");
        }

        Log.d(LOGTAG, "addNotification: " + json.toString());

        NotifyManager.addNotification(notification);
    }
}
