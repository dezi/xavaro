package de.xavaro.android.common;

import android.content.IntentFilter;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.io.File;

public class BatteryManager
{
    private static final String LOGTAG = BatteryManager.class.getSimpleName();

    private static final ArrayList<BatteryManagerCallback> callbacks = new ArrayList<>();
    private static final JSONObject batteryStatus = new JSONObject();

    private static long nextCheck;
    private static int sequence;
    private static int lastStatus;
    private static int lastPercent;

    public static void commTick()
    {
        long now = Simple.nowAsTimeStamp();
        if (now < nextCheck) return;
        nextCheck = now + 3 * 1000;
        sequence++;

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent status = Simple.getAnyContext().registerReceiver(null, batteryLevelFilter);

        if (putBatteryStatus(status) || ((sequence % 200) == 0))
        {
            if (Simple.getSharedPrefBoolean("monitors.battery.record"))
            {
                saveBatteryStatus();
            }
        }

        Simple.makePost(doCallbacks);

        if ((sequence % 20) == 0) checkWarnings();
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

    public static void unsubscribeAll()
    {
        synchronized (callbacks)
        {
            callbacks.clear();
        }
    }

    public static JSONObject getBatteryStatus()
    {
        synchronized (batteryStatus)
        {
            return Json.clone(batteryStatus);
        }
    }

    private static void checkWarnings()
    {
        int status = Json.getInt(batteryStatus, "status");
        int plugged = Json.getInt(batteryStatus, "plugged");

        if (status == android.os.BatteryManager.BATTERY_STATUS_CHARGING)
        {
            //
            // Check if an assistance warning was send.
            //

            String date = Simple.getSharedPrefString("monitors.battery.lastassist");

            if (date != null)
            {
                // assist clear.
            }

            //
            // Reset all warning times.
            //

            Simple.removeSharedPref("monitors.battery.lastremind");
            Simple.removeSharedPref("monitors.battery.lastwarn");
            Simple.removeSharedPref("monitors.battery.lastassist");

            return;
        }

        int percent = Json.getInt(batteryStatus, "percent");

        int remindval = 0;
        int warnval = 0;
        int assistval = 0;

        String remind = Simple.getSharedPrefString("monitors.battery.remind");
        if ((remind != null) && ! remind.equals("never")) remindval = Integer.parseInt(remind);

        String warn = Simple.getSharedPrefString("monitors.battery.warn");
        if ((warn != null) && ! warn.equals("never")) warnval = Integer.parseInt(warn);

        String assist = Simple.getSharedPrefString("monitors.battery.assistance");
        if ((assist != null) && ! assist.equals("never")) assistval = Integer.parseInt(assist);

        if ((percent > remindval) && (percent > warnval) && (percent > assistval)) return;

        //
        // Some warnings might be due.
        //

        int repeatval = 0;
        String repeat = Simple.getSharedPrefString("monitors.battery.repeat");
        if ((repeat != null) && ! repeat.equals("once")) repeatval = Integer.parseInt(repeat);
        repeatval *= 60 * 1000;

        if ((percent <= remindval) && (percent > warnval)
                && (plugged != android.os.BatteryManager.BATTERY_PLUGGED_USB))
        {
            String date = Simple.getSharedPrefString("monitors.battery.lastremind");
            String ddue = Simple.timeStampAsISO(Simple.nowAsTimeStamp() - repeatval);

            if ((date == null) || ((repeatval > 0) && (date.compareTo(ddue) <= 0)))
            {
                Speak.speak(Simple.getTrans(R.string.battery_manager_remind), 50);
                Simple.setSharedPrefString("monitors.battery.lastremind", Simple.nowAsISO());
            }
        }

        if (percent <= warnval)
        {
            String date = Simple.getSharedPrefString("monitors.battery.lastwarn");
            String ddue = Simple.timeStampAsISO(Simple.nowAsTimeStamp() - repeatval);

            if ((date == null) || ((repeatval > 0) && (date.compareTo(ddue) <= 0)))
            {
                Speak.speak(Simple.getTrans(R.string.battery_manager_warn), 100);
                Simple.setSharedPrefString("monitors.battery.lastwarn", Simple.nowAsISO());
            }
        }

        if (percent <= assistval)
        {
            String date = Simple.getSharedPrefString("monitors.battery.lastassist");
            String ddue = Simple.timeStampAsISO(Simple.nowAsTimeStamp() - repeatval);

            if ((date == null) || ((repeatval > 0) && (date.compareTo(ddue) <= 0)))
            {
                // assist warn.

                Simple.setSharedPrefString("monitors.battery.lastassist", Simple.nowAsISO());
            }
        }
    }

    private static boolean putBatteryStatus(Intent intent)
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

        Log.d(LOGTAG, "putBatteryStatus:" + percent + "% " + stag + " " + ptag);

        boolean changed = (lastStatus != status) || (lastPercent != percent);
        lastStatus = status;
        lastPercent = percent;

        return changed;
    }

    private static void saveBatteryStatus()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String filename = String.format("battery.%04d.%02d.%02d.json", year, month, day);
        File statusfile = Simple.getIdentityFile(filename);
        String content = Simple.getFileContent(statusfile);
        JSONArray values = (content != null) ? Json.fromStringArray(content) : new JSONArray();

        JSONObject status = getBatteryStatus();
        JSONObject saveit = new JSONObject();

        Json.put(saveit, "dst", Simple.nowAsISO());
        Json.copy(saveit, "sta", status, "status");
        Json.copy(saveit, "plu", status, "plugged");
        Json.copy(saveit, "sca", status, "scale");
        Json.copy(saveit, "raw", status, "rawlevel");

        Json.put(values, saveit);

        Simple.putFileContent(statusfile, Json.toPretty(values));
    }

    private static final Runnable doCallbacks = new Runnable()
    {
        @Override
        public void run()
        {
            JSONObject status = getBatteryStatus();

            synchronized (callbacks)
            {
                for (BatteryManagerCallback callback : callbacks)
                {
                    callback.onBatteryStatus(status);
                }
            }
        }
    };

    public interface BatteryManagerCallback
    {
        void onBatteryStatus(JSONObject status);
    }
}
