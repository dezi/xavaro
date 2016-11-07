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

        /*
        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getRecordCount();
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);
        */

        actfile = 2;

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.data = getFile();
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);

        fireNext(0);
    }

    private int sequence;
    private boolean seqerror;

    private int actrecord;
    private int maxrecord;

    private int actfile;

    private byte[] resultBuffer;
    private byte[] rawData;

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

                    generateResult(true);
                }
            }

            if (format == 3)
            {
                //
                // Measurement record.
                //

                int channel;
                short value;

                for (int inx = 4; inx < 20; inx += 4)
                {
                    channel = rd[ inx ];

                    value = (short) (((rd[ inx + 2 ] & 0xff) << 8) + ((rd[ inx + 3 ] & 0xff)));

                    if (channel == 70)
                    {
                        //
                        // Puls
                        //

                        Json.put(resultPls, value);
                    }

                    if (channel == 71)
                    {
                        //
                        // Diastolic
                        //

                        Json.put(resultDia, value);
                    }

                    if (channel == 72)
                    {
                        //
                        // Timestamp
                        //

                        Json.put(resultTim, value);
                    }

                    if (channel == 73)
                    {
                        //
                        // Raw voltage
                        //

                        if (resultEcv.length() < 8704) Json.put(resultEcv, value);
                    }

                    if (channel == 74)
                    {
                        //
                        // ECG size
                        //

                        Json.put(resultEsz, value);
                    }
                }
            }

            if (format == 4)
            {
                //
                // Record storage.
                //

                BlueTooth.GattAction ga = new BlueTooth.GattAction();

                byte subtype = rd[ 2 ];

                if (subtype == 1)
                {
                    //
                    // Number of records.
                    //

                    int numRecs1 = rd[ 3 ];
                    int numRecs2 = rd[ 4 ];

                    actrecord = 0;
                    maxrecord = numRecs1 + numRecs2;

                    Log.d(LOGTAG, "parseResponse: nr1=" + numRecs1 + " nr2=" + numRecs2);

                    ga.data = (actrecord < maxrecord) ? getRecord() : getReady();
                }

                if (subtype == 2)
                {
                    //
                    // Record header.
                    //

                    byte subseq = rd[ 3 ];

                    System.arraycopy(rd, 4, resultBuffer, (subseq * 16), 16);

                    if (subseq == 15)
                    {
                        //
                        // We have some result.
                        //

                        generateRecord();

                        ga.data = (actrecord < maxrecord) ? getRecord() : getReady();
                    }
                }

                if (subtype == 3)
                {
                    //
                    // Record data.
                    //

                    byte subseq = rd[ 3 ];

                    System.arraycopy(rd, 4, resultBuffer, (subseq * 16), 16);

                    if (subseq == 15)
                    {
                        //
                        // Copy chunk into raw data.
                        //

                        if (rawData == null) rawData = new byte[ 80 * 256 ];
                        System.arraycopy(resultBuffer, 0, rawData, (actrecord * 256), 256);

                        actrecord++;

                        if (actrecord < 80)
                        {
                            ga.data = getFile();
                        }
                        else
                        {
                            //
                            // Store file.
                            //

                            generateFile();

                            rawData = null;
                            actrecord = 0;

                            //
                            // Check for next file.
                            //

                            ga.data = getReady();
                        }
                    }
                }

                //
                // Fire next command.
                //

                if (ga.data != null)
                {
                    ga.mode = BlueTooth.GattAction.MODE_WRITE;
                    ga.characteristic = currentSecondary;

                    gattSchedule.add(ga);
                    fireNext(0);
                }
            }
        }
    }

    private int readBufferByte(int index)
    {
        return resultBuffer[ index ] & 0xff;
    }

    private int readBufferShort(int index)
    {
        return (short) ((resultBuffer[ index ] & 0xff) + ((resultBuffer[ index + 1 ] & 0xff) << 8));
    }

    private int readBufferInt(int index)
    {
        return (resultBuffer[ index ] & 0xff)
                + ((resultBuffer[ index + 1 ] & 0xff) << 8)
                + ((resultBuffer[ index + 2 ] & 0xff) << 16)
                + ((resultBuffer[ index + 3 ] & 0xff) << 24)
                ;
    }

    private String readBufferDigits(int index, int count)
    {
        String res = "";

        for (int inx = 0; inx < count; inx++)
        {
            res += String.format(Locale.ROOT, "%02d", resultBuffer[ index + inx ]);
        }

        return res;
    }

    private JSONObject generateStatus()
    {
        JSONObject status = new JSONObject();

        Json.put(status, "Sequence", readBufferByte(0));
        Json.put(status, "Signature", readBufferDigits(1, 3));
        Json.put(status, "FirmwareVersion", readBufferShort(4));
        Json.put(status, "HardwareVersion", readBufferShort(6));
        Json.put(status, "HeaderSize", readBufferShort(8));
        Json.put(status, "VersionTag", readBufferDigits(10, 6));
        Json.put(status, "DeviceID", readBufferInt(16));
        Json.put(status, "DateYear", readBufferByte(20));
        Json.put(status, "DateMonth", readBufferByte(21));
        Json.put(status, "DateDay", readBufferByte(22));
        Json.put(status, "DateHour", readBufferByte(23));
        Json.put(status, "DateMinute", readBufferByte(24));
        Json.put(status, "DateSecond", readBufferByte(25));
        Json.put(status, "SamplingRate", readBufferShort(26));
        Json.put(status, "GainSetting", readBufferByte(28));
        Json.put(status, "Resolution", readBufferByte(29));
        Json.put(status, "Noise", readBufferByte(30));
        Json.put(status, "PhysicalMinimum", readBufferShort(31));
        Json.put(status, "PhysicalMaximum", readBufferShort(33));
        Json.put(status, "DigitalMinimum", readBufferShort(35));
        Json.put(status, "DigitalMaximum", readBufferShort(37));
        Json.put(status, "Prefiltering", readBufferByte(39));
        Json.put(status, "TotalSize", readBufferInt(40));
        Json.put(status, "UserMode", readBufferByte(44));
        Json.put(status, "RSensitivity", readBufferByte(45));
        Json.put(status, "WSensitivity", readBufferByte(46));
        Json.put(status, "HeartRate", readBufferByte(47));
        Json.put(status, "Tachycardia", readBufferByte(48));
        Json.put(status, "Bradycardia", readBufferByte(49));
        Json.put(status, "Pause", readBufferByte(50));
        Json.put(status, "PauseValue", readBufferByte(51));
        Json.put(status, "Rhythm", readBufferByte(52));
        Json.put(status, "Waveform", readBufferByte(53));
        Json.put(status, "WaveformStable", readBufferByte(54));
        Json.put(status, "EntryPosition", readBufferByte(55));
        Json.put(status, "TachycardiaValue", readBufferByte(56));
        Json.put(status, "BradycardiaValue", readBufferByte(57));
        Json.put(status, "MID", readBufferInt(58));
        Json.put(status, "BPMNoiseFlag", readBufferByte(62));
        Json.put(status, "BPHeartRate", readBufferByte(63));
        Json.put(status, "HighBloodPressure", readBufferShort(64));
        Json.put(status, "LowBloodPressure", readBufferShort(66));
        Json.put(status, "WHOIndicate", readBufferByte(68));
        Json.put(status, "DCValue", readBufferShort(69));
        Json.put(status, "AnalysisType", readBufferByte(71));
        Json.put(status, "CheckSum", readBufferByte(255));

        return status;
    }

    private void generateRecord()
    {
        Calendar calendar = new GregorianCalendar();

        calendar.set(resultBuffer[ 20 ] + 2000,
                resultBuffer[ 21 ] - 1,
                resultBuffer[ 22 ],
                resultBuffer[ 23 ],
                resultBuffer[ 24 ],
                resultBuffer[ 25 ]);

        String dts = Simple.timeStampAsISO(calendar.getTimeInMillis());

        String datetime = String.format(Locale.ROOT, "%04d%02d%02d.%02d%02d%02d",
                resultBuffer[ 20 ] + 2000,
                resultBuffer[ 21 ],
                resultBuffer[ 22 ],
                resultBuffer[ 23 ],
                resultBuffer[ 24 ],
                resultBuffer[ 25 ]);

        Log.d(LOGTAG, "generateRecord: pos=" + (actrecord + 1) + " seq=" + resultBuffer[ 0 ] + " dts=" + datetime);

        actrecord++;
    }

    private void generateFile()
    {
        //
        // Get record header from raw data.
        //

        System.arraycopy(rawData, 0, resultBuffer, 0, 256);

        //
        // Copy ECG data from raw data.
        //

        int offset = 256;

        for (int inx = 0; inx < 8704; inx++)
        {
            short value = (short) (((rawData[ offset++ ] & 0xff) << 8) + (rawData[ offset++ ] & 0xff));

            Json.put(resultEcv, value);
        }

        //
        // Puls data (skip first value).
        //

        offset = 256 + (8704 * 2);

        for (int inx = 1; inx < 128; inx++)
        {
            short value = (short) (((rawData[ offset++ ] & 0xff) << 8) + (rawData[ offset++ ] & 0xff));
            if (value == -1) break;

            value = (short) (60000 / value);
            Json.put(resultPls, value);
        }

        //
        // Puls time (skip first value).
        //

        offset = 256 + (8704 * 2) + (5 * 256);

        for (int inx = 1; inx < 128; inx++)
        {
            short value = (short) (((rawData[ offset++ ] & 0xff) << 8) + (rawData[ offset++ ] & 0xff));
            if (value == -1) break;

            //
            // Timing value in storage is
            // for some reason one less.

            Json.put(resultTim, value + 1);
        }

        //
        // Dump all trailers.
        //

        /*
        for (int trailer = 69; trailer < 80; trailer++)
        {
            JSONArray list = new JSONArray();

            offset = trailer * 256;

            for (int inx = 0; inx < 128; inx++)
            {
                short value = (short) (((rawData[ offset++ ] & 0xff) << 8) + (rawData[ offset++ ] & 0xff));
                Json.put(list, value);
            }

            Json.put(resultDia, list);
        }
        */

        generateResult(false);
    }

    private void generateResult(boolean online)
    {
        JSONObject result = new JSONObject();

        Calendar calendar = new GregorianCalendar();

        calendar.set(resultBuffer[ 20 ] + 2000,
                resultBuffer[ 21 ] - 1,
                resultBuffer[ 22 ],
                resultBuffer[ 23 ],
                resultBuffer[ 24 ],
                resultBuffer[ 25 ]);

        String dts = Simple.timeStampAsISO(calendar.getTimeInMillis());

        String datetime = String.format(Locale.ROOT, "%04d%02d%02d.%02d%02d%02d",
                resultBuffer[ 20 ] + 2000,
                resultBuffer[ 21 ],
                resultBuffer[ 22 ],
                resultBuffer[ 23 ],
                resultBuffer[ 24 ],
                resultBuffer[ 25 ]);

        Json.put(result, "dts", dts);
        Json.put(result, "inf", generateStatus());

        Json.put(result, "pls", resultPls);
        Json.put(result, "tim", resultTim);
        Json.put(result, "dia", resultDia);
        Json.put(result, "esz", resultEsz);
        Json.put(result, "ecv", resultEcv);

        String resname = "ecg." + datetime + (online ? ".online" : ".download") + ".json";
        File resfile = new File(Simple.getExternalFilesDir(), resname);

        Simple.putFileJSON(resfile, result);

        Log.d(LOGTAG, "parseResponse: file=" + resfile);

        clearBuffers();
    }

    public static final byte BT_CONFIG_INFO = 7;
    public static final byte BT_CONFIG_INFO_DEVICE = 1;
    public static final byte BT_CONFIG_INFO_SETTING = 2;
    public static final byte BT_DOWNLOAD = 4;
    public static final byte BT_DOWNLOAD_HEADER = 2;
    public static final byte BT_DOWNLOAD_RAWD = 3;
    public static final byte BT_DOWNLOAD_U1_U2_COUNT = 1;
    public static final byte BT_DOWNLOAD_WAIT = 0;
    public static final byte BT_ERASE_ALL_FLASH = 6;
    public static final byte BT_HEADER = 1;
    public static final byte BT_MEASURE = 3;
    public static final byte BT_SETUP = 5;
    public static final byte BT_SETUP_700X = 2;
    public static final byte BT_SETUP_ID = 1;
    public static final byte BT_STANDBY = 2;
    public static final byte BT_START = 8;
    public static final byte BT_START_BP = 1;
    public static final byte BT_START_BP_ECG = 3;
    public static final byte BT_START_ECG = 2;
    public static final byte BT_WAIT = 0;

    private byte[] getEraseAll()
    {
        Log.d(LOGTAG, "getEraseAll");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = BT_ERASE_ALL_FLASH;

        return data;
    }

    private byte[] getRecordCount()
    {
        Log.d(LOGTAG, "getRecordCount");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = BT_DOWNLOAD;
        data[ 2 ] = BT_DOWNLOAD_U1_U2_COUNT;

        return data;
    }

    private byte[] getRecord()
    {
        Log.d(LOGTAG, "getRecord");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = BT_DOWNLOAD;
        data[ 2 ] = BT_DOWNLOAD_HEADER;
        data[ 3 ] = (byte) actrecord;

        return data;
    }

    private byte[] getFile()
    {
        Log.d(LOGTAG, "getFile");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = BT_DOWNLOAD;
        data[ 2 ] = BT_DOWNLOAD_RAWD;
        data[ 3 ] = 0;
        data[ 4 ] = (byte) actfile;
        data[ 5 ] = (byte) actrecord;

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

        data[  1 ] = BT_SETUP;
        data[  2 ] = BT_SETUP_700X;
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

        data[ 1 ] = BT_START_ECG;

        return data;
    }

    private byte[] getInfoDevice()
    {
        Log.d(LOGTAG, "getInfoDevice");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = BT_CONFIG_INFO;
        data[ 2 ] = BT_CONFIG_INFO_DEVICE;

        return data;
    }

    private byte[] getInfoSetting()
    {
        Log.d(LOGTAG, "getInfoSetting");

        byte[] data = new byte[ 20 ];

        data[ 1 ] = BT_CONFIG_INFO;
        data[ 2 ] = BT_CONFIG_INFO_SETTING;

        return data;
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }
}
