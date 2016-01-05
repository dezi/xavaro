package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

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
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a52-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected void enableDevice()
    {
        Log.d(LOGTAG, "enableDevice: " + deviceName);

        GattAction ga;

        //
        // Indicate control.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_INDICATE;
        ga.characteristic = currentControl;

        gattSchedule.add(ga);

        //
        // Notify primary.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_NOTIFY;
        ga.characteristic = currentPrimary;

        gattSchedule.add(ga);

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

        fireNext(true);
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + StaticUtils.hexBytesToString(rd));
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
