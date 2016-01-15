package de.xavaro.android.safehome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootLoader extends BroadcastReceiver
{
    private static final String LOGTAG = BootLoader.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(LOGTAG,"onReceive:" + intent);
    }
}
