package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class Social
{
    private static final String LOGTAG = Social.class.getSimpleName();

    protected static boolean verbose = true;

    public static void setVerbose(boolean yesno)
    {
        verbose = yesno;
    }

    protected static Bundle getParameters(JSONObject jparams)
    {
        Bundle bparams = new Bundle();

        if (jparams != null)
        {
            Iterator<String> keysIterator = jparams.keys();

            while (keysIterator.hasNext())
            {
                String key = keysIterator.next();
                Object val = Json.get(jparams, key);

                if (val instanceof Boolean)
                {
                    bparams.putBoolean(key, (Boolean) val);
                    continue;
                }

                if (val instanceof Integer)
                {
                    bparams.putInt(key, (Integer) val);
                    continue;
                }

                if (val instanceof Long)
                {
                    bparams.putLong(key, (Long) val);
                    continue;
                }

                if (val instanceof String)
                {
                    bparams.putString(key, (String) val);
                    continue;
                }

                if (val instanceof JSONArray)
                {
                    JSONArray array = (JSONArray) val;
                    String imploded = "";

                    for (int inx = 0; inx < array.length(); inx++)
                    {
                        Object jobj = Json.get(array, inx);
                        if (! (jobj instanceof String)) continue;

                        if (imploded.length() > 0) imploded += ",";
                        imploded  += (String) jobj;
                    }

                    bparams.putString(key, imploded);
                }
            }
        }

        return bparams;
    }
}
