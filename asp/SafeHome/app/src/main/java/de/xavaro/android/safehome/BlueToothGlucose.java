package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import org.json.JSONObject;

public class BlueToothGlucose extends BlueTooth
{
    private static final String LOGTAG = BlueToothGlucose.class.getSimpleName();

    private BlueTooth.BlueToothPhysicalDevice oneTouchDevice = new BlueToothGlucoseOneTouch(this);

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
        return oneTouchDevice.isCompatibleService(service);
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return oneTouchDevice.isCompatiblePrimary(characteristic);
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return oneTouchDevice.isCompatibleSecondary(characteristic);
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return oneTouchDevice.isCompatibleControl(characteristic);
    }

    @Override
    protected void enableDevice()
    {
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
