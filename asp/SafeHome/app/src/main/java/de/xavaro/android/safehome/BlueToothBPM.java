package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;

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

    private static class BPMs
    {
        public static final String BM75 = "BM75";
        public static final String SBM37 = "SBM37";
        public static final String SBM67 = "SBM67";
        public static final String BEURER_BC57 = "BC57";
        public static final String SANITAS_SBM67 = "BPM Smart";
        public static final String SANITAS_SBM37 = "Sanitas SBM37";
    }

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
    @SuppressLint("NewApi")
    protected void enableDevice()
    {
        Log.d(LOGTAG,"enableDevice: " + currentControl);

        if (currentControl != null)
        {
            GattAction ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_INDICATE;
            ga.characteristic = currentControl;

            gattSchedule.add(ga);

            fireNext();
        }
    }

    @Override
    public void parseResponse(byte[] rd)
    {
        Log.d(LOGTAG, "parseResponse: " + StaticUtils.hexBytesToString(rd));
        Log.d(LOGTAG, "parseResponse: " + getMaskString(rd[ 0 ]));

        int offset = 0;

        Float systolic  = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);
        Float diastolic = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);
        Float meanap    = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);

        Log.d(LOGTAG,"parseResponse:"
            + " systolic=" + systolic
            + " diastolic=" + diastolic
            + " meanap=" + meanap);

        if ((rd[ 0 ] & 0x02) >= 1)
        {
            int year   = unsignedBytesToInt(rd[ ++offset ], rd[ ++offset ]);
            int month  = unsignedByteToInt(rd[ ++offset ]);
            int day    = unsignedByteToInt(rd[ ++offset ]);
            int hour   = unsignedByteToInt(rd[ ++offset ]);
            int minute = unsignedByteToInt(rd[ ++offset ]);
            int second = unsignedByteToInt(rd[ ++offset ]);

            Log.d(LOGTAG,"parseResponse:"
                    + " year=" + year
                    + " month=" + month
                    + " day=" + day
                    + " hour=" + hour
                    + " minute=" + minute
                    + " second=" + second);
        }

        if ((rd[ 0 ] & 0x04) >= 1)
        {
            Float pulse = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);

            Log.d(LOGTAG,"parseResponse:" + " pulse=" + pulse);
        }

        if ((rd[ 0 ] & 0x08) >= 1)
        {
            int user = unsignedByteToInt(rd[ ++offset ]);

            Log.d(LOGTAG,"parseResponse:" + " user=" + user);
        }

        if ((rd[ 0 ] & 0x10) >= 1)
        {
            int flags = unsignedByteToInt(rd[ ++offset ]);

            Log.d(LOGTAG,"parseResponse:" + " flags=" + flags);
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

    //region Conversion helper

    private int unsignedByteToInt(byte b)
    {
        return b & 0xFF;
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

    private int unsignedBytesToInt(byte b0, byte b1)
    {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    //endregion Conversion helper
}
