package de.xavaro.android.safehome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"WeakerAccess", "UnusedParameters"})

public class StaticUtils
{
    private static final String LOGTAG = "StaticUtils";

    //
    // Convert array of bytes to hex string.
    //
    public static String hexBytesToString(byte[] bytes)
    {
        if (bytes == null) return null;

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[ bytes.length << 1 ];

        for (int inx = 0; inx < bytes.length; inx++)
        {
            //noinspection PointlessArithmeticExpression
            hexChars[ (inx << 1) + 0 ] = hexArray[ (bytes[ inx ] >> 4) & 0x0f ];
            //noinspection PointlessBitwiseExpression
            hexChars[ (inx << 1) + 1 ] = hexArray[ (bytes[ inx ] >> 0) & 0x0f ];
        }

        return String.valueOf(hexChars);
    }

    //
    // Convert array of bytes to hex string.
    //
    public static byte[] hexStringToBytes(String hexstring)
    {
        if (hexstring == null) return null;

        byte[] bytes = new byte[ hexstring.length() >> 1 ];

        for (int inx = 0; inx < hexstring.length(); inx += 2)
        {
            //noinspection PointlessBitwiseExpression,PointlessArithmeticExpression
            bytes[ inx >> 1 ] = (byte)
                    ((Character.digit(hexstring.charAt(inx + 0), 16) << 4)
                    + Character.digit(hexstring.charAt(inx + 1), 16) << 0);
        }

        return bytes;
    }

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
    // Retrieve profile image bitmap for any telefone number.
    //
    public static Bitmap getAnyProfileBitmap(Context context, String phone)
    {
        Bitmap bitmap = null;

        try
        {
            //noinspection ConstantConditions
            if (bitmap == null) bitmap = getWhatsAppProfileBitmap(context, phone);
            if (bitmap == null) bitmap = getContactsProfileBitmap(context,phone);
        }
        catch (Exception ignore)
        {
        }

        return bitmap;
    }

    //
    // Retrieve profile image bitmap for contacts telefone number.
    //
    public static Bitmap getContactsProfileBitmap(Context context, String phone)
    {
        boolean ismatch = false;
        String photo = null;

        phone = phone.replaceAll(" ","");

        JSONObject contacts = new ContactsHandler(context).contacts2JSONObject();

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                for (int inx = 0; inx < contact.length(); inx++)
                {
                    JSONObject item = contact.getJSONObject(inx);

                    if (item.has("NUMBER"))
                    {
                        String number = item.getString("NUMBER").replaceAll(" ","");

                        if (number.endsWith(phone))
                        {
                            ismatch = true;
                        }
                    }

                    if (item.has("PHOTO"))
                    {
                        photo = item.getString("PHOTO");
                    }
                }

                if (ismatch && (photo != null)) break;
            }
        }
        catch (JSONException ignore)
        {
        }

        if ((! ismatch) || (photo == null)) return null;

        byte[] rawbytes = StaticUtils.hexStringToBytes(photo);

        return BitmapFactory.decodeByteArray(rawbytes, 0, rawbytes.length);
    }

    //
    // Retrieve profile image bitmap for WhatsApp telefone number.
    //
    public static Bitmap getWhatsAppProfileBitmap(Context context, String phone)
    {
        File wappdir = new File(Environment.getExternalStorageDirectory().getPath() + "/WhatsApp/Profile Pictures");

        if (! wappdir.isDirectory()) return null;

        String[] wappdirlist = wappdir.list();

        if (wappdirlist == null) return null;

        for (String ppfile : wappdirlist)
        {
            if (ppfile.startsWith(phone))
            {
                try
                {
                    FileInputStream fi = new FileInputStream(wappdir + File.separator + ppfile);
                    Bitmap thumbnail = BitmapFactory.decodeStream(fi);
                    fi.close();

                    Log.d(LOGTAG, "getWhatsAppProfileBitmap: OK.");

                    return thumbnail;

                }
                catch (Exception ex)
                {
                    Log.e(LOGTAG,"getWhatsAppProfileBitmap: " + ex.getMessage());

                    return null;
                }
            }
        }

        return null;
    }

    //
    // Draw bitmap as circle into new bitmap.
    //
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

    public static JSONObject getAllInstalledApps(Context context)
    {
        JSONArray joappsarray = new JSONArray();

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages)
        {
            JSONObject joapp = new JSONObject();

            try
            {
                joapp.put("packagename", packageInfo.packageName);
                joapp.put("sourcedir", packageInfo.sourceDir);
                joapp.put("launchintent", pm.getLaunchIntentForPackage(packageInfo.packageName));

                Log.d(LOGTAG, "Installed package: " + joapp.getString("packagename"));
                Log.d(LOGTAG, "Source dir: " + joapp.getString("sourcedir"));
                Log.d(LOGTAG, "Launch Activity: " + joapp.getString("launchintent"));

                joappsarray.put(joapp);
            }
            catch (JSONException ignore)
            {
            }
        }

        try
        {
            return new JSONObject().put("packages",joappsarray);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    public static String JSON2String(JSONObject jsonObject, boolean dump)
    {
        if (jsonObject == null) return null;

        String json = null;

        try
        {
            json = jsonObject.toString(2);
        }
        catch (JSONException ignored)
        {
        }

        if (json == null) return null;

        if (dump)
        {
            String[] lines = json.split("\n");

            for (String line : lines)
            {
                Log.d(LOGTAG, line);
            }
        }

        return json;
    }
}