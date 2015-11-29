package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

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
    public static String readRawTextResource(Context context, int resId)
    {
        InputStream inputStream = context.getResources().openRawResource(resId);

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
    public static JSONObject readRawTextResourceJSON(Context context, int resId)
    {
        String json = readRawTextResource(context, resId);
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

    //
    // Retrieve package name handling home button press.
    //
    public static String getDefaultHome(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

    //
    // Retrieve package name handling home button press.
    //
    public static String getDefaultAssist(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

}