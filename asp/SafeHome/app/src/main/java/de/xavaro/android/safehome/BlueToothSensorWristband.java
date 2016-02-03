package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import org.json.JSONObject;

public class BlueToothSensorWristband implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothSensorWristband.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothSensorWristband(BlueTooth parent)
    {
        this.parent = parent;
    }

    public boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("0000fff0-0000-1000-8000-00805f9b34fb");
    }

    public boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fff2-0000-1000-8000-00805f9b34fb");
    }

    public boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    public boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fff1-0000-1000-8000-00805f9b34fb");
    }

    public void enableDevice()
    {
        //
        // Notify primary.
        //

        BlueTooth.GattAction ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_NOTIFY;
        ga.characteristic = parent.currentPrimary;

        parent.gattSchedule.add(ga);

        parent.fireNext(false);
    }

    public void sendCommand(JSONObject command)
    {
    }

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
    }
}
