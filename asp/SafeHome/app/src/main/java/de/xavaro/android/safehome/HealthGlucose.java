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

    private HealthGlucose()
    {
        super();

        this.deviceType = "glucose";
    }

    @Override
    protected void evaluateEvents()
    {
        JSONArray events = EventManager.getComingEvents("webapps.medicator");
        if (events == null) return;

        if (actRecord == null) return;

        String actDts = Json.getString(actRecord, "dts");
        int actBGV = (int) Math.round(Json.getDouble(actRecord, "bgv"));

        long now = Simple.nowAsTimeStamp();

        for (int inx = 0; inx < events.length(); inx++)
        {
            JSONObject event = Json.getObject(events, inx);

            if ((event == null) || Json.getBoolean(event, "taken")) continue;

            String date = Json.getString(event, "date");
            String medication = Json.getString(event, "medication");

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZG")) continue;

            //
            // Check event and measurement dates.
            //

            long mts = Simple.getTimeStamp(date);
            if (Math.abs(now - mts) > 2 * 3600 * 1000) continue;

            long dts = Simple.getTimeStamp(actDts);
            if (Math.abs(dts - mts) > 2 * 3600 * 1000) continue;

            //
            // Event is suitable.
            //

            Json.put(event, "taken", true);
            Json.put(event, "takendate", actDts);
            Json.put(event, "glucose", actBGV);

            EventManager.updateComingEvent("webapps.medicator", event);

            Log.d(LOGTAG, "evaluateEvents: updated=" + event.toString());

            break;
        }

        NotifyManager.removeNotification("medicator.take.bloodglucose");
        Simple.makePost(CommonConfigs.UpdateNotifications);
    }

    @Override
    protected void evaluateMessage()
    {
        if (actRecord == null) return;

        int bgv = (int) Math.round(Json.getDouble(actRecord, "bgv"));

        boolean iswarning = false;

        String sm = Simple.getTrans(R.string.health_glucose_spoken, bgv);
        String lm = Simple.getTrans(R.string.health_glucose_logger, bgv);
        String am = Simple.getTrans(R.string.health_glucose_assist, Simple.getOwnerName(), bgv);

        String at = "";

        if (Simple.getSharedPrefBoolean("health.glucose.alert.enable"))
        {
            //
            // Check alerts.
            //

            int low = Simple.getSharedPrefInt("health.glucose.alert.lowglucose");

            Log.d(LOGTAG, "evaluateMessage: tmp=" + bgv + " low=" + low);

            if ((low > 0) && (low >= bgv))
            {
                at += " " + Simple.getTrans(R.string.health_glucose_glucose_low);

                iswarning = true;
            }

            int high = Simple.getSharedPrefInt("health.glucose.alert.highglucose");

            Log.d(LOGTAG, "evaluateMessage: tmp=" + bgv + " high=" + high);

            if ((high > 0) && (high <= bgv))
            {
                at += " " + Simple.getTrans(R.string.health_glucose_glucose_high);

                iswarning = true;
            }
        }

        if (! at.isEmpty())
        {
            sm += " " + at.trim();
            lm += " " + at.trim();
            am += " " + at.trim();
        }

        Speak.speak(sm);
        ActivityOldManager.recordActivity(lm);

        handleAssistance(am, iswarning);

        if (connectCallback != null)
        {
            connectCallback.onBluetoothUpdated(deviceName);
        }
    }
}
