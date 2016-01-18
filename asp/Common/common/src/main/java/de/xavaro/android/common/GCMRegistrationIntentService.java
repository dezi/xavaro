package de.xavaro.android.common;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class GCMRegistrationIntentService extends IntentService
{
    private static final String LOGTAG = GCMRegistrationIntentService.class.getSimpleName();

    public GCMRegistrationIntentService()
    {
        super(LOGTAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(LOGTAG, "onHandleIntent");

        try
        {
            InstanceID instanceID = InstanceID.getInstance(this);

            String token = instanceID.getToken(CommonStatic.gcm_defaultSenderId,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(LOGTAG, "GCM Sender-ID: " + CommonStatic.gcm_defaultSenderId);
            Log.i(LOGTAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);
        }
        catch (Exception ex)
        {
            Log.d(LOGTAG, "Failed to complete token refresh", ex);
        }
    }

    private void sendRegistrationToServer(String token)
    {
    }
}