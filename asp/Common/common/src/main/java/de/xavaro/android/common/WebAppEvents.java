package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONObject;

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
