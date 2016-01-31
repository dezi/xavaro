package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.xavaro.android.common.Simple;

public class BlueToothGlucose extends BlueTooth
{
    private static final String LOGTAG = BlueToothGlucose.class.getSimpleName();

    public BlueToothGlucose(Context context)
    {
        super(context);
    }

    public BlueToothGlucose(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("af9df7a1-e595-11e3-96b4-0002a5d5c51b");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("af9df7a3-e595-11e3-96b4-0002a5d5c51b");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("af9df7a2-e595-11e3-96b4-0002a5d5c51b");
    }

    @Override
    protected void enableDevice()
    {
        Log.d(LOGTAG, "enableDevice: " + deviceName);

        //
        // Notify primary.
        //

        GattAction ga = new GattAction();

        ga.mode = GattAction.MODE_NOTIFY;
        ga.characteristic = currentPrimary;

        gattSchedule.add(ga);

        //
        // Fire initial command.
        //

        schedulePackets(getReadMeterDieID());

        fireNextPacket();
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }

    private static final int COMMAND_READDIEID = 1;
    private static final int COMMAND_PREMIUMMODE = 2;
    private static final int COMMAND_READTIME = 3;
    private static final int COMMAND_READUOM = 4;
    private static final int COMMAND_READHIGHRANGE = 5;
    private static final int COMMAND_READLOWRANGE = 6;
    private static final int COMMAND_READRECORDCOUNT = 7;
    private static final int COMMAND_READTESTCOUNT = 8;
    private static final int COMMAND_READGLUCOSERECORD = 9;

    private byte[] incomingMessage;
    private int incomingNumPackets;

    private ArrayList<byte[]> outgoingPackets = new ArrayList<>();
    private int command;

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        int opcode = rd[ 0 ] & 0xc0;
        int packet = rd[ 0 ] & 0x3f;

        Log.d(LOGTAG, "parseResponse: opcode " + opcode + ":" + packet);

        if (opcode == 0x80)
        {
            Log.d(LOGTAG, "ACK Received - processing...");

            incomingMessage = new byte[ 0 ];

            fireNextPacket();

            return;
        }

