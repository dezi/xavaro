package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

public class BlueToothGlucoseGeneric implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothGlucoseGeneric.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothGlucoseGeneric(BlueTooth parent)
    {
        this.parent = parent;
    }

    public boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("00001808-0000-1000-8000-00805f9b34fb");
    }

    public boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a18-0000-1000-8000-00805f9b34fb");
    }

    public boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a34-0000-1000-8000-00805f9b34fb");
    }

    public boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a52-0000-1000-8000-00805f9b34fb");
    }

    public void enableDevice()
    {
    }

    public void syncSequence()
    {
        BlueTooth.GattAction ga;

        //
        // Read number of records.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getNumberOfRecords();
        ga.characteristic = parent.currentControl;

        parent.gattSchedule.add(ga);

        parent.fireNext(false);
    }

    public void sendCommand(JSONObject command)
    {
    }

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + rd[ 0 ]);
    }

    public byte[] getNumberOfRecords()
    {
        Log.d(LOGTAG, "getNumberOfRecords");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = 4;
        data[ 1 ] = 1;

        return data;
    }

    public byte[] getAllRecords()
    {
        Log.d(LOGTAG, "getAllRecords");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = 1;
        data[ 1 ] = 1;

        return data;
    }
}
