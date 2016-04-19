package de.xavaro.android.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryLevel
{
    private static final String LOGTAG = BatteryLevel.class.getSimpleName();

    /*
    private void batteryLevel()
    {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                batterLevel.setText("Battery Level Remaining: " + level + "%");
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
    */

    public static void batteryLevel()
    {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                //context.unregisterReceiver(this);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                int level = -1;
                if (rawlevel >= 0 && scale > 0)
                {
                    level = (rawlevel * 100) / scale;
                }

                String cmess = "";
                String pmess = "";

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) cmess = "BATTERY_STATUS_CHARGING";
                if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) cmess = "BATTERY_STATUS_DISCHARGING";
                if (status == BatteryManager.BATTERY_STATUS_FULL) cmess = "BATTERY_STATUS_FULL";
                if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) cmess = "BATTERY_STATUS_NOT_CHARGING";
                if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) cmess = "BATTERY_STATUS_UNKNOWN";

                if (plugged == BatteryManager.BATTERY_PLUGGED_AC) pmess = "BATTERY_PLUGGED_AC";
                if (plugged == BatteryManager.BATTERY_PLUGGED_USB) pmess = "BATTERY_PLUGGED_USB";
                if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) pmess = "BATTERY_PLUGGED_WIRELESS";

                Log.d(LOGTAG, "Battery Level Remaining: " + status + "="  + level + "%" + cmess + "=" + pmess + ":" + plugged);
            }
        };

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Simple.getAppContext().registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

    public static boolean isCharging()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = Simple.getAppContext().registerReceiver(null, ifilter);

        if (batteryStatus != null)
        {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);


            int rawlevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int level = -1;
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }
            Log.d(LOGTAG,"==========================" +  status + "=" + rawlevel + "=" + scale + "=" + level + "%");
        }

        return false;
    }

}
