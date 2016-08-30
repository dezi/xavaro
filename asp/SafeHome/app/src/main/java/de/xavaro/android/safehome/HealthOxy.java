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

public class HealthOxy extends HealthBase
{
    private static final String LOGTAG = HealthOxy.class.getSimpleName();

    private static HealthOxy instance;

    public static HealthOxy getInstance()
    {
        if (instance == null) instance = new HealthOxy();

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

        if (! data.has("oxy")) return;

        //
        // The results come in unordered.
        //

        lastRecord = Json.getObject(data, "oxy");
        if (lastRecord == null) return;

        String type = Json.getString(lastRecord, "type");

        if (Simple.equals(type, "OxyMeasurement"))
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

    JSONObject lastRecord;
    String lastDts;
    int lastSat;
    int lastPls;

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
        if (! Simple.getSharedPrefBoolean("health.oxy.alert.alertgroup")) return;

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

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZO")) continue;

            long dts = Simple.getTimeStamp(date);

            if (Math.abs(now - dts) > 2 * 3600 * 1000) continue;
            
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

        //
        // Check for demo mode take pills after
        // oxygen measurement as the trigger.
        //

        if (Simple.getSharedPrefBoolean("developer.demomode.takepills"))
        {
            for (int inx = 0; inx < events.length(); inx++)
            {
                JSONObject event = Json.getObject(events, inx);
                if (event == null) continue;

                String date = Json.getString(event, "date");
                String medication = Json.getString(event, "medication");

                if ((date == null) || (medication == null)) continue;
                String mediform = medication.substring(medication.length() - 3, medication.length());
                if (mediform.startsWith("ZZ")) continue;

                long dts = Simple.getTimeStamp(date);

                if ((dts < now) || (Math.abs(now - dts) > 24 * 3600 * 1000)) continue;

                Json.put(event, "date", Simple.timeStampAsISO(now + 5 * 60 * 1000));
                Json.put(event, "taken", false);
                Json.put(event, "completed", false);
                Json.put(event, "reminded", 0);

                EventManager.updateComingEvent("webapps.medicator", event);

                Log.d(LOGTAG, "evaluateEvents: faked=" + event.toString());
            }
        }
    }

    private void evaluateMessage()
    {
        if (lastRecord == null) return;
        String type = Json.getString(lastRecord, "type");
        if (! Simple.equals(type, "OxyMeasurement")) return;

        String sm = Simple.getTrans(R.string.health_oxy_spoken, lastSat, lastPls);
        String am = Simple.getTrans(R.string.health_oxy_activity, lastSat, lastPls);

        Speak.speak(sm);
        ActivityManager.recordActivity(am);

        evaluateEvents();

        if (!Simple.getSharedPrefBoolean("health.oxy.alert.enable")) return;

        try
        {
            int low = Simple.getSharedPrefInt("health.oxy.alert.lowsat");

            if (low >= lastSat)
            {
                Speak.speak(Simple.getTrans(R.string.health_oxy_lowsat));
                ActivityManager.recordAlert(R.string.health_oxy_lowsat);
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
                ActivityManager.recordAlert(R.string.health_oxy_lowpls);
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
                ActivityManager.recordAlert(R.string.health_oxy_highpls);
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
