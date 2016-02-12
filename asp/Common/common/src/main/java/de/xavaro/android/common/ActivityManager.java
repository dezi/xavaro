package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONObject;

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

        JSONObject protocoll = getInstance().getProtocoll();
        if (protocoll != null) callback.onProtocollMessages(protocoll);
    }

    private ActivityMessageCallback callback;

    @Nullable
    private JSONObject getProtocoll()
    {
        String filename = Simple.getPackageName() + ".activities.json";
        String content = Simple.readDatadirFile(filename);
        JSONObject protocoll = Json.fromString(content);

        if (! protocoll.has("incoming")) Json.put(protocoll, "incoming", new JSONObject());
        if (! protocoll.has("outgoing")) Json.put(protocoll, "outgoing", new JSONObject());

        return protocoll;
    }

    private void putProtocoll(JSONObject protocoll)
    {
        String filename = Simple.getPackageName() + ".activities.json";
        Simple.writeDatadirFile(filename, Json.toPretty(protocoll));
    }

    public static void onOutgoingMessage(JSONObject message)
    {
        getInstance().onMessage(message, false);
    }

    public static void onIncomingMessage(JSONObject message)
    {
        getInstance().onMessage(message, true);
    }

    private void onMessage(JSONObject message, boolean incoming)
    {
        if (! message.has("date")) Json.put(message, "date", Simple.nowAsISO());
        if (! message.has("uuid")) Json.put(message, "uuid", Simple.getUUID());

        JSONObject protocoll = getProtocoll();
        if (protocoll == null) return;

        String uuid = Json.getString(message, "uuid");
        if (uuid == null) return;

        JSONObject branch = Json.getObject(protocoll, incoming ? "incoming" : "outgoing");
        if (branch == null) return;

        JSONObject proto = null;

        if (branch.has(uuid))
        {
            proto = Json.getObject(branch, uuid);
            Json.copy(proto, message);
        }
        else
        {
            Json.put(branch, uuid, message);
        }

        putProtocoll(protocoll);

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
