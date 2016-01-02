package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class BlueToothBPM extends BlueTooth
{
    private static final String LOGTAG = BlueToothBPM.class.getSimpleName();

    public BlueToothBPM(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    private static class BPMs
    {
        public static final String BM75 = "BM75";
        public static final String SBM37 = "SBM37";
        public static final String SBM67 = "SBM67";
        public static final String BEURER_BC57 = "BC57";
        public static final String SANITAS_SBM67 = "BPM Smart";
        public static final String SANITAS_SBM37 = "Sanitas SBM37";
    }

    @SuppressWarnings("unused")
    public static boolean isCompatibleBPM(String devicename)
    {
        return (devicename.equalsIgnoreCase(BPMs.BM75) ||
                devicename.equalsIgnoreCase(BPMs.SBM37) ||
                devicename.equalsIgnoreCase(BPMs.SBM67) ||
                devicename.equalsIgnoreCase(BPMs.BEURER_BC57) ||
                devicename.equalsIgnoreCase(BPMs.SANITAS_SBM67) ||
                devicename.equalsIgnoreCase(BPMs.SANITAS_SBM37));
    }

    @Override
    protected void enableDevice()
    {
        Log.d(LOGTAG,"enableDevice: " + currentControl);

        if (currentControl != null)
        {
            GattAction ga;

            //
            // Subscribe to normal data indication.
            //

            ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_INDICATE;
            ga.characteristic = currentControl;

            gattSchedule.add(ga);

            //
            // Subscribe to intermediate data notification
            // when user is conduction measurement.
            //

            ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_NOTIFY;
            ga.characteristic = currentIntermediate;

            gattSchedule.add(ga);

            fireNext();
        }

        if (connectCallback != null)
        {
            //
            // BPM equippment ist active, when it is connected.
            //

            connectCallback.onBluetoothActive(currentGatt.getDevice());
        }
    }

    @Override
    public void parseResponse(byte[] rd, boolean intermediate)
    {
        Log.d(LOGTAG, "parseResponse: " + StaticUtils.hexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + getMaskString(rd[ 0 ]));

        try
        {
            JSONObject bpmdata = new JSONObject();

            int offset = 0;

            bpmdata.put("result", intermediate ? "intermediate" : "final");

            bpmdata.put("systolic", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));
            bpmdata.put("diastolic", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));
            bpmdata.put("meanap", bytesToFloat(rd[ ++offset ], rd[ ++offset ]));

            if ((rd[ 0 ] & 0x02) >= 1)
            {
                bpmdata.put("year", unsignedBytesToInt(rd[ ++offset ], rd[ ++offset ]));
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

            if (dataCallback != null) dataCallback.onBluetoothReceivedData(currentGatt.getDevice(), data);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
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
