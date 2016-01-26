package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class PersistManager
{
    private static final String LOGTAG = PersistManager.class.getSimpleName();

    private static JSONObject settings;
    private static Context context;
    private static boolean dirty;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void initialize()
    {
        if (settings != null) return;

        context = Simple.getAnyContext();

        File file = new File(context.getFilesDir(), "settings.act.json");

        if (! file.exists())
        {
            file = new File(context.getFilesDir(), "settings.bak.json");
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

        File tmp = new File(context.getFilesDir(), "settings.tmp.json");
        File bak = new File(context.getFilesDir(), "settings.bak.json");
        File act = new File(context.getFilesDir(), "settings.act.json");

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
        initialize();

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
        return getXpathString(xpath, true);
    }

    @Nullable
    public static String getXpathString(String xpath, boolean ignore)
    {
        try
        {
            String[] parts = xpath.split("/");
            JSONObject jo = resolveXpath(parts, false);
            return jo.getString(parts[ parts.length - 1 ]);
        }
        catch (Exception ex)
        {
            if (! ignore) OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static boolean getXpathBoolean(String xpath)
    {
        return getXpathBoolean(xpath, true);
    }

    public static boolean getXpathBoolean(String xpath, boolean ignore)
    {
        try
        {
            String[] parts = xpath.split("/");
            JSONObject jo = resolveXpath(parts, false);
            return jo.getBoolean(parts[ parts.length - 1 ]);
        }
        catch (Exception ex)
        {
            if (! ignore) OopsService.log(LOGTAG,ex);
        }

        return false;
    }

    public static int getXpathInt(String xpath)
    {
        return getXpathInt(xpath, true);
    }

    public static int getXpathInt(String xpath, boolean ignore)
    {
        try
        {
            String[] parts = xpath.split("/");
            JSONObject jo = resolveXpath(parts, false);
            return jo.getInt(parts[ parts.length - 1 ]);
        }
        catch (Exception ex)
        {
            if (! ignore) OopsService.log(LOGTAG,ex);
        }

        return 0;
    }

    @Nullable
    public static JSONObject getXpathJSONObject(String xpath)
    {
        return getXpathJSONObject(xpath, true);
    }

    @Nullable
    public static JSONObject getXpathJSONObject(String xpath, boolean ignore)
    {
        try
        {
            String[] parts = xpath.split("/");
            JSONObject jo = resolveXpath(parts, false);
            return jo.getJSONObject(parts[ parts.length - 1 ]);
        }
        catch (Exception ex)
        {
            if (! ignore) OopsService.log(LOGTAG,ex);
        }

        return null;
    }

    public static void putXpath(String xpath, Object value)
    {
        try
        {
            String[] parts = xpath.split("/");
            JSONObject jo = resolveXpath(parts, true);
            jo.put(parts[ parts.length - 1 ], value);

            dirty = true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }

    public static boolean delXpath(String xpath)
    {
        try
        {
            String[] parts = xpath.split("/");
            JSONObject jo = resolveXpath(parts, true);

            if (jo.has(parts[ parts.length - 1 ]))
            {
                jo.remove(parts[ parts.length - 1 ]);

                dirty = true;

                Log.d(LOGTAG,"delXpath: deleted=" + xpath);

                return true;
            }
            else
            {
                Log.d(LOGTAG,"delXpath: notfound=" + xpath);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }

        return false;
    }
}
