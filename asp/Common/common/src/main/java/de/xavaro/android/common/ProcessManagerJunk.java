package de.xavaro.android.common;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessManagerJunk
{
    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessInfoxx(Context ctx)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            Map<String, Integer> runningAppProcesses = ProcessManagerJunk.getRunningAppProcesses();

            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : runningAppProcesses.entrySet())
            {
                ActivityManager.RunningAppProcessInfo info =
                        new ActivityManager.RunningAppProcessInfo(
                                entry.getKey(), entry.getValue(), null
                );

                //info.uid = process.uid;
                // TODO: Get more information about the process. pkgList, importance, lru, etc.
                appProcessInfos.add(info);
            }
            return appProcessInfos;
        }

        //ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        //return am.getRunningAppProcesses();
    }

    protected static String readFile(String path) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            StringBuilder output = new StringBuilder();
            reader = new BufferedReader(new FileReader(path));
            for (String line = reader.readLine(), newLine = ""; line != null; line = reader.readLine())
            {
                output.append(newLine).append(line);
                newLine = "\n";
            }
            return output.toString();
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }
    public static Map<String, Integer> getRunningAppProcesses()
    {
        Map<String, Integer> processes = new HashMap<>();

        File[] files = new File("/proc").listFiles();

        for (File file : files)
        {
            if (file.isDirectory())
            {
                String cmdline;
                int pid;

                try
                {
                    pid = Integer.parseInt(file.getName());
                }
                catch (NumberFormatException ex)
                {
                    continue;
                }

                try
                {
                    cmdline = readFile(String.format("/proc/%d/cmdline", pid)).trim();
                }
                catch (IOException ex)
                {
                    continue;
                }

                if (cmdline.equals("")) continue;
                if (cmdline.startsWith("/")) continue;
                if (! cmdline.contains(".")) continue;

                boolean isjunk = false;
                byte[] bytes = cmdline.getBytes();
                for (int inx = 0; inx < bytes.length; inx++)
                {
                    if ((bytes[ inx ] & 0xff) > 127)
                    {
                        isjunk = true;
                        break;
                    }
                }

                if (isjunk) continue;

                try
                {
                    String stat = readFile(String.format("/proc/%d/status", pid)).trim();

                    Log.d("STATUZS",cmdline);
                    Log.d("STATUZS",stat);
                }
                catch (IOException ex)
                {
                    continue;
                }

                processes.put(cmdline, pid);
            }
        }

        return processes;
    }
}
