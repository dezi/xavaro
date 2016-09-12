package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.ActivityManager;
import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.EventManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class HealthThermo extends HealthBase
{
    private static final String LOGTAG = HealthThermo.class.getSimpleName();

    private static HealthThermo instance;

    public static HealthThermo getInstance()
    {
        if (instance == null) instance = new HealthThermo();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    @Override
    public void onBluetoothReceivedData(String deviceName, JSONObject data)
    {
        Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());

        if (! data.has("thermo")) return;

        //
        // The results come in unordered.
        //

        lastRecord = Json.getObject(data, "thermo");
        if (lastRecord == null) return;

        String type = Json.getString(lastRecord, "type");

        if (Simple.equals(type, "ThermoMeasurement"))
        {
            String date = Json.getString(lastRecord, "dts");
            if (date == null) return;

            double temperature = Json.getDouble(lastRecord, "tmp");

            if ((lastDts == null) || (lastDts.compareTo(date) <= 0))
            {
                lastTmp = temperature;
                lastDts = date;

                handler.removeCallbacks(messageSpeaker);
                handler.postDelayed(messageSpeaker, 500);
            }
        }
    }

    JSONObject lastRecord;
    String lastDts;
    double lastTmp;

    private void informAssistance(int resid)
    {
        long last = Simple.getTimeStamp(lastDts);
        long now = Simple.nowAsTimeStamp();

        if ((now - last) > 1000 * 1000)
        {
            Log.d(LOGTAG,"informAssistance: outdated:" + lastDts);
            return;
        }

        if (! Simple.getSharedPrefBoolean("alertgroup.enable")) return;
        if (! Simple.getSharedPrefBoolean("health.thermo.alert.alertgroup")) return;

        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        String name = Simple.getOwnerName();
        String bval = "" + lastTmp;

        String text = Simple.getTrans(R.string.health_thermo_alert, name, bval)
                + " " + Simple.getTrans(resid);

        JSONObject assistMessage = new JSONObject();
        Json.put(assistMessage, "uuid", Simple.getUUID());
        Json.put(assistMessage, "message", text);
        Json.put(assistMessage, "priority", "alertinfo");

        ChatManager.getInstance().sendOutgoingMessage(groupIdentity, assistMessage);

        Log.d(LOGTAG, "informAssistance: send alertinfo:" + text);
    }

    private void evaluateEvents()
    {
        JSONArray events = EventManager.getComingEvents("webapps.medicator");
        if (events == null) return;

        long now = Simple.nowAsTimeStamp();

        for (int inx = 0; inx < events.length(); inx++)
        {
            JSONObject event = Json.getObject(events, inx);

            if ((event == null) || Json.getBoolean(event, "taken")) continue;

            String date = Json.getString(event, "date");
            String medication = Json.getString(event, "medication");

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZO")) continue;

            //
            // Check event and measurement dates.
            //

            long dts = Simple.getTimeStamp(date);
            if (Math.abs(now - dts) > 2 * 3600 * 1000) continue;

            long mts = Simple.getTimeStamp(lastDts);
            if (Math.abs(dts - mts) > 2 * 3600 * 1000) continue;

            //
            // Event is suitable.
            //

            Json.put(event, "taken", true);
            Json.put(event, "takendate", lastDts);
            Json.put(event, "temp", lastTmp);

            EventManager.updateComingEvent("webapps.medicator", event);

            Log.d(LOGTAG, "evaluateEvents: updated=" + event.toString());

            break;
        }

        NotifyManager.removeNotification("medicator.take.bloodoxygen");
        Simple.makePost(CommonConfigs.UpdateNotifications);
    }

    private void evaluateMessage()
    {
        if (lastRecord == null) return;
        String type = Json.getString(lastRecord, "type");
        if (! Simple.equals(type, "ThermoMeasurement")) return;

        String sm = Simple.getTrans(R.string.health_thermo_spoken, lastTmp);
        String am = Simple.getTrans(R.string.health_thermo_activity, lastTmp);

        Speak.speak(sm);
        ActivityManager.recordActivity(am);

        evaluateEvents();

        if (!Simple.getSharedPrefBoolean("health.thermo.alert.enable")) return;

        try
        {
            String lowstr = Simple.getSharedPrefString("health.thermo.alert.lowtemp");
            Double low = Simple.parseDouble(lowstr);

            if (low >= lastTmp)
            {
                Speak.speak(Simple.getTrans(R.string.health_thermo_lowtemp));
                ActivityManager.recordAlert(R.string.health_thermo_lowtemp);
                informAssistance(R.string.health_thermo_lowtemp);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            String highstr = Simple.getSharedPrefString("health.thermo.alert.hightemp");
            Double high = Simple.parseDouble(highstr);

            if (high <= lastTmp)
            {
                Speak.speak(Simple.getTrans(R.string.health_thermo_hightemp));
                ActivityManager.recordAlert(R.string.health_thermo_hightemp);
                informAssistance(R.string.health_thermo_hightemp);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private Runnable messageSpeaker = new Runnable()
    {
        @Override
        public void run()
        {
            evaluateMessage();
        }
    };
}
