package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Formatter;

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
            String type = mesg.getString("type");

            if (type.equals("Measurement"))
            {
                int systolic = mesg.getInt("systolic");
                int diastolic = mesg.getInt("diastolic");
                int pulse = mesg.getInt("pulse");
                int year = mesg.getInt("year");
                int month = mesg.getInt("month");
                int day = mesg.getInt("day");
                int hour = mesg.getInt("hour");
                int minute = mesg.getInt("minute");
                int second = mesg.getInt("second");

                String result = mesg.getString("result");
                String date = new Formatter().format("%04d.%02d.%02d %02d:%02d:%02d",
                        year, month, day, hour, minute, second).toString();

                if (result.equals("final"))
                {
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
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
