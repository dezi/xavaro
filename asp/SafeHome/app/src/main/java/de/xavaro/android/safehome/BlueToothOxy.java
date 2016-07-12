package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.xavaro.android.common.HealthData;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

//
// Health data record format:
//
//  dts => ISO timestamp
//  sat => Oxygene saturation
//  pls => Pulse
//

public class BlueToothOxy extends BlueTooth
{
    private static final String LOGTAG = BlueToothOxy.class.getSimpleName();

    public BlueToothOxy(Context context)
    {
        super(context);
    }

    public BlueToothOxy(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().startsWith("ba11f08c-5f14-0b0d");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        //noinspection SimplifiableIfStatement
        if (currentPrimary != null)
        {
            //
            // Stupid device delivers same characteristic
            // twice under different services.
            //

            return false;
        }

        return characteristic.getUuid().toString().equals("0000cd01-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000cd04-0000-1000-8000-00805f9b34fb");
    }

    private BluetoothGattCharacteristic c2;
    private BluetoothGattCharacteristic c3;

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        //
        // Stupid device wants all notifiers enabled, otherwise does not respond.
        //

        if (characteristic.getUuid().toString().equals("0000cd02-0000-1000-8000-00805f9b34fb"))
        {
            c2 = characteristic;
        }

        if (characteristic.getUuid().toString().equals("0000cd03-0000-1000-8000-00805f9b34fb"))
        {
            c3 = characteristic;
        }

        return characteristic.getUuid().toString().equals("0000cd20-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected void enableDevice()
    {
        if (currentPrimary != null) gattSchedule.add(new GattAction(currentPrimary));

        if (c2 != null) gattSchedule.add(new GattAction(c2));
        if (c3 != null) gattSchedule.add(new GattAction(c3));

        if (currentSecondary != null) gattSchedule.add(new GattAction(currentSecondary));
    }

    @Override
    protected void syncSequence()
    {
        BlueTooth.GattAction ga;

        //
        // Send stupid password to device.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getPassword();
        ga.characteristic = currentControl;

        gattSchedule.add(ga);

        /*
        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getReadRealtime();
        ga.characteristic = currentControl;

        gattSchedule.add(ga);
        */

        fireNext(false);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        if (characteristic == currentSecondary)
        {
            JSONObject oxydata = new JSONObject();

            Json.put(oxydata, "type", "OXYMeasurement");

            float sat = rd[ 3 ];
            float pls = rd[ 4 ];

            Calendar calendar = new GregorianCalendar();
            long utc = calendar.getTimeInMillis();
            Json.put(oxydata, "dts", Simple.timeStampAsISO(utc));

            Log.d(LOGTAG,"parseResponse result=" + Simple.timeStampAsISO(utc) + "=" + sat + "/" + pls);

            Json.put(oxydata, "sat", sat);
            Json.put(oxydata, "pls", pls);

            JSONObject data = new JSONObject();
            Json.put(data, "oxy", oxydata);

            if (dataCallback != null) dataCallback.onBluetoothReceivedData(deviceName, data);

            //
            // Store data.
            //

            JSONObject record = Json.clone(oxydata);
            Json.remove(record, "type");

            HealthData.addRecord("oxy", record);
            HealthData.setLastReadDate("oxy");
        }
    }

    private byte[] getPassword()
    {
        Log.d(LOGTAG, "getPassword");

        byte[] data = new byte[ 7 ];

        data[ 0 ] = (byte) 170;
        data[ 1 ] = (byte) 85;
        data[ 2 ] = (byte) 4;
        data[ 3 ] = (byte) 177;
        data[ 4 ] = (byte) 0;
        data[ 5 ] = (byte) 0;
        data[ 6 ] = (byte) 0;

        calcChecksum(data);

        return data;
    }

    @SuppressWarnings("unused")
    private byte[] getReadRealtime()
    {
        Log.d(LOGTAG, "getReadRealtime");

        byte[] data = new byte[ 6 ];

        data[ 0 ] = (byte) 170;
        data[ 1 ] = (byte) 85;
        data[ 2 ] = (byte) 3;
        data[ 3 ] = (byte) 80;
        data[ 4 ] = (byte) 0;
        data[ 5 ] = (byte) 0;

        calcChecksum(data);

        return data;
    }

    private void calcChecksum(byte[] data)
    {
        byte cs = 0;

        for (int inx = 2; inx < data.length - 1; inx++)
        {
            cs += data[ inx ];
        }

        data[ data.length - 1 ] = cs;
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }
}
