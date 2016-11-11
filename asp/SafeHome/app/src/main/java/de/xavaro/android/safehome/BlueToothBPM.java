package de.xavaro.android.safehome;

import android.bluetooth.BluetoothDevice;
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
//  sys => Systolic blood pressure
//  dia => Diastolic blood pressure
//  map => Mean arterial blood pressure
//  pls => Pulse
//  usr => User id
//  flg => Device flags
//

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
        gattSchedule.add(new GattAction(currentPrimary));
    }

    @Override
    protected void syncSequence()
    {
        fireNext(false);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + getMaskString(rd[ 0 ]));

        boolean isfinal = (characteristic == currentPrimary);

        int offset = 0;

        JSONObject bpmdata = new JSONObject();

        float sys = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);
        float dia = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);
        float map = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);

        if ((rd[ 0 ] & 0x02) >= 1)
        {
            int year = unsignedBytesToIntRev(rd[ ++offset ], rd[ ++offset ]);
            int month = unsignedByteToInt(rd[ ++offset ]);
            int day = unsignedByteToInt(rd[ ++offset ]);
            int hour = unsignedByteToInt(rd[ ++offset ]);
            int minute = unsignedByteToInt(rd[ ++offset ]);
            int second = unsignedByteToInt(rd[ ++offset ]);

            Calendar calendar = new GregorianCalendar(year, month - 1, day, hour, minute, second);
            long utc = calendar.getTimeInMillis();
            Json.put(bpmdata, "dts", Simple.timeStampAsISO(utc));

            Log.d(LOGTAG,"parseResponse result=" + hour + "::" + Simple.timeStampAsISO(utc));
        }

        Json.put(bpmdata, "sys", sys);
        Json.put(bpmdata, "dia", dia);
        Json.put(bpmdata, "map", map);

        if ((rd[ 0 ] & 0x04) >= 1)
        {
            Json.put(bpmdata, "pls", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));
        }

        if ((rd[ 0 ] & 0x08) >= 1)
        {
            Json.put(bpmdata, "usr", unsignedByteToInt(rd[ ++offset ]));
        }

        if ((rd[ 0 ] & 0x10) >= 1)
        {
            Json.put(bpmdata, "flg", unsignedByteToInt(rd[ ++offset ]));
        }

        Log.d(LOGTAG,"parseResponse final=" + isfinal + " json=" + bpmdata.toString());

        if (isfinal)
        {
            if (dataCallback != null)
            {
                JSONObject data = new JSONObject();
                Json.put(data, "bpm", bpmdata);

                dataCallback.onBluetoothReceivedData(deviceName, data);
            }

            //
            // Store data.
            //

            JSONObject record = Json.clone(bpmdata);
            HealthData.addRecord("bpm", record);
            HealthData.setLastReadDate("bpm");
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
