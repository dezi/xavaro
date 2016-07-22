package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.app.Application;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public void loadProtos(int resid)
    {
        protos = readRawTextResourceJSON(resid);

        if (protos == null)
        {
            Log.d(LOGTAG, "loadProtos: failed.");
        }
        else
        {
            Log.d(LOGTAG, "loadProtos: success.");
        }
    }

    public void setProtos(JSONObject protos)
    {
        this.protos = protos;
    }

    private static Application getApplicationUsingReflection() throws Exception
    {
        return (Application) Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication").invoke(null, (Object[]) null);
    }

    private String readRawTextResource(int resId)
    {
        try
        {
            Application appcontext = getApplicationUsingReflection();

            InputStream inputStream = appcontext.getResources().openRawResource(resId);

            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader buffreader = new BufferedReader(inputreader);
            StringBuilder text = new StringBuilder();
            String line;

            while ((line = buffreader.readLine()) != null)
            {
                text.append(line);
                text.append('\n');
            }

            return text.toString();
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    private JSONObject readRawTextResourceJSON(int resId)
    {
        String json = readRawTextResource(resId);
        if (json == null) return null;

        try
        {
            return new JSONObject(json);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }

        return null;
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

    @Nullable
    public JSONObject decode(String message)
    {
        JSONObject current = getJSONObject(protos, message);

        if (current == null)
        {
            Log.d(LOGTAG, "decode: unknown message=" + message);

            return null;
        }

        Log.d(LOGTAG, "decode: start decoding...");

        JSONObject json = new JSONObject();

        int next;
        int wire;
        int idid;

        String name;
        String type;

        boolean packed;
        boolean repeat;

        while (true)
        {
            next = getNextByte();
            if (next < 0) break;

            idid = next >> 3;
            wire = next & 0x07;

            type = getProtoType(current, idid);
            name = getProtoName(current, idid) + "@" + type;
            packed = getProtoPacked(current, idid);
            repeat = getProtoRepeat(current, idid);

            Log.d(LOGTAG, "decode"
                    + " pos=" + (offset - 1)
                    + " len=" + length
                    + " idid=" + idid
                    + " wire=" + wire
                    + " name=" + name
                    + " pck=" + packed
                    + " rpt=" + repeat
                );

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
                // Double
                //

                put(json, name, decodeDouble(), repeat);

                continue;
            }

            if (wire == 2)
            {
                //
                // Bytes
                //

                int seqlen = (int) decodeVarint();
                Log.d(LOGTAG, "decode wire=2 len=" + seqlen);

                byte[] seqbytes = getNextBytes(seqlen);

                /*
                if (protos.has(type))
                {
                    ProtoBufferDecode pbdecode = new ProtoBufferDecode(seqbytes);
                    pbdecode.setProtos(protos);

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
                */

                String hex = Simple.getHexBytesToString(seqbytes);
                put(json, name, hex, repeat);

                continue;
            }

            if (wire == 5)
            {
                //
                // Float
                //

                put(json, name, decodeFloat(), repeat);

                continue;
            }

            Log.d(LOGTAG, "Not implemented: wire=" + wire);

            break;
        }

        Log.d(LOGTAG, "decode: done decoding...");

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

                    Log.d(LOGTAG, "decodePacked: type=" + type + " pos=" + dp.offset + " len=" + dp.length + " varint=" + varint);

                    valarray.put(varint);
                }
            }

            Log.d(LOGTAG, "decodePacked: valarray=" + valarray.length());

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

    private float decodeFloat()
    {
        return ByteBuffer.wrap(getNextBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    private void put(JSONObject json, String name, byte[] value, boolean repeat)
    {
        JSONArray arraybytes = new JSONArray();

        if (value != null)
        {
            for (byte aValue : value)
            {
                arraybytes.put(aValue & 0xff);
            }
        }

        put(json, name, arraybytes, repeat);
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
