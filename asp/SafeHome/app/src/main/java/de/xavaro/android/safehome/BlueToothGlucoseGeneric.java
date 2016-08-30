package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.xavaro.android.common.Json;
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
        /*
        BlueTooth.GattAction ga;

        //
        // Read number of records.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getNumberOfRecords();
        ga.characteristic = parent.currentControl;

        parent.gattSchedule.add(ga);
        */

        parent.fireNext(true);
    }

    public void sendCommand(JSONObject command)
    {
    }

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + getMaskString(rd[ 0 ]));

        int offset = 0;

        JSONObject bpmdata = new JSONObject();

        Json.put(bpmdata, "type", "GlucoseRecord");

        int sequence = rd[ ++offset ] + (rd[ ++offset  ] << 8);

        Log.d(LOGTAG, "parseResponse: seq=" + sequence);

        if ((rd[ 0 ] & 0x01) >= 1)
        {
            int year = rd[ ++offset ] + (rd[ ++offset ] << 8);
            int month = rd[ ++offset ];
            int day = rd[ ++offset ];
            int hour = rd[ ++offset ];
            int minute = rd[ ++offset ];
            int second = rd[ ++offset ];

            Calendar calendar = new GregorianCalendar(year, month - 1, day, hour, minute, second);
            long utc = calendar.getTimeInMillis();
            Json.put(bpmdata, "dts", Simple.timeStampAsISO(utc));

            Log.d(LOGTAG,"parseResponse result=" + hour + "::" + Simple.timeStampAsISO(utc));
        }

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

    private String getMaskString(int mask)
    {
        String pstr = "";

        if ((mask & 0x01) >= 1) pstr += "TIME ";
        if ((mask & 0x02) >= 1) pstr += "SAMPLE ";
        if ((mask & 0x04) == 0) pstr += "kg/L ";
        if ((mask & 0x04) >= 1) pstr += "mol/L ";
        if ((mask & 0x08) >= 1) pstr += "STATUS ";
        if ((mask & 0x10) >= 1) pstr += "CONTEXT ";

        return pstr.trim();
    }

}
