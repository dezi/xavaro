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

        WriteDescriptor(desc);
        WriteCharacteristic(chara);
        ChangedCharacteristic(chara);
        ReadCharacteristic(chara);
    }

    public static String getHexBytesToString(byte[] bytes)
    {
        if (bytes == null) return "..null..";

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

    public static void WriteDescriptor(BluetoothGattDescriptor desc)
    {
        Log.d(LOGTAG, "WriteDescriptor desc=" + desc.getUuid().toString());
        Log.d(LOGTAG, "WriteDescriptor char=" + desc.getCharacteristic().getUuid().toString());
        Log.d(LOGTAG, "WriteDescriptor vals=" + getHexBytesToString(desc.getValue()));
    }

    public static void WriteCharacteristic(BluetoothGattCharacteristic chara)
    {
        Log.d(LOGTAG, "WriteCharacteristic char=" + chara.getUuid().toString());
        Log.d(LOGTAG, "WriteCharacteristic vals=" + getHexBytesToString(chara.getValue()));
    }

    public static void ReadCharacteristic(BluetoothGattCharacteristic chara)
    {
        Log.d(LOGTAG, "ReadCharacteristic char=" + chara.getUuid().toString());
        Log.d(LOGTAG, "ReadCharacteristic vals=" + getHexBytesToString(chara.getValue()));
    }

    public static void ChangedCharacteristic(BluetoothGattCharacteristic chara)
    {
        Log.d(LOGTAG, "ChangedCharacteristic char=" + chara.getUuid().toString());
        Log.d(LOGTAG, "ChangedCharacteristic vals=" + getHexBytesToString(chara.getValue()));
    }
}
