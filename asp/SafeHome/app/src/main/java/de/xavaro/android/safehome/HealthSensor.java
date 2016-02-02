package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class HealthSensor extends HealthBase
{
    private static final String LOGTAG = HealthSensor.class.getSimpleName();

    private static HealthSensor instance;

    public static HealthSensor getInstance()
    {
        if (instance == null) instance = new HealthSensor();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    @Override
    public void onBluetoothReceivedData(String deviceName, JSONObject data)
    {
        Log.d(LOGTAG, "onBluetoothReceivedData: " + data.toString());

        if (! data.has("sensor")) return;

        lastRecord = Json.getObject(data, "sensor");

        handler.post(messageSpeaker);
    }

    private JSONObject lastRecord;

    private Runnable messageSpeaker = new Runnable()
    {
        @Override
        public void run()
        {
            if (lastRecord == null) return;

            String type = Json.getString(lastRecord, "type");

            if (Simple.equals(type, "TodaysData"))
            {
                int steps = Json.getInt(lastRecord, "steps");

                DitUndDat.SpeekDat.speak("Sie sind heute " + steps + " Schritte gegangen");
            }
        }
    };
}
