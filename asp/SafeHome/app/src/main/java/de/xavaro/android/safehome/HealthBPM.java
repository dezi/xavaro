package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Formatter;

import de.xavaro.android.common.OopsService;

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

    private String lastDate;
    private int lastSystolic;
    private int lastDiastolic;
    private int lastPulse;

    private Runnable messageSpeaker = new Runnable()
    {
        @Override
        public void run()
        {
            DitUndDat.SpeekDat.speak("Die letzte Messung ergab einen Blutdruck von "
                    + lastSystolic + " zu " + lastDiastolic + " "
                    + "Der Puls beträgt " + lastPulse);
        }
    };

    @Override
    public void onBluetoothReceivedData(String deviceName, JSONObject data)
    {
        Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());

        if (! data.has("bpm")) return;

        try
        {
            JSONObject mesg = data.getJSONObject("bpm");

            String date = mesg.getString("utc");
            int systolic = mesg.getInt("sys");
            int diastolic = mesg.getInt("dia");
            int pulse = mesg.getInt("pls");

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
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
