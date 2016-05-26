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
    public int getImportanceUrgent()
    {
        return NotifyIntent.URGENT;
    }

    @JavascriptInterface
    public int getImportanceAssistance()
    {
        return NotifyIntent.ASSISTANCE;
    }

    @JavascriptInterface
    public int getImportanceWarning()
    {
        return NotifyIntent.WARNING;
    }

    @JavascriptInterface
    public int getImportanceReminder()
    {
        return NotifyIntent.REMINDER;
    }

    @JavascriptInterface
    public int getImportanceInfoOnly()
    {
        return NotifyIntent.INFOONLY;
    }

    @JavascriptInterface
    public void addNotification(String intent)
    {
        JSONObject json = Json.fromStringObject(intent);

        NotifyIntent notification = new NotifyIntent();

        notification.key = Json.getString(json, "key");
        notification.title = Json.getString(json, "title");
        notification.importance  = Json.getInt(json, "importance");
        notification.followText  = Json.getString(json, "followText");;
        notification.declineText = Json.getString(json, "declineText");;
        notification.speakOnce = Json.getBoolean(json, "speakOnce");

        if (Json.has(json, "icon"))
        {
            notification.iconpath = "webapp|" + webappname + "|" + Json.getString(json, "icon");
        }

        Log.d(LOGTAG, "addNotification: " + json.toString());

        NotifyManager.addNotification(notification);
    }

    @JavascriptInterface
    public void removeNotification(String intent)
    {
        JSONObject json = Json.fromStringObject(intent);

        NotifyIntent notification = new NotifyIntent();

        notification.key = Json.getString(json, "key");

        Log.d(LOGTAG, "removeNotification: " + json.toString());

        NotifyManager.removeNotification(notification);
    }
}
