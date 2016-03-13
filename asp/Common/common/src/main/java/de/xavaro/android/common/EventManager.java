package de.xavaro.android.common;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.acl.LastOwnerException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventManager
{
    private static final String LOGTAG = EventManager.class.getSimpleName();

    private static JSONObject eventcache;
    private static boolean dirty;

    public static JSONArray getComingEvents(String eventgroup)
    {
        Simple.removePost(freeMemory);
        getStorage();

        Simple.makePost(freeMemory, 10 * 1000);

        JSONObject coming = Json.getObject(eventcache, "coming");

        return ((coming != null) && coming.has(eventgroup))
                ? Json.getArray(coming, eventgroup)
                : new JSONArray();
    }

    public static void putComingEvents(String eventgroup, JSONArray events)
    {
        Simple.removePost(freeMemory);
        getStorage();

        JSONObject coming = Json.getObject(eventcache, "coming");
        Json.put(coming, eventgroup, Json.sort(events, "date", false));
        dirty = true;

        Simple.makePost(freeMemory, 10 * 1000);
    }

    public static void addComingEvent(String eventgroup, JSONObject event)
    {
        Simple.removePost(freeMemory);
        getStorage();

        JSONObject coming = Json.getObject(eventcache, "coming");

        if (coming != null)
        {
            if (!coming.has(eventgroup)) Json.put(coming, eventgroup, new JSONArray());
            JSONArray events = Json.getArray(coming, eventgroup);

            Json.put(events, event);

            Json.put(coming, eventgroup, Json.sort(events, "date", false));
            dirty = true;
        }

        Simple.makePost(freeMemory, 10 * 1000);
    }

    public static void updateComingEvent(String eventgroup, JSONObject event)
    {
        Simple.removePost(freeMemory);
        getStorage();

        JSONObject coming = Json.getObject(eventcache, "coming");
        JSONArray events = Json.getArray(coming, eventgroup);

        if (events != null)
        {
            for (int inx = 0; inx < events.length(); inx++)
            {
                JSONObject oldevent = Json.getObject(events, inx);

                if (Json.equals(oldevent, "date", event) &&
                        Json.equals(oldevent, "medication", event))
                {
                    //
                    // Integrate all values from event into old event,
                    //

                    Iterator<String> eventkeys = event.keys();

                    while (eventkeys.hasNext())
                    {
                        String eventkey = eventkeys.next();
                        Json.copy(oldevent, eventkey, event);
                    }

                    dirty = true;
                }
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);
    }

    public static JSONArray getPassedEvents(String eventgroup)
    {
        Simple.removePost(freeMemory);
        getStorage();

        Simple.makePost(freeMemory, 10 * 1000);

        JSONObject passed = Json.getObject(eventcache, "passed");

        return ((passed != null) && passed.has(eventgroup))
                ? Json.getArray(passed, eventgroup)
                : new JSONArray();
    }

    public static void putPassedEvents(String eventgroup, JSONArray events)
    {
        Simple.removePost(freeMemory);
        getStorage();

        JSONObject passed = Json.getObject(eventcache, "passed");
        Json.put(passed, eventgroup, Json.sort(events, "date", false));
        dirty = true;

        Simple.makePost(freeMemory, 10 * 1000);
    }

    private static final Runnable freeMemory = new Runnable()
    {
        @Override
        public void run()
        {
            if (eventcache != null)
            {
                if (dirty) putStorage();

                eventcache = null;
            }
        }
    };

    private static void getStorage()
    {
        if (eventcache != null) return;

        File file = new File(Simple.getFilesDir(), "events.act.json");
        if (!file.exists()) file = new File(Simple.getFilesDir(), "events.bak.json");

        try
        {
            if (!file.exists())
            {
                eventcache = new JSONObject();
            }
            else
            {
                String json = Simple.getFileContent(file);
                eventcache = (json != null) ? new JSONObject(json) : new JSONObject();
            }

            if (!eventcache.has("passed")) Json.put(eventcache, "passed", new JSONObject());
            if (!eventcache.has("coming")) Json.put(eventcache, "coming", new JSONObject());

        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private static void putStorage()
    {
        if (eventcache == null) return;

        File tmp = new File(Simple.getFilesDir(), "events.tmp.json");
        File bak = new File(Simple.getFilesDir(), "events.bak.json");
        File act = new File(Simple.getFilesDir(), "events.act.json");

        try
        {
            if (Simple.putFileContent(tmp, Json.defuck(Json.toPretty(eventcache))))
            {
                boolean ok = true;

                if (bak.exists()) ok = bak.delete();
                if (act.exists()) ok &= act.renameTo(bak);
                if (tmp.exists()) ok &= tmp.renameTo(act);

                dirty = false;

                Log.d(LOGTAG, "putStorage: ok=" + ok);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private static void notifyEvents(String eventgroupkey, JSONArray events)
    {
        Log.d(LOGTAG, "notifyEvents:" + eventgroupkey + "=" + events.length());

        if (eventgroupkey.startsWith("webapps."))
        {
            String webappname = eventgroupkey.substring(8);
            WebApp.handleEvent(webappname, Json.clone(events));
        }

        if (eventgroupkey.equals("media.recorder"))
        {
            MediaRecorder.handleEvent(Json.clone(events));
        }
    }

    private static long nextLoadTime = 30 + Simple.nowAsTimeStamp() / 1000;

    public static void commTick()
    {
        if (nextLoadTime > (Simple.nowAsTimeStamp() / 1000)) return;
        nextLoadTime = (Simple.nowAsTimeStamp() / 1000) / 60 * 60 + 60;

        Simple.removePost(freeMemory);
        getStorage();

        //
        // Check for passed events.
        //

        long now = Simple.nowAsTimeStamp();
        long today = Simple.todayAsTimeStamp();

        JSONObject passed = Json.getObject(eventcache, "passed");
        JSONObject coming = Json.getObject(eventcache, "coming");

        if (passed == null) Json.put(eventcache, "passed", passed = new JSONObject());
        if (coming == null) Json.put(eventcache, "coming", coming = new JSONObject());

        Iterator<String> eventgroups = coming.keys();

        while (eventgroups.hasNext())
        {
            String eventgroupkey = eventgroups.next();

            JSONArray evgcoming = Json.getArray(coming, eventgroupkey);
            JSONArray evgpassed = Json.getArray(passed, eventgroupkey);
            JSONArray evgfiring = new JSONArray();

            if (evgcoming == null) Json.put(coming, eventgroupkey, evgcoming = new JSONArray());
            if (evgpassed == null) Json.put(passed, eventgroupkey, evgpassed = new JSONArray());

            for (int inx = 0; inx < evgcoming.length(); inx++)
            {
                JSONObject event = Json.getObject(evgcoming, inx);
                if (event == null) continue;

                String date = Json.getString(event, "date");

                if (date == null)
                {
                    Json.remove(evgcoming, inx);
                    dirty = true;
                    continue;
                }

                long timestamp = Simple.getTimeStamp(date);

                if (timestamp < today)
                {
                    Json.put(evgpassed, event);
                    Json.remove(evgcoming, inx--);
                    dirty = true;
                    continue;
                }

                if ((timestamp < now) && ! event.has("completed"))
                {
                    Json.put(evgfiring, event);
                    dirty = true;
                }
            }

            if (dirty)
            {
                Json.put(coming, eventgroupkey, Json.sort(evgcoming, "date", false));
                Json.put(passed, eventgroupkey, Json.sort(evgpassed, "date", false));
            }

            if (evgfiring.length() > 0) notifyEvents(eventgroupkey, evgfiring);
        }

        Simple.makePost(freeMemory, 10 * 1000);
    }
}