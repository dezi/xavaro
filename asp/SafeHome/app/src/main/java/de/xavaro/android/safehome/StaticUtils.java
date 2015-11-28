package de.xavaro.android.safehome;


import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StaticUtils
{
    //
    // Read raw text resource into string.
    //
    public static String readRawTextResource(Context ctx, int resId)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        StringBuilder text = new StringBuilder();
        String line;

        try
        {
            while ((line = buffreader.readLine()) != null)
            {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e)
        {
            return null;
        }

        return text.toString();
    }

    //
    // Read raw text JSON resource into JSONObject.
    //
    public static JSONObject readRawTextResourceJSON(Context ctx, int resId)
    {
        String json = readRawTextResource(ctx, resId);
        if (json == null) return null;

        try
        {
            return new JSONObject(json);
        } catch (JSONException ex)
        {
            ex.printStackTrace();
        }

        return null;
    }
}