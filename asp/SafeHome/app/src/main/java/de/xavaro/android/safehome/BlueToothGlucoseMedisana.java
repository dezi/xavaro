package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

public class BlueToothGlucoseMedisana implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothGlucoseMedisana.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothGlucoseMedisana(BlueTooth parent)
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
        BlueTooth.GattAction ga;

        //
        // Indicate control.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_INDICATE;
        ga.characteristic = parent.currentControl;

        parent.gattSchedule.add(ga);

        //
        // Notify primary.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_NOTIFY;
        ga.characteristic = parent.currentPrimary;

        parent.gattSchedule.add(ga);

        /*
        //
        // Indicate service changed characteristic.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_INDICATE;
        ga.characteristic = currentChanged;

        gattSchedule.add(ga);

        for (int inx = 0; inx < 20; inx++)
        {
            ga = new GattAction();

            ga.mode = GattAction.MODE_READ;
            ga.characteristic = currentSerial;

            gattSchedule.add(ga);
        }


        /*

        //
        // Notify secondary.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_NOTIFY;
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);

        //
        // Read number of records.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_WRITE;
        ga.data = getNumberOfRecords();
        ga.characteristic = currentControl;

        gattSchedule.add(ga);

        /*
        //
        // Read serial number.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_READ;
        ga.characteristic = currentSerial;

        gattSchedule.add(ga);

        */

        parent.fireNext(true);
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }

    @Override
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
