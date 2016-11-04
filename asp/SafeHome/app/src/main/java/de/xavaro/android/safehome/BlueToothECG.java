package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.io.File;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

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

        sequence = 0;

        clearBuffers();
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

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getStorageStatus();
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);

        fireNext(0);
    }

    private int sequence;
    private boolean seqerror;
    private int maxrecord;
    private int actrecord;
    private byte[] resultBuffer;

    private JSONArray resultPls;
    private JSONArray resultTim;
    private JSONArray resultDia;
    private JSONArray resultEsz;
    private JSONArray resultEcv;

    private void clearBuffers()
    {
        seqerror = false;

        resultBuffer = new byte[ 256 ];

        resultPls = new JSONArray();
        resultTim = new JSONArray();
        resultDia = new JSONArray();
        resultEsz = new JSONArray();
        resultEcv = new JSONArray();
    }

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        if (characteristic == currentPrimary)
        {
            byte recseq = rd[ 0 ];
            byte format = rd[ 1 ];

            if (sequence != recseq) seqerror = true;
            sequence = recseq + 1;

            if (format == 1)
            {
                //
                // Status record.
                //

                byte subseq = rd[ 2 ];

                System.arraycopy(rd, 4, resultBuffer, (subseq * 16), 16);

                if (subseq == 15)
                {
                    //
                    // We have some result.
                    //

                    generateResult();
                }
            }

            if (format == 3)
            {
                //
                // Measurement record.
                //

                int channel;
                int value;

                for (int inx = 4; inx < 20; inx += 4)
                {
                    channel = rd[ inx ];

                    value = ((rd[ inx + 2 ] & 0xff) << 8) + ((rd[ inx + 3 ] & 0xff));

                    if (channel == 70)
                    {
                        //
                        // Puls
                        //

                        Json.put(resultPls, (short) value);
                    }

                    if (channel == 71)
                    {
                        //
                        // Diastolic
                        //

                        Json.put(resultDia, (short) value);
                    }

                    if (channel == 72)
                    {
                        //
                        // Timestamp
                        //

                        Json.put(resultTim, (short) value);
                    }

                    if (channel == 73)
                    {
                        //
                        // Raw voltage
                        //

                        Json.put(resultEcv, (short) value);
                    }

                    if (channel == 74)
                    {
                        //
                        // ECG size
                        //

                        Json.put(resultEsz, (short) value);
                    }
                }
            }

            if (format == 4)
            {
                //
                // Storage record.
                //

                boolean firenext = false;

                byte subtype = rd[ 2 ];

                if (subtype == 1)
                {
                    int numRecs1 = rd[ 3 ];
                    int numRecs2 = rd[ 4 ];

                    actrecord = 0;
                    maxrecord = numRecs1;

                    Log.d(LOGTAG, "parseResponse: nr1=" + numRecs1 + " nr2=" + numRecs2);

                    firenext = true;
                }

                if (subtype == 2)
                {
                    byte subseq = rd[ 3 ];

                    System.arraycopy(rd, 4, resultBuffer, (subseq * 16), 16);

                    if (subseq == 15)
                    {
                        //
                        // We have some result.
                        //

                        generateStored();

                        firenext = true;
                    }
                }

                //
                // Fire next command.
                //

                if (firenext)
                {
                    BlueTooth.GattAction ga = new BlueTooth.GattAction();

                    ga.mode = BlueTooth.GattAction.MODE_WRITE;
                    ga.characteristic = currentSecondary;
                    ga.data = (actrecord < maxrecord) ? getStorage() : getReady();

                    gattSchedule.add(ga);
                    fireNext(0);
                }
            }
        }
    }

    private int getBufferByte(int index)
    {
        return resultBuffer[ index ] & 0xff;
    }

    private int getBufferShort(int index)
    {
        return (resultBuffer[ index ] & 0xff) + ((resultBuffer[ index + 1 ] & 0xff) << 8);
    }

    private int getBufferInt(int index)
    {
        return (resultBuffer[ index ] & 0xff)
                + ((resultBuffer[ index + 1 ] & 0xff) << 8)
                + ((resultBuffer[ index + 2 ] & 0xff) << 16)
                + ((resultBuffer[ index + 3 ] & 0xff) << 24)
                ;
    }

    private String getBufferDigits(int index, int count)
    {
        String res = "";

        for (int inx = 0; inx < count; inx++)
        {
            res += String.format(Locale.ROOT, "%02d", resultBuffer[ index + inx ]);
        }

        return res;
    }

    private void generateStatus()
    {
        JSONObject status = new JSONObject();

        Json.put(status, "Sequence", getBufferByte(0));
        Json.put(status, "Signature", getBufferDigits(1, 3));
        Json.put(status, "FirmwareVersion", getBufferShort(4));
        Json.put(status, "HardwareVersion", getBufferShort(6));
        Json.put(status, "HeaderSize", getBufferShort(8));
        Json.put(status, "VersionTag", getBufferDigits(10, 6));
        Json.put(status, "DeviceID", getBufferInt(16));
        Json.put(status, "DateYear", getBufferByte(20));
        Json.put(status, "DateMonth", getBufferByte(21));
        Json.put(status, "DateDay", getBufferByte(22));
        Json.put(status, "DateHour", getBufferByte(23));
        Json.put(status, "DateMinute", getBufferByte(24));
        Json.put(status, "DateSecond", getBufferByte(25));
        Json.put(status, "SamplingRate", getBufferShort(26));
        Json.put(status, "GainSetting", getBufferByte(28));
        Json.put(status, "Resolution", getBufferByte(29));
        Json.put(status, "Noise", getBufferByte(30));
        Json.put(status, "PhysicalMinimum", getBufferShort(31));
        Json.put(status, "PhysicalMaximum", getBufferShort(33));
        Json.put(status, "DigitalMinimum", getBufferShort(35));
        Json.put(status, "DigitalMaximum", getBufferShort(37));
        Json.put(status, "Prefiltering", getBufferByte(39));
        Json.put(status, "TotalSize", getBufferInt(40));
        Json.put(status, "UserMode", getBufferByte(44));
        Json.put(status, "RSensitivity", getBufferByte(45));
        Json.put(status, "WSensitivity", getBufferByte(46));
        Json.put(status, "HeartRate", getBufferByte(47));
        Json.put(status, "Tachycardia", getBufferByte(48));
        Json.put(status, "Bradycardia", getBufferByte(49));
        Json.put(status, "Pause", getBufferByte(50));
        Json.put(status, "PauseValue", getBufferByte(51));
        Json.put(status, "Rhythm", getBufferByte(52));
        Json.put(status, "Waveform", getBufferByte(53));
        Json.put(status, "WaveformStable", getBufferByte(54));
        Json.put(status, "EntryPosition", getBufferByte(55));
        Json.put(status, "TachycardiaValue", getBufferByte(56));
        Json.put(status, "BradycardiaValue", getBufferByte(57));
        Json.put(status, "MID", getBufferInt(58));
        Json.put(status, "BPMNoiseFlag", getBufferByte(62));
        Json.put(status, "BPHeartRate", getBufferByte(63));
        Json.put(status, "HighBloodPressure", getBufferShort(64));
        Json.put(status, "LowBloodPressure", getBufferShort(66));
        Json.put(status, "WHOIndicate", getBufferByte(68));
        Json.put(status, "DCValue", getBufferShort(69));
        Json.put(status, "AnalysisType", getBufferByte(71));
        Json.put(status, "CheckSum", getBufferByte(255));
    }

    private void generateStored()
    {
        String datetime = String.format(Locale.ROOT, "%02d.%02d.%02d.%02d.%02d.%02d",
                resultBuffer[ 20 ],
                resultBuffer[ 21 ],
                resultBuffer[ 22 ],
                resultBuffer[ 23 ],
                resultBuffer[ 24 ],
                resultBuffer[ 25 ]);

        Log.d(LOGTAG, "generateStored: pos=" + (actrecord + 1) + " seq=" + resultBuffer[ 0 ] + " dts=" + datetime);

        actrecord++;
    }

    private void generateResult()
    {
        JSONObject result = new JSONObject();

        String datetime = String.format(Locale.ROOT, "%02d.%02d.%02d.%02d.%02d.%02d",
                resultBuffer[ 20 ],
                resultBuffer[ 21 ],
                resultBuffer[ 22 ],
                resultBuffer[ 23 ],
                resultBuffer[ 24 ],
                resultBuffer[ 25 ]);

        Json.put(result, "dts", datetime);

        Json.put(result, "pls", resultPls);
        Json.put(result, "tim", resultTim);
        Json.put(result, "dia", resultDia);
        Json.put(result, "esz", resultEsz);
        Json.put(result, "ecv", resultEcv);

        String resname = "ecg." + Simple.nowAsISO() + ".json";
        File resfile = new File(Simple.getExternalFilesDir(), resname);

        Simple.putFileJSON(resfile, result);

        Log.d(LOGTAG, "parseResponse: file=" + resfile);

        clearBuffers();
    }

    private byte[] getStorageStatus()
    {
        Log.d(LOGTAG, "getStorageStatus");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = 4;
        data[ 2 ] = 1;

        return data;
    }

    private byte[] getStorage()
    {
        Log.d(LOGTAG, "getStorage");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = 4;
        data[ 2 ] = 2;
        data[ 3 ] = (byte) actrecord;

        return data;
    }

    private byte[] getConfiguration()
    {
        Log.d(LOGTAG, "getConfiguration");

        Calendar date = new GregorianCalendar();

        byte year = (byte) (date.get(Calendar.YEAR) - 2000);
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

    private byte[] getReady()
    {
        Log.d(LOGTAG, "getReady");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = 2;

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
