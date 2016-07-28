package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

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

                        result.put(dest, getHexBytesToString(encodeVarint(enumint)));
                    }
                    else
                    {
                        //
                        // Subtype encoding.
                        //

                        Log.d(LOGTAG, "encode: mess name=" + name + " type=" + type);

                        JSONObject subjson = json.getJSONObject(name);
                        result.put(dest, encodeJSON(subjson, type));
                    }
                }
                else
                {
                    Log.d(LOGTAG, "encode: valu name=" + name + " type=" + type);

                    result.put(dest, encodeScalar(type, json.get(key)));
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

    private byte[] encodeScalar(String type, Object value)
    {

        return null;
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

    @Nullable
    private JSONObject getProtoDescriptor(String enumtype, String name)
    {
        try
        {
            JSONObject message = protos.getJSONObject(name);
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
