package de.xavaro.android.common;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class GCMListenerService extends GcmListenerService
{
    private static final String LOGTAG = GCMListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        String message = data.getString("message");

        Log.d(LOGTAG, "From: " + from);
        Log.d(LOGTAG, "Message: " + message);

        if (from.startsWith("/topics/"))
        {
            // message received from some topic.
        }
        else
        {
            // normal downstream message.
        }
    }
}
