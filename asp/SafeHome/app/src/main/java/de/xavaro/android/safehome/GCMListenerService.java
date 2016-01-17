package de.xavaro.android.safehome;

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
    }
}
