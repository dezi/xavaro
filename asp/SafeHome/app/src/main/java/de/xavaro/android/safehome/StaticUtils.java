package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StaticUtils
{
    private static final String LOGTAG = "StaticUtils";

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

    //
    // Retrieve profile image for WhatsApp telefon number.
    //
    public static Bitmap getWhatsAppProfileBitmap(Context context, String phone)
    {
        File wappdir = new File("/sdcard/WhatsApp/Profile Pictures");

        if ((wappdir == null) || ! wappdir.isDirectory()) return null;

        String[] wappdirlist = wappdir.list();

        if (wappdirlist == null) return null;

        for (String ppfile : wappdirlist)
        {
            if (ppfile.startsWith(phone))
            {
                Bitmap thumbnail = null;

                try
                {
                    FileInputStream fi = new FileInputStream(wappdir + File.separator + ppfile);
                    thumbnail = BitmapFactory.decodeStream(fi);
                    fi.close();
                }
                catch (Exception ex)
                {
                    Log.e(LOGTAG,"getWhatsAppProfileBitmap: " + ex.getMessage());

                    return null;
                }

                Log.d(LOGTAG, "getWhatsAppProfileBitmap: OK.");

                return thumbnail;
            }
        }

        return null;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap)
    {
        Bitmap output = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        int color = Color.RED;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}