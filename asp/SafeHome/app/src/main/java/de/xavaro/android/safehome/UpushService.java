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

public class UpushService extends Service
{
    private static final String LOGTAG = UpushService.class.getSimpleName();

    //
    // Binder service.
    //

    private final IBinder binder = new UpushBinder();

    //
    // Watch dog background thread.
    //

    private Thread wdThread = null;

    private boolean running = false;

    private static final long INTERVAL = 10000;

    //region Static singleton methods.

    private static UpushService myService;

    public static void setInstance(UpushService service)
    {
        myService = service;
    }

    @Nullable
    public static UpushService getInstance()
    {
        return myService;
    }

    public static void Log(String tag,String message)
    {
        if (myService == null) return;
        
        Log.d(LOGTAG,tag + ":" + message);
    }

    //endregion

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
                        Log.d(LOGTAG,"nix.");

                        try
                        {
                            Thread.sleep(INTERVAL);

                        } catch (InterruptedException e)
                        {
                            Log.d(LOGTAG, "wdThread: sleep interrupted.");
                        }
                    }
                }
            });

            wdThread.start();
        }

        return Service.START_NOT_STICKY;
    }

    //
    // Binder stuff allows activity to call methods here.
    //

    public class UpushBinder extends Binder
    {
        UpushService getService()
        {
            return UpushService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }
}

