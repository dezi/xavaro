package de.xavaro.android.common;

import android.content.Context;
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

    private void startActivity(JSONObject config)
    {
        Context activity = Simple.getActContext();

        if (activity instanceof AppWorkerHandler)
        {
            ((AppWorkerHandler) activity).onStartWorker(config);
        }
    }

    @JavascriptInterface
    public void addNotification(String intent)
    {
        JSONObject json = Json.fromStringObject(intent);

        NotifyIntent notification = new NotifyIntent();

        notification.key = Json.getString(json, "key");
        notification.title = Json.getString(json, "title");
        notification.importance  = Json.getInt(json, "importance");
        notification.speakOnce = Json.getBoolean(json, "speakOnce");

        notification.followText  = Json.getString(json, "followText");;

        if (notification.followText != null)
        {
            final String javacall = Json.getString(json, "followJavaCall");

            notification.followRunner = new Runnable()
            {
                @Override
                public void run()
                {
                    JSONObject config = new JSONObject();

                    Json.put(config, "type", "webapp");
                    Json.put(config, "subtype", webappname);
                    Json.put(config, "javacall", javacall);

                    startActivity(config);
                }
            };
        }

        notification.declineText = Json.getString(json, "declineText");;

        if (notification.declineText != null)
        {
            final String javacall = Json.getString(json, "declineJavaCall");

            notification.declineRunner = new Runnable()
            {
                @Override
                public void run()
                {
                    JSONObject config = new JSONObject();

                    Json.put(config, "type", "webapp");
                    Json.put(config, "subtype", webappname);
                    Json.put(config, "javacall", javacall);

                    startActivity(config);
                }
            };
        }

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
