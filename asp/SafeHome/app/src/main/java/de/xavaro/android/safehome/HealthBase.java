package de.xavaro.android.safehome;

import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class HealthBase implements
        BlueTooth.BlueToothDataCallback,
        BlueTooth.BlueToothConnectCallback
{
    protected String deviceName;
    protected boolean isConnected;
    protected BlueTooth blueTooth;
    protected BlueTooth.BlueToothConnectCallback connectCallback;

    protected final Handler handler = new Handler();

    public void setBlueTooth(BlueTooth blueTooth)
    {
        this.blueTooth = blueTooth;

        this.blueTooth.setDataCallback(this);
        this.blueTooth.setConnectCallback(this);

        this.blueTooth.connectRunnable.run();
    }

    public void setConnectCallback(BlueTooth.BlueToothConnectCallback subscriber)
    {
        connectCallback = subscriber;

        if (isConnected && (connectCallback != null)) connectCallback.onBluetoothConnect(deviceName);
    }

    public abstract void onBluetoothReceivedData(String deviceName, JSONObject data);

    public void onBluetoothConnect(String deviceName)
    {
        this.deviceName = deviceName;

        isConnected = true;

        if (connectCallback != null) connectCallback.onBluetoothConnect(deviceName);
    }

    public void onBluetoothDisconnect(String deviceName)
    {
        isConnected = false;

        if (connectCallback != null) connectCallback.onBluetoothDisconnect(deviceName);
    }

    public void onBluetoothFakeConnect(String deviceName)
    {
        if (connectCallback != null) connectCallback.onBluetoothFakeConnect(deviceName);
    }

    public void onBluetoothFakeDisconnect(String deviceName)
    {
        if (connectCallback != null) connectCallback.onBluetoothFakeDisconnect(deviceName);
    }
}
