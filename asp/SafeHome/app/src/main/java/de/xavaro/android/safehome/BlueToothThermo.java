package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import org.json.JSONObject;

public class BlueToothThermo extends BlueTooth
{
    private static final String LOGTAG = BlueToothThermo.class.getSimpleName();

    private BlueToothPhysicalDevice genericDevice = new BlueToothThermoGeneric(this);

    private boolean isGeneric;

    public BlueToothThermo(Context context)
    {
        super(context);
    }

    public BlueToothThermo(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        boolean isGenericThis = genericDevice.isCompatibleService(service);

        isGeneric |= isGenericThis;

        return isGenericThis;
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return genericDevice.isCompatiblePrimary(characteristic);
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return genericDevice.isCompatibleSecondary(characteristic);
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return genericDevice.isCompatibleControl(characteristic);
    }

    @Override
    protected boolean isSpecialBonding()
    {
        return false;
    }

    @Override
    protected void enableDevice()
    {
        super.enableDevice();

        if (isGeneric) genericDevice.enableDevice();
    }

    @Override
    protected void syncSequence()
    {
        if (isGeneric) genericDevice.syncSequence();
    }

    @Override
    public void sendCommand(JSONObject command)
    {
        if (isGeneric) genericDevice.sendCommand(command);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        if (isGeneric) genericDevice.parseResponse(rd, characteristic);
    }
}
