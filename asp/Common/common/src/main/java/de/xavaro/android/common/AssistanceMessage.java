package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONObject;

public class AssistanceMessage
{
    private static final String LOGTAG = AssistanceMessage.class.getSimpleName();

    public static boolean hasAssistance()
    {
        if (! Simple.getSharedPrefBoolean("alertgroup.enable")) return false;
        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        return (groupIdentity != null);
    }

    public static void alertAssistance(String text)
    {
        if (! Simple.getSharedPrefBoolean("alertgroup.enable")) return;
        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        JSONObject assistMessage = new JSONObject();

        Json.put(assistMessage, "uuid", Simple.getUUID());
        Json.put(assistMessage, "message", text);
        Json.put(assistMessage, "priority", "alertinfo");

        ChatManager.getInstance().sendOutgoingMessage(groupIdentity, assistMessage);

        Log.d(LOGTAG, "alertAssistance: send alert:" + text);
    }

    public static void informAssistance(String text)
    {
        if (! Simple.getSharedPrefBoolean("alertgroup.enable")) return;
        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        JSONObject assistMessage = new JSONObject();

        Json.put(assistMessage, "uuid", Simple.getUUID());
        Json.put(assistMessage, "message", text);
        Json.put(assistMessage, "priority", "infoonly");

        ChatManager.getInstance().sendOutgoingMessage(groupIdentity, assistMessage);

        Log.d(LOGTAG, "informAssistance: send info:" + text);
    }

    public static void sendInfoMessage(JSONObject message)
    {
        if (! Simple.getSharedPrefBoolean("alertgroup.enable")) return;
        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        JSONObject infoMessage = Json.clone(message);

        Json.put(infoMessage, "idremote", groupIdentity);

        if (! infoMessage.has("date")) Json.put(infoMessage, "date", Simple.nowAsISO());

        CommService.sendEncrypted(infoMessage, true);
    }
}
