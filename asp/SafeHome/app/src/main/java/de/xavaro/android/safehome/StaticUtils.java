package de.xavaro.android.safehome;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
// Static all purpose utility methods.
//

@SuppressWarnings({"WeakerAccess", "UnusedParameters"})
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

    //region Default home and assist methods.

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

    public static boolean isDefaultHome(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo != null) && res.activityInfo.packageName.equals(context.getPackageName());
    }

    //
    // Retrieve package name handling assist button press.
    //

    public static String getDefaultAssist(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

    public static boolean isDefaultAssist(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo != null) && res.activityInfo.packageName.equals(context.getPackageName());
    }

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

    @Nullable
    public static Bitmap getIconFromAppStore(Context context, String packageName)
    {
        String iconfile = "appstore." + packageName + ".thumbnail.png";

        Bitmap icon = CacheManager.getThumbnail(context,iconfile);
        if (icon != null) return icon;

        try
        {
            String url = "https://play.google.com/store/apps/details?id=" + packageName;

            Log.d(LOGTAG,"getIconFromAppStore:" + url);

            String content = StaticUtils.getContentFromUrl(url);
            if (content == null) return null;

            Pattern pattern = Pattern.compile("class=\"cover-image\" src=\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(content);
            if (! matcher.find()) return null;

            String iconurl = matcher.group(1);

            Log.d(LOGTAG, "getIconFromAppStore:" + iconurl);

            return CacheManager.cacheThumbnail(context, iconurl, iconfile);
        }
        catch (Exception oops)
        {
            OopsService.log(LOGTAG,oops);
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
        catch (IOException ex)
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
        catch (IOException ex)
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
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            return myBitmap;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
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
                    for (int idx = 0; idx < mac.length; idx++)
                    {
                        buf.append(String.format("%02X:", mac[ idx ]));
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

                        Log.d(LOGTAG,"getMACAddress addresses:" + intf.getName() + "=" + sAddr);
                    }
                }
            }
        }
        catch (Exception ex)
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

    //endregion
}