package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class ProtoBufferDecode
{
    private static final String LOGTAG = ProtoBufferDecode.class.getSimpleName();

    private byte[] buffer;
    private int offset;
    private int length;

    private JSONObject protos;
    private boolean debug;
    private boolean flat;
    private boolean offs;

    public ProtoBufferDecode(byte[] buffer)
    {
        if (buffer != null)
        {
            this.buffer = buffer;
            this.length = buffer.length;
            this.offset = 0;
        }
    }

    public ProtoBufferDecode(byte[] buffer, int offset, int length)
    {
        if (buffer != null)
        {
            this.buffer = buffer;
            this.length = length;
            this.offset = offset;
        }
    }

    public void setProtos(JSONObject protos)
    {
        this.protos = protos;
    }

    public void setFlat(boolean flat)
    {
        this.flat = flat;
    }

    public void setOffs(boolean offs)
    {
        this.offs = offs;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    private int getNextByte()
    {
        if ((buffer != null) && (offset < length))
        {
            return buffer[ offset++ ] & 0xff;
        }

        return -1;
    }

    private byte[] getNextBytes(int size)
    {
        size = Math.min(size, length - offset);

        byte[] bytes = new byte[ size ];

        for (int inx = 0; inx < size; inx++)
        {
            int next = getNextByte();
            if (next < 0) break;

            bytes[ inx ] = (byte) next;
        }

        return bytes;
    }

    public static String getHexBytesToString(byte[] bytes)
    {
        return getHexBytesToString(bytes, 0, bytes.length);
    }

    public static String getHexBytesToString(byte[] bytes, int offset, int length)
    {
        if (length == 0) return "";

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[ (length * 3) - 1 ];

        for (int inx = offset; inx < (length + offset); inx++)
        {
            //noinspection PointlessArithmeticExpression
            hexChars[ ((inx - offset) * 3) + 0 ] = hexArray[ (bytes[ inx ] >> 4) & 0x0f ];
            //noinspection PointlessBitwiseExpression
            hexChars[ ((inx - offset) * 3) + 1 ] = hexArray[ (bytes[ inx ] >> 0) & 0x0f ];

            if (inx + 1 >= (length + offset)) break;
            hexChars[ ((inx - offset) * 3) + 2 ] = ' ';
        }

        return String.valueOf(hexChars);
    }

    @Nullable
    public JSONObject decode(String message)
    {
        JSONObject current = getJSONObject(protos, message);

        if (current == null)
        {
            Log.d(LOGTAG, "decode: unknown message=" + message);

            return null;
        }

        if (debug) Log.d(LOGTAG, "decode: start decoding...");

        if (debug) Log.d(LOGTAG, "decode: " + getHexBytesToString(buffer, offset, length - offset));

        JSONObject json = new JSONObject();

        int next;
        int wire;
        int idid;

        String name;
        String type;

        boolean packed;
        boolean repeat;

        while (offset < length)
        {
            next = (int) decodeVarint();

            idid = next >> 3;
            wire = next & 0x07;

            type = getProtoType(current, idid);
            name = getProtoName(current, idid) + "@" + type;
            packed = getProtoPacked(current, idid);
            repeat = getProtoRepeat(current, idid);

            if (debug)
            {
                Log.d(LOGTAG, "decode"
                        + " pos=" + (offset - 1)
                        + " idid=" + idid
                        + " wire=" + wire
                        + " name=" + name
                        + " pck=" + packed
                        + " rpt=" + repeat
                );
            }

            if (wire == 0)
            {
                //
                // Varint
                //

                long varint = decodeVarint();

                if (protos.has(type))
                {
                    put(json, name, getProtoEnum(type, varint), repeat);
                }
                else
                {
                    put(json, name, varint, repeat);
                }

                continue;
            }

            if (wire == 1)
            {
                //
                // Fixed 64 bit
                //

                if (offs) put(json, name + "@", offset, repeat);

                if (type.equals("double"))
                {
                    put(json, name, decodeDouble(), repeat);
                }
                else
                {
                    put(json, name, decodeFixed64(), repeat);
                }

                continue;
            }

            if (wire == 2)
            {
                //
                // Bytes
                //

                int seqlen = (int) decodeVarint();

                if (debug) Log.d(LOGTAG, "decode wire=2 len=" + seqlen);

                if (offs) put(json, name + "@", offset, repeat);

                byte[] seqbytes = getNextBytes(seqlen);

                if (! flat)
                {
                    if (protos.has(type) && ! isProtoEnum(type))
                    {
                        ProtoBufferDecode pbdecode = new ProtoBufferDecode(seqbytes);
                        pbdecode.setProtos(protos);
                        pbdecode.setOffs(offs);

                        JSONObject seqjson = pbdecode.decode(type);
                        put(json, name, seqjson, repeat);

                        continue;
                    }
                    else
                    {
                        if ((type != null) && type.equals("string"))
                        {
                            put(json, name, new String(seqbytes), repeat);
                            continue;
                        }

                        if ((type != null) && type.equals("bytes"))
                        {
                            put(json, name, seqbytes, repeat);
                            continue;
                        }

                        if (type != null)
                        {
                            JSONArray packedvals = decodePacked(type, seqbytes);

                            if (packedvals != null)
                            {
                                put(json, name, packedvals, repeat);
                                continue;
                            }
                        }
                    }

                    Log.d(LOGTAG, "decode: unknown type=" + type);
                }

                String hex = getHexBytesToString(seqbytes);
                put(json, name, hex, repeat);

                continue;
            }

            if (wire == 5)
            {
                //
                // Fixed 32 bit
                //

                if (offs) put(json, name + "@", offset, repeat);

                if (type.equals("float"))
                {
                    put(json, name, decodeFloat(), repeat);
                }
                else
                {
                    put(json, name, decodeFixed32(), repeat);

                }

                continue;
            }

            Log.d(LOGTAG, "Not implemented: wire=" + wire);

            break;
        }

        if (debug) Log.d(LOGTAG, "decode: done decoding...");

        return json;
    }

    @Nullable
    private JSONArray decodePacked(String type, byte[] data)
    {
        try
        {
            JSONArray valarray = new JSONArray();

            ProtoBufferDecode dp = new ProtoBufferDecode(data);

            if (type.equals("float") || type.equals("fixed32"))
            {
                float floatval;

                while (dp.offset < dp.length)
                {
                    floatval = dp.decodeFloat();
                    valarray.put((double) floatval);
                }
            }

            if (type.equals("double") || type.equals("fixed64"))
            {
                double doubleval;

                while (dp.offset < dp.length)
                {
                    doubleval = dp.decodeDouble();
                    valarray.put(doubleval);
                }
            }

            if (type.equals("bool")
                    || type.equals("int32") || type.equals("uint32")
                    || type.equals("int64") || type.equals("uint64"))
            {
                long varint;

                while (dp.offset < dp.length)
                {
                    varint = dp.decodeVarint();

                    if (debug) Log.d(LOGTAG, "decodePacked: type=" + type + " pos=" + dp.offset + " len=" + dp.length + " varint=" + varint);

                    valarray.put(varint);
                }
            }

            if (isProtoEnum(type))
            {
                long varint;

                while (dp.offset < dp.length)
                {
                    varint = dp.decodeVarint();

                    if (debug) Log.d(LOGTAG, "decodePacked: type=" + type + " pos=" + dp.offset + " len=" + dp.length + " enum=" + varint);

                    valarray.put(getProtoEnum(type,varint));
                }
            }

            if (debug) Log.d(LOGTAG, "decodePacked: valarray=" + valarray.length());

            return valarray;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    private long decodeVarint()
    {
        long result = 0;
        long next;
        int shift = 0;

        while ((next = getNextByte()) >= 0)
        {
            result |= (next & 0x7f) << shift;
            if ((next & 0x80) == 0) break;
            shift += 7;
        }

        return result;
    }

    private double decodeDouble()
    {
        return ByteBuffer.wrap(getNextBytes(8)).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    private long decodeFixed64()
    {
        return ByteBuffer.wrap(getNextBytes(8)).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    private float decodeFloat()
    {
        return ByteBuffer.wrap(getNextBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    private long decodeFixed32()
    {
        return ByteBuffer.wrap(getNextBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private void put(JSONObject json, String name, byte[] value, boolean repeat)
    {
        put(json, name, getHexBytesToString(value), repeat);

        /*
        if (name.equals("returns@bytes"))
        {
            put(json, name, getHexBytesToString(value), repeat);

            return;
        }

        JSONArray arraybytes = new JSONArray();

        if (value != null)
        {
            for (byte aValue : value)
            {
                arraybytes.put(aValue & 0xff);
            }
        }

        put(json, name, arraybytes, repeat);
        */
    }

    private void put(JSONObject json, String name, Object value, boolean repeat)
    {
        try
        {
            if (repeat)
            {
                JSONArray array;

                if (json.has(name))
                {
                    array = (JSONArray) json.get(name);
                }
                else
                {
                    array = new JSONArray();
                    json.put(name, array);
                }

                array.put(value);
            }
            else
            {
                json.put(name, value);
            }
        }
        catch (Exception ignore)
        {
        }
    }

    private int getInt(JSONObject json, String name)
    {
        try
        {
            return json.getInt(name);
        }
        catch (Exception ignore)
        {
            return 0;
        }
    }

    @Nullable
    private String getString(JSONObject json, String name)
    {
        try
        {
            return json.getString(name);
        }
        catch (Exception ignore)
        {
            return null;
        }
    }

    @Nullable
    private boolean getBoolean(JSONObject json, String name)
    {
        try
        {
            return json.getBoolean(name);
        }
        catch (Exception ignore)
        {
            return false;
        }
    }

    @Nullable
    private JSONObject getJSONObject(JSONObject json, String name)
    {
        try
        {
            return json.getJSONObject(name);
        }
        catch (Exception ignore)
        {
            return null;
        }
    }

    private String getProtoName(JSONObject current, int idid)
    {
        Iterator<String> keysIterator = current.keys();

        while (keysIterator.hasNext())
        {
            String name = keysIterator.next();
            JSONObject item = getJSONObject(current, name);
            if (item == null) continue;

            if (idid == getInt(item, "id"))
            {
                return name;
            }
        }

        return "" + idid;
    }

    private String getProtoType(JSONObject current, int idid)
    {
        Iterator<String> keysIterator = current.keys();

        while (keysIterator.hasNext())
        {
            String name = keysIterator.next();
            JSONObject item = getJSONObject(current, name);
            if (item == null) continue;

            if (idid == getInt(item, "id"))
            {
                return getString(item, "type");
            }
        }

        return "unknown";
    }

    private boolean getProtoRepeat(JSONObject current, int idid)
    {
        Iterator<String> keysIterator = current.keys();

        while (keysIterator.hasNext())
        {
            String name = keysIterator.next();
            JSONObject item = getJSONObject(current, name);
            if (item == null) continue;

            if (idid == getInt(item, "id"))
            {
                return getBoolean(item, "repeated");
            }
        }

        return false;
    }

    private boolean getProtoPacked(JSONObject current, int idid)
    {
        Iterator<String> keysIterator = current.keys();

        while (keysIterator.hasNext())
        {
            String name = keysIterator.next();
            JSONObject item = getJSONObject(current, name);
            if (item == null) continue;

            if (idid == getInt(item, "id"))
            {
                return getBoolean(item, "packed");
            }
        }

        return false;
    }

    private boolean isProtoEnum(String enumtype)
    {
        JSONObject enumobj = getJSONObject(protos, enumtype);
        if (enumobj == null) return false;

        Iterator<String> keysIterator = enumobj.keys();

        if (keysIterator.hasNext())
        {
            String name = keysIterator.next();

            try
            {
                enumobj.getInt(name);
            }
            catch (Exception ignore)
            {
                return false;
            }
        }

        return true;
    }

    private Object getProtoEnum(String enumtype, long value)
    {
        JSONObject enumobj = getJSONObject(protos, enumtype);
        if (enumobj == null) return value;

        Iterator<String> keysIterator = enumobj.keys();

        while (keysIterator.hasNext())
        {
            String name = keysIterator.next();
            long enumval = getInt(enumobj, name);

            if (enumval == value)
            {
                return name + "@" + value;
            }
        }

        return value;
    }
}
