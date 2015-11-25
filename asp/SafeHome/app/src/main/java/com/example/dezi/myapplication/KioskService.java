package com.example.dezi.myapplication;

import java.util.List;
import java.util.ArrayList;

import android.content.Intent;
import android.content.Context;
import android.app.ActivityManager;
import android.app.Service;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.view.Gravity;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.widget.TextView;

/*
 * Service to monitor foreign and system apps activity
 * and eventually close undesired activities.
 */

public class KioskService extends Service
{
    //
    // List of names of white-listed packages.
    //

    private final ArrayList<String> whitelistApps = new ArrayList<>();

    //
    // List of names of black-listed packages.
    //

    private final ArrayList<String> blacklistApps = new ArrayList<>();

    //
    // List of names of system default packages.
    //

    private final ArrayList<String> defsystemApps = new ArrayList<>();

    //
    // Watch dog background thread.
    //

    private Thread wdThread = null;

    //
    // Log tag.
    //

    private static final String LOGTAG = KioskService.class.getSimpleName();

    //
    // Window manager.
    //

    private WindowManager windowManager;

    //
    // Window manager.
    //

    private TextView alertText;
    private String alertMessage;

    private final Handler handler = new Handler();

    private String recentProc = null;
    private boolean running = false;

    private static final long INTERVAL = 500;

    @Override
    public void onCreate()
    {
        Log.d(LOGTAG, "onCreate...");

        super.onCreate();

        running = true;

        recentProc = getPackageName();

        defsystemApps.add("system:ui");
        defsystemApps.add("android:ui");

        whitelistApps.add("com.whatsapp");

        blacklistApps.add("com.android.systemui.recentsactivity");
        blacklistApps.add("com.samsung.android.email.composer");
        blacklistApps.add("com.samsung.android.email.ui");

        alertText = new TextView(this);
        alertText.setBackgroundColor(Color.argb(150, 255, 155, 155));
        alertText.setTextSize(24f);
        alertText.setPadding(25, 25, 25, 25);
        alertText.setGravity(Gravity.CENTER);

        WindowManager.LayoutParams alertLayout = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        alertLayout.gravity = Gravity.TOP;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        windowManager.addView(alertText, alertLayout);
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
            registerDefsystemApps();

            running = true;

            Log.d(LOGTAG, "onStartCommand: starting watchdog thread.");

            wdThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (running)
                    {
                        handleKioskMode();

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

        return Service.START_STICKY; // Service.START_NOT_STICKY;
    }

    //
    // Register all currently running foreground
    // apps into system default app list.
    //

    private void registerDefsystemApps()
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> piList = am.getRunningAppProcesses();

        for (int inx = 0; inx < piList.size(); inx++)
        {
            ActivityManager.RunningAppProcessInfo pi = piList.get(inx);

            if (pi.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                //
                // Only process foreground apps.
                //

                continue;
            }

            Log.d(LOGTAG, "registerDefsystemApps=" + pi.processName + "=" + pi.importance);

            defsystemApps.add(pi.processName);
        }
    }

    private String getAppType(String processName)
    {
        String mode = "xx";

        if (defsystemApps.contains(processName))
        {
            mode = "ds";
        }

        if (whitelistApps.contains(processName))
        {
            mode = "wl";
        }

        if (blacklistApps.contains(processName))
        {
            mode = "bl";
        }

        if (processName.equals(getPackageName()))
        {
            mode = "me";
        }

        return mode;
    }

    private void handleKioskMode()
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> piList = am.getRunningAppProcesses();

        for (int inx = 0; inx < piList.size(); inx++)
        {
            ActivityManager.RunningAppProcessInfo pi = piList.get(inx);

            if (pi.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                continue;
            }

            if (inx > 0) continue;

            String proc = pi.processName;
            String mode = getAppType(proc);

            Log.d(LOGTAG, "APP:" + mode + "=" + proc + "=" + pi.importance);

            alertMessage = mode + " => " + proc;

            if (mode.equals("xx") || mode.equals("bl"))
            {
                alertMessage = "Blocking" + " " + proc;
            }

            if (mode.equals("me") || mode.equals("wl"))
            {
                recentProc = proc;
            }

            handler.post(new Runnable()
            {
                public void run()
                {
                    alertText.setText(alertMessage);
                }
            });

            if (mode.equals("xx") || mode.equals("bl"))
            {
                restoreRecentProc();
            }
        }
    }

    //
    // Restore the last valid process to top.
    //

    private void restoreRecentProc()
    {
        try
        {
            Log.d(LOGTAG, "restoreRecentProc: " + recentProc);

            Intent intent = getPackageManager().getLaunchIntentForPackage(recentProc);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.setPackage(recentProc);
            startActivity(intent);
        }
        catch (Throwable ex)
        {
            Log.d(LOGTAG, "restoreRecentProc: Retry with HOME.");

            Intent intent = new Intent(this, FullscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}