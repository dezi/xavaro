package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsManager
{
    private static final String LOGTAG = SettingsManager.class.getSimpleName();

    private static JSONObject settings;
    private static boolean dirty;
    private static Context ctx;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void initialize(Context context)
    {
        if (settings != null) return;

        ctx = context;

        File file = new File(ctx.getFilesDir(), "settings.act.json");

        if (! file.exists())
        {
            file = new File(ctx.getFilesDir(), "settings.bak.json");
        }

        try
        {
            if (! file.exists())
            {
                settings = new JSONObject();
            }
            else
            {
                InputStream in = new FileInputStream(file);
                byte[] buf = new byte[ (int) file.length() ];

                in.read(buf);
                in.close();

                settings = new JSONObject(new String(buf));
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void flush()
    {
        if (! dirty) return;

        File tmp = new File(ctx.getFilesDir(), "settings.tmp.json");
        File bak = new File(ctx.getFilesDir(), "settings.bak.json");
        File act = new File(ctx.getFilesDir(), "settings.act.json");

        try
        {
            OutputStream out = new FileOutputStream(tmp);
            String json = settings.toString(2);
            out.write(json.getBytes());
            out.close();

            if (bak.exists()) bak.delete();
            if (act.exists()) act.renameTo(bak);
            if (tmp.exists()) tmp.renameTo(act);

            dirty = false;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }

    private static JSONObject resolveXpath(String[] parts, boolean create) throws JSONException
    {
        JSONObject jo = settings;

        for (int inx = 0; inx + 1 < parts.length; inx++)
        {
            if (create && ! jo.has(parts[ inx ]))
            {
                jo.put(parts[ inx ],new JSONObject());
            }

            jo = jo.getJSONObject(parts[ inx ]);
        }

        return jo;
    }

    @Nullable
    public static String getXpathString(String xpath)
    {
        String[] parts = xpath.split("/");

        try
        {
            JSONObject jo = resolveXpath(parts, false);
            return jo.getString(parts[ parts.length - 1 ]);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }

        return null;
    }

    public static void putXpath(String xpath,String value)
    {
        String[] parts = xpath.split("/");

        try
        {
            JSONObject jo = resolveXpath(parts, true);
            jo.put(parts[ parts.length - 1 ], value);

            dirty = true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }
}