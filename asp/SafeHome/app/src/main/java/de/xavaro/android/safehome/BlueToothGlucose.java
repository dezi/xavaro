package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import org.json.JSONObject;

public class BlueToothGlucose extends BlueTooth
{
    private static final String LOGTAG = BlueToothGlucose.class.getSimpleName();

    private BlueTooth.BlueToothPhysicalDevice oneTouchDevice = new BlueToothGlucoseOneTouch(this);
    private BlueTooth.BlueToothPhysicalDevice medisanaDevice = new BlueToothGlucoseGeneric(this);

    private boolean isOneTouch;
    private boolean isMedisana;

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
        boolean isOneTouchThis = oneTouchDevice.isCompatibleService(service);
        boolean isMedisanaThis = medisanaDevice.isCompatibleService(service);

        isOneTouch |= isOneTouchThis;
        isMedisana |= isMedisanaThis;

        return isOneTouchThis || isMedisanaThis;
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return oneTouchDevice.isCompatiblePrimary(characteristic) ||
                medisanaDevice.isCompatiblePrimary(characteristic);
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return oneTouchDevice.isCompatibleSecondary(characteristic) ||
                medisanaDevice.isCompatibleSecondary(characteristic);
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return oneTouchDevice.isCompatibleControl(characteristic) ||
                medisanaDevice.isCompatibleControl(characteristic);
    }

    @Override
    protected boolean isSpecialBonding()
    {
        if (deviceName.equals("Accu-Chek")) return true;
        if (deviceName.equals("MediTouch 2 BT")) return true;

        return false;
    }

    @Override
    protected void enableDevice()
    {
        if (isOneTouch) oneTouchDevice.enableDevice();
        if (isMedisana) medisanaDevice.enableDevice();
    }

    @Override
    public void sendCommand(JSONObject command)
    {
        if (isOneTouch) oneTouchDevice.sendCommand(command);
        if (isMedisana) medisanaDevice.sendCommand(command);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        if (isOneTouch) oneTouchDevice.parseResponse(rd, characteristic);
        if (isMedisana) medisanaDevice.parseResponse(rd, characteristic);
    }
}
