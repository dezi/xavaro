package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.PokemonProto;
import de.xavaro.android.common.ProtoBufferDecode;
import de.xavaro.android.common.Simple;

public class Pokemongo extends FrameLayout
{
    private static final String LOGTAG = "POKEDEZI #11";

    private WindowManager.LayoutParams overlayParam;

    private Pokemongo(Context context)
    {
        super(context);

        setBackgroundColor(0x88880000);

        setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.d(LOGTAG, "Alert touch...");
                return false;
            }
        });

        overlayParam = new WindowManager.LayoutParams(
                200, 200,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        overlayParam.gravity = Gravity.TOP + Gravity.LEFT;

        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .addView(this, overlayParam);

        Log.d(LOGTAG, "Added system alert window...");
    }

    private static Pokemongo instance;

    private static void initPokemongo()
    {
        if (instance != null) return;

        try
        {
            Application app = getApplicationUsingReflection();

            instance = new Pokemongo(app);
        }
        catch (Exception ex)
        {
            Log.d(LOGTAG, "Init failed...");
            ex.printStackTrace();
        }
    }

    private static Application getApplicationUsingReflection() throws Exception
    {
        return (Application) Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication").invoke(null, (Object[]) null);
    }

    private void sampleCalls(Location location)
    {
        deziLocation(location);

        String bla = "fasel";

        byte[] data = new byte[ 1 ];
        int offset = 1;
        int size = 2;
        ByteBuffer buffer = null;
        String url = "url";

        pokeOpenFile(url);
        pokeWriteBytes(url, data, offset, size);
        pokeWriteBuffer(url, buffer, offset, size);
        pokeCloseFile(url);
    }

    private static double lat = 53.0;
    private static double lon = 10.0;

    public static void deziLocation(Location location)
    {
        initPokemongo();

        location.setLatitude(lat);
        location.setLongitude(lon);

        //lat += 0.0003;

        Log.d(LOGTAG, "lat=" + location.getLatitude() + " lng=" + location.getLongitude());
    }

    private static final Map<Integer, OutputStream> outputs = new HashMap<>();

    public static void pokeOpenFile(String url)
    {
        int urlhash = System.identityHashCode(url);

        Log.d(LOGTAG, "pokeOpenFile url=" + url + " hash=" + urlhash);

        if (! url.contains("pgorelease.nianticlabs.com")) return;

        DateFormat df = new SimpleDateFormat("yyyyMMdd'.'HHmmss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String filename = "pm." + df.format(new Date()) + ".txt";

        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, filename);

        if (extfile.exists())
        {
            Log.d(LOGTAG, "pokeOpenFile toofast... url=" + url + " hash=" + urlhash);

            return;
        }

        Log.d(LOGTAG, "pokeOpenFile url=" + url + " file=" + extfile.toString());

        try
        {
            OutputStream out = new FileOutputStream(extfile);

            out.write(url.getBytes());
            out.write("\n--\n".getBytes());

            outputs.put(urlhash, out);
        }
        catch (Exception ignore)
        {
        }
    }

    public static void pokeWriteBytes(String url, byte[] buffer, int offset, int count)
    {
        int urlhash = System.identityHashCode(url);

        Log.d(LOGTAG, "pokeWriteBytes: offset=" + offset + " len=" + count + " hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);

        if (out != null)
        {
            try
            {
                Log.d(LOGTAG, "pokeWriteBytes: found... offset=" + offset + " len=" + count);

                out.write(buffer, offset, count);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeWriteBuffer(String url, ByteBuffer buffer, int offset, int count)
    {
        int urlhash = System.identityHashCode(url);

        Log.d(LOGTAG, "pokeWriteBuffer: offset=" + offset + " len=" + count + " hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);

        if (out != null)
        {
            try
            {
                Log.d(LOGTAG, "pokeWriteBuffer: found... offset=" + offset + " len=" + count);

                out.write("\n--\n".getBytes());

                if ((buffer != null) && buffer.hasArray())
                {
                    out.write(buffer.array(), offset, count);
                }
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeCloseFile(String url)
    {
        int urlhash = System.identityHashCode(url);

        Log.d(LOGTAG, "pokeCloseFile: hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);

        if (out != null)
        {
            try
            {
                Log.d(LOGTAG, "pokeCloseFile: closed... out=" + out + " hash=" + urlhash);

                out.close();

                outputs.remove(urlhash);
            }
            catch (Exception ignore)
            {
            }

            out = null;
        }
    }

    public static void testDat()
    {
        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, "pm.20160722.132741.txt");

        byte[] data = Simple.readBinaryFile(extfile);
        Log.d(LOGTAG, "testDat: size=" + data.length);

        byte[] match = "\n--\n".getBytes();

        int von = -1;
        int bis = -1;

        for (int inx = 4; inx < data.length; inx++)
        {
            if ((data[ inx - 4 ] == (int) '\n') && (data[ inx - 3 ] == (int) '-') && (data[ inx - 2 ] == (int) '-') && (data[ inx - 1 ] == (int) '\n'))
            {
                if (von < 0)
                {
                    von = inx;
                }
                else
                {
                    if (bis < 0)
                    {
                        bis = inx - 4;
                    }
                }
            }
        }

        ProtoBufferDecode decode;
        JSONObject protos = PokemonProto.getProtos();
        JSONObject json;

        Log.d(LOGTAG, "testDat: post=" + (bis - von));
        Log.d(LOGTAG, "testDat: post=" + Simple.getHexBytesToString(data, von, 16));

        /*
        decode = new ProtoBufferDecode(data, von, bis);
        decode.setProtos(protos);
        json = decode.decode(".POGOProtos.Networking.Envelopes.RequestEnvelope");
        tuneRequest(json);
        Log.d(LOGTAG,"testDat: " + Json.toPretty(json));
        */

        bis += 4;
        bis += 4;

        Log.d(LOGTAG, "testDat: read=" + (data.length - bis));
        Log.d(LOGTAG, "testDat: read=" + Simple.getHexBytesToString(data, bis, 16));
        decode = new ProtoBufferDecode(data, bis, data.length);
        decode.setProtos(protos);
        json = decode.decode(".POGOProtos.Networking.Envelopes.ResponseEnvelope");
        Log.d(LOGTAG,"testDat: " + Json.toPretty(json));
    }

    @Nullable
    public static String decodeRequest(byte[] request)
    {
        ProtoBufferDecode decode = new ProtoBufferDecode(request);
        JSONObject protos = PokemonProto.getProtos();
        decode.setProtos(protos);
        JSONObject json = decode.decode(".POGOProtos.Networking.Envelopes.RequestEnvelope");
        tuneRequest(json);

        if (json != null)
        {
            try
            {
                return json.toString(2);
            }
            catch (Exception ignored)
            {
            }
        }

        return null;
    }

    public static void tuneRequest(JSONObject json)
    {
        try
        {
            json.remove("auth_ticket@.POGOProtos.Networking.Envelopes.AuthTicket");
            json.remove("unknown6@.POGOProtos.Networking.Envelopes.Unknown6");
            json.remove("unknown12@int64");

            JSONArray requests = json.getJSONArray("requests@.POGOProtos.Networking.Requests.Request");

            for (int rinx = 0; rinx < requests.length(); rinx++)
            {
                JSONObject request = requests.getJSONObject(rinx);

                String reqtype = request.getString("request_type@.POGOProtos.Networking.Requests.RequestType");

                if (request.has("request_message@bytes"))
                {
                    JSONArray reqbytes = request.getJSONArray("request_message@bytes");
                    byte[] reqdata = new byte[ reqbytes.length() ];

                    for (int inx = 0; inx < reqbytes.length(); inx++)
                    {
                        reqdata[ inx ] = (byte) reqbytes.getInt(inx);
                    }

                    String messagename = "";
                    boolean nextUp = true;

                    for (int inx = 0; inx < reqtype.length(); inx++)
                    {
                        if (reqtype.charAt(inx) == '@') break;

                        if (reqtype.charAt(inx) == '_')
                        {
                            nextUp = true;
                            continue;
                        }

                        messagename += nextUp ? reqtype.charAt(inx) : Character.toLowerCase(reqtype.charAt(inx));
                        nextUp = false;
                    }

                    messagename = ".POGOProtos.Networking.Requests.Messages." + messagename + "Message";

                    Log.d(LOGTAG, "tuneUp reqtype=" + reqtype + " messagename=" + messagename);

                    ProtoBufferDecode decode = new ProtoBufferDecode(reqdata);
                    decode.setProtos(PokemonProto.getProtos());

                    JSONObject tune = decode.decode(messagename);
                    request.put("request_message@bytes", tune);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
