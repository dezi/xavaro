package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

@SuppressWarnings("unused")
public class ActivityManager
{
    private static final String LOGTAG = ActivityManager.class.getSimpleName();

    //region Static singleton methods.

    private static ActivityManager instance;

    public static ActivityManager getInstance()
    {
        if (instance == null) instance = new ActivityManager();

        return instance;
    }

    public static void subscribe(ActivityMessageCallback callback)
    {
        getInstance().callback = callback;

        JSONObject protocoll = getInstance().getStorage();
        if (protocoll != null) callback.onProtocollMessages(protocoll);
    }

    private ActivityMessageCallback callback;

    private void writeArchive(JSONObject protocoll)
    {
        String lastdate = Simple.todayAsISO(-2);
        String suffix = lastdate.substring(0, 10).replace("-", ".");

        File arch = Simple.getPackageFile("activities." + suffix + ".json");

        Log.d(LOGTAG,"writeArchive: lastdate=" + lastdate + "=" + arch.toString());

        if (arch.exists()) return;

        JSONObject incomingact = Json.getObject(protocoll, "incoming");
        JSONObject outgoingact = Json.getObject(protocoll, "outgoing");

        JSONObject archive = new JSONObject();
        JSONObject incomingarc = new JSONObject();
        JSONObject outgoingarc = new JSONObject();
        Json.put(archive, "incoming", incomingarc);
        Json.put(archive, "outgoing", outgoingarc);

        Iterator<String> keysIterator;
        boolean dirty = false;

        if (incomingact != null)
        {
            keysIterator = incomingact.keys();
            while (keysIterator.hasNext())
            {
                String uuid = keysIterator.next();

                JSONObject activity = Json.getObject(incomingact, uuid);
                String date = Json.getString(activity, "date");
                if ((date == null) || (date.compareTo(lastdate) > 0)) continue;

                Json.put(incomingarc, uuid, activity);
                dirty = true;
            }
        }

        if (outgoingact != null)
        {
            keysIterator = outgoingact.keys();
            while (keysIterator.hasNext())
            {
                String uuid = keysIterator.next();

                JSONObject activity = Json.getObject(outgoingact, uuid);
                String date = Json.getString(activity, "date");
                if ((date == null) || (date.compareTo(lastdate) > 0)) continue;

                Json.put(outgoingarc, uuid, activity);
                dirty = true;
            }
        }

        if (! dirty) return;

        if (Simple.putFileContent(arch, Json.defuck(Json.toPretty(archive))))
        {
            //
            // Commit archive.
            //

            keysIterator = incomingarc.keys();
            while (keysIterator.hasNext())
            {
                Json.remove(incomingact, keysIterator.next());
            }

            keysIterator = outgoingarc.keys();
            while (keysIterator.hasNext())
            {
                Json.remove(outgoingact, keysIterator.next());
            }

            putStorage(protocoll);
        }
    }

    @Nullable
    private JSONObject getStorage()
    {
        File act = Simple.getPackageFile("activities.act.json");
        File bak = Simple.getPackageFile("activities.bak.json");

        //
        // Legacy rename.
        //

        File legacy = Simple.getPackageFile("activities.json");

        if (legacy.exists() && ! legacy.renameTo(act))
        {
            Log.d(LOGTAG, "getStorage: legacy rename failed.");
        }

        JSONObject protocoll = null;

        try
        {
            if (act.exists())
            {
                protocoll = Json.fromString(Simple.getFileContent(act));
            }
            else
            {
                if (bak.exists())
                {
                    protocoll = Json.fromString(Simple.getFileContent(act));
                }
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (protocoll == null) protocoll = new JSONObject();

        if (! protocoll.has("incoming")) Json.put(protocoll, "incoming", new JSONObject());
        if (! protocoll.has("outgoing")) Json.put(protocoll, "outgoing", new JSONObject());

        writeArchive(protocoll);

        return protocoll;
    }

    private void putStorage(JSONObject protocoll)
    {
        if (protocoll == null) return;

        File act = Simple.getPackageFile("activities.act.json");
        File bak = Simple.getPackageFile("activities.bak.json");
        File tmp = Simple.getPackageFile("activities.tmp.json");

        try
        {
            if (Simple.putFileContent(tmp, Json.defuck(Json.toPretty(protocoll))))
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

    public static void recordActivity(int resid)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", Simple.getTrans(resid));

        recordActivity(jmessage);
    }

    public static void recordActivity(String message)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", message);

        recordActivity(jmessage);
    }

    public static void recordActivity(String message, int iconres)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", message);
        Json.put(jmessage, "iconres", iconres);

        recordActivity(jmessage);
    }

    public static void recordActivity(String message, String mediapath)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", message);
        Json.put(jmessage, "mediapath", mediapath);

        recordActivity(jmessage);
    }

    public static void recordActivity(JSONObject jmessage)
    {
        getInstance().onMessage(jmessage, false);
    }

    public static void recordAlert(int resid)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", Simple.getTrans(resid));

        recordAlert(jmessage);
    }

    public static void recordAlert(String message)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", message);

        recordAlert(jmessage);
    }

    public static void recordAlert(String message, int iconres)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", message);
        Json.put(jmessage, "iconres", iconres);

        recordAlert(jmessage);
    }

    public static void recordAlert(String message, String mediapath)
    {
        JSONObject jmessage = new JSONObject();
        Json.put(jmessage, "message", message);
        Json.put(jmessage, "mediapath", mediapath);

        recordAlert(jmessage);
    }

    public static void recordAlert(JSONObject jmessage)
    {
        Json.put(jmessage, "priority", "alertinfo");

        getInstance().onMessage(jmessage, false);
    }

    public static void onIncomingMessage(JSONObject message)
    {
        getInstance().onMessage(message, true);
    }

    private void onMessage(JSONObject message, boolean incoming)
    {
        if (! message.has("date")) Json.put(message, "date", Simple.nowAsISO());
        if (! message.has("uuid")) Json.put(message, "uuid", Simple.getUUID());

        JSONObject protocoll = getStorage();
        if (protocoll == null) return;

        String uuid = Json.getString(message, "uuid");
        if (uuid == null) return;

        JSONObject branch = Json.getObject(protocoll, incoming ? "incoming" : "outgoing");
        if (branch == null) return;

        if (branch.has(uuid))
        {
            JSONObject proto = Json.getObject(branch, uuid);
            Json.copy(proto, message);
        }
        else
        {
            Json.put(branch, uuid, message);
        }

        putStorage(protocoll);

        if (callback != null)
        {
            if (incoming)
            {
                callback.onIncomingMessage(message);
            }
            else
            {
                callback.onOutgoingMessage(message);
            }
        }
    }

    //region Callback interface

    public interface ActivityMessageCallback
    {
        void onProtocollMessages(JSONObject protocoll);
        void onIncomingMessage(JSONObject message);
        void onOutgoingMessage(JSONObject message);
    }

    //endregion Callback interface
}
