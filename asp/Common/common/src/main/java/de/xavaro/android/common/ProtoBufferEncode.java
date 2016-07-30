package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class ProtoBufferEncode
{
    private static final String LOGTAG = ProtoBufferEncode.class.getSimpleName();

    private byte[] buffer;
    private int offset;
    private int length;

    private JSONObject protos;

    public void setProtos(JSONObject protos)
    {
        this.protos = protos;
    }

    @Nullable
    public JSONObject encodeJSON(JSONObject json, String message)
    {
        if ((protos == null) || ! protos.has(message)) return null;

        try
        {
            JSONObject result = new JSONObject();

            Iterator<String> keysIterator = json.keys();

            while (keysIterator.hasNext())
            {
                String key = keysIterator.next();
                if (key.endsWith("@")) continue;

                String[] parts = key.split("@");
                if (parts.length >= 3) continue;

                String name = parts[ 0 ];
                JSONObject desc = getProtoDescriptor(message, name);

                if (desc == null)
                {
                    Log.d(LOGTAG, "encode: fail name=" + name);
                    continue;
                }

                int idid = desc.getInt("id");
                String type = (parts.length == 2) ? parts[ 1 ] : desc.getString("type");
                String dest = name + "@" + desc.getInt("id");

                if (protos.has(type))
                {
                    if (isProtoEnum(protos, type))
                    {
                        //
                        // Enum encoding.
                        //

                        Log.d(LOGTAG, "encode: enum name=" + name);

                        int enumint = getProtoEnumValue(name, json.get(key));
                        byte[] bytes = encodeVarint(enumint);
                        result.put(dest, getHexBytesToString(encodeSection(idid, 0, bytes)));
                    }
                    else
                    {
                        //
                        // Subtype encoding.
                        //

                        Log.d(LOGTAG, "encode: mess name=" + name + " type=" + type);

                        JSONObject subjson = json.getJSONObject(key);
                        result.put(dest, encodeJSON(subjson, type));
                    }
                }
                else
                {
                    int mode = getModeFromType(type);

                    Log.d(LOGTAG, "encode: valu name=" + name + " type=" + type + "mode=" + mode);

                    byte[] bytes = (mode == 0)
                            ? encodeVarint(json.getLong(key))
                            : (mode == 2)
                            ? encodeVector(mode, json.get(key))
                            : encodeScalar(mode, json.get(key));

                    result.put(dest, getHexBytesToString(encodeSection(idid, mode, bytes)));
                }
            }

            return result;
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();

            return null;
        }
    }

    private byte[] encodeVector(int mode, Object value)
    {
        byte[] data = new byte[ 0 ];

        if (value instanceof String)
        {
            data = ((String) value).getBytes();
        }

        if (value instanceof byte[])
        {
            data = (byte[]) value;
        }

        return data;
    }

    private byte[] encodeScalar(int mode, Object value)
    {
        byte[] data = new byte[ (mode == 1) ? 8 : 4 ];

        if (value instanceof Double)
        {
            if (mode == 1)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putDouble((double) value);
            }

            if (mode == 5)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putFloat((float) (double) value);
            }
        }

        if (value instanceof Long)
        {
            if (mode == 1)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putLong((long) value);
            }

            if (mode == 5)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putLong((int) (long) value);
            }
        }

        if (value instanceof Float)
        {
            if (mode == 1)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putDouble((double) (float) value);
            }

            if (mode == 5)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putFloat((float) value);
            }
        }

        if (value instanceof Integer)
        {
            if (mode == 1)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putLong((long) (int) value);
            }

            if (mode == 5)
            {
                ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putInt((int) value);
            }
        }

        return data;
    }

    private byte[] encodeSection(long idid, int mode, byte[] section)
    {
        byte[] id = encodeId(idid, mode);

        byte[] result = new byte[ id.length + section.length ];

        System.arraycopy(id, 0, result, 0, id.length);
        System.arraycopy(section, 0, result, id.length, section.length);

        return result;
    }

    private byte[] encodeId(long idid, int mode)
    {
        long value = (idid << 3) | mode;
        return encodeVarint(value);
    }

    private byte[] encodeVarint(long value)
    {
        byte[] buffer = new byte[ 12 ];
        int offset = 0;

        while (value > 0)
        {
            int next = (int) value & 0x7f;
            value = value >>> 7;
            if (value > 0) next |= 0x80;

            buffer[ offset++ ] = (byte) next;
        }

        byte[] result = new byte[ offset ];
        System.arraycopy(buffer, 0, result, 0, offset);
        return result;
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

    private int getModeFromType(String type)
    {
        if (type.equals("string") || type.equals("bytes")) return 2;
        if (type.equals("float") || type.equals("fixed32") || type.equals("sfixed32")) return 5;
        if (type.equals("double") || type.equals("fixed64") || type.equals("sfixed64")) return 1;

        return 0;
    }

    @Nullable
    private JSONObject getProtoDescriptor(String messageName, String name)
    {
        try
        {
            JSONObject message = protos.getJSONObject(messageName);
            return message.getJSONObject(name);
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }

        return null;
    }

    private int getProtoEnumValue(String enumtype, Object value)
    {
        try
        {
            if (value instanceof Integer)
            {
                return (int) value;
            }

            if (value instanceof String)
            {
                String nametag = (String) value;

                String[] parts = nametag.split("@");

                if (parts.length == 2)
                {
                    return Integer.parseInt(parts[ 1 ], 10);
                }

                JSONObject enumobj = protos.getJSONObject(enumtype);

                return getInt(enumobj, nametag);
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }

        return -1;
    }

    private static boolean isProtoEnum(JSONObject protos, String enumtype)
    {
        try
        {
            JSONObject enumobj = protos.getJSONObject(enumtype);

            Iterator<String> keysIterator = enumobj.keys();

            if (keysIterator.hasNext())
            {
                String name = keysIterator.next();

                enumobj.getInt(name);
            }

            return true;
        }
        catch (Exception ignore)
        {
            return false;
        }
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
}
