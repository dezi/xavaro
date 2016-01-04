package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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

        if (! data.has("bpm")) return;

        try
        {
            JSONObject mesg = data.getJSONObject("bpm");
            String type = mesg.getString("type");

        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
