package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class BlueToothBPM extends BlueTooth
{
    private static final String LOGTAG = BlueToothBPM.class.getSimpleName();

    public BlueToothBPM(Context context)
    {
        super(context);
    }

    public BlueToothBPM(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("00001810-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a35-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a36-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    @Override
    protected void enableDevice()
    {
        super.enableDevice();

        Log.d(LOGTAG,"enableDevice: " + deviceName);

        fireNext(true);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + StaticUtils.hexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + getMaskString(rd[ 0 ]));

        try
        {
            JSONObject bpmdata = new JSONObject();

            int offset = 0;

            bpmdata.put("type", "Measurement");

            bpmdata.put("result", (characteristic == currentPrimary) ? "final" : "intermediate");

            bpmdata.put("systolic", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));
            bpmdata.put("diastolic", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));
            bpmdata.put("meanap", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));

            if ((rd[ 0 ] & 0x02) >= 1)
            {
                bpmdata.put("year", unsignedBytesToIntRev(rd[ ++offset ], rd[ ++offset ]));
                bpmdata.put("month", unsignedByteToInt(rd[ ++offset ]));
                bpmdata.put("day", unsignedByteToInt(rd[ ++offset ]));
                bpmdata.put("hour", unsignedByteToInt(rd[ ++offset ]));
                bpmdata.put("minute", unsignedByteToInt(rd[ ++offset ]));
                bpmdata.put("second", unsignedByteToInt(rd[ ++offset ]));
            }

            if ((rd[ 0 ] & 0x04) >= 1)
            {
                bpmdata.put("pulse", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));
            }

            if ((rd[ 0 ] & 0x08) >= 1)
            {
                bpmdata.put("user", unsignedByteToInt(rd[ ++offset ]));
            }

            if ((rd[ 0 ] & 0x10) >= 1)
            {
                bpmdata.put("flags", unsignedByteToInt(rd[ ++offset ]));
            }

            JSONObject data = new JSONObject();
            data.put("bpm", bpmdata);

            if (dataCallback != null) dataCallback.onBluetoothReceivedData(deviceName, data);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }

    private String getMaskString(int mask)
    {
        String pstr = "";

        if ((mask & 0x01) == 0) pstr += "mmHg ";
        if ((mask & 0x01) >= 1) pstr += "kPa ";
        if ((mask & 0x02) >= 1) pstr += "TIME ";
        if ((mask & 0x04) >= 1) pstr += "PULSE ";
        if ((mask & 0x08) >= 1) pstr += "USER ";
        if ((mask & 0x10) >= 1) pstr += "STATUS ";

        return pstr.trim();
    }
}
