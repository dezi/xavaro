package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
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

    private static final int maxdaystodo = 10;
    private static final int maxpositodo = 1440;

    private int actdaystodo;
    private int actpositodo;

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
                int steps = mesg.getInt("steps");

                DitUndDat.SpeekDat.speak("Sie sind heute " + steps + " Schritte gegangen");

                actdaystodo = 0;
                actpositodo = 0;
            }

            if (type.equals("StepHistoryData"))
            {
                Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());
            }

            if (type.equals("SleepHistoryData"))
            {
                Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());
            }

            if (actdaystodo < maxdaystodo)
            {
                command.put("command", "getStepHistoryData");
                command.put("day", actdaystodo);

                actdaystodo += 1;
            }
            else
            {
                if (actpositodo < maxpositodo)
                {
                    command.put("command", "getSleepHistoryData");
                    command.put("position", actpositodo);

                    actpositodo += 18;
                }
                else
                {
                    command.put("command", "getDisconnect");
                }
            }

            if (command.has("command")) blueTooth.sendCommand(command);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
