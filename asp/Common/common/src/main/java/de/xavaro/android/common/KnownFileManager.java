package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class KnownFileManager
{
    private static final String LOGTAG = KnownFileManager.class.getSimpleName();

    private static JSONObject filestats;
    private static boolean dirty;

    public static int checkDirectory(String basedir, JSONArray files)
    {
        if (files == null) return 0;

        Simple.removePost(freeMemory);
        getStorage();

        int newcount = 0;
        int skiplen = basedir.length() + 1;

        boolean initial = ! filestats.has(basedir);

        JSONObject oldfiles = initial ? null : Json.getObject(filestats, basedir);
        JSONObject newfiles = new JSONObject();

        for (int inx = 0; inx < files.length(); inx++)
        {
            JSONObject file = Json.getObject(files, inx);
            String name = Json.getString(file, "file");
            String time = Json.getString(file, "time");
            if ((name == null) || (time == null)) continue;

            name = name.substring(skiplen);

            if (oldfiles != null)
            {
                if (oldfiles.has(name))
                {
                    oldfiles.remove(name);
                    Json.put(newfiles, name, time);
                }
                else
                {
                    newcount++;
                }
            }
            else
            {
                Json.put(newfiles, name, time);
            }
        }

        Json.put(filestats, basedir, newfiles);

        int delcount = (oldfiles != null) ? oldfiles.length() : 0;

        if ((delcount > 0) || (newcount > 0) || initial)
        {
            dirty = true;
        }

        Simple.makePost(freeMemory, 10 * 1000);

        return newcount;
    }

    public static void putKnownFileStatus(String filepath)
    {
        Simple.removePost(freeMemory);
        getStorage();

        String basedir = new File(filepath).getParent();

        if (filestats.has(basedir))
        {
            JSONObject oldfiles = Json.getObject(filestats, basedir);

            if (oldfiles != null)
            {
                File file = new File(filepath);
                Json.put(oldfiles, file.getName(), Simple.timeStampAsISO(file.lastModified()));
                dirty = true;
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);
    }

    public static boolean getKnownFileStatus(String filepath)
    {
        boolean known = false;

        Simple.removePost(freeMemory);
        getStorage();

        String basedir = new File(filepath).getParent();
        String filename = new File(filepath).getName();

        if (filestats.has(basedir))
        {
            JSONObject oldfiles = Json.getObject(filestats, basedir);
            if (oldfiles != null) known = oldfiles.has(filename);
        }

        Simple.makePost(freeMemory, 10 * 1000);

        return known;
    }

    private static final Runnable freeMemory = new Runnable()
    {
        @Override
        public void run()
        {
            if (filestats != null)
            {
                if (dirty) putStorage();

                filestats = null;
            }
        }
    };

    private static void getStorage()
    {
        if (filestats != null) return;

        File file = new File(Simple.getFilesDir(), "filestats.act.json");
        if (! file.exists()) file = new File(Simple.getFilesDir(), "filestats.bak.json");

        try
        {
            if (! file.exists())
            {
                filestats = new JSONObject();
            }
            else
            {
                String json = Simple.getFileContent(file);
                filestats = (json != null) ? new JSONObject(json) : new JSONObject();
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }

    private static void putStorage()
    {
        if (filestats == null) return;

        File tmp = new File(Simple.getFilesDir(), "filestats.tmp.json");
        File bak = new File(Simple.getFilesDir(), "filestats.bak.json");
        File act = new File(Simple.getFilesDir(), "filestats.act.json");

        try
        {
            if (Simple.putFileContent(tmp, Json.defuck(Json.toPretty(filestats))))
            {
                boolean ok = true;

                if (bak.exists()) ok = bak.delete();
                if (act.exists()) ok &= act.renameTo(bak);
                if (tmp.exists()) ok &= tmp.renameTo(act);

                Log.d(LOGTAG, "putStorage: ok=" + ok);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }
}
