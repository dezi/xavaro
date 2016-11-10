package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.io.File;

import de.xavaro.android.common.HealthData;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

//
// Health data record format:
//
//  dts => ISO timestamp
//  exf => External data file name
//  aty => Analysis type
//  pls => Average puls / heart rate
//  noi => Noise
//  raf => Rhythm abnormal flag
//  waf => Waveform abnormal flag
//  dap => Data present flag
//  dev => Device name
//
// Additional in external file:
//
//  inf => Info record
//  pld => Puls values
//  tim => Puls timing values
//  efi => ECG data filtered
//  ecv => ECG data raw
//

@SuppressWarnings("unused")
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
        //
        // Note: Characteristics is custom and NOT what Bluetooth doc says.
        //

        return characteristic.getUuid().toString().equals("00002a36-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        //
        // Note: Characteristics is custom and NOT what Bluetooth doc says.
        //

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
        super.enableDevice();

        recordsToLoad = new ArrayList<>();
        callbackResults = new ArrayList<>();

        firFilter = new FIRfilter();
        iirFilter = new IIRFilter();

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
        ga.data = getRecordCount();
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

    private JSONArray resultPld;
    private JSONArray resultTim;
    private JSONArray resultDia;
    private JSONArray resultEsz;
    private JSONArray resultEcv;
    private JSONArray resultEfi;

    private ArrayList<byte[]> recordsToLoad;
    private ArrayList<JSONObject> callbackResults;

    private FIRfilter firFilter;
    private IIRFilter iirFilter;

    private void clearBuffers()
    {
        seqerror = false;

        resultBuffer = new byte[ 256 ];

        resultPld = new JSONArray();
        resultTim = new JSONArray();
        resultDia = new JSONArray();
        resultEsz = new JSONArray();
        resultEcv = new JSONArray();
        resultEfi = new JSONArray();

        firFilter.reset();
        iirFilter.reset();
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

            BlueTooth.GattAction ga = new BlueTooth.GattAction();

            if (format == BT_HEADER)
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

            if (format == BT_MEASURE)
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

                        Json.put(resultPld, value);
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

                        if (resultEcv.length() < 8704)
                        {
                            Json.put(resultEcv, value);

                            value = firFilter.filter(iirFilter.filter(value));

                            Json.put(resultEfi, value);
                        }
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

            if (format == BT_DOWNLOAD)
            {
                if (rd.length > 2)
                {
                    //
                    // Record storage.
                    //

                    byte subtype = rd[ 2 ];

                    if (subtype == BT_DOWNLOAD_U1_U2_COUNT)
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

                    if (subtype == BT_DOWNLOAD_HEADER)
                    {
                        //
                        // Record header.
                        //

                        byte subseq = rd[ 3 ];

                        System.arraycopy(rd, 4, resultBuffer, (subseq * 16), 16);

                        if (subseq == 15)
                        {
                            //
                            // We read a complete record header.
                            //

                            generateRecord();

                            if (actrecord < maxrecord)
                            {
                                ga.data = getRecord();
                            }
                            else
                            {
                                ga.data = (recordsToLoad.size() > 0) ? getNextFile() : getEraseAll();
                            }
                        }
                    }

                    if (subtype == BT_DOWNLOAD_RAWD)
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
                                ga.data = getFileRecords();
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
                                // Check for next file or erase flash after download.
                                //

                                ga.data = (recordsToLoad.size() > 0) ? getNextFile() : getEraseAll();
                            }
                        }
                    }
                }
            }

            if (format == BT_ERASE_ALL_FLASH)
            {
                //
                // Erase all response.
                //

                ga.data = getConfiguration();
                gattSchedule.add(ga);

                ga = new GattAction();
                ga.data = getReady();
            }

            if (ga.data != null)
            {
                //
                // Fire next command.
                //

                ga.mode = BlueTooth.GattAction.MODE_WRITE;
                ga.characteristic = currentSecondary;

                gattSchedule.add(ga);
                fireNext(0);
            }
            else
            {
                //
                // No commands to fire, check for data callbacks now.
                //

                while (callbackResults.size() > 0)
                {
                    JSONObject data = callbackResults.remove(0);

                    if (dataCallback != null)
                    {
                        dataCallback.onBluetoothReceivedData(deviceName, data);
                    }
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
        Calendar calendar = new GregorianCalendar();

        calendar.set(readBufferByte(20) + 2000,
                readBufferByte(21) - 1,
                readBufferByte(22),
                readBufferByte(23),
                readBufferByte(24),
                readBufferByte(25));

        String dts = Simple.timeStampAsISO(calendar.getTimeInMillis());

        File resfile = generateFileName();

        JSONObject status = new JSONObject();

        Json.put(status, "Timestamp", dts);
        Json.put(status, "Filename", resfile.getName());
        Json.put(status, "SequenceError", seqerror);
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

    private String generateDatetime()
    {
        return String.format(Locale.ROOT, "%04d%02d%02d.%02d%02d%02d",
                resultBuffer[ 20 ] + 2000,
                resultBuffer[ 21 ],
                resultBuffer[ 22 ],
                resultBuffer[ 23 ],
                resultBuffer[ 24 ],
                resultBuffer[ 25 ]);
    }

    private File generateFileName()
    {
        String filename = "ecg." + generateDatetime() + ".data.json";
        return new File(HealthData.getDataDir(), filename);
    }

    private void generateRecord()
    {
        String datetime = generateDatetime();

        Log.d(LOGTAG, "generateRecord: pos=" + (actrecord + 1) + " seq=" + resultBuffer[ 0 ] + " dts=" + datetime);

        File resfile = generateFileName();

        if (! resfile.exists())
        {
            Log.d(LOGTAG, "generateRecord: new=" + (actrecord + 1) + " seq=" + resultBuffer[ 0 ] + " dts=" + datetime);

            recordsToLoad.add(resultBuffer);
            resultBuffer = new byte[ 256 ];
        }

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

            value = firFilter.filter(iirFilter.filter(value));

            Json.put(resultEfi, value);
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
            Json.put(resultPld, value);
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

        generateResult();
    }

    private void generateResult()
    {
        JSONObject ecgdata = new JSONObject();
        JSONObject status = generateStatus();

        boolean dataPresent = (resultEfi.length() > 1000) && (resultPld.length() > 5);

        Json.put(ecgdata, "dts", Json.getString(status, "Timestamp"));
        Json.put(ecgdata, "exf", Json.getString(status, "Filename"));
        Json.put(ecgdata, "aty", Json.getInt(status, "AnalysisType"));
        Json.put(ecgdata, "pls", Json.getInt(status, "HeartRate"));
        Json.put(ecgdata, "noi", Json.getInt(status, "Noise"));
        Json.put(ecgdata, "raf", Json.getInt(status, "Rhythm"));
        Json.put(ecgdata, "waf", Json.getInt(status, "Waveform"));
        Json.put(ecgdata, "dap", dataPresent ? 1 : 0);
        Json.put(ecgdata, "dev", deviceName);

        //
        // Schedule data for application callback.
        //

        if (dataCallback != null)
        {
            JSONObject data = new JSONObject();
            Json.put(data, "ecg", Json.clone(ecgdata));

            callbackResults.add(data);
        }

        //
        // Store data.
        //

        JSONObject record = Json.clone(ecgdata);

        HealthData.addRecord("ecg", record);
        HealthData.setLastReadDate("ecg");

        //
        // Add file only data.
        //

        Json.put(ecgdata, "inf", status);

        Json.put(ecgdata, "pld", resultPld);
        Json.put(ecgdata, "tim", resultTim);
        Json.put(ecgdata, "efi", resultEfi);
        Json.put(ecgdata, "ecv", resultEcv);

        //Json.put(ecgdata, "dia", resultDia);
        //Json.put(ecgdata, "esz", resultEsz);

        File resfile = generateFileName();
        Simple.putFileJSON(resfile, ecgdata);

        Log.d(LOGTAG, "generateResult: file=" + resfile);

        clearBuffers();
    }

    private static final byte BT_CONFIG_INFO = 7;
    private static final byte BT_CONFIG_INFO_DEVICE = 1;
    private static final byte BT_CONFIG_INFO_SETTING = 2;
    private static final byte BT_DOWNLOAD = 4;
    private static final byte BT_DOWNLOAD_HEADER = 2;
    private static final byte BT_DOWNLOAD_RAWD = 3;
    private static final byte BT_DOWNLOAD_U1_U2_COUNT = 1;
    private static final byte BT_DOWNLOAD_WAIT = 0;
    private static final byte BT_ERASE_ALL_FLASH = 6;
    private static final byte BT_HEADER = 1;
    private static final byte BT_MEASURE = 3;
    private static final byte BT_SETUP = 5;
    private static final byte BT_SETUP_700X = 2;
    private static final byte BT_SETUP_ID = 1;
    private static final byte BT_STANDBY = 2;
    private static final byte BT_START = 8;
    private static final byte BT_START_BP = 1;
    private static final byte BT_START_BP_ECG = 3;
    private static final byte BT_START_ECG = 2;
    private static final byte BT_WAIT = 0;

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

    private byte[] getNextFile()
    {
        if (recordsToLoad.size() == 0) return null;

        byte[] header = recordsToLoad.remove(0);

        Log.d(LOGTAG, "getNextFile: seq=" + header[ 0 ]);

        actfile = header[ 0 ];
        actrecord = 0;

        return getFileRecords();
    }

    private byte[] getFileRecords()
    {
        Log.d(LOGTAG, "getFileRecords");

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

        data[ 1 ] = BT_SETUP;
        data[ 2 ] = BT_SETUP_700X;
        data[ 3 ] = year;
        data[ 4 ] = month;
        data[ 5 ] = day;
        data[ 6 ] = hour;
        data[ 7 ] = minute;
        data[ 8 ] = second;
        data[ 9 ] = 1; // Key beep
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

    private class FIRfilter
    {
        private static final int FIR_SIZE = 29;

        private final double[] Bcoff = new double[]
                {
                        -0.000548709320230d,
                        -0.001861858218000d,
                        -0.003197339503612d,
                        -0.002018976092472d,
                        +0.003614763960978d,
                        +0.011297422965327d,
                        +0.013585528900925d,
                        +0.002771137785404d,
                        -0.020484387472122d,
                        -0.041602859844199d,
                        -0.036772038992183d,
                        +0.011973653974951d,
                        +0.100565210202073d,
                        +0.198612358279079d,
                        +0.263134308468351d,
                        +0.263134308468351d,
                        +0.198612358279079d,
                        +0.100565210202073d,
                        +0.011973653974951d,
                        -0.036772038992183d,
                        -0.041602859844199d,
                        -0.020484387472122d,
                        +0.002771137785404d,
                        +0.013585528900925d,
                        +0.011297422965327d,
                        +0.003614763960978d,
                        -0.002018976092472d,
                        -0.003197339503612d,
                        -0.001861858218000d,
                        -0.000548709320230d
                };

        private final short[] inputs = new short[ FIR_SIZE ];

        public void reset()
        {
            for (int inx = 0; inx < inputs.length; inx++)
            {
                inputs[ inx ] = (short) 0;
            }
        }

        public short filter(short input)
        {
            double output = Bcoff[ 0 ] * (double) input;

            for (int inx = FIR_SIZE - 1; inx >= 0; inx--)
            {
                output += Bcoff[ inx + 1 ] * (double) inputs[ inx ];

                if (inx > 0)
                {
                    inputs[ inx ] = inputs[ inx - 1 ];
                }
            }

            inputs[ 0 ] = input;

            return (short) ((int) output);
        }
    }

    private class IIRFilter
    {
        private final double[] Bcoff = new double[ 2 ];
        private double Acoff;
        private short previous;
        private double output;

        public void reset()
        {
            int Fs = 256;
            float Fc = 0.8f;

            double tan_term = Math.tan((Math.PI * ((double) Fc)) / ((double) Fs));

            double k1 = 1.0d / (1.0d + tan_term);
            double k2 = (1.0d - tan_term) * k1;

            Bcoff[0] = k2;
            Bcoff[1] = -1.0d * k2;

            Acoff = -1.0d * k1;

            output = 0.0d;
            previous = (short) 0;
        }

        public short filter(short input)
        {
            output = ((Bcoff[ 0 ] * ((double) input)) + (Bcoff[ 1 ] * ((double) previous))) - (Acoff * output);

            previous = input;
            return (short) ((int) output);
        }
    }
}
