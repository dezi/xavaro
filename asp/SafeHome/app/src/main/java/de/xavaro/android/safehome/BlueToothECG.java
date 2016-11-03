package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
        return characteristic.getUuid().toString().equals("00002a36-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("00002a37-0000-1000-8000-00805f9b34fb");
    }

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
        BlueTooth.GattAction ga;

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getConfiguration();
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);

        fireNext(false);
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        if (characteristic == currentPrimary)
        {
            byte sequence = rd[ 0 ];
            byte format = rd[ 1 ];
            byte subtype;
            int value;

            if (format == 3)
            {
                for (int inx = 4; inx < 20; inx += 4)
                {
                    subtype = rd[ inx ];

                    value = ((rd[ inx + 2 ] & 0xff) << 8) + (rd[ inx + 3 ] & 0xff);

                    if (subtype == 70)
                    {
                        //
                        // Puls
                        //

                        Log.d(LOGTAG, "parseResponse: pls=" + value);
                    }

                    if (subtype == 71)
                    {
                        //
                        // Diastolic
                        //

                        Log.d(LOGTAG, "parseResponse: dia=" + value);
                    }

                    if (subtype == 72)
                    {
                        //
                        // Timestamp
                        //

                        Log.d(LOGTAG, "parseResponse: tim=" + value);
                    }

                    if (subtype == 73)
                    {
                        //
                        // Raw voltage
                        //

                        //Log.d(LOGTAG, "parseResponse: ecv=" + value);
                    }

                    if (subtype == 74)
                    {
                        //
                        // ECG size
                        //

                        Log.d(LOGTAG, "parseResponse: siz=" + value);
                    }
                }
            }
        }
    }

    private byte[] getConfiguration()
    {
        Log.d(LOGTAG, "getConfiguration");

        Calendar date = new GregorianCalendar();

        byte year = (byte) (date.get(Calendar.YEAR) - 100);
        byte month = (byte) (date.get(Calendar.MONTH) + 1);
        byte day = (byte) date.get(Calendar.DAY_OF_MONTH);
        byte hour = (byte) date.get(Calendar.HOUR_OF_DAY);
        byte minute = (byte) date.get(Calendar.MINUTE);
        byte second = (byte) date.get(Calendar.SECOND);

        byte[] data = new byte[ 20 ];

        data[  1 ] = 5;
        data[  2 ] = 2;
        data[  3 ] = year;
        data[  4 ] = month;
        data[  5 ] = day;
        data[  6 ] = hour;
        data[  7 ] = minute;
        data[  8 ] = second;
        data[  9 ] = 1; // Key beep
        data[ 10 ] = 1; // Heartbeat beep
        data[ 11 ] = 1; // 24h display ???

        return data;
    }

    private byte[] getInfo1()
    {
        Log.d(LOGTAG, "getInfo1");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = 7;
        data[ 2 ] = 1;

        return data;
    }

    private byte[] getInfo2()
    {
        Log.d(LOGTAG, "getInfo2");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = 7;
        data[ 2 ] = 2;

        return data;
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }
}
