package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

public class BlueToothGlucose extends BlueTooth
{
    private static final String LOGTAG = BlueToothGlucose.class.getSimpleName();

    private BlueToothGlucoseOneTouch oneTouchDevice = new BlueToothGlucoseOneTouch(this);

    public BlueToothGlucose(Context context)
    {
        super(context);
    }

    public BlueToothGlucose(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("af9df7a1-e595-11e3-96b4-0002a5d5c51b");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("af9df7a3-e595-11e3-96b4-0002a5d5c51b");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("af9df7a2-e595-11e3-96b4-0002a5d5c51b");
    }

    @Override
    protected void enableDevice()
    {
        Log.d(LOGTAG, "enableDevice: " + deviceName);

        oneTouchDevice.enableDevice();
    }

    @Override
    public void sendCommand(JSONObject command)
    {
        oneTouchDevice.sendCommand(command);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        oneTouchDevice.parseResponse(rd, characteristic);
    }
}
