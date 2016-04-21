package de.xavaro.android.safehome;

import java.util.ArrayList;

import android.content.Intent;
import android.app.Service;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.widget.Toast;

import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;

//
// Service to monitor foreign and system apps activity
// and eventually close undesired activities.
//

public class KioskService extends Service
{
    private static final String LOGTAG = KioskService.class.getSimpleName();

    private final ArrayList<String> whitelistApps = new ArrayList<>();
    private final ArrayList<String> blacklistApps = new ArrayList<>();

    private Thread wdThread;
    private String recentProc;
    private boolean running;

    private String alertMessage;
    private Toast alertToast;

    private final Handler handler = new Handler();

    private static final long INTERVAL = 100;

    @Override
    public void onCreate()
    {
        Log.d(LOGTAG, "onCreate...");

        super.onCreate();

        running = true;

        recentProc = getPackageName();

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
            running = true;

            wdThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    long sequence = 0;

                    while (running)
                    {
                        if (((sequence % 10) == 0) || ! CommonStatic.focused)
                        {
                            handleKioskMode();
                        }

                        Simple.sleep(INTERVAL);

                        sequence++;
                    }
                }
            });

            wdThread.start();
        }

        return Service.START_STICKY; // Service.START_STICKY <=> Service.START_NOT_STICKY;
    }

    private String getAppType(String processName)
    {
        String mode = "xx";

        if (whitelistApps.contains(processName)) mode = "wl";
        if (blacklistApps.contains(processName)) mode = "bl";
        if (ProcessManager.isOneShotProcess(processName)) mode = "os";
        if (ProcessManager.isSystemProcess(processName)) mode = "ds";
        if (processName.equals(getPackageName())) mode = "me";

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
        ProcessManager.ProcessInfo pi = ProcessManager.getRunning();
        if (pi == null) return;

        String proc = pi.name;
        String mode = getAppType(proc);

        if (mode.equals("ds")) return;

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

        if ((alertMessage == null) || !alertMessage.equals(currentMessage))
        {
            alertMessage = currentMessage;

            if (showit) handler.post(handleAlert);
        }

        if (proc.equals("com.android.systemui.recentsactivity"))
        {
            String what = Simple.getSharedPrefString("admin.recent.button");
            if ((what != null) && what.equals("android")) blockit = false;
        }

        if (blockit && DefaultApps.isDefaultHome())
        {
            restoreRecentProc();
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

            Intent intent = new Intent(this, HomeActivity.class);
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
