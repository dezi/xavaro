package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

//
// Health data record format:
//
//  utc => UTC timestamp
//  bgv => Blood glucose value
//  ngv => Non glucose value
//  csv => Control solution value
//

public class BlueToothGlucoseOneTouch implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothGlucoseOneTouch.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothGlucoseOneTouch(BlueTooth parent)
    {
        this.parent = parent;
    }

    //region Interface BlueTooth.BlueToothPhysicalDevice

    public boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("af9df7a1-e595-11e3-96b4-0002a5d5c51b");
    }

    public boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("af9df7a3-e595-11e3-96b4-0002a5d5c51b");
    }

    public boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    public boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("af9df7a2-e595-11e3-96b4-0002a5d5c51b");
    }

    public void enableDevice()
    {
        //
        // Notify primary.
        //

        BlueTooth.GattAction ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_NOTIFY;
        ga.characteristic = parent.currentPrimary;

        parent.gattSchedule.add(ga);

        //
        // Fire initial command.
        //

        schedulePackets(getReadMeterChallenge());

        fireNextPacket();
    }

    public void sendCommand(JSONObject command)
    {
    }

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        parseResponseInternal(rd, characteristic);
    }

    //endregion Interface BlueTooth.BlueToothPhysicalDevice

    //region Device internal implementation

    private static final int COMMAND_READCHALLENGE = 1;
    private static final int COMMAND_ENABLEFEATURES = 2;
    private static final int COMMAND_READTIME = 3;
    private static final int COMMAND_READUNIT = 4;
    private static final int COMMAND_READHIGHRANGE = 5;
    private static final int COMMAND_READLOWRANGE = 6;
    private static final int COMMAND_READRECORDCOUNT = 7;
    private static final int COMMAND_READTESTCOUNT = 8;
    private static final int COMMAND_READGLUCOSERECORD = 9;

    private byte[] incomingMessage;
    private int incomingNumPackets;
    private byte[] challenge;

    private ArrayList<byte[]> outgoingPackets = new ArrayList<>();
    private int outgoingCommand;

    private static final int defaultLowRange = 70;
    private static final int defaultHighRange = 180;
    private static final long DEVICE_DELTA_SECONDS = 946684800L;

    private JSONObject status;
    private int lastTestCount;
    private int recordsToRead;
    private int recordCount;
    private int testCount;

    private void parseResponseInternal(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        int opcode = rd[ 0 ] & 0xc0;
        int packet = rd[ 0 ] & 0x3f;

        Log.d(LOGTAG, "parseResponse: opcode " + opcode + ":" + packet);

        if (opcode == 0x80)
        {
            Log.d(LOGTAG, "ACK - processing");

            incomingMessage = new byte[ 0 ];

            fireNextPacket();

            return;
        }

        if (opcode == 0xc0)
        {
            Log.d(LOGTAG, "NACK - not processing");

            return;
        }

        if (opcode == 0x00 || opcode == 0x40)
        {
            int ackpack = packet;

            if (opcode == 0)
            {
                Log.d(LOGTAG, "Receive total packets:" + packet);

                incomingNumPackets = packet;

                packet = 0;
            }
            else
            {
                Log.d(LOGTAG, "Receive current packet:" + packet);
            }

            BlueTooth.GattAction ga = new BlueTooth.GattAction();

            ga.mode = BlueTooth.GattAction.MODE_WRITE;
            ga.characteristic = parent.currentControl;

            if ((packet + 1) == incomingNumPackets)
            {
                ga.data = new byte[]{(byte) (ackpack | 0xc0)};
            }
            else
            {
                ga.data = new byte[]{(byte) (ackpack | 0x80)};
            }

            parent.gattSchedule.add(ga);

            parent.fireNext(false);

            byte[] temp = new byte[ incomingMessage.length + rd.length - 1 ];

            if (incomingMessage.length > 0)
            {
                System.arraycopy(incomingMessage, 0, temp, 0, incomingMessage.length);
            }

            System.arraycopy(rd, 1, temp , incomingMessage.length, rd.length - 1);
            incomingMessage = temp;

            if ((packet + 1) == incomingNumPackets)
            {
                Log.d(LOGTAG,"Complete message received: "
                        +  Simple.getHexBytesToString(incomingMessage));

                ArrayList<byte[]> next = null;

                if (validateResponse(incomingMessage))
                {
                    byte[] payload = extractPayload(incomingMessage);
                    next = parseCommands(payload);
                }
                else
                {
                    if (outgoingCommand == COMMAND_READGLUCOSERECORD)
                    {
                        //
                        // Failed command was read record. Try the next
                        // one if it exists.
                        //

                        next = getReadGlucoseRecord();
                    }
                }

                //
                // Fire next command.
                //

                if (next != null)
                {
                    schedulePackets(next);

                    fireNextPacket();
                }
            }
        }
    }

    public ArrayList<byte[]> parseCommands(byte[] payload)
    {
        if (outgoingCommand == COMMAND_READCHALLENGE)
        {
            parseMeterChallenge(payload);
            return getEnableFeatures();
        }

        if (outgoingCommand == COMMAND_ENABLEFEATURES)
        {
            parseEnableFeatures(payload);
            return getReadMeterUnit();
        }

        if (outgoingCommand == COMMAND_READUNIT)
        {
            parseMeterUnit(payload);
            return getReadMeterTime();
        }

        if (outgoingCommand == COMMAND_READTIME)
        {
            parseMeterTime(payload);
            return getReadMeterLowRange();
        }

        if (outgoingCommand == COMMAND_READLOWRANGE)
        {
            parseMeterLowRange(payload);
            return getReadMeterHighRange();
        }

        if (outgoingCommand == COMMAND_READHIGHRANGE)
        {
            parseMeterHighRange(payload);
            return getReadMeterRecordCount();
        }

        if (outgoingCommand == COMMAND_READRECORDCOUNT)
        {
            parseMeterRecordCount(payload);
            return getReadMeterTestCount();
        }

        if (outgoingCommand == COMMAND_READTESTCOUNT)
        {
            parseMeterTestCount(payload);
            return getReadGlucoseRecord();
        }

        if (outgoingCommand == COMMAND_READGLUCOSERECORD)
        {
            parseGlucoseRecord(payload);
            return getReadGlucoseRecord();
        }

        return null;
    }

    public void parseGlucoseRecord(byte[] pl)
    {
        Log.d(LOGTAG, "parseGlucoseRecord: result=" + Simple.getHexBytesToString(pl));

        if (pl[  9 ] != 0) Log.d(LOGTAG, "parseGlucoseRecord: Non-Glucose value:" + pl[ 9 ]);
        if (pl[  6 ] != 0) Log.d(LOGTAG, "parseGlucoseRecord: Control Solution value :" + pl[ 6 ]);
        if (pl[ 10 ] != 0) Log.d(LOGTAG, "parseGlucoseRecord: Sensor status:" + pl[ 10 ]);

        byte[] timeArray = new byte[ 4 ];
        System.arraycopy(pl, 0, timeArray, 0, 4);
        byte[] bgValueArray = new byte[ 2 ];
        System.arraycopy(pl, 4, bgValueArray, 0, 2);

        float bgValue = Simple.getIntFromLEByteArray(bgValueArray);

        long  timeStamp = Simple.getIntFromLEByteArray(timeArray);
        timeStamp += DEVICE_DELTA_SECONDS;
        timeStamp *= 1000L;

        String utciso = Simple.timeStampAsISO(Simple.getLocalTimeToUTC(timeStamp));

        Log.d(LOGTAG, "parseGlucoseRecord: result=LOC=" + Simple.timeStampAsISO(timeStamp));
        Log.d(LOGTAG, "parseGlucoseRecord: result=UTC=" + utciso);
        Log.d(LOGTAG, "parseGlucoseRecord: result=" + bgValue);

        JSONObject record = new JSONObject();

        Json.put(record, "utc", utciso);
        Json.put(record, "bgv", bgValue);
        Json.put(record, "ngv", pl[  9 ]);
        Json.put(record, "csv", pl[  6 ]);

        HealthData.addRecord("glucose", record);

        //
        // Announce last record to user interface.
        //

        if (lastTestCount == testCount)
        {
            JSONObject data = new JSONObject();
            Json.put(data, "glucose", record);

            if (parent.dataCallback != null)
            {
                parent.dataCallback.onBluetoothReceivedData(parent.deviceName, data);
            }
        }
    }

    public void parseMeterHighRange(byte[] pl)
    {
        int intval = Simple.getIntFromLEByteArray(pl);

        Log.d(LOGTAG, "parseMeterHighRange: result=" + intval);
    }

    public void parseMeterLowRange(byte[] pl)
    {
        int intval = Simple.getIntFromLEByteArray(pl);

        Log.d(LOGTAG, "parseMeterLowRange: result=" + intval);
    }

    public void parseMeterRecordCount(byte[] pl)
    {
        recordCount = Simple.getIntFromLEByteArray(pl);

        Log.d(LOGTAG, "parseMeterRecordCount: result=" + recordCount);
    }

    public void parseMeterTestCount(byte[] pl)
    {
        testCount = Simple.getIntFromLEByteArray(pl);

        Log.d(LOGTAG, "parseMeterTestCount: result=" + testCount);
    }

    public void parseEnableFeatures(byte[] pl)
    {
        Log.d(LOGTAG, "parseEnableFeatures: result=" + Simple.getHexBytesToString(pl));
    }

    public void parseMeterChallenge(byte[] pl)
    {
        String id = "";

        for (int inx = 0; inx < pl.length; inx += 2)
        {
            int utf16 = pl[ inx ] + (pl[ inx + 1 ] << 8);

            if (utf16 > 0) id += (char) utf16;
        }

        challenge = Simple.getHexStringToBytes(id);

        Log.d(LOGTAG, "parseMeterChallenge: result=" + id + ":" + Simple.getHexBytesToString(pl));
    }

    public void parseMeterUnit(byte[] pl)
    {
        int intval = Simple.getIntFromLEByteArray(pl);

        Log.d(LOGTAG, "parseMeterUnit: result=" + intval + "=" + ((intval == 0) ? "mg/dL" : "mmol/L"));
    }

    public void parseMeterTime(byte[] pl)
    {
        long timestamp = Simple.getIntFromLEByteArray(pl);

        timestamp += DEVICE_DELTA_SECONDS;
        timestamp *= 1000L;

        Log.d(LOGTAG, "parseMeterTime: result=" + Simple.timeStampAsISO(timestamp));
    }

    public ArrayList<byte[]> getReadGlucoseRecord()
    {
        Log.d(LOGTAG, "getReadGlucoseRecord");

        if (status == null) status = HealthData.getStatus("glucose");

        lastTestCount = status.has("lastTestCount") ? Json.getInt(status, "lastTestCount") : 0;

        Log.d(LOGTAG, "getReadGlucoseRecord records=" + recordCount + ":" + testCount);

        if (lastTestCount >= testCount)
        {
            Log.d(LOGTAG, "getReadGlucoseRecord no new data");

            return null;
        }

        Json.put(status, "lastTestCount", ++lastTestCount);
        Json.put(status, "lastReadDate", Simple.nowAsISO());

        HealthData.putStatus("glucose", status);

        Log.d(LOGTAG, "getReadGlucoseRecord next=" + lastTestCount);

        outgoingCommand = COMMAND_READGLUCOSERECORD;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = (byte) -77;
        data[ 1 ] = (byte) (lastTestCount & 0xff);
        data[ 2 ] = (byte) (lastTestCount >> 8);

        return createTransmitChunks(data);
    }
    
    public ArrayList<byte[]> getReadMeterRecordCount()
    {
        Log.d(LOGTAG, "getReadMeterRecordCount");

        outgoingCommand = COMMAND_READRECORDCOUNT;

        byte[] data = new byte[ 2 ];

        data[ 0 ] = 39;
        data[ 1 ] = 0;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterHighRange()
    {
        Log.d(LOGTAG, "getReadMeterHighRange");

        outgoingCommand = COMMAND_READHIGHRANGE;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 10;
        data[ 1 ] = 2;
        data[ 2 ] = 8;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterLowRange()
    {
        Log.d(LOGTAG, "getReadMeterLowRange");

        outgoingCommand = COMMAND_READLOWRANGE;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 10;
        data[ 1 ] = 2;
        data[ 2 ] = 7;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterTestCount()
    {
        Log.d(LOGTAG, "getReadMeterTestCount");

        outgoingCommand = COMMAND_READTESTCOUNT;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 10;
        data[ 1 ] = 2;
        data[ 2 ] = 6;

        return createTransmitChunks(data);
    }

    private byte[] makeTimePassword(byte[] date)
    {
        byte[] fuzzy = new byte[]{ date[ 0 ], date[ 3 ], date[ 2 ], date[ 1 ] };
        byte[] xorme = new byte[]{ (byte) -19, (byte) 85, (byte) -50, (byte) -84 };
        byte[] fukit = new byte[ 4 ];

        for (int inx = 0; inx < fuzzy.length; inx++)
        {
            fukit[ inx ] = (byte) (fuzzy[ inx ] ^ xorme[ inx ]);
        }

        return fukit;
    }

    @Nullable
    private byte[] makeCipherToken(byte[] challenge)
    {
        String fukit = Simple.dezify("==:@M6J0JGMD?6=7839291L4M0?F011A");
        byte[] mukit = Simple.getHexStringToBytes(fukit);

        byte[] rchallenge = Simple.getReversedBytes(challenge);
        byte[] fchallenge = new byte[ 16 ];

        System.arraycopy(rchallenge, 2, fchallenge, 0, 2);
        System.arraycopy(rchallenge, 4, fchallenge, 2, 4);
        System.arraycopy(rchallenge, 0, fchallenge, 6, 2);
        System.arraycopy(fchallenge, 0, fchallenge, 8, 8);

        try
        {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(1, new SecretKeySpec(mukit, "AES"));
            return cipher.doFinal(fchallenge);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    private ArrayList<byte[]> getEnableFeatures()
    {
        Log.d(LOGTAG, "getEnableFeatures");

        outgoingCommand = COMMAND_ENABLEFEATURES;

        byte[] token;

        if (challenge.length == 4)
        {
            token = makeTimePassword(challenge);
        }
        else
        {
            token = makeCipherToken(challenge);
        }

        if (token == null)
        {
            Log.d(LOGTAG,"Token not generated!");

            return null;
        }

        byte[] data = new byte[ token.length + 1 ];

        data[ 0 ] = (byte) 17;
        System.arraycopy(token, 0, data, 1, token.length);

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterChallenge()
    {
        Log.d(LOGTAG, "getReadMeterChallenge");

        outgoingCommand = COMMAND_READCHALLENGE;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = (byte) -26;
        data[ 1 ] = 2;
        data[ 2 ] = 8;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterUnit()
    {
        Log.d(LOGTAG, "getReadMeterUnit");

        outgoingCommand = COMMAND_READUNIT;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 9;
        data[ 1 ] = 2;
        data[ 2 ] = 2;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterTime()
    {
        Log.d(LOGTAG, "getReadMeterTime");

        outgoingCommand = COMMAND_READTIME;

        byte[] data = new byte[ 2 ];

        data[ 0 ] = 32;
        data[ 1 ] = 2;

        return createTransmitChunks(data);
    }

    private void fireNextPacket()
    {
        if (outgoingPackets.size() == 0) return;

        byte[] packet = outgoingPackets.remove(0);

        BlueTooth.GattAction ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.characteristic = parent.currentControl;
        ga.data = packet;

        parent.gattSchedule.add(ga);

        parent.fireNext(true);
    }

    private void schedulePackets(ArrayList<byte[]> packets)
    {
        for (byte[] packet : packets) outgoingPackets.add(packet);
    }

    private ArrayList<byte[]> createTransmitChunks(byte[] payload)
    {
        final byte stx = 2;
        final byte etx = 3;
        final byte maxDataLen = 19;

        //
        // Add transmit frame wit checksum around payload.
        //

        int size = payload.length + 7;

        byte[] transmit = new byte[ size ];

        transmit[ 0 ] = stx;
        transmit[ 1 ] = (byte) size;
        transmit[ 2 ] = 0;
        transmit[ 3 ] = etx;

        System.arraycopy(payload, 0, transmit, 4, payload.length);

        transmit[ size - 3 ] = etx;
        transmit[ size - 2 ] = 0;
        transmit[ size - 1 ] = 0;

        Simple.getCRC16ccittEmbed(transmit);

        //
        // Split into packets with maximum 19 bytes
        // and add packet count header.
        //

        ArrayList<byte[]> list = new ArrayList<>();

        int maxPackets = (transmit.length / maxDataLen) + 1;

        for (int packetinx = 0; packetinx < maxPackets; packetinx++)
        {
            int startPos = packetinx * maxDataLen;
            int numBytes = Math.min(transmit.length - startPos, maxDataLen);

            byte[] pbytes = new byte[(numBytes + 1)];

            if (packetinx == 0)
            {
                pbytes[ 0 ] = (byte) maxPackets;
            }
            else
            {
                pbytes[ 0 ] = (byte) (0x40 | packetinx);
            }

            System.arraycopy(transmit, startPos, pbytes, 1, numBytes);

            list.add(pbytes);
        }

        return list;
    }

    private boolean validateResponse(byte[] message)
    {
        if (message[ 1 ] != message.length)
        {
            Log.d(LOGTAG, "Message length mismatch.");

            return false;
        }

        if (! Simple.getCRC16ccittVerify(message))
        {
            Log.d(LOGTAG, "Message checksum mismatch.");

            return false;
        }

        if (message[ 4 ] != OK)
        {
            Log.d(LOGTAG, "Message response invalid: "
                    + Integer.toHexString(message[ 4 ])
                    + " => " + getErrorMessage(message[ 4 ]));

            return false;
        }

        return true;
    }

    private byte[] extractPayload(byte[] message)
    {
        byte[] payload = new byte[ message.length - 8 ];

        System.arraycopy(message, 5, payload, 0, payload.length);

        return payload;
    }

    private final static int SIZE_MISMATCH = 0;
    private final static int CHECKSUM_MISMATCH = 1;
    private final static int UNSUPPORTED = 8;
    private final static int UNAUTHORIZED = 7;
    private final static int INVALID_VALUE = 9;
    private final static int FAILED = 15;
    private final static int OK = 6;

    private String getErrorMessage(int errorCode)
    {
        switch (errorCode)
        {
            case OK: return "OK";
            case FAILED: return "FAILED";
            case SIZE_MISMATCH: return "INVALID SIZE";
            case UNSUPPORTED: return "NOT SUPPORTED";
            case UNAUTHORIZED: return "NOT AUTHORIZED";
            case INVALID_VALUE: return "INVALID PARAMETER";
            case CHECKSUM_MISMATCH: return "CHECKSUM ERROR";
        }

        return "UNDEFINED OPERATION";
    }

    //endregion Device internal implementation
}
