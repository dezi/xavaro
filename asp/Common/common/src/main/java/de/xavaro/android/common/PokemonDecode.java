package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class PokemonDecode
{
    private static final String LOGTAG = PokemonDecode.class.getSimpleName();

    private static JSONObject protos;

    public PokemonDecode()
    {
        if (protos == null) protos = PokemonProto.getProtos();
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
            reqenvelop.remove("auth_ticket@.POGOProtos.Networking.Envelopes.AuthTicket");
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
