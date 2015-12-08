package de.xavaro.android.safehome;

import android.system.ErrnoException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
// Error logging for "This should never happen" problems.
//

public class OopsHandler
{
    public static void Log(String tag, String message)
    {
        Log.e(tag, message);
    }

    public static void Log(String tag,Exception exception)
    {
        try
        {
            JSONObject err = new JSONObject();

            err.put("tag", tag);
            err.put("msg", exception.getMessage());

            Throwable errnoex = exception.getCause();
            if (errnoex instanceof ErrnoException)
            {
                err.put("err", ((ErrnoException) errnoex).errno);
            }

            StackTraceElement[] st = exception.getStackTrace();

            //
            // Put must recent caller as the one to blame.
            //

            JSONObject ex = new JSONObject();
            ex.put("cn", st[ 0 ].getClassName());
            ex.put("mn", st[ 0 ].getMethodName());
            ex.put("ln", st[ 0 ].getLineNumber());

            err.put("ex", ex);

            //
            // Put at most two callers from own app into dump.
            //

            String appname = UpushService.class.getPackage().getName();

            JSONArray bys = new JSONArray();

            for (int inx = 1; inx < st.length; inx++)
            {
                if (! st[ inx ].getClassName().startsWith(appname))
                {
                    continue;
                }

                JSONObject by = new JSONObject();
                by.put("cn", st[ inx ].getClassName());
                by.put("mn", st[ inx ].getMethodName());
                by.put("ln", st[ inx ].getLineNumber());

                bys.put(by);

                if (bys.length() >= 2) break;
            }

            err.put("by", bys);

            Log.e(tag, err.toString());
        }
        catch (JSONException ignore)
        {
            // Fuckit.
        }
    }
}
