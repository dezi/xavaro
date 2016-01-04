package de.xavaro.android.safehome;

import android.content.SharedPreferences;
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

    public void setBlueTooth(BlueTooth blueTooth)
    {
        this.blueTooth = blueTooth;

        this.blueTooth.setDataCallback(this);
        this.blueTooth.setConnectCallback(this);

        this.blueTooth.connect();
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
}
