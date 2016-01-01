package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

public class BlueToothBPM
{
    private static final String LOGTAG = BlueToothBPM.class.getSimpleName();

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

    public BlueToothBPM(String model)
    {
        this.model = model;
    }

    private String model;

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

    private int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3)
    {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    private int unsignedBytesToInt(byte b0, byte b1)
    {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    public boolean parseData(byte[] rd)
    {
        Log.d(LOGTAG, "parseData: " + rd[ 0 ] + " " + rd[ 1 ]);
        Log.d(LOGTAG, "parseData: " + StaticUtils.hexBytesToString(rd));

        Log.d(LOGTAG,"parseData: " + getMaskString(rd[ 0 ]));

        int offset = 0;

        Float systolic  = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);
        Float diastolic = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);
        Float meanap    = bytesToFloat(rd[ ++offset ], rd[ ++offset ]);

        Log.d(LOGTAG,"parseData:"
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

            Log.d(LOGTAG,"parseData:"
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

            Log.d(LOGTAG,"parseData:" + " pulse=" + pulse);
        }

        if ((rd[ 0 ] & 0x08) >= 1)
        {
            int user = unsignedByteToInt(rd[ ++offset ]);

            Log.d(LOGTAG,"parseData:" + " user=" + user);
        }

        if ((rd[ 0 ] & 0x10) >= 1)
        {
            int flags = unsignedByteToInt(rd[ ++offset ]);

            Log.d(LOGTAG,"parseData:" + " flags=" + flags);
        }

        return false;
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
