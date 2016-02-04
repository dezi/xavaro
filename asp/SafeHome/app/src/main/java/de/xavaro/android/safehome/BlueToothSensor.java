package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import org.json.JSONObject;

//
// Health data record format:
//
//  dts => ISO timestamp
//  stp => Number of steps
//  ext => Exercise time
//  goa => Goal value
//  gou => Goal unit (0 = steps, 1 = calories)
//  std => Width of one step in cm
//  diu => Distance unit (0 = km, 1 = miles)
//  cps => Calories burned per 10.000 steps
//  BMR => Basal metabolic rate
//  s00 => Activity values per 3 hours fragment
//  s03 => ...
//

public class BlueToothSensor extends BlueTooth
{
    private static final String LOGTAG = BlueToothSensor.class.getSimpleName();

    private BlueTooth.BlueToothPhysicalDevice sanitasDevice = new BlueToothSensorSanitas(this);
    private BlueTooth.BlueToothPhysicalDevice whristbandDevice = new BlueToothSensorWristband(this);

    private boolean isSanitas;
    private boolean isWhristband;

    public BlueToothSensor(Context context)
    {
        super(context);
    }

    public BlueToothSensor(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        boolean isSanitasthis = sanitasDevice.isCompatibleService(service);
        boolean isWhristbandthis = whristbandDevice.isCompatibleService(service);

        isSanitas |= isSanitasthis;
        isWhristband |= isWhristbandthis;

        return isSanitasthis || isWhristbandthis;
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return sanitasDevice.isCompatiblePrimary(characteristic) ||
                whristbandDevice.isCompatiblePrimary(characteristic);
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return sanitasDevice.isCompatibleSecondary(characteristic) ||
                whristbandDevice.isCompatibleSecondary(characteristic);
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return sanitasDevice.isCompatibleControl(characteristic) ||
                whristbandDevice.isCompatibleControl(characteristic);
    }

    @Override
    protected void enableDevice()
    {
        if (isSanitas) sanitasDevice.enableDevice();
        if (isWhristband) whristbandDevice.enableDevice();
    }

    @Override
    public void sendCommand(JSONObject command)
    {
        if (isSanitas) sanitasDevice.sendCommand(command);
        if (isWhristband) whristbandDevice.sendCommand(command);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        if (isSanitas) sanitasDevice.parseResponse(rd, characteristic);
        if (isWhristband) whristbandDevice.parseResponse(rd, characteristic);

    }
}
