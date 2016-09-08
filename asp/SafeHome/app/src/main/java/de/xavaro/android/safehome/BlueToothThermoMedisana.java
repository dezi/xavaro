package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import de.xavaro.android.common.HealthData;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

//
// Health data record format:
//
//  dts => ISO timestamp
//  tmp => Temperatur value
//  unt => Temperature unit
//  loc => Sample location
//

public class BlueToothThermoMedisana implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothThermoMedisana.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothThermoMedisana(BlueTooth parent)
    {
        this.parent = parent;
    }

    public boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("a72435c3-d797-44ed-a625-cf29d84aa64c");
    }

    public boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("5869cf77-a8ea-47d8-a239-cd2100fa30a1");
    }

    public boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("db765158-8854-48aa-a228-add88374eae9");
    }

    public boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("e9a2825d-6aab-438a-ac95-a914a36036e6");
    }

    public void enableDevice()
    {
    }

    public void syncSequence()
    {
        BlueTooth.GattAction ga;

        //
        // Get all records.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getAllRecords();
        ga.characteristic = parent.currentControl;

        parent.gattSchedule.add(ga);

        parent.fireNext(true);
    }

    public void sendCommand(JSONObject command)
    {
    }

    private final Map<Integer, JSONObject> lastResults = new HashMap<>();

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + characteristic.getUuid().toString());
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        /*
        if (isCompatiblePrimary(characteristic))
        {
            Log.d(LOGTAG, "parseResponse: " + getMaskPrimaryString(rd[ 0 ]));

            int offset = 0;

            final int mask = rd[ offset++ ] & 0xff;

            String unit = ((mask & 0x01) == 0) ? "C" : "F";
            float temperature = bytesToFloat(rd[ offset++ ], rd[ offset++ ], rd[ offset++ ], rd[ offset++ ]);
            int location = 0;

            Calendar calendar = new GregorianCalendar();

            if ((mask & 0x02) != 0)
            {
                int year = (rd[ offset++ ] & 0xff) + ((rd[ offset++ ] & 0xff) << 8);
                int month = rd[ offset++ ] & 0xff;
                int day = rd[ offset++ ] & 0xff;
                int hour = rd[ offset++ ] & 0xff;
                int minute = rd[ offset++ ] & 0xff;
                int second = rd[ offset++ ] & 0xff;

                calendar = new GregorianCalendar(year, month - 1, day, hour, minute, second);
            }

            if ((mask & 0x04) != 0)
            {
                location = rd[ offset++ ] & 0xff;
            }

            long utc = calendar.getTimeInMillis();
            Log.d(LOGTAG, "parseResponse: dts=" + Simple.timeStampAsISO(utc));

            Log.d(LOGTAG, "parseResponse: temperature=" + temperature + " unit=" + unit);
            Log.d(LOGTAG, "parseResponse: location=" + location);

            Log.d(LOGTAG, "parseResponse: size=" + rd.length + " used=" + offset);

            //
            // Announce last record to user interface.
            //

            JSONObject result = new JSONObject();

            Json.put(result, "type", "ThermoRecord");

            Json.put(result, "dts", Simple.timeStampAsISO(utc));
            Json.put(result, "tmp", temperature);
            Json.put(result, "unt", unit);
            Json.put(result, "loc", location);
            Json.put(result, "dev", parent.deviceName);

            if (parent.dataCallback != null)
            {
                JSONObject data = new JSONObject();
                Json.put(data, "thermo", result);

                parent.dataCallback.onBluetoothReceivedData(parent.deviceName, data);
            }

            //
            // Store data.
            //

            JSONObject record = Json.clone(result);
            record.remove("type");
            HealthData.addRecord("thermo", record);
        }
        */
    }

    private String getMaskPrimaryString(int mask)
    {
        String pstr = "";

        if ((mask & 0x01) == 0) pstr += "Celsius ";
        if ((mask & 0x01) != 0) pstr += "Fahrenheit ";
        if ((mask & 0x02) != 0) pstr += "TIME ";
        if ((mask & 0x04) != 0) pstr += "TYPE ";

        return pstr.trim();
    }

    public byte[] getAllRecords()
    {
        Log.d(LOGTAG, "getAllRecords");

        byte[] data = new byte[ 4 ];

        data[ 0 ] = 0x00;
        data[ 1 ] = 0x08;
        data[ 2 ] = 0x00;
        data[ 3 ] = 0x01;

        return data;
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

    private float bytesToFloat(byte b0, byte b1, byte b2, byte b3)
    {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + (unsignedByteToInt(b1) << 8)
                + (unsignedByteToInt(b2) << 16), 24);

        return (float) (mantissa * Math.pow(10, b3));
    }
}
