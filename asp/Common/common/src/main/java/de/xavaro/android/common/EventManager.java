package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.io.File;

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

        for (int inx = 0; inx < events.length(); inx++)
        {
            JSONObject event = Json.getObject(events, inx);
            if (! Json.has(event, "uuid")) Json.put(event, "uuid", Simple.getUUID());
        }

        JSONObject coming = Json.getObject(eventcache, "coming");
        Json.put(coming, eventgroup, Json.sort(events, "date", false));
        dirty = true;

        Simple.makePost(freeMemory, 10 * 1000);
        nextLoadTime = 0;
    }

    public static void addComingEvent(String eventgroup, JSONObject event)
    {
        Simple.removePost(freeMemory);
        getStorage();

        if (! Json.has(event, "uuid")) Json.put(event, "uuid", Simple.getUUID());

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
        if (dirty) nextLoadTime = 0;
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
                        Json.equals(oldevent, "uuid", event))
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
                    break;
                }
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);
        if (dirty) nextLoadTime = 0;
    }

    public static void removeComingEvent(String eventgroup, JSONObject event)
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
                        Json.equals(oldevent, "uuid", event))
                {
                    //
                    // Remove identified event,
                    //

                    Json.remove(events, inx);
                    dirty = true;
                    break;
                }
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);
        if (dirty) nextLoadTime = 0;
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

    private static void migrateOutdatedArchive()
    {
        String lastdate = Simple.todayAsISO(-2);
        String suffix = lastdate.substring(0, 10).replace("-", ".");

        File arch = Simple.getPackageFile("events." + suffix + ".json");
        if (arch.exists()) return;

        JSONObject passed = Json.getObject(eventcache, "passed");

        JSONObject archive = new JSONObject();

        Iterator<String> typeIterator;

        if (passed != null)
        {
            typeIterator = passed.keys();
            while (typeIterator.hasNext())
            {
                String type = typeIterator.next();

                JSONArray events = Json.getArray(passed, type);
                if (events == null) continue;

                JSONArray eventsarc = new JSONArray();
                Json.put(archive, type, eventsarc);

                for (int inx = 0; inx < events.length(); inx++)
                {
                    JSONObject event = Json.getObject(events, inx);
                    String date = Json.getString(event, "date");
                    if ((date == null) || (date.compareTo(lastdate) > 0)) continue;

                    Json.remove(events, inx--);
                    Json.put(eventsarc, event);
                    dirty = true;
                }
            }
        }

        if (! dirty) return;

        Log.d(LOGTAG,"migrateOutdatedArchive: lastdate=" + lastdate + "=" + arch.toString());

        if (Simple.putFileContent(arch, Json.defuck(Json.toPretty(archive))))
        {
            //
            // Commit archive.
            //

            putStorage();
        }
    }

    private static void getStorage()
    {
        if (eventcache != null) return;

        File act = Simple.getPackageFile("events.act.json");
        File bak = Simple.getPackageFile("events.bak.json");

        //
        // Legacy rename.
        //

        File legacyact = new File(Simple.getFilesDir(), "events.act.json");
        File legacybak = new File(Simple.getFilesDir(), "events.act.json");

        if (legacyact.exists() && ! legacyact.renameTo(act))
        {
            Log.d(LOGTAG, "getStorage: legacy rename failed.");
        }

        if (legacybak.exists() && ! legacybak.renameTo(bak))
        {
            Log.d(LOGTAG, "getStorage: legacy rename failed.");
        }

        try
        {
            if (act.exists())
            {
                eventcache = Json.fromString(Simple.getFileContent(act));
            }
            else
            {
                if (bak.exists())
                {
                    eventcache = Json.fromString(Simple.getFileContent(act));
                }
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (eventcache == null) eventcache = new JSONObject();

        if (! eventcache.has("passed")) Json.put(eventcache, "passed", new JSONObject());
        if (! eventcache.has("coming")) Json.put(eventcache, "coming", new JSONObject());

        migrateOutdatedArchive();
    }

    private static void putStorage()
    {
        if (eventcache == null) return;

        File act = Simple.getPackageFile("events.act.json");
        File bak = Simple.getPackageFile("events.bak.json");
        File tmp = Simple.getPackageFile("events.tmp.json");

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

    private static long nextLoadTime = Simple.nowAsTimeStamp() + 30 * 1000;

    public static void commTick()
    {
        if (nextLoadTime > Simple.nowAsTimeStamp()) return;
        nextLoadTime = Simple.nowAsTimeStamp() + 60 * 1000;

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