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
//

public class BlueToothThermoMedisana implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothThermoMedisana.class.getSimpleName();

    private final BlueTooth parent;

    private BluetoothGattCharacteristic dateTimeChara;

    private BluetoothGattCharacteristic unknown1;
    private BluetoothGattCharacteristic unknown2;
    private BluetoothGattCharacteristic unknown3;

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
        String uuid = characteristic.getUuid().toString();

        if (uuid.equals("874a9717-352e-11e2-89b6-7fb28bc8d12d")) dateTimeChara = characteristic;
        if (uuid.equals("e7d6818f-8610-11e2-8412-7bc9455e1a3a")) unknown1 = characteristic;
        if (uuid.equals("bf7a1506-dcf2-410a-b4b2-8829eb93d423")) unknown2 = characteristic;
        if (uuid.equals("29a59c78-ccc0-11e2-b493-14cf921ae45d")) unknown3 = characteristic;

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
        // Adjust date and time.
        //

        ga = new BlueTooth.GattAction();
        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getCurrentTime();
        ga.characteristic = dateTimeChara;

        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction();
        ga.mode = BlueTooth.GattAction.MODE_READ;
        ga.characteristic = dateTimeChara;

        parent.gattSchedule.add(ga);

        //
        // Request ongoing measuerement.
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

        if (characteristic.getUuid().toString().equals(dateTimeChara.getUuid().toString()))
        {
            long timestamp = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0) & 0xffffffffL;

            Log.d(LOGTAG, "parseResponse:"
                    + " timestamp=" + timestamp
                    + " utc=" + Simple.timeStampAsISO(timestamp * 1000L)
            );
        }

        if (isCompatiblePrimary(characteristic))
        {
            int intValu1 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,  0);
            int intValu2 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,   4);
            int intValu3 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,  5);
            int intValu4 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,  7);
            int intValu5 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,  9);
            int checksum = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 11);

            Log.d(LOGTAG, "parseResponse:"
                    + " iv1=" + intValu1
                    + " iv2=" + intValu2
                    + " iv3=" + intValu3
                    + " iv4=" + intValu4
                    + " iv5=" + intValu5
                    + " chk=" + checksum
            );

            long utc  = (intValu1 & 0xffffffffL) * 1000L;
            double temperature = intValu3 / 1000.0;
            String unit = "C";

            Log.d(LOGTAG, "parseResponse:"
                    + " dts=" + Simple.timeStampAsISO(utc)
                    + " tmp=" + temperature
            );

            //
            // Announce last record to user interface.
            //

            JSONObject result = new JSONObject();

            Json.put(result, "type", "ThermoRecord");

            Json.put(result, "dts", Simple.timeStampAsISO(utc));
            Json.put(result, "tmp", temperature);
            Json.put(result, "unt", unit);
            Json.put(result, "dev", parent.deviceName);

            /*
            if (parent.dataCallback != null)
            {
                JSONObject data = new JSONObject();
                Json.put(data, "thermo", result);

                parent.dataCallback.onBluetoothReceivedData(parent.deviceName, data);
            }
            */

            //
            // Store data.
            //

            JSONObject record = Json.clone(result);
            record.remove("type");
            HealthData.addRecord("thermo", record);
        }
    }

    public byte[] getAllRecords()
    {
        Log.d(LOGTAG, "getAllRecords");

        byte[] data = new byte[ 7 ];

        data[ 0 ] = (byte) 0xff;
        data[ 1 ] = (byte) 0xff;
        data[ 2 ] = (byte) 0x00;
        data[ 3 ] = (byte) 0x00;
        data[ 4 ] = (byte) 0x00;
        data[ 5 ] = (byte) 0x00;
        data[ 6 ] = (byte) 0x00;

        return data;
    }

    public byte[] getCurrentTime()
    {
        Log.d(LOGTAG, "getCurrentTime");

        long now = Simple.nowAsTimeStamp() / 1000L;

        byte[] data = new byte[ 4 ];

        data[ 0 ] = (byte) (now & 0xff);
        data[ 1 ] = (byte) ((now >> 8) & 0xff);
        data[ 2 ] = (byte) ((now >> 16) & 0xff);
        data[ 3 ] = (byte) ((now >> 24) & 0xff);

        return data;
    }
}
