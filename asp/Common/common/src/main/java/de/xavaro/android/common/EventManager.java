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
                ? Json.getArray(eventcache, eventgroup)
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

                Log.d(LOGTAG, "putStorage: ok=" + ok);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private static long nextLoadTime;

    public static void commTick()
    {
        if (nextLoadTime > (Simple.nowAsTimeStamp() / 1000)) return;
        nextLoadTime = (Simple.nowAsTimeStamp() / 1000) / 60 * 60 + 60;

        Simple.removePost(freeMemory);
        getStorage();

        //
        // Check for passed events.
        //

        long millis = (Simple.nowAsTimeStamp() / (1000 * 86400)) * (1000 * 86400);
        String today = Simple.timeStampAsISO(millis);

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

                if (Simple.compareTo(date, today) < 0)
                {
                    Json.put(evgpassed, event);
                    Json.remove(evgcoming, inx--);
                    dirty = true;
                }
            }

            if (dirty)
            {
                Json.put(coming, eventgroupkey, Json.sort(evgcoming, "date", false));
                Json.put(passed, eventgroupkey, Json.sort(evgpassed, "date", false));
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);
    }
}