package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
//  bgv => Blood glucose value
//  unt => BVG unit
//  dty => Data type
//  loc => Sample location
//  sst => Sensor status
//

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
        Log.d(LOGTAG, "parseResponse: " + characteristic.getUuid().toString());
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + getMaskString(rd[ 0 ]));

        if (! isCompatiblePrimary(characteristic)) return;
        if (rd.length <= 4) return;

        int offset = 0;

        int mask = rd[ offset++ ] & 0xff;
        int sequence = (rd[ offset++ ] & 0xff) + ((rd[ offset++ ] & 0xff) << 8);

        Log.d(LOGTAG, "parseResponse: seq=" + sequence);

        int year = (rd[ offset++ ] & 0xff) + ((rd[ offset++ ] & 0xff) << 8);
        int month = rd[ offset++ ] & 0xff;
        int day = rd[ offset++ ] & 0xff;
        int hour = rd[ offset++ ] & 0xff;
        int minute = rd[ offset++ ] & 0xff;
        int second = rd[ offset++ ] & 0xff;
        int minoff = 0;

        if ((mask & 0x01) >= 1)
        {
            minoff = (rd[ offset++ ] & 0xff) + ((rd[ offset++ ] & 0xff) << 8);
        }

        Calendar calendar = new GregorianCalendar(year, month - 1, day, hour, minute, second);
        long utc = calendar.getTimeInMillis() + (minoff * 60 * 1000);

        Log.d(LOGTAG, "parseResponse: hour=" + hour + " minoff=" + minoff + "=>" + Simple.timeStampAsISO(utc));

        float glucose = 0.0f;
        int datatype = 0;
        int location = 0;

        String unit = ((mask & 0x04) == 0) ? "mg/DL" : "mol/L";

        if ((mask & 0x02) >= 1)
        {
            glucose = bytesToFloat(rd[ offset++ ], rd[ offset++ ]);
            glucose = glucose * (((mask & 0x04) == 0) ? 100000.0f : 1.0f);

            datatype = rd[ offset++ ] & 0xff;
            location = datatype & 0x0f;
            datatype = (datatype & 0xf0) >> 4;
        }

        Log.d(LOGTAG, "parseResponse: glucose=" + glucose + " unit=" + unit);
        Log.d(LOGTAG, "parseResponse: datatype=" + datatype + " location=" + location);

        int status = 0;

        if ((mask & 0x08) >= 1)
        {
            status = ((rd[ offset++ ] & 0xff) << 8) + (rd[ offset++ ] & 0xff);
            Log.d(LOGTAG, "parseResponse: status=" + status);
        }

        Log.d(LOGTAG, "parseResponse: size=" + rd.length + " used=" + offset);

        //
        // Announce last record to user interface.
        //

        JSONObject result = new JSONObject();

        Json.put(result, "type", "GlucoseRecord");

        Json.put(result, "dts", Simple.timeStampAsISO(utc));
        Json.put(result, "bgv", glucose);
        Json.put(result, "unt", unit);
        Json.put(result, "dty", datatype);
        Json.put(result, "loc", location);
        Json.put(result, "sst", status);
        Json.put(result, "dev", parent.deviceName);

        if (parent.dataCallback != null)
        {
            JSONObject data = new JSONObject();
            Json.put(data, "glucose", result);

            parent.dataCallback.onBluetoothReceivedData(parent.deviceName, data);
        }

        //
        // Store data.
        //

        JSONObject record = Json.clone(result);
        record.remove("type");
        HealthData.addRecord("glucose", record);
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

    private int unsignedByteToInt(byte b)
    {
        return b & 0xff;
    }

    private int unsignedToSigned(int unsigned, int size)
    {
        if ((unsigned & (1 << size - 1)) != 0)
        {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }

        return unsigned;
    }

    private float bytesToFloat(byte b0, byte b1)
    {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + ((unsignedByteToInt(b1) & 0x0F) << 8), 12);

        int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);

        return (float) (mantissa * Math.pow(10, exponent));
    }
}
