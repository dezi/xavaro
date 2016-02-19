package de.xavaro.android.common;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class NewFileManager
{
    private static final String LOGTAG = NewFileManager.class.getSimpleName();

    private static JSONObject filestats;
    private static boolean dirty;

    public static int checkDirectoryContent(String basedir, JSONArray files)
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

        int delcount = (oldfiles != null) ? oldfiles.length() : 0;

        if ((delcount > 0) || (newcount > 0) || initial)
        {
            Json.put(filestats, basedir, newfiles);
            dirty = true;
        }

        Simple.makePost(freeMemory, 10 * 1000);

        return newcount;
    }

    public static void markFileAsOld(String basedir, String filepath)
    {
        Simple.removePost(freeMemory);
        getStorage();

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
