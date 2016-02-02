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
        Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());
    }
}
