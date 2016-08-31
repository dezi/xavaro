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

public class HealthBPM extends HealthBase
{
    private static final String LOGTAG = HealthBPM.class.getSimpleName();

    private static HealthBPM instance;

    public static HealthBPM getInstance()
    {
        if (instance == null) instance = new HealthBPM();

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

        if (! data.has("bpm")) return;

        //
        // The results come in unordered.
        //

        lastRecord = Json.getObject(data, "bpm");
        if (lastRecord == null) return;

        String type = Json.getString(lastRecord, "type");

        if (Simple.equals(type, "BPMMeasurement"))
        {
            String date = Json.getString(lastRecord, "dts");
            if (date == null) return;

            int systolic = Json.getInt(lastRecord, "sys");
            int diastolic = Json.getInt(lastRecord, "dia");
            int pulse = Json.getInt(lastRecord, "pls");

            if ((lastDts == null) || (lastDts.compareTo(date) <= 0))
            {
                lastSys = systolic;
                lastDia = diastolic;
                lastPls = pulse;
                lastDts = date;

                handler.removeCallbacks(messageSpeaker);
                handler.postDelayed(messageSpeaker, 500);
            }
        }
    }

    JSONObject lastRecord;
    String lastDts;
    int lastSys;
    int lastDia;
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
        if (! Simple.getSharedPrefBoolean("health.bpm.alert.alertgroup")) return;

        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        String name = Simple.getOwnerName();
        String bval = lastSys + ":" + lastDia;
        String puls = "" + lastPls;

        String text = Simple.getTrans(R.string.health_bpm_alert, name, bval, puls)
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

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZB")) continue;

            long dts = Simple.getTimeStamp(date);

            if (Math.abs(now - dts) > 2 * 3600 * 1000) continue;

            //
            // Event is suitable.
            //

            Json.put(event, "taken", true);
            Json.put(event, "takendate", lastDts);
            Json.put(event, "diastolic", lastDia);
            Json.put(event, "systolic", lastSys);
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
        if (!Simple.equals(type, "BPMMeasurement")) return;

        String sm = Simple.getTrans(R.string.health_bpm_spoken, lastSys, lastDia, lastPls);
        String am = Simple.getTrans(R.string.health_bpm_activity, lastSys, lastDia, lastPls);

        Speak.speak(sm);
        ActivityManager.recordActivity(am);

        evaluateEvents();

        if (!Simple.getSharedPrefBoolean("health.bpm.alert.enable")) return;

        try
        {
            String low = Simple.getSharedPrefString("health.bpm.alert.lowbp");

            if (low != null)
            {
                String[] lp = low.split(":");

                if ((lp.length == 2) &&
                        ((Integer.parseInt(lp[ 0 ]) >= lastSys) ||
                                (Integer.parseInt(lp[ 1 ]) >= lastDia)))
                {
                    Speak.speak(Simple.getTrans(R.string.health_bpm_lowpb));
                    ActivityManager.recordAlert(R.string.health_bpm_lowpb);
                    informAssistance(R.string.health_bpm_lowpb);
                }
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            String high = Simple.getSharedPrefString("health.bpm.alert.highbp");

            if (high != null)
            {
                String[] hp = high.split(":");

                if ((hp.length == 2) &&
                        ((Integer.parseInt(hp[ 0 ]) <= lastSys) ||
                                (Integer.parseInt(hp[ 1 ]) <= lastDia)))
                {
                    Speak.speak(Simple.getTrans(R.string.health_bpm_highbp));
                    ActivityManager.recordAlert(R.string.health_bpm_highbp);
                    informAssistance(R.string.health_bpm_highbp);
                }
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            int low = Simple.getSharedPrefInt("health.bpm.alert.lowpls");

            if (low >= lastPls)
            {
                Speak.speak(Simple.getTrans(R.string.health_bpm_lowpls));
                ActivityManager.recordAlert(R.string.health_bpm_lowpls);
                informAssistance(R.string.health_bpm_lowpls);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            int high = Simple.getSharedPrefInt("health.bpm.alert.highpls");

            if (high <= lastPls)
            {
                Speak.speak(Simple.getTrans(R.string.health_bpm_highpls));
                ActivityManager.recordAlert(R.string.health_bpm_highpls);
                informAssistance(R.string.health_bpm_highpls);
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
