package de.xavaro.android.safehome;


import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class HealthScale implements BlueTooth.BlueToothDataCallback
{
    private static final String LOGTAG = HealthScale.class.getSimpleName();

    private static HealthScale instance;

    public static HealthScale getInstance()
    {
        if (instance == null) instance = new HealthScale();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    private BlueToothScale blueTooth;

    public void setBlueTooth(BlueToothScale blueTooth)
    {
        this.blueTooth = blueTooth;
        this.blueTooth.connect();
        this.blueTooth.setDataCallback(this);
    }

    public void setConnectCallback(BlueTooth.BlueToothConnectCallback subscriber)
    {
        if (blueTooth == null) return;

        blueTooth.setConnectCallback(subscriber);
    }

    public void onBluetoothReceivedData(BluetoothDevice device, JSONObject data)
    {
        Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());

        if (! data.has("scale")) return;

        try
        {
            JSONObject command = new JSONObject();
            JSONObject mesg;
            String type;

            mesg = data.getJSONObject("scale");
            type = mesg.getString("type");

            if (type.equals("DeviceReady"))
            {
                //
                // Scale is awake and live. Set and get scale time.
                //

                command.put("command", "getSetDateTime");
            }

            if (type.equals("RemoteTimeStamp"))
            {
                //
                // Scale time has now been set.
                //

                command.put("command", "getTakeUserMeasurement");
            }

            if (command.has("command")) blueTooth.sendCommand(command);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
