package de.xavaro.android.common;

import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
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
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
// Static all purpose utility methods.
//

@SuppressWarnings({"WeakerAccess", "UnusedParameters", "unused"})
public class StaticUtils
{
    private static final String LOGTAG = "StaticUtils";

    //region Generic conversion methods.

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
    // Convert JSON to string with indent and dump.
    //

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

    //endregion

    //region Config reader methods.

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
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    //endregion

    //
    // Retrieve package name handling emails.
    //

    public static String getDefaultEmail(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("mailto:"));
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

    //endregion

    //region Nice to have image methods.

    //
    // Draw bitmap as circle into new bitmap.
    //

    public static Drawable getflippedDrawable(Context context, int id, boolean horz, boolean vert, int rotate)
    {
        Bitmap sprite = BitmapFactory.decodeResource(context.getResources(), id);
        Matrix mirrorMatrix = new Matrix();
        mirrorMatrix.preScale(horz ? -1f : 1f, vert ? -1f : 1f);
        Bitmap fSprite = Bitmap.createBitmap(sprite, 0, 0, sprite.getWidth(), sprite.getHeight(), mirrorMatrix, false);

        if (rotate != 0)
        {
            Bitmap output = Bitmap.createBitmap(
                    fSprite.getWidth(),
                    fSprite.getHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(output);

            canvas.rotate(90, fSprite.getWidth() / 2, fSprite.getHeight() / 2);
            canvas.drawBitmap(fSprite, 0, 0, null);

            fSprite.recycle();
            fSprite = output;
        }

        return new BitmapDrawable(context.getResources(), fSprite);
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

    //endregion

    //region Get all installed apps.

    @Nullable
    public static JSONObject getAllInstalledApps(Context context)
    {
        JSONArray joappsarray = new JSONArray();
        JSONArray josyssarray = new JSONArray();

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages)
        {
            JSONObject joapp = new JSONObject();

            try
            {
                joapp.put("packagename", packageInfo.packageName);
                Log.d(LOGTAG, "Installed package: " + joapp.getString("packagename"));

                joapp.put("sourcedir", packageInfo.sourceDir);
                Log.d(LOGTAG, "Source dir: " + joapp.getString("sourcedir"));

                Intent launchintent = pm.getLaunchIntentForPackage(packageInfo.packageName);

                if (launchintent != null)
                {
                    joapp.put("launchintent", launchintent);
                    Log.d(LOGTAG, "Launch Activity: " + joapp.getString("launchintent"));

                    joappsarray.put(joapp);
                }
                else
                {
                    josyssarray.put(joapp);
                }
            }
            catch (JSONException ignore)
            {
            }
        }

        try
        {
            JSONObject jo =  new JSONObject();
            jo.put("apppackages", joappsarray);
            jo.put("syspackages",josyssarray);

            return jo;
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    //endregion

    //region Get content from HTTP methods.

    @Nullable
    public static String getContentFromUrl(String src)
    {
        try
        {
            URL loadurl = new URL(src);

            HttpURLConnection connection = (HttpURLConnection) loadurl.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            StringBuilder string = new StringBuilder();
            byte[] buffer = new byte[ 4096 ];
            int xfer;

            while ((xfer = input.read(buffer)) > 0)
            {
                string.append(new String(buffer, 0, xfer));
            }

            input.close();

            return string.toString();
        }
        catch (IOException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String getContentFromStream(InputStream input)
    {
        try
        {
            StringBuilder string = new StringBuilder();
            byte[] buffer = new byte[4096];
            int xfer;

            while ((xfer = input.read(buffer)) > 0)
            {
                string.append(new String(buffer, 0, xfer));
            }

            input.close();

            return string.toString();
        }
        catch (IOException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static Bitmap getBitmapFromURL(String src)
    {
        try
        {
            URL url = new URL(src);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings({"PointlessBitwiseExpression", "PointlessArithmeticExpression"})
    public static Bitmap downscaleAntiAliasBitmap(Bitmap src, int targetWidth, int targetHeight)
    {
        Bitmap nnn = Bitmap.createScaledBitmap(src, targetWidth << 1, targetHeight << 1, true);

        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

        int p00, p01, p10, p11;
        int a, r, g, b;

        for (int row = 0; row < nnn.getHeight();  row += 2)
        {
            for (int col = 0; col < nnn.getWidth(); col += 2)
            {
                // @formatter:off
                p00 = nnn.getPixel(col + 0, row + 0);
                p01 = nnn.getPixel(col + 0, row + 1);
                p10 = nnn.getPixel(col + 1, row + 0);
                p11 = nnn.getPixel(col + 1, row + 1);

                a = ((p00 >> 24) & 0xff) + ((p01 >> 24) & 0xff) + ((p10 >> 24) & 0xff) + ((p11 >> 24) & 0xff);
                r = ((p00 >> 16) & 0xff) + ((p01 >> 16) & 0xff) + ((p10 >> 16) & 0xff) + ((p11 >> 16) & 0xff);
                g = ((p00 >>  8) & 0xff) + ((p01 >>  8) & 0xff) + ((p10 >>  8) & 0xff) + ((p11 >>  8) & 0xff);
                b = ((p00 >>  0) & 0xff) + ((p01 >>  0) & 0xff) + ((p10 >>  0) & 0xff) + ((p11 >>  0) & 0xff);
                // @formatter:on

                target.setPixel(col >> 1, row >> 1, Color.argb(a >> 2, r >> 2, g >> 2, b >> 2));
            }
        }

        return target;
    }

    //endregion

    //region Networking methods.

    @Nullable
    public static String getMACAddress(String interfaceName)
    {
        try
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface intf : interfaces)
            {
                byte[] mac = intf.getHardwareAddress();

                StringBuilder buf = new StringBuilder();

                if (mac != null)
                {
                    for (byte byt : mac)
                    {
                        buf.append(String.format("%02X:", byt));
                    }
                }

                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);

                Log.d(LOGTAG,"getMACAddress interface:" + intf.getName() + "=" + buf.toString());

                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());

                for (InetAddress addr : addrs)
                {
                    //if (! addr.isLoopbackAddress())
                    {
                        String sAddr = addr.getHostAddress();

                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        Log.d(LOGTAG,"getMACAddress addresses:" + intf.getName() + "=" + sAddr + ":" + isIPv4);
                    }
                }
            }
        }
        catch (Exception ignore)
        {
        }

        return "";
    }

    //endregion

    //region Simplified methods.

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);

        }
        catch (InterruptedException ignore)
        {
        }
    }

    public static String defuckJSON(String json)
    {
        //
        // I hate slash escaping.
        //

        return json.replace("\\/","/");
    }

    public static void fileCopy(File src, File dst) throws IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

    @Nullable
    public static String findDat(String regex, String content)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (! matcher.find()) return null;

        return matcher.group(1);
    }

    public static int getStatusBarHeight(Context context)
    {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)  return context.getResources().getDimensionPixelSize(resourceId);

        return 0;
    }

    public static boolean getSharedPrefsBoolean(Context context, String key)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(key, false);
    }

    public static void dumpViewsChildren(View view)
    {
        dumpViewsChildrenRecurse(view, 0);
    }

    private static void dumpViewsChildrenRecurse(View view, int level)
    {
        if (! (view instanceof ViewGroup)) return;

        String tab = "";

        for (int inx = 0; inx < level; inx++) tab += "  ";

        for(int inx = 0; inx < ((ViewGroup) view).getChildCount(); ++inx)
        {
            View nextChild = ((ViewGroup) view).getChildAt(inx);

            Log.d(LOGTAG, "dumpViewsChildren:" + tab + nextChild);

            dumpViewsChildrenRecurse(nextChild, level + 1);
        }
    }

    public static String nowAsISO()
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }

    public static String timeStampAsISO(long timestamp)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(timestamp));
    }

    public static boolean isAppInstalled(Context context, String packageName)
    {
        try
        {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);

            return (appInfo != null);
        }
        catch (PackageManager.NameNotFoundException ignore)
        {
        }

        return false;
    }

    //endregion
}