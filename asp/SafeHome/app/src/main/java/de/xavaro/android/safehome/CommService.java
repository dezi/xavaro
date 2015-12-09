package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/*
 * Service to communicate with backend servers
 * via UPD packets.
 */

public class CommService extends Service
{
    private static final String LOGTAG = CommService.class.getSimpleName();

    //
    // Binder service.
    //

    private final IBinder binder = new CommBinder();

    //
    // Watch dog background thread.
    //

    private Thread wdThread = null;

    private boolean running = false;

    private static final long INTERVAL = 5000;

    //region Static singleton methods.

    private static CommService myService;

    public static void setInstance(CommService service)
    {
        myService = service;
    }

    @Nullable
    public static CommService getInstance()
    {
        return myService;
    }

    //endregion

    //region Overriden methods.

    @Override
    public void onCreate()
    {
        Log.d(LOGTAG, "onCreate...");

        super.onCreate();

        running = true;
    }

    @Override
    public void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy...");

        running = false;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(LOGTAG, "onStartCommand...");

        if (wdThread == null)
        {

            running = true;

            Log.d(LOGTAG, "onStartCommand: starting watchdog thread.");

            wdThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (running)
                    {
                        StaticUtils.sleep(1000);
                    }
                }
            });

            wdThread.start();
        }

        return Service.START_NOT_STICKY;
    }

    //endregion

    //region Binder methods.

    //
    // Binder stuff allows anyobe to call methods here.
    //

    public class CommBinder extends Binder
    {
        CommService getService()
        {
            return CommService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    //endregion
}

