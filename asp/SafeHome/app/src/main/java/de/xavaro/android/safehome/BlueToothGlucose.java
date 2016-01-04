package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class BlueToothGlucose extends BlueTooth
{
    private static final String LOGTAG = BlueToothGlucose.class.getSimpleName();

    public BlueToothGlucose(Context context)
    {
        super(context);
    }

    public BlueToothGlucose(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    private static class BPMs
    {
        public static final String MT2BT = "MediTouch 2 BT";
    }

    @Override
    protected boolean isCompatibleDevice(String devicename)
    {
        return (devicename.equalsIgnoreCase(BPMs.MT2BT) ||
                devicename.equalsIgnoreCase(BPMs.MT2BT));
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("00001808-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a18-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a34-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected void discoveredDevice()
    {
        if (connectCallback != null) connectCallback.onBluetoothConnect(deviceName);
    }

    @Override
    protected void enableDevice()
    {
        Log.d(LOGTAG,"enableDevice: " + currentPrimary);

    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }

    @Override
    public void parseResponse(byte[] rd, boolean intermediate)
    {
        Log.d(LOGTAG, "parseResponse: " + StaticUtils.hexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + rd[ 0 ]);
    }
}
