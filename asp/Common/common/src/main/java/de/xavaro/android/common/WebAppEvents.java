package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("unused")
public class WebAppEvents
{
    private static final String LOGTAG = WebAppEvents.class.getSimpleName();

    private final String webappname;
    private final String keyprefix;

    public WebAppEvents(String webappname)
    {
        this.webappname = webappname;

        keyprefix = "webapps." + this.webappname;
    }

    private final ArrayList<JSONArray> eventSchedule = new ArrayList<>();

    public void scheduleEventsNotification(JSONArray events)
    {
        Log.d(LOGTAG, "scheduleEventsNotification: " + webappname);

        synchronized (eventSchedule)
        {
            eventSchedule.add(events);
        }
    }

    @JavascriptInterface
    public String getCurrentEvents()
    {
        synchronized (eventSchedule)
        {
            if (eventSchedule.size() > 0)
            {
                return eventSchedule.remove(0).toString();
            }
        }

        return new JSONArray().toString();
    }

    @JavascriptInterface
    public String getComingEvents()
    {
        return EventManager.getComingEvents(keyprefix).toString();
    }

    @JavascriptInterface
    public void putComingEvents(String events)
    {
        EventManager.putComingEvents(keyprefix, Json.fromStringArray(events));
    }

    @JavascriptInterface
    public void updateComingEvent(String event)
    {
        EventManager.updateComingEvent(keyprefix, Json.fromStringObject(event));
    }

    @JavascriptInterface
    public String getPassedEvents()
    {
        return EventManager.getPassedEvents(keyprefix).toString();
    }

    @JavascriptInterface
    public void putPassedEvents(String events)
    {
        EventManager.putPassedEvents(keyprefix, Json.fromStringArray(events));
    }
}
