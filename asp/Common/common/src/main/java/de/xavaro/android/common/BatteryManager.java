package de.xavaro.android.common;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class BatteryManager
{
    private static final String LOGTAG = BatteryManager.class.getSimpleName();

    private static final ArrayList<BatteryManagerCallback> callbacks = new ArrayList<>();
    private static final JSONObject batteryStatus = new JSONObject();
    private static boolean registered;

    public static void registerReceiver()
    {
        if (! registered)
        {
            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Simple.getAppContext().registerReceiver(batteryLevelReceiver, batteryLevelFilter);

            registered = true;
        }
    }

    public static void unregisterReceiver()
    {
        if (registered)
        {
            Simple.getAppContext().unregisterReceiver(batteryLevelReceiver);

            registered = false;
        }
    }

    public static void subscribe(BatteryManagerCallback callback)
    {
        synchronized (callbacks)
        {
            if (! callbacks.contains(callback)) callbacks.add(callback);
        }
    }

    public static void unsubscribe(BatteryManagerCallback callback)
    {
        synchronized (callbacks)
        {
            if (callbacks.contains(callback)) callbacks.remove(callback);
        }
    }

    public static JSONObject getBatteryStatus()
    {
        synchronized (batteryStatus)
        {
            return Json.clone(batteryStatus);
        }
    }

    private final static BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            int status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1);
            int plugged = intent.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);

            int scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
            int rawlevel = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int percent = ((rawlevel >= 0) && (scale > 0)) ? (rawlevel * 100) / scale : -1;

            String stag = "none";
            String ptag = "none";

            // @formatter:off
            if (status == android.os.BatteryManager.BATTERY_STATUS_CHARGING)     stag = "charging";
            if (status == android.os.BatteryManager.BATTERY_STATUS_DISCHARGING)  stag = "discharging";
            if (status == android.os.BatteryManager.BATTERY_STATUS_FULL)         stag = "full";
            if (status == android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING) stag = "notcharging";
            if (status == android.os.BatteryManager.BATTERY_STATUS_UNKNOWN)      stag = "unknown";
            // @formatter:on

            // @formatter:off
            if (plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC)       ptag = "ac";
            if (plugged == android.os.BatteryManager.BATTERY_PLUGGED_USB)      ptag = "usb";
            if (plugged == android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS) ptag = "wireless";
            // @formatter:on

            synchronized (batteryStatus)
            {
                Json.put(batteryStatus, "status", status);
                Json.put(batteryStatus, "plugged", plugged);
                Json.put(batteryStatus, "scale", scale);
                Json.put(batteryStatus, "rawlevel", rawlevel);
                Json.put(batteryStatus, "percent", percent);
                Json.put(batteryStatus, "statustag", stag);
                Json.put(batteryStatus, "pluggedtag", ptag);
            }

            Log.d(LOGTAG, "onReceive:" + percent + "% " + stag + " " + ptag);

            synchronized (callbacks)
            {
                for (BatteryManagerCallback callback : callbacks)
                {
                    callback.onBatteryStatus(Json.clone(batteryStatus));
                }
            }
        }
    };

    public interface BatteryManagerCallback
    {
        void onBatteryStatus(JSONObject status);
    }
}
