package de.xavaro.android.common;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json
{
    private static final String LOGTAG = Json.class.getSimpleName();

    public static JSONObject fromString(String jsonstr)
    {
        if (jsonstr != null)
        {
            try
            {
                return new JSONObject(jsonstr);
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return new JSONObject();
    }

    public static JSONObject fromStringObject(String jsonstr)
    {
        return fromString(jsonstr);
    }

    public static JSONArray fromStringArray(String jsonstr)
    {
        if (jsonstr != null)
        {
            try
            {
                return new JSONArray(jsonstr);
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return new JSONArray();
    }

    public static JSONObject clone(JSONObject json)
    {
        try
        {
            return new JSONObject(json.toString());
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return new JSONObject();
    }

    public static JSONArray clone(JSONArray json)
    {
        try
        {
            return new JSONArray(json.toString());
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return new JSONArray();
    }

    public static void remove(JSONObject json, String key)
    {
        json.remove(key);
    }

    public static boolean equals(JSONObject j1, String k1, JSONObject j2, String k2)
    {
        String s1 = getString(j1, k1);
        String s2 = getString(j2, k2);

        return ((s1 != null) && (s2 != null) && s1.equals(s2));
    }

    public static boolean equals(JSONObject j1, String k1, JSONObject j2)
    {
        String s1 = getString(j1, k1);
        String s2 = getString(j2, k1);

        return ((s1 != null) && (s2 != null) && s1.equals(s2));
    }

    public static void copy(JSONObject j1, String k1, JSONObject j2)
    {
        put(j1, k1, get(j2, k1));
    }

    public static void put(JSONObject json, String key, Object val)
    {
        try
        {
            json.put(key, val);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static void put(JSONArray json, Object val)
    {
        json.put(val);
    }

    @Nullable
    public static Object get(JSONObject json, String key)
    {
        try
        {
            return json.get(key);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static int getInt(JSONObject json, String key)
    {
        try
        {
            return json.getInt(key);
        }
        catch (JSONException ignore)
        {
        }

        return 0;
    }

    @Nullable
    public static String getString(JSONObject json, String key)
    {
        try
        {
            return json.getString(key);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static JSONArray getArray(JSONObject json, String key)
    {
        try
        {
            return json.getJSONArray(key);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static JSONObject getObject(JSONObject json, String key)
    {
        try
        {
            return json.getJSONObject(key);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static JSONObject getObject(JSONArray json, int index)
    {
        try
        {
            return json.getJSONObject(index);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String toPretty(JSONObject jsonObject)
    {
        if (jsonObject != null)
        {
            try
            {
                return jsonObject.toString(2);
            }
            catch (JSONException ignored)
            {
            }
        }

        return null;
    }

    @Nullable
    public static String toPretty(JSONArray jsonArray)
    {
        if (jsonArray != null)
        {
            try
            {
                return jsonArray.toString(2);
            }
            catch (JSONException ignored)
            {
            }
        }

        return null;
    }

    public static String defuck(String json)
    {
        //
        // I hate slash escaping.
        //

        return json.replace("\\/","/");
    }
}
