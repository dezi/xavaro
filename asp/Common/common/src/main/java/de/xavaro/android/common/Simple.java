package de.xavaro.android.common;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

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

    //region All purpose simple methods

    public static View removeFromParent(View view)
    {
        ((ViewGroup) view.getParent()).removeView(view);

        return view;
    }

    //endregion All purpose simple methods

    //region All purpose simple getters

    public static Activity getContext()
    {
        return Simple.appContext;
    }

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

    //region Frame layout params

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

    //endregion Frame layout params
}
