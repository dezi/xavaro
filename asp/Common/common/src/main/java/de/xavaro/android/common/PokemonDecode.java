package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PokemonDecode
{
    private static final String LOGTAG = PokemonDecode.class.getSimpleName();

    private static JSONObject protos;

    public PokemonDecode()
    {
        if (protos == null) protos = PokemonProto.getProtos();
    }

    public void patch(String url, JSONObject result, byte[] responseBytes)
    {
        try
        {
            JSONObject reponseEnv = result.getJSONObject("response");

            JSONArray reponseArr = reponseEnv.getJSONArray("returns@array");
            JSONArray reponseOff= reponseEnv.getJSONArray("returns@array@");

            for (int inx = 0; inx < reponseArr.length(); inx++)
            {
                JSONObject response = reponseArr.getJSONObject(inx);
                int offset = reponseOff.getInt(inx);
                String type = response.getString("type");
                JSONObject data = response.getJSONObject("data");

                Log.d(LOGTAG, "patch: type=" + type + " offset=" + offset);

                if (type.equals(".POGOProtos.Networking.Responses.EncounterResponse"))
                {
                    offset += data.getInt("capture_probability@.POGOProtos.Data.Capture.CaptureProbability@");
                    data = data.getJSONObject("capture_probability@.POGOProtos.Data.Capture.CaptureProbability");

                    JSONArray pvals = data.getJSONArray("capture_probability@float");
                    JSONArray poffs = data.getJSONArray("capture_probability@float@");

                    pvals = pvals.getJSONArray(0);

                    for (int pinx = 0; pinx < pvals.length(); pinx++)
                    {
                        double probaval = pvals.getDouble(pinx);
                        int probaoff = offset + poffs.getInt(0) + pinx * 4;

                        byte[] fval = new byte[ 4 ];
                        byte[] nval = new byte[ 4 ];

                        System.arraycopy(responseBytes, probaoff, fval, 0, 4);
                        float bfloat = ByteBuffer.wrap(fval).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        ByteBuffer.wrap(nval).order(ByteOrder.LITTLE_ENDIAN).putFloat(100.0f);
                        System.arraycopy(nval, 0, responseBytes, probaoff, 4);

                        Log.d(LOGTAG, "patch: CaptureProbability=fval=" + getHexBytesToString(fval) + " nval=" + getHexBytesToString(nval));
                        Log.d(LOGTAG, "patch: CaptureProbability=" + probaval + " off=" + probaoff + " bfloat=" + bfloat);
                    }
                }
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    @Nullable
    public JSONObject decode(String url, byte[] requestBytes, byte[] responseBytes)
    {
        try
        {
            ProtoBufferDecode decode;

            decode = new ProtoBufferDecode(requestBytes);
            decode.setProtos(protos);
            decode.setOffs(true);
            JSONObject reqenvelope = decode.decode(".POGOProtos.Networking.Envelopes.RequestEnvelope");
            assembleRequest(reqenvelope);
            //Log.d(LOGTAG, "decode: " + Json.toPretty(reqenvelope));

            decode = new ProtoBufferDecode(responseBytes);
            decode.setProtos(protos);
            decode.setOffs(true);
            JSONObject resenvelope = decode.decode(".POGOProtos.Networking.Envelopes.ResponseEnvelope");
            assembleResponse(reqenvelope, resenvelope);
            //Log.d(LOGTAG, "decode: " + Json.toPretty(resenvelope));

            JSONObject result = new JSONObject();

            result.put("apiurl", url);
            result.put("request", reqenvelope);
            result.put("response", resenvelope);

            return result;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private void assembleRequest(JSONObject reqenvelop)
    {
        try
        {
            //reqenvelop.remove("auth_ticket@.POGOProtos.Networking.Envelopes.AuthTicket");

            reqenvelop.remove("unknown6@.POGOProtos.Networking.Envelopes.Unknown6");
            reqenvelop.remove("unknown12@int64");

            JSONArray requests = reqenvelop.getJSONArray("requests@.POGOProtos.Networking.Requests.Request");

            for (int rinx = 0; rinx < requests.length(); rinx++)
            {
                JSONObject request = requests.getJSONObject(rinx);

                String reqtype = request.getString("request_type@.POGOProtos.Networking.Requests.RequestType");
                String messagename = ".POGOProtos.Networking.Requests.Messages." + CamelName(reqtype) + "Message";

                if (request.has("request_message@bytes"))
                {
                    String hexbytes = request.getString("request_message@bytes");
                    byte[] reqdata = getHexStringToBytes(hexbytes);

                    Log.d(LOGTAG, "tuneUp reqtype=" + reqtype + " messagename=" + messagename);

                    ProtoBufferDecode decode = new ProtoBufferDecode(reqdata);
                    decode.setProtos(PokemonProto.getProtos());

                    JSONObject tune = decode.decode(messagename);

                    request.remove("request_message@bytes");
                    request.put(messagename, tune);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void assembleResponse(JSONObject reqenvelop, JSONObject resenvelop)
    {
        try
        {
            resenvelop.remove("unknown6@.POGOProtos.Networking.Envelopes.Unknown6Response");

            JSONArray requests = reqenvelop.getJSONArray("requests@.POGOProtos.Networking.Requests.Request");
            JSONArray reponses = resenvelop.getJSONArray("returns@bytes");

            JSONArray decoded = new JSONArray();

            for (int rinx = 0; rinx < requests.length(); rinx++)
            {
                JSONObject request = requests.getJSONObject(rinx);

                String restype = request.getString("request_type@.POGOProtos.Networking.Requests.RequestType");
                String messagename = ".POGOProtos.Networking.Responses." + CamelName(restype) + "Response";

                if (rinx < reponses.length())
                {
                    String hexbytes = reponses.getString(rinx);
                    byte[] resdata = getHexStringToBytes(hexbytes);

                    Log.d(LOGTAG, "tuneUp restype=" + restype + " messagename=" + messagename);

                    ProtoBufferDecode decode = new ProtoBufferDecode(resdata);
                    decode.setProtos(PokemonProto.getProtos());
                    decode.setOffs(true);

                    JSONObject tune = decode.decode(messagename);

                    JSONObject resmessage = new JSONObject();

                    resmessage.put("type", messagename);
                    resmessage.put("data", tune);

                    decoded.put(resmessage);
                }
            }

            resenvelop.remove("returns@bytes");
            resenvelop.put("returns@array", decoded);

            if (resenvelop.has("returns@bytes@"))
            {
                Object indexes = resenvelop.get("returns@bytes@");
                resenvelop.remove("returns@bytes@");
                resenvelop.put("returns@array@",indexes);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private String CamelName(String uppercase)
    {
        String camelname = "";
        boolean nextUp = true;

        for (int inx = 0; inx < uppercase.length(); inx++)
        {
            if (uppercase.charAt(inx) == '@') break;

            if (uppercase.charAt(inx) == '_')
            {
                nextUp = true;
                continue;
            }

            camelname += nextUp ? uppercase.charAt(inx) : Character.toLowerCase(uppercase.charAt(inx));
            nextUp = false;
        }

        return camelname;
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
    public static byte[] getHexStringToBytes(String hexstring)
    {
        if (hexstring == null) return null;

        hexstring = hexstring.replace(" ", "");

        byte[] bytes = new byte[ hexstring.length() >> 1 ];

        for (int inx = 0; inx < hexstring.length(); inx += 2)
        {
            //noinspection PointlessBitwiseExpression,PointlessArithmeticExpression
            bytes[ inx >> 1 ] = (byte)
                    ((Character.digit(hexstring.charAt(inx + 0), 16) << 4)
                            + Character.digit(hexstring.charAt(inx + 1), 16) << 0);
        }

        return bytes;
    }
}
