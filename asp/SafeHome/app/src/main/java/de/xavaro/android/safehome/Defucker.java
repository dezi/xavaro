package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

public class Defucker
{
    public static final String LOGTAG = Defucker.class.getSimpleName();

    public static void Sample()
    {

        BluetoothGattDescriptor desc = null;
        BluetoothGattCharacteristic chara = null;

        DumpDescriptor(desc);
        DumpCharacteristic(chara);
    }

    public static String getHexBytesToString(byte[] bytes)
    {
        int offset = 0;
        int length = bytes.length;

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[ length << 1 ];

        for (int inx = offset; inx < (length + offset); inx++)
        {
            //noinspection PointlessArithmeticExpression
            hexChars[ ((inx - offset) << 1) + 0 ] = hexArray[ (bytes[ inx ] >> 4) & 0x0f ];
            //noinspection PointlessBitwiseExpression
            hexChars[ ((inx - offset) << 1) + 1 ] = hexArray[ (bytes[ inx ] >> 0) & 0x0f ];
        }

        return String.valueOf(hexChars);
    }

    public static void DumpDescriptor(BluetoothGattDescriptor desc)
    {
        Log.d(LOGTAG, "DumpDescriptor desc=" + desc.getUuid().toString());
        Log.d(LOGTAG, "DumpDescriptor char=" + desc.getCharacteristic().getUuid().toString());
        Log.d(LOGTAG, "DumpDescriptor vals=" + getHexBytesToString(desc.getValue()));
    }

    public static void DumpCharacteristic(BluetoothGattCharacteristic chara)
    {
        Log.d(LOGTAG, "DumpCharacteristic char=" + chara.getUuid().toString());
        Log.d(LOGTAG, "DumpCharacteristic vals=" + getHexBytesToString(chara.getValue()));
    }
}
