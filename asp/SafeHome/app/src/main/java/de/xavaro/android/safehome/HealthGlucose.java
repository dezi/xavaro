package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
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

public class HealthGlucose extends HealthBase
{
    private static final String LOGTAG = HealthGlucose.class.getSimpleName();

    private static HealthGlucose instance;

    public static HealthGlucose getInstance()
    {
        if (instance == null) instance = new HealthGlucose();

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

        if (! data.has("glucose")) return;

        //
        // The results come in unordered.
        //

        JSONObject record = Json.getObject(data, "glucose");
        if (record == null) return;

        String type = Json.getString(record, "type");

        if (Simple.equals(type, "GlucoseRecord"))
        {
            String date = Json.getString(record, "dts");
            if (date == null) return;

            if ((lastDts == null) || (lastDts.compareTo(date) <= 0))
            {
                lastDts = date;
                lastRecord = record;

                handler.removeCallbacks(messageSpeaker);
                handler.postDelayed(messageSpeaker, 500);
            }
        }
    }

    private JSONObject lastRecord;
    String lastDts;

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
        if (! Simple.getSharedPrefBoolean("health.glucose.alert.alertgroup")) return;

        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        String name = Simple.getOwnerName();
        String bval = "" + (int) Math.round(Json.getDouble(lastRecord, "bgv"));

        String text = Simple.getTrans(R.string.health_glucose_alert, name, bval)
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

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZG")) continue;

            long dts = Simple.getTimeStamp(date);

            if (Math.abs(now - dts) > 2 * 3600 * 1000) continue;

            //
            // Event is suitable.
            //

            int bgv = (int) Math.round(Json.getDouble(lastRecord, "bgv"));

            Json.put(event, "taken", true);
            Json.put(event, "takendate", lastDts);
            Json.put(event, "glucose", bgv);

            EventManager.updateComingEvent("webapps.medicator", event);

            Log.d(LOGTAG, "evaluateEvents: updated=" + event.toString());

            break;
        }

        NotifyManager.removeNotification("medicator.take.bloodglucose");
        Simple.makePost(CommonConfigs.UpdateNotifications);
    }

    private void evaluateMessage()
    {
        if (lastRecord == null) return;
        String type = Json.getString(lastRecord, "type");
        if (!Simple.equals(type, "GlucoseRecord")) return;

        int bgv = (int) Math.round(Json.getDouble(lastRecord, "bgv"));

        String sm = Simple.getTrans(R.string.health_glucose_spoken, bgv);
        String am = Simple.getTrans(R.string.health_glucose_activity, bgv);

        Speak.speak(sm);
        ActivityManager.recordActivity(am);

        evaluateEvents();

        if (!Simple.getSharedPrefBoolean("health.glucose.alert.enable")) return;

        try
        {
            int low = Simple.getSharedPrefInt("health.glucose.alert.lowglucose");

            if (low >= bgv)
            {
                Speak.speak(Simple.getTrans(R.string.health_glucose_lowglucose));
                ActivityManager.recordAlert(R.string.health_glucose_lowglucose);
                informAssistance(R.string.health_glucose_lowglucose);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            int high = Simple.getSharedPrefInt("health.glucose.alert.highglucose");

            if (high <= bgv)
            {
                Speak.speak(Simple.getTrans(R.string.health_glucose_highglucose));
                ActivityManager.recordAlert(R.string.health_glucose_highglucose);
                informAssistance(R.string.health_glucose_highglucose);
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
