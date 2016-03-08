package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class WebAppAssistance
{
    private static final String LOGTAG = WebAppAssistance.class.getSimpleName();

    @JavascriptInterface
    public void informAssistance(String text)
    {
        if (! Simple.getSharedPrefBoolean("alertgroup.enable")) return;
        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        JSONObject assistMessage = new JSONObject();

        Json.put(assistMessage, "uuid", Simple.getUUID());
        Json.put(assistMessage, "message", text);
        Json.put(assistMessage, "priority", "alertinfo");

        ChatManager.getInstance().sendOutgoingMessage(groupIdentity, assistMessage);

        Log.d(LOGTAG, "informAssistance: send alertinfo:" + text);
    }
}
