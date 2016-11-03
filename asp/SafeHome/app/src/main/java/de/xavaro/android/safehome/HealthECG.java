package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.ActivityOldManager;
import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.EventManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class HealthECG extends HealthBase
{
    private static final String LOGTAG = HealthECG.class.getSimpleName();

    private static HealthECG instance;

    public static HealthECG getInstance()
    {
        if (instance == null) instance = new HealthECG();

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

        if (! data.has("ecg")) return;

        //
        // The results come in unordered.
        //

        lastRecord = Json.getObject(data, "ecg");
        if (lastRecord == null) return;

        String type = Json.getString(lastRecord, "type");

        if (Simple.equals(type, "ECGMeasurement"))
        {
            String date = Json.getString(lastRecord, "dts");
            if (date == null) return;

            int saturation = Json.getInt(lastRecord, "sat");
            int pulse = Json.getInt(lastRecord, "pls");

            if ((lastDts == null) || (lastDts.compareTo(date) <= 0))
            {
                lastSat = saturation;
                lastPls = pulse;
                lastDts = date;

                handler.removeCallbacks(messageSpeaker);
                handler.postDelayed(messageSpeaker, 500);
            }
        }
    }

    private JSONObject lastRecord;
    private String lastDts;
    private int lastSat;
    private int lastPls;

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
        if (! Simple.getSharedPrefBoolean("health.ecg.alert.alertgroup")) return;

        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        String name = Simple.getOwnerName();
        String bval = "" + lastSat;
        String puls = "" + lastPls;

        String text = Simple.getTrans(R.string.health_oxy_alert, name, bval, puls)
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

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZE")) continue;

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
            Json.put(event, "saturation", lastSat);
            Json.put(event, "puls", lastPls);

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
        if (! Simple.equals(type, "ECGMeasurement")) return;

        String sm = Simple.getTrans(R.string.health_oxy_spoken, lastSat, lastPls);
        String am = Simple.getTrans(R.string.health_oxy_activity, lastSat, lastPls);

        Speak.speak(sm);
        ActivityOldManager.recordActivity(am);

        evaluateEvents();

        if (!Simple.getSharedPrefBoolean("health.oxy.alert.enable")) return;

        try
        {
            int low = Simple.getSharedPrefInt("health.oxy.alert.lowsat");

            if (low >= lastSat)
            {
                Speak.speak(Simple.getTrans(R.string.health_oxy_lowsat));
                ActivityOldManager.recordAlert(R.string.health_oxy_lowsat);
                informAssistance(R.string.health_oxy_lowsat);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            int low = Simple.getSharedPrefInt("health.oxy.alert.lowpls");

            if (low >= lastPls)
            {
                Speak.speak(Simple.getTrans(R.string.health_oxy_lowpls));
                ActivityOldManager.recordAlert(R.string.health_oxy_lowpls);
                informAssistance(R.string.health_oxy_lowpls);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            int high = Simple.getSharedPrefInt("health.oxy.alert.highpls");

            if (high <= lastPls)
            {
                Speak.speak(Simple.getTrans(R.string.health_oxy_highpls));
                ActivityOldManager.recordAlert(R.string.health_oxy_highpls);
                informAssistance(R.string.health_oxy_highpls);
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
