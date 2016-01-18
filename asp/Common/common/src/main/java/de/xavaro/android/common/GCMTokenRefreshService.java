package de.xavaro.android.common;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class GCMTokenRefreshService extends InstanceIDListenerService
{
    private static final String LOGTAG = GCMTokenRefreshService.class.getSimpleName();

    @Override
    public void onTokenRefresh()
    {
        Log.d(LOGTAG,"onTokenRefresh");

        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        startService(intent);
    }
}
