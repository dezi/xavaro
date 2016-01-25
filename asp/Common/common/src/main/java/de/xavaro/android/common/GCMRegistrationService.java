package de.xavaro.android.common;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class GCMRegistrationService extends IntentService
{
    private static final String LOGTAG = GCMRegistrationService.class.getSimpleName();

    public GCMRegistrationService()
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

            String senderId = "" + Simple.dezify(Simple.getGCMsendeird());

            CommonStatic.gcm_token = instanceID.getToken(senderId,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.d(LOGTAG, "GCM Sender-ID: " + senderId);
            Log.d(LOGTAG, "GCM Registration Token: " + CommonStatic.gcm_token);
        }
        catch (Exception ex)
        {
            Log.d(LOGTAG, "Failed to complete token refresh", ex);
        }
    }
}