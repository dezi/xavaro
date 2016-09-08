package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

public class BlueToothThermo extends BlueTooth
{
    private static final String LOGTAG = BlueToothThermo.class.getSimpleName();

    private BlueToothPhysicalDevice genericDevice = new BlueToothThermoGeneric(this);
    private BlueToothPhysicalDevice medisanaDevice = new BlueToothThermoMedisana(this);

    private boolean isGeneric;
    private boolean isMedisana;

    public BlueToothThermo(Context context)
    {
        super(context);

        Log.d(LOGTAG, "=============================================thermo");
    }

    public BlueToothThermo(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        boolean isGenericThis = genericDevice.isCompatibleService(service);
        boolean isMedisanaThis = medisanaDevice.isCompatibleService(service);

        isGeneric |= isGenericThis;
        isMedisana |= isMedisanaThis;

        return isGenericThis || isMedisanaThis;
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return genericDevice.isCompatiblePrimary(characteristic) ||
                medisanaDevice.isCompatiblePrimary(characteristic);
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return genericDevice.isCompatibleSecondary(characteristic) ||
                medisanaDevice.isCompatibleSecondary(characteristic);
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return genericDevice.isCompatibleControl(characteristic) ||
                medisanaDevice.isCompatibleControl(characteristic);
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
        if (isMedisana) medisanaDevice.enableDevice();
    }

    @Override
    protected void syncSequence()
    {
        if (isGeneric) genericDevice.syncSequence();
        if (isMedisana) medisanaDevice.syncSequence();
    }

    @Override
    public void sendCommand(JSONObject command)
    {
        if (isGeneric) genericDevice.sendCommand(command);
        if (isMedisana) medisanaDevice.sendCommand(command);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        if (isGeneric) genericDevice.parseResponse(rd, characteristic);
        if (isMedisana) medisanaDevice.parseResponse(rd, characteristic);
    }
}