        if (opcode == 0xc0)
        {
            Log.d(LOGTAG, "NACK Received - not processed!");

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

            GattAction ga = new GattAction();

            ga.mode = GattAction.MODE_WRITE;
            ga.characteristic = currentControl;

            if ((packet + 1) == incomingNumPackets)
            {
                ga.data = new byte[]{(byte) (ackpack | 0xc0)};
            }
            else
            {
                ga.data = new byte[]{(byte) (ackpack | 0x80)};
            }

            gattSchedule.add(ga);

            fireNext(false);

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

                if (checkResponse(incomingMessage))
                {
                    byte[] payload = extractPayload(incomingMessage);
                    ArrayList<byte[]> next = parseCommands(payload);

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
    }

    public static final int defaultLowRange = 70;
    public static final int defaultHighRange = 180;
    public static final long UNIXTIME_TO_METERTIME_DELTA_MILLIS = 946684800000L;

    public ArrayList<byte[]> parseCommands(byte[] payload)
    {
        if (command == COMMAND_READDIEID)
        {
            parseReadMeterDieID(payload);
            return getPremiumMode();
        }

        if (command == COMMAND_PREMIUMMODE)
        {
            parsePremiumMode(payload);
            return getReadMeterUOM();
        }

        if (command == COMMAND_READUOM)
        {
            parseReadMeterUOM(payload);
            return getReadMeterTime();
        }

        if (command == COMMAND_READTIME)
        {
            parseReadMeterTime(payload);
            return getReadMeterLowRange();
        }

        if (command == COMMAND_READLOWRANGE)
        {
            parseReadMeterLowRange(payload);
            return getReadMeterHighRange();
        }

        if (command == COMMAND_READHIGHRANGE)
        {
            parseReadMeterHighRange(payload);
            return getReadMeterRecordCount();
        }

        if (command == COMMAND_READRECORDCOUNT)
        {
            parseReadMeterRecordCount(payload);
            return getReadMeterTestCount();
        }

        if (command == COMMAND_READTESTCOUNT)
        {
            parseReadMeterTestCount(payload);
            return getReadGlucoseRecord(8);
        }

        if (command == COMMAND_READGLUCOSERECORD)
        {
            parseReadGlucoseRecord(payload);
        }

        return null;
    }

    public void parseReadGlucoseRecord(byte[] pl)
    {
        Log.d(LOGTAG, "parseReadGlucoseRecordCommand: result=" + Simple.getHexBytesToString(pl));

        if (pl[  9 ] != 0)
            Log.d(LOGTAG, "parseReadGlucoseRecordCommand: Non-Glucose value found =" + pl[ 9 ]);
        if (pl[  6 ] != 0)
            Log.d(LOGTAG, "parseReadGlucoseRecordCommand: Control Solution value found =" + pl[ 6 ]);
        if (pl[ 10 ] != 0)
            Log.d(LOGTAG, "parseReadGlucoseRecordCommand: Sensor status =" + pl[ 10 ]);

        byte[] timeArray = new byte[ 4 ];
        System.arraycopy(pl, 0, timeArray, 0, 4);
        byte[] bgValueArray = new byte[ 2 ];
        System.arraycopy(pl, 4, bgValueArray, 0, 2);

        // ECF5401E
        // 77E4401E
        //
        float bgValue = byteArrayToInt(bgValueArray);
        long  timeStamp = byteArrayToInt(timeArray);
        timeStamp *= 1000L;
        timeStamp += UNIXTIME_TO_METERTIME_DELTA_MILLIS;

        Log.d(LOGTAG, "parseReadGlucoseRecordCommand: " + Simple.getHexBytesToString(pl));
        Log.d(LOGTAG, "parseReadGlucoseRecordCommand: " + Simple.timeStampAsISO(timeStamp));
        Log.d(LOGTAG, "parseReadGlucoseRecordCommand: " + bgValue);
    }

    public void parseReadMeterHighRange(byte[] pl)
    {
        int intval = byteArrayToInt(pl);

        Log.d(LOGTAG, "parseReadMeterHighRangeCommand: result=" + intval);
    }
    public void parseReadMeterLowRange(byte[] pl)
    {
        int intval = byteArrayToInt(pl);

        Log.d(LOGTAG, "parseReadMeterLowRangeCommand: result=" + intval);
    }

    public void parseReadMeterRecordCount(byte[] pl)
    {
        int intval = byteArrayToInt(pl);

        Log.d(LOGTAG, "parseReadMeterRecordCountCommand: result=" + intval + "=" + Simple.getHexBytesToString(pl));
    }

    public ArrayList<byte[]> getReadMeterRecordCount()
    {
        Log.d(LOGTAG, "getReadMeterRecordCountCommand");

        command = COMMAND_READRECORDCOUNT;

        byte[] data = new byte[ 2 ];

        data[ 0 ] = 39;
        data[ 1 ] = 0;

        return createTransmitChunks(data);
    }

    public void parseReadMeterTestCount(byte[] pl)
    {
        int intval = byteArrayToInt(pl);

        Log.d(LOGTAG, "parseReadMeterTestCountCommand: result=" + intval);
    }
    public void parsePremiumMode(byte[] pl)
    {
        Log.d(LOGTAG, "parsePremiumModeCommand:" + Simple.getHexBytesToString(pl));
    }
    public void parseReadMeterDieID(byte[] pl)
    {
        //
        // id=1323191058D0C9B7
        // id=0B035B36FF763569
        //

        String id = "";

        for (int inx = 0; inx < pl.length; inx += 2)
        {
            int utf16 = pl[ inx ] + (pl[ inx + 1 ] << 8);

            if (utf16 > 0) id += (char) utf16;
        }

        premiumModeClear = Simple.getHexStringToBytes(id);

        Log.d(LOGTAG, "parseReadMeterDieIDCommand: id=" + id + ":" + Simple.getHexBytesToString(pl));
    }
    public void parseReadMeterUOM(byte[] pl)
    {
        int intval = byteArrayToInt(pl);

        Log.d(LOGTAG, "parseReadMeterUOMCommand: result=" + intval + "=" + ((intval == 0) ? "mg/dL" : "mmol/L"));
    }
    public void parseReadMeterTime(byte[] pl)
    {
        long timestamp = byteArrayToInt(pl);

        timestamp *= 1000L;
        timestamp += UNIXTIME_TO_METERTIME_DELTA_MILLIS;

        Log.d(LOGTAG, "parseReadMeterTimeCommand: " + Simple.getHexBytesToString(pl) + "=" + Simple.timeStampAsISO(timestamp));
    }

    public ArrayList<byte[]> getReadGlucoseRecord(int index)
    {
        Log.d(LOGTAG, "getReadGlucoseRecordCommand");

        command = COMMAND_READGLUCOSERECORD;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = (byte) -77;
        data[ 1 ] = (byte) (index & 0xff);
        data[ 2 ] = (byte) (index >> 8);

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterHighRange()
    {
        Log.d(LOGTAG, "getReadMeterHighRangeCommands");

        command = COMMAND_READHIGHRANGE;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 10;
        data[ 1 ] = 2;
        data[ 2 ] = 8;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterLowRange()
    {
        Log.d(LOGTAG, "getReadMeterLowRangeCommand");

        command = COMMAND_READLOWRANGE;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 10;
        data[ 1 ] = 2;
        data[ 2 ] = 7;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterTestCount()
    {
        Log.d(LOGTAG, "getReadMeterTestCountCommand");

        command = COMMAND_READTESTCOUNT;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 10;
        data[ 1 ] = 2;
        data[ 2 ] = 6;

        return createTransmitChunks(data);
    }

    private byte[] reverseByteArray(byte[] array)
    {
        int len = array.length;

        byte[] reversedArray = new byte[ len ];

        for (int index = 0; index < len; index++)
        {
            reversedArray[ (len - index) - 1 ] = array[index];
        }

        return reversedArray;
    }

    private byte[] generatePasswordArray(byte[] date)
    {
        byte[] invertedArray = new byte[]{date[ 3 ], date[ 2 ], date[ 1 ], date[ 0 ]};
        byte[] time = new byte[]{invertedArray[ 2 ], invertedArray[ 1 ], invertedArray[ 0 ], invertedArray[ 3 ]};
        byte[] constant = new byte[]{(byte) -84, (byte) -50, (byte) 85, (byte) -19};
        byte[] result = new byte[ 4 ];

        for (int inx = 0; inx < time.length; inx++)
        {
            result[ inx ] = (byte) (time[ inx ] ^ constant[ inx ]);
        }

        return new byte[]{result[ 3 ], result[ 2 ], result[ 1 ], result[ 0 ]};
    }

    @Nullable
    private byte[] generateEncryptedToken(byte[] clearTextSource)
    {
        byte[] premiumModeMasterKey = new byte[]{
                (byte) 72, (byte) 59, (byte) -45, (byte) -62,
                (byte) -53, (byte) -33, (byte) 99, (byte) 69,
                (byte) 22, (byte) 0, (byte) 4, (byte) -26,
                (byte) -43, (byte) 109, (byte) -108, (byte) -116};

        // 483BD3C2CBDF6345160004E6D56D948C

        byte[] reversedClearText = reverseByteArray(clearTextSource);

        byte[] premiumModeClearText = new byte[ 16 ];
        System.arraycopy(reversedClearText, 2, premiumModeClearText, 0, 2);
        System.arraycopy(reversedClearText, 4, premiumModeClearText, 2, 4);
        System.arraycopy(reversedClearText, 0, premiumModeClearText, 6, 2);

        System.arraycopy(premiumModeClearText, 0, premiumModeClearText, 8, 8);

        try
        {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(1, new SecretKeySpec(premiumModeMasterKey, "AES"));
            return cipher.doFinal(premiumModeClearText);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    byte[] premiumModeClear;

    private ArrayList<byte[]> getPremiumMode()
    {
        Log.d(LOGTAG, "getPremiumModeCommand");

        command = COMMAND_PREMIUMMODE;

        byte[] token;

        if (premiumModeClear.length == 4)
        {
            token = generatePasswordArray(premiumModeClear);
        }
        else
        {
            token = generateEncryptedToken(premiumModeClear);
        }

        if (token == null)
        {
            Log.d(LOGTAG,"Premium Token not generated!!!!!!!!!!!!!");

            return null;
        }

        byte[] data = new byte[ token.length + 1 ];

        data[ 0 ] = (byte) 17;
        System.arraycopy(token, 0, data, 1, token.length);

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterDieID()
    {
        Log.d(LOGTAG, "getReadMeterDieIDCommand");

        command = COMMAND_READDIEID;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = (byte) -26;
        data[ 1 ] = 2;
        data[ 2 ] = 8;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterUOM()
    {
        Log.d(LOGTAG, "getReadMeterUOMCommand");

        command = COMMAND_READUOM;

        byte[] data = new byte[ 3 ];

        data[ 0 ] = 9;
        data[ 1 ] = 2;
        data[ 2 ] = 2;

        return createTransmitChunks(data);
    }

    private ArrayList<byte[]> getReadMeterTime()
    {
        Log.d(LOGTAG, "getReadMeterTimeCommand");

        command = COMMAND_READTIME;

        byte[] data = new byte[ 2 ];

        data[ 0 ] = 32;
        data[ 1 ] = 2;

        return createTransmitChunks(data);
    }

    private void fireNextPacket()
    {
        if (outgoingPackets.size() == 0) return;

        byte[] packet = outgoingPackets.remove(0);

        GattAction ga = new GattAction();

        ga.mode = GattAction.MODE_WRITE;
        ga.characteristic = currentControl;
        ga.data = packet;

        gattSchedule.add(ga);

        fireNext(true);
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

    private boolean checkResponse(byte[] message)
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

    private final static int INVALID_SIZE = 0;
    private final static int CHECKSUM_ERROR = 1;
    private final static int NOT_SUPPORTED = 8;
    private final static int NOT_AUTHORIZED = 7;
    private final static int INVALID_PARAMETER = 9;
    private final static int FAILED = 15;
    private final static int OK = 6;

    private String getErrorMessage(int errorCode)
    {
        switch (errorCode)
        {
            case OK: return "OK";
            case FAILED: return "FAILED";
            case INVALID_SIZE: return "INVALID SIZE";
            case NOT_SUPPORTED: return "NOT SUPPORTED";
            case CHECKSUM_ERROR: return "CHECKSUM ERROR";
            case NOT_AUTHORIZED: return "NOT AUTHORIZED";
            case INVALID_PARAMETER: return "INVALID PARAMETER";
        }

        return "UNDEFINED OPERATION";
    }
}
