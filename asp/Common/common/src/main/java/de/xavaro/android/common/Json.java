package de.xavaro.android.common;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json
{
    private static final String LOGTAG = Json.class.getSimpleName();

    @Nullable
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

        return null;
    }

    public static void remove(JSONObject json, String key)
    {
        json.remove(key);
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
    public static String getString(JSONObject json, String key)
    {
        try
        {
            return json.getString(key);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
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
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
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
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
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
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
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

    public static String defuck(String json)
    {
        //
        // I hate slash escaping.
        //

        return json.replace("\\/","/");
    }
}
