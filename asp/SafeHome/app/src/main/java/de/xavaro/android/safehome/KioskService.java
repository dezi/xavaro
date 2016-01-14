package de.xavaro.android.safehome;

import java.util.List;
import java.util.ArrayList;

import android.content.Intent;
import android.content.Context;
import android.app.ActivityManager;
import android.app.Service;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.widget.Toast;

import de.xavaro.android.common.CommonStatic;

/*
 * Service to monitor foreign and system apps activity
 * and eventually close undesired activities.
 */

public class KioskService extends Service
{
    private static final String LOGTAG = KioskService.class.getSimpleName();

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
    // Binder service.
    //

    private final IBinder binder = new KioskBinder();

    private String alertMessage;
    private Toast alertToast;

    private final Handler handler = new Handler();

    private String recentProc = null;
    private boolean running = false;
    private boolean focused = false;

    private static final long INTERVAL = 100;

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
        whitelistApps.add("org.wikipedia");
        whitelistApps.add("com.skype.raider");

        blacklistApps.add("com.android.systemui.recentsactivity");
        blacklistApps.add("com.samsung.android.email.composer");
        blacklistApps.add("com.samsung.android.email.ui");
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
                        if (! focused) handleKioskMode();

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

        return Service.START_NOT_STICKY; // Service.START_STICKY <=> Service.START_NOT_STICKY;
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

        if (CommonStatic.oneshotApps.contains(processName))
        {
            mode = "os";
        }

        if (processName.equals(getPackageName()))
        {
            mode = "me";
        }

        return mode;
    }

    private final Runnable handleAlert = new Runnable()
    {
        public void run()
        {
            if (alertToast == null)
            {
                WindowManager.LayoutParams alp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);

                alertToast = Toast.makeText(getBaseContext(), alertMessage, Toast.LENGTH_LONG);

                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                wm.addView(alertToast.getView(), alp);
            }
            else
            {
                alertToast.setText(alertMessage);
            }

            alertToast.show();
        }
    };

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

            //Log.d(LOGTAG, "APP:" + mode + "=" + proc + "=" + pi.importance);

            String currentMessage = mode + " => " + proc;

            boolean showit = true;
            boolean blockit = false;

            if (mode.equals("xx") || mode.equals("bl"))
            {
                currentMessage = "Blocking" + " " + proc;
                blockit = true;
            }

            if (mode.equals("me") || mode.equals("wl") || mode.equals("os"))
            {
                if (recentProc.equals(proc)) showit = false;

                recentProc = proc;
            }

            if ((alertMessage == null) || ! alertMessage.equals(currentMessage))
            {
                alertMessage = currentMessage;

                if (showit) handler.post(handleAlert);
            }

            if (proc.equals("com.android.systemui.recentsactivity"))
            {
                String what = DitUndDat.SharedPrefs.sharedPrefs.getString("admin.recent.button","");

                if (what.equals("android")) blockit = false;
            }

            if (blockit && DitUndDat.DefaultApps.isDefaultHome(this))
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

    //
    // Set if main activity has focus.
    //

    public void setFocused(String activity,boolean hasFocus)
    {
        Log.d(LOGTAG, "setFocused: " + activity + "=" + hasFocus);

        recentProc = getPackageName();
        focused = hasFocus;
    }

    //
    // Binder stuff allows activity to call methods here.
    //

    public class KioskBinder extends Binder
    {
        KioskService getService()
        {
            return KioskService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }
}