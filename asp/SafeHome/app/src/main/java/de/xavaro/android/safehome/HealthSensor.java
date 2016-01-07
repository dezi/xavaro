package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.xavaro.android.common.OopsService;

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
        Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());

        if (! data.has("sensor")) return;

        try
        {
            JSONObject command = new JSONObject();
            JSONObject mesg = data.getJSONObject("sensor");
            String type = mesg.getString("type");

            if (type.equals("TodaysData"))
            {
                command.put("command", "getStepHistoryData");
                command.put("day", 0);

                int steps = mesg.getInt("steps");

                DitUndDat.SpeekDat.speak("Sie sind heute " + steps + " Schritte gegangen");
            }

            if (type.equals("StepHistoryData"))
            {
                command.put("command", "getSleepHistoryData");
                command.put("position", 0);
            }

            if (type.equals("SleepHistoryData"))
            {
                command.put("command", "getDisconnect");
            }

            if (command.has("command")) blueTooth.sendCommand(command);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
