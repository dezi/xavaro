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
                String name = keysIterator.next();

                String field = keysIterator.next();
                String[] parts = name.split("@");
                if (parts.length >= 3) continue;
                if (parts.length == 2) field = parts[ 0 ];

                Log.d(LOGTAG, "encode: field=" + field);

                if (protos.has(name))
                {
                    if (isProtoEnum(protos, name))
                    {
                        //
                        // Enum encoding.
                        //

                        Log.d(LOGTAG, "encode: enum field=" + field);

                        Object enumval = json.get(name);

                        if (enumval instanceof String)
                        {

                        }

                        if (enumval instanceof Integer)
                        {
                        }
                    }
                    else
                    {
                        //
                        // Subtype encoding.
                        //

                        Log.d(LOGTAG, "encode: mess field=" + field);

                        JSONObject submess = json.getJSONObject(name);
                        result.put(field, submess);
                    }
                }
                else
                {
                    Log.d(LOGTAG, "encode: type field=" + field);
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
}
