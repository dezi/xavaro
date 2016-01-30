package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;

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

        GattAction ga;

        //
        // Notify primary.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_NOTIFY;
        ga.characteristic = currentPrimary;

        gattSchedule.add(ga);

        //
        // Read time.
        //

        ga = new GattAction();

        ga.mode = GattAction.MODE_WRITE;
        ga.characteristic = currentControl;
        ga.data = getReadMeterTimeCommand();
        gattSchedule.add(ga);

        fireNext(true);
    }

    @Override
    public void sendCommand(JSONObject command)
    {
    }

    private int packets;

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        byte opcode = (byte) (rd[ 0 ] & -64);
        int packetID = rd[ 0 ] & 63;

        Log.d(LOGTAG, "parseResponse: opcode " + opcode + ":" + packetID);

        if (opcode == -128)
        {
            Log.d(LOGTAG, "ReceiveICDPacket: ACK Received - processing...");
        }

        else if (opcode == -64)
        {
            Log.d(LOGTAG, "ReceiveICDPacket: NACK Received - not processed!");
        }

        if (opcode == 0 || opcode == 64)
        {
            if (opcode == 0)
            {
                Log.d(LOGTAG, "ReceiveICDPacket: beginSeq Received - processing... " + packetID);

                packets = packetID;
            }
            else
            {
                Log.d(LOGTAG, "ReceiveICDPacket: xfer Received - processing..." + packetID);
            }

            GattAction ga = new GattAction();

            ga.mode = GattAction.MODE_WRITE;
            ga.characteristic = currentControl;

            if (packetID + 1 == packets)
            {
                ga.data = new byte[]{(byte) (packetID | 0xc0)};

            }
            else
            {
                ga.data = new byte[]{(byte) (packetID | 0x80)};
            }

            gattSchedule.add(ga);

            fireNext(false);

            byte[] fertig = new byte[ rd.length - 1 ];

            System.arraycopy(rd, 1, fertig, 0, rd.length - 1);

            byte[] result = new BleDataParser().parse(fertig);

            int time = (result[ 3 ] << 24) + (result[ 2 ] << 16) + (result[ 1 ] << 8) + result[ 0 ];
            Log.d(LOGTAG, "parseResponse: result="
                    + Simple.getHexBytesToString(result)
                    + "=" + time
                    + "=" + (new Date().getTime() / 1000));

            Log.d(LOGTAG, "TTTTTTTT= " + Simple.timeStampAsISO(time * 1000L + UNIXTIME_TO_METERTIME_DELTA_MILLIS));
        }
    }

    private long UNIXTIME_TO_METERTIME_DELTA_MILLIS = 946684800000L;

    public byte[] getReadMeterTimeCommand()
    {
        Log.d(LOGTAG, "getReadMeterTimeCommand");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) 32;
        data[ 1 ] = 2;

        Log.d(LOGTAG, "================================> " + Simple.getHexBytesToString(packetBytes(createICDByteArray(data))));

        return packetBytes(createICDByteArray(data));
    }

    public byte[] getReadMeterDieIDCommand()
    {
        Log.d(LOGTAG, "getReadMeterDieIDCommand");

        byte[] data = new byte[ 3 ];

        data[ 0 ] = (byte) -26;
        data[ 1 ] = 2;
        data[ 2 ] = 8;

        Log.d(LOGTAG, "================================> " + Simple.getHexBytesToString(packetBytes(createICDByteArray(data))));

        return packetBytes(createICDByteArray(data));
    }

    public byte[] packetBytes(byte[] data)
    {
        byte[] pbytes = new byte[ data.length + 1];

        pbytes[ 0 ] = 1;

        System.arraycopy(data, 0, pbytes, 1, data.length);

        return pbytes;
    }

    public byte[] getReadTime()
    {
        Log.d(LOGTAG, "getReadTime");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) 32;
        data[ 1 ] = 2;

        return createICDByteArray(data);
    }

    public static final byte EnablePremMode = (byte) 17;
    public static final byte ReadWriteParameter = (byte) 30;
    public static final byte ReadWriteRTC = (byte) 32;
    public static final byte ReadWriteSerialNo = (byte) 11;
    public static final byte ack = Byte.MIN_VALUE;
    public static final byte appParam = (byte) 2;
    public static final byte beginSeq = (byte) 0;
    private static final byte controlByte = (byte) 0;
    private static final byte etx = (byte) 3;
    public static final byte manufParam = (byte) 1;
    public static final int maxDataLen = 19;
    public static final byte nack = (byte) -64;
    public static final byte opcodeMask = (byte) -64;
    public static final byte parameterMask = (byte) 63;
    private static final byte premMode = (byte) 3;
    private static final byte pubMode = (byte) 4;
    public static final byte readDeviceInfo = (byte) 6;
    public static final byte readOperation = (byte) 2;
    private static final byte stx = (byte) 2;
    public static final byte userfParam = (byte) 3;
    public static final byte writeOperation = (byte) 1;
    public static final byte xfer = (byte) 64;

    public byte[] createICDByteArray(byte[] data)
    {
        int index;
        int size = data.length + 7;
        byte[] byteArray = new byte[ size];
        byteArray[0] = stx;
        byteArray[1] = (byte) size;
        byteArray[2] = controlByte;
        byteArray[3] = userfParam;
        byte[] arr$ = data;
        int len$ = arr$.length;
        int i$ = 0;
        int index2 = 4;
        while (i$ < len$)
        {
            index = index2 + 1;
            byteArray[index2] = arr$[i$];
            i$++;
            index2 = index;
        }
        index = index2 + 1;
        byteArray[index2] = userfParam;
        byte[] cc = crc16ccitt(byteArray);
        index2 = index + 1;
        byteArray[index] = cc[0];
        index = index2 + 1;
        byteArray[index2] = cc[1];
        return byteArray;
    }

    private static final int POLYNOMIAL_BE = 4129;

    public byte[] crc16ccitt(byte[] byteArray)
    {
        byte[] bytes = Arrays.copyOf(byteArray, byteArray.length - 2);

        int crc = 0xffff;

        for (byte item : bytes)
        {
            for (int i = 0; i < 8; i++)
            {
                boolean bitMsg;
                boolean bit16;

                if (((item >> (7 - i)) & 1) == 1)
                {
                    bitMsg = true;
                } else
                {
                    bitMsg = false;
                }
                if (((crc >> 15) & 1) == 1)
                {
                    bit16 = true;
                } else
                {
                    bit16 = false;
                }
                crc <<= 1;

                if ((bit16 ^ bitMsg) != false)
                {
                    crc ^= POLYNOMIAL_BE;
                }
            }
        }
        return createByteArray(crc & 0xffff);
    }

    public boolean verifyChecksum(byte[] bytes, byte[] crc)
    {
        byte[] newCrc = crc16ccitt(bytes);
        if (newCrc.length != crc.length)
        {
            return false;
        }
        for (int i = 0; i < newCrc.length; i++)
        {
            if (newCrc[i] != crc[i])
            {
                return false;
            }
        }
        return true;
    }

    private byte[] createByteArray(int value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value);
        return new byte[]{buffer.array()[0], buffer.array()[1]};
    }

    public class BleDataParser
    {
        public static final int CHECKSUM_ERROR = 1;
        public static final int FAILED = 15;
        public static final int INVALID_PARAMETER = 9;
        public static final int INVALID_SIZE = 0;
        public static final int NOT_AUTHORIZED = 7;
        public static final int NOT_SUPPORTED = 8;
        public static final int OK = 6;

        public byte[] parse(byte[] message)
        {
            if (message[ 1 ] != message.length)
            {
                Log.d(LOGTAG, "Message length and size from message didn't match!");
                reportError(INVALID_SIZE);
            }
            if (!verifyChecksum(message, new byte[]{message[ message.length - 2 ], message[ message.length - 1 ]}))
            {
                Log.d(LOGTAG, "Message checksum didn't match with the received checksum!");
                reportError(CHECKSUM_ERROR);
            }
            if (message[4] == OK)
            {
                return extractData(message);
            }

            Log.d(LOGTAG, "Message response is not OK! (" + Integer.toHexString(message[ 4 ]) + ")");
            reportError(message[4]);
            return null;
        }

        private byte[] extractData(byte[] message)
        {
            byte[] data = new byte[(message.length - 8)];
            int j = INVALID_SIZE;
            for (int i = 5; i < message.length - 3; i += CHECKSUM_ERROR)
            {
                data[j] = message[i];
                j += CHECKSUM_ERROR;
            }
            return data;
        }

        private void reportError(int errorCode)
        {
            String errorMessage;
            switch (errorCode)
            {
                case INVALID_SIZE /*0*/:
                    errorMessage = "INVALID SIZE VERIFICATION";
                    break;
                case CHECKSUM_ERROR /*1*/:
                    errorMessage = "INVALID CHECKSUM VERIFICATION";
                    break;
                case NOT_AUTHORIZED /*7*/:
                    errorMessage = "OPERATION NOT AUTHORIZED";
                    break;
                case NOT_SUPPORTED /*8*/:
                    errorMessage = "OPERATION NOT SUPPORTED";
                    break;
                case INVALID_PARAMETER /*9*/:
                    errorMessage = "INVALID PARAMETER";
                    break;
                case FAILED /*15*/:
                    errorMessage = "OPERATION FAILED";
                    break;
                default:
                    errorMessage = "UNDEFINED OPERATION";
                    break;
            }
        }
    }
}
