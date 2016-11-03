package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

//
// Health data record format:
//
//  dts => ISO timestamp
//  sat => Oxygene saturation
//  pls => Pulse
//

public class BlueToothECG extends BlueTooth
{
    private static final String LOGTAG = BlueToothECG.class.getSimpleName();

    public BlueToothECG(Context context)
    {
        super(context);
    }

    public BlueToothECG(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("0000a000-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a37-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a35-0000-1000-8000-00805f9b34fb");
    }

    private BluetoothGattCharacteristic c2;
    private BluetoothGattCharacteristic c3;

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    @Override
    protected void enableDevice()
    {
        if (currentPrimary != null) gattSchedule.add(new GattAction(currentPrimary));
        if (currentSecondary != null) gattSchedule.add(new GattAction(currentSecondary));
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

        /*
        if (characteristic == currentSecondary)
        {
            JSONObject oxydata = new JSONObject();

            Json.put(oxydata, "type", "OxyMeasurement");

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
        */
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }
}
