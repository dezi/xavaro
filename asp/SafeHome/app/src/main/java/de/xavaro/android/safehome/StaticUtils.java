package de.xavaro.android.safehome;


import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StaticUtils
{
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
        }
        catch (IOException e)
        {
            return null;
        }

        return text.toString();
    }
}
