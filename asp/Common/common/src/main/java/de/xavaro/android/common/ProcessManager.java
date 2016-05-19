package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessManager
{
    private static final String LOGTAG = ProcessManager.class.getSimpleName();

    private static final int MODE_UNSET = 0;
    private static final int MODE_ANDROID = 1;
    private static final int MODE_LINUXPROC = 2;

    private static int sequence = 0;
    private static int procmode = 0;

    private static final Map<Integer, ProcessInfo> processes = new HashMap<>();
    private static final Map<String, Integer> systemprocs = new HashMap<>();
    private static final ArrayList<String> oneshotApps = new ArrayList<>();

    private static ProcessInfo running;

    public static class ProcessInfo
    {
        public static final int WATCHMODE_NORMAL = 0;
        public static final int WATCHMODE_IMPORTANT = 1;
        public static final int WATCHMODE_JUNK = 2;

        public static final int IMPORTANCE_FOREGROUND = 100;
        public static final int IMPORTANCE_BACKGROUND = 200;

        public String name;
        public int importance;

        public int pid;
        public int uid;
        public int gid;

        public int iniThreads;
        public int actThreads;

        public int voSwitch;
        public int nvSwitch;

        public int sequence;
        public int watchmode;
    }

    public static void clearOneShotApps()
    {
        oneshotApps.clear();
    }

    public static void launchApp(String packagename)
    {
        if (packagename != null)
        {
            oneshotApps.add(packagename);

            try
            {
                Context context = Simple.getAppContext();
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packagename);
                context.startActivity(launchIntent);
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }
    }

    public static boolean launchIntent(Intent intent)
    {
        try
        {
            Context context = Simple.getActContext();
            ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

            if (res.activityInfo != null)
            {
                oneshotApps.add(res.activityInfo.packageName);

                context.startActivity(intent);

                return true;
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    public static boolean isOneShotProcess(String name)
    {
        return oneshotApps.contains(name);
    }

    public static boolean isSystemProcess(String name)
    {
        return systemprocs.containsKey(name);
    }

    @Nullable
    public static ProcessInfo getRunning()
    {
        getProcesses();

        return running;
    }

    public static Map<Integer, ProcessInfo> getProcesses()
    {
        if (! CommonStatic.initialized) return processes;

        if (procmode == MODE_UNSET) getProcMode();
        if (procmode == MODE_ANDROID) getAndroidProcesses();
        if (procmode == MODE_LINUXPROC) getLinuxProcProcesses();

        return processes;
    }

    private static void getProcMode()
    {
        Context context = Simple.getAppContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> piList = am.getRunningAppProcesses();

        procmode = (piList.size() > 1) ? MODE_ANDROID : MODE_LINUXPROC;
    }

    private static void getAndroidProcesses()
    {
        if (sequence == 0)
        {
            //
            // Register ubiquitious processes not present yet.
            //

            systemprocs.put("system:ui", -1);
            systemprocs.put("android:ui", -1);
        }

        sequence++;
        running = null;

        Context context = Simple.getAppContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> piList = am.getRunningAppProcesses();

        for (int inx = 0; inx < piList.size(); inx++)
        {
            ActivityManager.RunningAppProcessInfo andpi = piList.get(inx);

            if (andpi.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                continue;
            }

            int pid = andpi.pid;

            ProcessInfo pi;

            if (processes.containsKey(pid))
            {
                pi = processes.get(pid);
                pi.sequence = sequence;
            }
            else
            {
                pi = new ProcessInfo();
                processes.put(pid, pi);

                pi.pid = pid;
                pi.sequence = sequence;

                pi.uid = andpi.uid;
                pi.name = andpi.processName;
                pi.watchmode = ProcessInfo.WATCHMODE_NORMAL;

                if (pi.name.contains("recent")) pi.watchmode = ProcessInfo.WATCHMODE_IMPORTANT;

                if (! CommonStatic.lostfocus)
                {
                    //
                    // As long as we never lost the focus, we register anything
                    // as an background runnable process.
                    //

                    systemprocs.put(pi.name, pi.pid);
                }
            }

            pi.importance = andpi.importance;

            if (inx == 0) running = pi;
        }

        //
        // Check and remove obsoleted processes.
        //

        ArrayList<Integer> remove = new ArrayList<>();

        for (Map.Entry<Integer, ProcessInfo> pid : processes.entrySet())
        {
            if (pid.getValue().sequence != sequence) remove.add(pid.getKey());
        }

        for (int pid : remove) processes.remove(pid);
    }

    private static void getLinuxProcProcesses()
    {
        sequence++;
        running = null;

        File[] files = new File("/proc").listFiles();

        for (File file : files)
        {
            String numeric = Simple.getFirstMatch("([0-9]*)", file.getName());

            if ((numeric == null) || (! numeric.equals(file.getName())) || ! file.isDirectory())
            {
                continue;
            }

            ProcessInfo pi;
            String status;
            int pid;

            try
            {
                pid = Integer.parseInt(file.getName());
            }
            catch (NumberFormatException ex)
            {
                continue;
            }

            if (processes.containsKey(pid))
            {
                pi = processes.get(pid);
                pi.sequence = sequence;

                if ((! CommonStatic.focused) && (pi.watchmode != ProcessInfo.WATCHMODE_IMPORTANT))
                {
                    //
                    // In background we only watch important processes.
                    //

                    continue;
                }
            }
            else
            {
                pi = new ProcessInfo();
                processes.put(pid,pi);

                pi.pid = pid;
                pi.sequence = sequence;
                pi.watchmode = ProcessInfo.WATCHMODE_NORMAL;

                try
                {
                    pi.name = StaticUtils.readFile(String.format("/proc/%d/cmdline", pid)).trim();
                }
                catch (IOException ex)
                {
                    continue;
                }

                if (pi.name.contains("recent")) pi.watchmode = ProcessInfo.WATCHMODE_IMPORTANT;

                if (! CommonStatic.lostfocus)
                {
                    //
                    // As long as we never lost the focus, we register anything
                    // as an background runnable process.
                    //

                    systemprocs.put(pi.name, pi.pid);
                }

                if (pi.name.equals("") || pi.name.startsWith("/")
                        || (! pi.name.contains(".")) || ! StaticUtils.isPlainASCII(pi.name))
                {
                    pi.watchmode = ProcessInfo.WATCHMODE_JUNK;

                    continue;
                }
            }

            if ((pi.iniThreads > 0) && (pi.watchmode == ProcessInfo.WATCHMODE_JUNK))
            {
                continue;
            }

            try
            {
                status = StaticUtils.readFile(String.format("/proc/%d/status", pid)).trim();
            }
            catch (IOException ex)
            {
                continue;
            }

            String uid = Simple.getFirstMatch("Uid:[\\s]*([0-9]*)", status);
            String gid = Simple.getFirstMatch("Gid:[\\s]*([0-9]*)", status);
            String threads = Simple.getFirstMatch("Threads:[\\s]*([0-9]*)", status);
            String voswitch = Simple.getFirstMatch("voluntary_ctxt_switches:[\\s]*([0-9]*)", status);
            String nvswitch = Simple.getFirstMatch("nonvoluntary_ctxt_switches:[\\s]*([0-9]*)", status);

            pi.uid = (uid == null) ? -1 : Integer.parseInt(uid);
            pi.gid = (gid == null) ? -1 : Integer.parseInt(gid);
            pi.actThreads = (threads == null) ? -1 : Integer.parseInt(threads);
            pi.voSwitch = (voswitch == null) ? -1 : Integer.parseInt(voswitch);
            pi.nvSwitch = (nvswitch == null) ? -1 : Integer.parseInt(nvswitch);

            /*
            if (pi.watchmode == ProcessInfo.WATCHMODE_IMPORTANT)
            {
                Log.d(LOGTAG, "IMPORTANT: " + pi.name
                        + " threads=" + pi.actThreads
                        + " vo=" + pi.voSwitch
                        + " nv=" + pi.nvSwitch);
            }
            */
        }

        //
        // Check obsoleted processes.
        //

        ArrayList<Integer> remove = new ArrayList<>();

        String mypackage = Simple.getPackageName();

        for (Map.Entry<Integer, ProcessInfo> pid : processes.entrySet())
        {
            ProcessInfo pi = pid.getValue();
            if (pi.name == null) continue;

            if (pi.sequence != sequence)
            {
                //
                // Process is gone.
                //

                remove.add(pid.getKey());
                continue;
            }

            if (pi.iniThreads == 0) pi.iniThreads = pi.actThreads;

            if (pi.name.equals(mypackage))
            {
                pi.importance = CommonStatic.focused ?
                        ProcessInfo.IMPORTANCE_FOREGROUND :
                        ProcessInfo.IMPORTANCE_BACKGROUND;
            }
            else
            {
                if (CommonStatic.focused)
                {
                    //
                    // We hold a focus so everybody else is in background.
                    //

                    pi.iniThreads = pi.actThreads;
                    pi.importance = ProcessInfo.IMPORTANCE_BACKGROUND;
                }
                else
                {
                    if (pi.actThreads > pi.iniThreads)
                    {
                        //
                        // This process has increased its number of threads,
                        // so we assume it is doing something.
                        //

                        pi.importance = ProcessInfo.IMPORTANCE_FOREGROUND;
                    }
                    else
                    {
                        pi.importance = ProcessInfo.IMPORTANCE_BACKGROUND;
                    }
                }
            }

            if (pi.importance == ProcessInfo.IMPORTANCE_FOREGROUND) running = pi;
        }

        //
        // Remove obsoleted processes.
        //

        for (int pid : remove) processes.remove(pid);
    }
}
