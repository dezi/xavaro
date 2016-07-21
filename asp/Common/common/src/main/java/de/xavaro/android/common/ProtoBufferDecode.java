package de.xavaro.android.common;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ProtoBufferDecode
{
    private static final String LOGTAG = ProtoBufferDecode.class.getSimpleName();

    public static void testDat()
    {
        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, "pm.20160721.061012.txt");


        byte[] data = Simple.readBinaryFile(extfile);
        Log.d(LOGTAG, "testDat: size=" + data.length);

        byte[] match = "\n------------------------------\n".getBytes();

        int von = -1;
        int bis = -1;

        for (int inx = 4; inx < data.length; inx++)
        {
            if ((data[ inx - 3 ] == (int) '-') && (data[ inx - 2 ] == (int) '-') && (data[ inx - 1 ] == (int) '\n'))
            {
                if (von < 0)
                {
                    von = inx;
                }
                else
                {
                    if (bis < 0)
                    {
                        bis = inx - 32;
                    }
                }
            }
        }

        ProtoBufferDecode decode = new ProtoBufferDecode(data, von, bis - von);

        JSONObject json = decode.decode();

        Log.d(LOGTAG,"testDat: " + Json.toPretty(json));
    }

    private byte[] buffer;
    private int offset;
    private int length;
    private int position;

    public ProtoBufferDecode(byte[] buffer)
    {
        if (buffer != null)
        {
            this.buffer = buffer;
            this.length = buffer.length;
            this.offset = 0;
            this.position = 0;
        }
    }

    public ProtoBufferDecode(byte[] buffer, int offset, int length)
    {
        if (buffer != null)
        {
            this.buffer = buffer;
            this.length = length;
            this.offset = offset;
            this.position = offset;
        }
    }

    private int getNextByte()
    {
        if ((buffer != null) && (position < (offset + length)))
        {
            return buffer[ position++ ] & 0xff;
        }

        return -1;
    }

    private byte[] getNextBytes(int size)
    {
        size = Math.min(size, (offset + length) - position);

        byte[] bytes = new byte[ size ];

        for (int inx = 0; inx < size; inx++)
        {
            int next = getNextByte();
            if (next < 0) break;

            bytes[ inx ] = (byte) next;
        }

        return bytes;
    }

    public JSONObject decode()
    {
        JSONObject json = new JSONObject();

        int next;
        int wire;
        int idid;

        while ((next = getNextByte()) >= 0)
        {
            idid = next >> 3;
            wire = next & 0x03;

            if (wire == 0)
            {
                //
                // Varint
                //

                put(json, "" + idid, decodeVarint());

                continue;
            }

            if (wire == 1)
            {
                //
                // Double
                //

                put(json, "" + idid, decodeDouble());

                continue;
            }

            if (wire == 2)
            {
                //
                // Bytes
                //

                int seqlen = (int) decodeVarint();
                put(json, "" + idid, getNextBytes(seqlen));

                continue;
            }

            if (wire == 5)
            {
                //
                // Float
                //

                put(json, "" + idid, decodeFloat());

                continue;
            }

            Log.d(LOGTAG, "Not implemented: wire=" + wire);

            break;
        }

        return json;
    }

    private long decodeVarint()
    {
        long result = 0;

        int next;

        while ((next = getNextByte()) >= 0)
        {
            result = (result << 7) | (next & 0x7f);
            if ((next & 0x80) == 0) break;
        }

        return result;
    }

    private double decodeDouble()
    {
        return ByteBuffer.wrap(getNextBytes(8)).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    private float decodeFloat()
    {
        return ByteBuffer.wrap(getNextBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    private void put(JSONObject json, String name, byte[] value)
    {
        JSONArray arraybytes = new JSONArray();

        if (value != null)
        {
            for (byte aValue : value)
            {
                arraybytes.put(aValue & 0xff);

                break;
            }
        }

        put(json, name, arraybytes);
    }

    private void put(JSONObject json, String name, Object value)
    {
        try
        {
            json.put(name, value);
        }
        catch (Exception ignore)
        {
        }
    }
}
