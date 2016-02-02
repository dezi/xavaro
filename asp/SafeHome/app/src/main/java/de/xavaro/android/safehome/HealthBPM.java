package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Formatter;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;

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
            String date = Json.getString(lastRecord, "utc");
            if (date == null) return;

            int systolic = Json.getInt(lastRecord, "sys");
            int diastolic = Json.getInt(lastRecord, "dia");
            int pulse = Json.getInt(lastRecord, "pls");

            if ((lastDate == null) || (lastDate.compareTo(date) <= 0))
            {
                lastSystolic = systolic;
                lastDiastolic = diastolic;
                lastPulse = pulse;
                lastDate = date;

                handler.removeCallbacks(messageSpeaker);
                handler.postDelayed(messageSpeaker, 500);
            }
        }
    }

    JSONObject lastRecord;

    int lastSystolic;
    int lastDiastolic;
    int lastPulse;
    String lastDate;

    private Runnable messageSpeaker = new Runnable()
    {
        @Override
        public void run()
        {
            if (lastRecord == null) return;

            String type = Json.getString(lastRecord, "type");

            if (Simple.equals(type, "BPMMeasurement"))
            {
                DitUndDat.SpeekDat.speak("Die letzte Messung ergab einen Blutdruck von "
                        + lastSystolic + " zu " + lastDiastolic + " "
                        + "Der Puls beträgt " + lastPulse);
            }
        }
    };
}
