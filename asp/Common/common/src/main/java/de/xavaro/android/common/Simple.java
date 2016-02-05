package de.xavaro.android.common;

import android.content.pm.ResolveInfo;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.support.annotation.Nullable;

import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Simple
{
    private static final String LOGTAG = Simple.class.getSimpleName();

    //region Initialisation

    private static Activity appContext;
    private static Handler appHandler;
    private static Context anyContext;

    public static void setAppContext(Activity context)
    {
        Simple.appContext = context;
        Simple.anyContext = context;
        Simple.appHandler = new Handler();
    }

    public static Activity getAppContext()
    {
        return Simple.appContext;
    }

    public static void setAnyContext(Context context)
    {
        Simple.anyContext = context;
    }

    public static Context getAnyContext()
    {
        return Simple.anyContext;
    }

    //endregion Initialisation

    //region GCM stuff

    public static boolean isGCMInitialized()
    {
        return (getGCMapeyki() != null);
    }

    @Nullable
    public static String getGCMToken()
    {
        return CommonStatic.gcm_token;
    }

    public static long getGCMsendeird()
    {
        if (anyContext != null)
        {
            if (anyContext.getPackageName().equals("de.xavaro.android.safehome"))
            {
                return 2955796475946577539L;
            }
        }

        return 0;
    }

    @Nullable
    public static String getGCMapeyki()
    {
        if (anyContext != null)
        {
            if (anyContext.getPackageName().equals("de.xavaro.android.safehome"))
            {
                return "HLscZ|HCQc]g}nZG=MKu~3H0?b?7sm1wp_kPcn=";
            }
        }

        return null;
    }

    //endregion GCM stuff

    //region Keyboard stuff

    public static void dismissKeyboard(View view)
    {
        if (anyContext == null) return;

        InputMethodManager imm = (InputMethodManager) anyContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view)
    {
        if (anyContext == null) return;

        final View runview = view;

        appHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager imm = (InputMethodManager) anyContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(runview, 0);
            }
        }, 500);
    }

    //endregion Keyboard stuff

    //region Application stuff

    public static boolean isAppInstalled(String packageName)
    {
        if (anyContext == null) return false;

        try
        {
            ApplicationInfo appInfo = anyContext.getPackageManager().getApplicationInfo(packageName, 0);

            return (appInfo != null);
        }
        catch (PackageManager.NameNotFoundException ignore)
        {
        }

        return false;
    }

    public static void installAppFromPlaystore(String packagename)
    {
        if (anyContext == null) return;

        Intent goToMarket = new Intent(Intent.ACTION_VIEW);
        goToMarket.setData(Uri.parse("market://details?id=" + packagename));

        ProcessManager.launchIntent(anyContext, goToMarket);
    }

    public static void uninstallApp(String packagename)
    {
        if (anyContext == null) return;

        try
        {
            Uri packageUri = Uri.parse("package:" + packagename);
            Intent unInstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            anyContext.startActivity(unInstall);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static void startActivityForResult(Intent intent, int tag)
    {
        appContext.startActivityForResult(intent, tag);
    }

    //endregion Application stuff

    //region All purpose simple methods

    public static boolean equals(String str1, String str2)
    {
        return (str1 != null) && (str2 != null) && str1.equals(str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2)
    {
        return (str1 != null) && (str2 != null) && str1.equalsIgnoreCase(str2);
    }

    public static boolean startsWith(String str1, String str2)
    {
        return (str1 != null) && (str2 != null) && str1.startsWith(str2);
    }

    public static int compareTo(String str1, String str2)
    {
        if ((str1 != null) && (str2 != null)) return str1.compareTo(str2);

        return 0;
    }

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

    public static long dezify(long number)
    {
        return number ^ 0x2905196228051998L;
    }

    public static String dezify(String string)
    {
        byte[] dezi = { 0x29, 0x05, 0x19, 0x62 };
        byte[] bytes = string.getBytes();

        for (int inx = 0; inx < bytes.length; inx++)
        {
            bytes[ inx ] = (byte) (bytes[ inx ] ^ (0x0f & dezi[ inx % 4 ]));
        }

        return new String(bytes);
    }

    public static String getPreferenceString(String prefkey)
    {
        return getSharedPrefs().getString(prefkey, null);
    }

    public static Map<String, Object> getAllPreferences(String prefix)
    {
        Map<String, Object> result = new HashMap<>();

        Map<String, ?> prefs = getSharedPrefs().getAll();

        for (Map.Entry<String, ?> entry : prefs.entrySet())
        {
            if (! entry.getKey().startsWith(prefix)) continue;

            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static void removePreference(String key)
    {
        Simple.getSharedPrefs().edit().remove(key).commit();
    }

    public static void removeAllPreferences(String prefix)
    {
        SharedPreferences sp = getSharedPrefs();

        Map<String, ?> prefs = sp.getAll();

        for (Map.Entry<String, ?> entry : prefs.entrySet())
        {
            if (!entry.getKey().startsWith(prefix)) continue;

            sp.edit().remove(entry.getKey()).apply();
        }
    }

    public static View removeFromParent(View view)
    {
        ((ViewGroup) view.getParent()).removeView(view);

        return view;
    }

    public static void makeRoundedCorners(View view, boolean solid)
    {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(4);

        if (solid)
        {
            shape.setColor(CommonConfigs.PreferenceTextButtonColor);
        }
        else
        {
            shape.setStroke(2, CommonConfigs.PreferenceTextButtonColor);
        }

        view.setPadding(12, 10, 10, 12);
        view.setBackground(shape);
    }

    public static void makeStandardButton(View view, boolean preferred)
    {
        makeRoundedCorners(view, preferred);

        view.setLayoutParams(Simple.layoutParamsMM(10, 0, 10, 0));

        if (view instanceof TextView)
        {
            ((TextView) view).setTextColor(preferred ? Color.WHITE : Color.BLACK);
            ((TextView) view).setTextSize(Simple.getPreferredTextSize());
            ((TextView) view).setAllCaps(true);
        }
    }

    public static void makeToast(String text)
    {
        Toast.makeText(anyContext, "Nix <type> configured.", Toast.LENGTH_LONG).show();
    }

    public static void dumpDirectory(String path)
    {
        dumpDirectory(new File(path));
    }

    public static void dumpDirectory(File path)
    {
        File[] files = path.listFiles();

        Log.d(LOGTAG, "dumpDirectory: " + path.getAbsoluteFile());

        for (File file : files)
        {
            Log.d(LOGTAG, "dumpDirectory: => " + file.getAbsoluteFile());
        }
    }

    @Nullable
    public static String readDatadirFile(String filename)
    {
        try
        {
            FileInputStream inputStream;
            inputStream = anyContext.openFileInput(filename);
            int size = (int) inputStream.getChannel().size();
            byte[] content = new byte[ size ];
            int xfer = inputStream.read(content);
            inputStream.close();

            return new String(content, 0, xfer);
        }
        catch (FileNotFoundException ignore)
        {
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }

        return null;
    }

    public static boolean writeDatadirFile(String filename, String content)
    {
        if (content != null)
        {
            try
            {
                FileOutputStream outputStream;
                outputStream = anyContext.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(content.getBytes());
                outputStream.close();

                return true;
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return false;
    }

    //endregion All purpose simple methods

    //region All purpose simple getters

    public static boolean hasNavigationBar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            return ! ViewConfiguration.get(anyContext).hasPermanentMenuKey();
        }

        return false;
    }

    public static float getDeviceTextSize(float textsize)
    {
        return (textsize / getDeviceDPI()) * 160;
    }

    public static int getDeviceDPI()
    {
        return anyContext.getResources().getDisplayMetrics().densityDpi;
    }

    public static String getUUID()
    {
        return UUID.randomUUID().toString();
    }

    public static SharedPreferences getSharedPrefs()
    {
        return PreferenceManager.getDefaultSharedPreferences(anyContext);
    }

    public static NotificationManager getNotificationManager()
    {
        return (NotificationManager) anyContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static InputMethodManager getInputMethodManager ()
    {
        return (InputMethodManager) anyContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static String getAppName()
    {
        return anyContext.getString(anyContext.getApplicationInfo().labelRes);
    }

    public static String getPackageName()
    {
        return anyContext.getPackageName();
    }

    public static String getDefaultEmail()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("mailto:"));
        ResolveInfo res = anyContext.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

    @Nullable
    public static String getMacAddress()
    {
        if (anyContext == null) return null;

        WifiManager wifiManager = (WifiManager) anyContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public static int getDeviceWidth()
    {
        if (anyContext == null) return 0;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getDeviceHeight()
    {
        if (anyContext == null) return 0;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static float getPreferredEditSize()
    {
        if (anyContext == null) return 16f;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();

        int pixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        if (pixels < 500) return 17f;

        return 24f;
    }

    public static float getPreferredTextSize()
    {
        if (anyContext == null) return 17f;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();

        int pixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        if (pixels < 500) return 17f;

        return 18f;
    }

    public static int getActionBarHeight()
    {
        TypedValue tv = new TypedValue();

        if (anyContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            DisplayMetrics dm = anyContext.getResources().getDisplayMetrics();
            return TypedValue.complexToDimensionPixelSize(tv.data, dm);
        }

        return 64;
    }

    public static int getStatusBarHeight()
    {
        int resourceId = anyContext.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) return anyContext.getResources().getDimensionPixelSize(resourceId);

        return 20;
    }

    public static int getNavigationBarHeight()
    {
        int resourceId = anyContext.getResources()
                .getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) return anyContext.getResources().getDimensionPixelSize(resourceId);

        return 64;
    }

    @SuppressWarnings("SameReturnValue")
    public static String getDeviceName()
    {
        return android.os.Build.MODEL;
    }

    public static byte[] getUUIDBytes(String uuid)
    {
        String uuidstr = uuid.replace("-", "");
        return getHexStringToBytes(uuidstr);
    }

    public static String getUUIDString(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    public static String getHexBytesToString(byte[] bytes)
    {
        return getHexBytesToString(bytes, 0, bytes.length);
    }

    public static String getHexBytesToString(byte[] bytes, int offset, int length)
    {
        if (bytes == null) return null;

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[ length << 1 ];

        for (int inx = offset; inx < (length + offset); inx++)
        {
            //noinspection PointlessArithmeticExpression
            hexChars[ ((inx - offset) << 1) + 0 ] = hexArray[ (bytes[ inx ] >> 4) & 0x0f ];
            //noinspection PointlessBitwiseExpression
            hexChars[ ((inx - offset) << 1) + 1 ] = hexArray[ (bytes[ inx ] >> 0) & 0x0f ];
        }

        return String.valueOf(hexChars);
    }

    public static byte[] getHexStringToBytes(String hexstring)
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

    public static String getAllInput(InputStream input)
    {
        StringBuilder string = new StringBuilder();
        byte[] buffer = new byte[ 4096 ];
        int xfer;

        try
        {
            while ((xfer = input.read(buffer)) > 0)
            {
                string.append(new String(buffer, 0, xfer));
            }
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return string.toString();
    }

    public static Bitmap getBitmapFromResource(int resid)
    {
        return BitmapFactory.decodeResource(anyContext.getResources(), resid);
    }

    public static int getIconResourceId(String iconname)
    {
        return anyContext.getResources().getIdentifier(
                iconname, "drawable", anyContext.getPackageName());
    }

    @Nullable
    public static String getFirstMatch(String regex, String content)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (! matcher.find()) return null;

        return matcher.group(1);
    }

    public static byte[] getCRC16ccittCompute(byte[] byteArray, int length)
    {
        int crc = 0xffff;

        byte byt;
        boolean bitbyt;
        boolean bitcrc;

        for (int inx = 0; inx < length; inx++)
        {
            byt = byteArray[ inx ];

            for (int cnt = 0; cnt < 8; cnt++)
            {
                bitbyt = (((byt >> (7 - cnt)) & 1) == 1);
                bitcrc = (((crc >> 15) & 1) == 1);

                crc <<= 1;

                if (bitcrc ^ bitbyt) crc ^= 4129;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(crc & 0xffff);

        return new byte[]{buffer.array()[0], buffer.array()[1]};
    }

    public static byte[] getCRC16ccittEmbed(byte[] byteArray)
    {
        byte[] cc = getCRC16ccittCompute(byteArray, byteArray.length - 2);

        byteArray[ byteArray.length - 2 ] = cc[ 0 ];
        byteArray[ byteArray.length - 1 ] = cc[ 1 ];

        return byteArray;
    }

    public static boolean getCRC16ccittVerify(byte[] bytes)
    {
        byte[] newCrc = getCRC16ccittCompute(bytes, bytes.length - 2);

        return !((newCrc[ 0 ] != bytes[ bytes.length - 2 ]) ||
                (newCrc[ 1 ] != bytes[ bytes.length - 1 ]));
    }

    public static int getIntFromLEByteArray(byte[] data)
    {
        return getIntFromByteArray(data, true);
    }

    public static int getIntFromBEByteArray(byte[] data)
    {
        return getIntFromByteArray(data, false);
    }

    public static int getIntFromByteArray(byte[] data, boolean littleEndian)
    {
        int returnValue = 0;

        int start = littleEndian ? data.length - 1 : 0;

        for (int offset = 0; offset < data.length; offset++)
        {
            int index = Math.abs(start - offset);

            returnValue |= (data[ index ] & 0xff) << (index << 3);
        }

        return returnValue;
    }

    public static byte[] getReversedBytes(byte[] array)
    {
        int len = array.length;

        byte[] reversedArray = new byte[ len ];

        for (int index = 0; index < len; index++)
        {
            reversedArray[ (len - index) - 1 ] = array[index];
        }

        return reversedArray;
    }

    //endregion All purpose simple getters

    //region JSON stuff

    @Nullable
    public static JSONObject JSONClone(JSONObject json)
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

    public static void JSONremove(JSONObject json, String key)
    {
        json.remove(key);
    }

    public static void JSONput(JSONObject json, String key, Object val)
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

    @Nullable
    public static String JSONgetString(JSONObject json, String key)
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
    public static JSONArray JSONgetArray(JSONObject json, String key)
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
    public static JSONObject JSONgetObject(JSONObject json, String key)
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
    public static JSONObject JSONgetObject(JSONArray json, int index)
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
    public static String JSON2String(JSONObject jsonObject)
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

    public static String JSONdefuck(String json)
    {
        //
        // I hate slash escaping.
        //

        return json.replace("\\/","/");
    }

    //endregion JSON stuff

    //region Date and time

    private static final String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO8601DATENOSECS = "yyyy-MM-dd'T'HH:mm'Z'";

    public static long nowAsTimeStamp()
    {
        return new Date().getTime();
    }

    public static String nowAsISO()
    {
        return timeStampAsISO(nowAsTimeStamp());
    }

    @Nullable
    public static String timeStampAsISO(long timestamp)
    {
        DateFormat df = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(timestamp));
    }

    public static long getTimeStampFromISO(String isodate)
    {
        try
        {
            SimpleDateFormat df = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(isodate).getTime();
        }
        catch (ParseException ignore)
        {
            //
            // Date missing seconds?
            //
        }

        try
        {
            SimpleDateFormat df = new SimpleDateFormat(ISO8601DATENOSECS, Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(isodate).getTime();
        }
        catch (ParseException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return 0;
    }

    public static String get24HHourFromISO(String isodate)
    {
        return get24HHourFromTimeStamp(getTimeStampFromISO(isodate));
    }

    public static String get24HHourFromTimeStamp(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("HH", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(timeStamp));
    }

    public static String getLocal24HTimeFromISO(String isodate)
    {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date(getTimeStampFromISO(isodate)));
    }

    public static String getLocalDateFromISO(String isodate)
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date(getTimeStampFromISO(isodate)));
    }

    public static int getSecondsAgoFromISO(String isodate)
    {
        long iso = getTimeStampFromISO(isodate);
        long now = new Date().getTime();

        iso /= 1000;
        now /= 1000;

        return (int) (now - iso);
    }

    public static int getDaysAgoFromISO(String isodate)
    {
        long iso = getTimeStampFromISO(isodate);
        long now = new Date().getTime();

        iso /= 86400 * 1000;
        now /= 86400 * 1000;

        return (int) (now - iso);
    }

    public static long getLocalTimeToUTC(long timestamp)
    {
        try
        {
            String isodate = timeStampAsISO(timestamp);
            SimpleDateFormat df = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
            df.setTimeZone(TimeZone.getDefault());
            return df.parse(isodate).getTime();
        }
        catch (ParseException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return 0L;
    }

    //endregion Date and time

    //region Layout params

    public static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    public static ViewGroup.LayoutParams layoutParamsWW()
    {
        return new ViewGroup.LayoutParams(WC, WC);
    }

    public static ViewGroup.LayoutParams layoutParamsMM()
    {
        return new ViewGroup.LayoutParams(MP, MP);
    }

    public static ViewGroup.LayoutParams layoutParamsMW()
    {
        return new ViewGroup.LayoutParams(MP, WC);
    }

    public static ViewGroup.LayoutParams layoutParamsWM()
    {
        return new ViewGroup.LayoutParams(WC, MP);
    }

    public static FrameLayout.LayoutParams layoutParamsWW(int gravity)
    {
        return new FrameLayout.LayoutParams(WC, WC, gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsMM(int gravity)
    {
        return new FrameLayout.LayoutParams(MP, MP, gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsMW(int gravity)
    {
        return new FrameLayout.LayoutParams(MP, WC, gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsWM(int gravity)
    {
        return new FrameLayout.LayoutParams(WC, MP, gravity);
    }

    public static LinearLayout.LayoutParams layoutParamsWW(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WC, WC);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsMM(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MP, MP);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsMW(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MP, WC);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsWM(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WC, MP);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    //endregion Layout params
}
