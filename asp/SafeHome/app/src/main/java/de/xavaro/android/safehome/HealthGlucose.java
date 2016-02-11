package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
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

        lastRecord = Json.getObject(data, "glucose");

        handler.removeCallbacks(messageSpeaker);
        handler.postDelayed(messageSpeaker, 1000);
    }

    private JSONObject lastRecord;

    private Runnable messageSpeaker = new Runnable()
    {
        @Override
        public void run()
        {
            if (lastRecord == null) return;

            String type = Json.getString(lastRecord, "type");

            if (Simple.equals(type, "GlucoseRecord"))
            {
                int ngv = Json.getInt(lastRecord, "ngv");
                int csv = Json.getInt(lastRecord, "csv");
                int bgv = Json.getInt(lastRecord, "bgv");

                String speak = "Die letzte Messung ergab einen Blutzuckerwert von " + bgv;

                if (csv != 0)
                    speak = "Die letzte Messung wurde mit Kontrollflüssigkeit durchgeführt";
                if (ngv != 0) speak = "Die letzte Messung war fehlerhaft";

                Speak.speak(speak);
            }

            lastRecord = null;
        }
    };
}
