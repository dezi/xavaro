package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;

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

    private JSONObject lastRecord;

    private Runnable messageSpeaker = new Runnable()
    {
        @Override
        public void run()
        {
            if (lastRecord == null) return;

            int ngv = Json.getInt(lastRecord, "ngv");
            int csv = Json.getInt(lastRecord, "csv");
            int bgv = Json.getInt(lastRecord, "bgv");

            if (ngv != 0)
            {
                DitUndDat.SpeekDat.speak("Die letzte Messung war fehlerhaft");
                return;
            }

            if (csv != 0)
            {
                DitUndDat.SpeekDat.speak(
                        "Die letzte Messung wurde mit Kontrollflüssigkeit durchgeführt");
                return;
            }

            DitUndDat.SpeekDat.speak("Die letzte Messung ergab einen Blutzuckerwert von " + bgv);
        }
    };

    @Override
    public void onBluetoothReceivedData(String deviceName, JSONObject data)
    {
        Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());

        if (! data.has("glucose")) return;

        lastRecord = Json.getObject(data, "glucose");

        handler.removeCallbacks(messageSpeaker);
        handler.postDelayed(messageSpeaker, 500);
    }
}
