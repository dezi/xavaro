package de.xavaro.android.common;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProcessManager
{
    private static final String LOGTAG = ProcessManager.class.getSimpleName();

    public static class ProcessInfo
    {
        public int pid;
        public String name;
        public int uid;
        public int gid;
        public int sequence;
    }

    private static final Map<Integer, ProcessInfo> processes = new HashMap<>();
    private static int sequence = 0;

    public static Map<Integer, ProcessInfo> getProcesses(boolean log)
    {
        sequence++;

        File[] files = new File("/proc").listFiles();

        for (File file : files)
        {
            if (file.isDirectory())
            {
                String status;
                String name;
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
                    processes.get(pid).sequence = sequence;

                    continue;
                }

                try
                {
                    name = StaticUtils.readFile(String.format("/proc/%d/cmdline", pid)).trim();
                }
                catch (IOException ex)
                {
                    continue;
                }

                if (name.equals("")) continue;
                if (name.startsWith("/")) continue;
                if (! name.contains(".")) continue;
                if (! StaticUtils.isPlainASCII(name)) continue;

                try
                {
                    status = StaticUtils.readFile(String.format("/proc/%d/status", pid)).trim();
                }
                catch (IOException ex)
                {
                    continue;
                }

                String uid = Simple.getFirstMatch("Uid:[\\s]*([0-9]*)",status);
                String gid = Simple.getFirstMatch("Gid:[\\s]*([0-9]*)",status);

                ProcessInfo pi = new ProcessInfo();

                pi.pid = pid;
                pi.name = name;
                pi.sequence = sequence;
                pi.uid = (uid == null) ? -1 : Integer.parseInt(uid);
                pi.gid = (gid == null) ? -1 : Integer.parseInt(gid);

                if (pi.uid < 10000) continue;

                if (pi.name.contains("recentsactivity"))
                {
                    Log.d(LOGTAG,"recentsactivity!!!!");
                }

                processes.put(pid, pi);
            }
        }

        //
        // Check obsoleted processes.
        //

        ArrayList<Integer> remove = new ArrayList<>();

        for (Map.Entry<Integer, ProcessInfo> pid : processes.entrySet())
        {
            ProcessInfo pi = pid.getValue();

            if (pi.sequence != sequence)
            {
                //
                // Process is gone.
                //

                remove.add(pid.getKey());
                continue;
            }

            if (log)
            {
                Log.d(LOGTAG, "getProcesses"
                        + " pid=" + pi.pid
                        + " uid=" + pi.uid
                        + " gid=" + pi.gid
                        + " name=" + pi.name);
            }
        }

        //
        // Remove obsoleted processes.
        //

        for (int pid : remove) processes.remove(pid);

        return processes;
    }
}
