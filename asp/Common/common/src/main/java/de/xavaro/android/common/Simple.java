package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("unused")
public class Simple
{
    private static final String LOGTAG = Simple.class.getSimpleName();

    //region Initialisation

    private static Activity appContext;
    private static Handler appHandler;

    public static void setContext(Activity context)
    {
        Simple.appContext = context;
        Simple.appHandler = new Handler();
    }

    public static Activity getContext()
    {
        return Simple.appContext;
    }

    //endregion Initialisation

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

    public static void launchApp(String packagename)
    {
        if (appContext == null) return;

        CommonStatic.addOneShotApp(packagename);

        try
        {
            Intent launchIntent = appContext.getPackageManager().getLaunchIntentForPackage(packagename);
            appContext.startActivity(launchIntent);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static void installAppFromPlaystore(String packagename)
    {
        if (appContext == null) return;

        CommonStatic.addOneShotApp(CommonConfigs.packagePlaystore);

        try
        {
            Intent goToMarket = new Intent(Intent.ACTION_VIEW);
            goToMarket.setData(Uri.parse("market://details?id=" + packagename));
            appContext.startActivity(goToMarket);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
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

    //endregion Application stuff

    //region All purpose simple methods

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


    //endregion All purpose simple methods

    //region All purpose simple getters

    @Nullable
    public static String getMacAddress()
    {
        if (appContext == null) return null;

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
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

    @SuppressWarnings("SameReturnValue")
    public static String getDeviceName()
    {
        return android.os.Build.MODEL;
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

    public static FrameLayout.LayoutParams layoutParamsWW(int gravity)
    {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsMM(int gravity)
    {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsMW(int gravity)
    {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsWM(int gravity)
    {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                gravity);
    }

    public static ViewGroup.LayoutParams layoutParamsWW()
    {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static ViewGroup.LayoutParams layoutParamsMM()
    {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static ViewGroup.LayoutParams layoutParamsMW()
    {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static ViewGroup.LayoutParams layoutParamsWM()
    {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static LinearLayout.LayoutParams layoutParamsWW(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsMM(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsMW(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsWM(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    //endregion Layout params
}
