package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.TypedValue;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        if (appContext == null) return;

        InputMethodManager imm = (InputMethodManager) appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view)
    {
        if (appContext == null) return;

        final View runview = view;

        appHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager imm = (InputMethodManager) appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(runview, 0);
            }
        }, 500);
    }

    //endregion Keyboard stuff

    //region Application stuff

    public static void installAppFromPlaystore(String packagename)
    {
        if (appContext == null) return;

        Intent goToMarket = new Intent(Intent.ACTION_VIEW);
        goToMarket.setData(Uri.parse("market://details?id=" + packagename));

        ProcessManager.launchIntent(appContext, goToMarket);
    }

    public static void uninstallApp(String packagename)
    {
        if (appContext == null) return;

        try
        {
            Uri packageUri = Uri.parse("package:" + packagename);
            Intent unInstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            appContext.startActivity(unInstall);
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

    public static boolean isAppInstalled(String packageName)
    {
        if (appContext == null) return false;

        try
        {
            ApplicationInfo appInfo = appContext.getPackageManager().getApplicationInfo(packageName, 0);

            return (appInfo != null);
        }
        catch (PackageManager.NameNotFoundException ignore)
        {
        }

        return false;
    }

    public static boolean isGCMInitialized()
    {
        return (getGCMapeyki() != null);
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

    //endregion All purpose simple methods

    //region All purpose simple getters

    public static SharedPreferences getSharedPrefs()
    {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    public static String getAppName()
    {
        return appContext.getString(appContext.getApplicationInfo().labelRes);
    }

    public static String getPackageName()
    {
        return appContext.getPackageName();
    }

    @Nullable
    public static String getMacAddress()
    {
        if (appContext == null) return null;

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    @Nullable
    public static String getGCMToken()
    {
        return CommonStatic.gcm_token;
    }

    public static int getDeviceWidth()
    {
        if (appContext == null) return 0;

        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getDeviceHeight()
    {
        if (appContext == null) return 0;

        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static float getPreferredEditSize()
    {
        if (appContext == null) return 16f;

        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();

        int pixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        if (pixels < 500) return 17f;

        return 24f;
    }

    public static float getPreferredTextSize()
    {
        if (appContext == null) return 17f;

        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();

        int pixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        if (pixels < 500) return 17f;

        return 18f;
    }

    public static int getActionBarHeight()
    {
        if (appContext != null)
        {
            TypedValue tv = new TypedValue();

            if (appContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                DisplayMetrics dm = appContext.getResources().getDisplayMetrics();
                return TypedValue.complexToDimensionPixelSize(tv.data, dm);
            }
        }

        return 64;
    }

    public static int getStatusBarHeight()
    {
        if (appContext != null)
        {
            int resourceId = appContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) return appContext.getResources().getDimensionPixelSize(resourceId);
        }

        return 20;
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

    @Nullable
    public static String getFirstMatch(String regex, String content)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (! matcher.find()) return null;

        return matcher.group(1);
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

    public static String nowAsISO()
    {
        return timeStampAsISO(new Date().getTime());
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
