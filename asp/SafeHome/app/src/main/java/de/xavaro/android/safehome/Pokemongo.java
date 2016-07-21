package de.xavaro.android.safehome;

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
import java.util.Locale;
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

        logDat(bla);
        byte[] data = new byte[ 1 ];
        int offset = 1;
        int size = 2;
        ByteBuffer buffer = null;

        pokeOpenFile("url", "headers");
        pokeWriteText("info1");
        pokeWriteBytes(data, offset, size);
        pokeWriteBuffer(buffer, offset, size);
        pokeCloseFile();
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

    private static OutputStream out;

    public static void pokeOpenFile(String url, String headers)
    {
        pokeCloseFile();

        if (! url.contains("pgorelease.nianticlabs.com")) return;

        DateFormat df = new SimpleDateFormat("yyyyMMdd'.'HHmmss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String filename = "pm." + df.format(new Date()) + ".txt";

        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, filename);

        Log.d(LOGTAG, "pokeOpenFile " + extfile.toString() + " url=" + url + " headers=" + headers);

        try
        {
            out = new FileOutputStream(extfile);

            out.write(url.getBytes());
            out.write("\n".getBytes());
            out.write(headers.getBytes());
            out.write("\n--\n".getBytes());
        }
        catch (Exception ignore)
        {
            out = null;
        }
    }

    public static void pokeWriteText(String text)
    {
        if (out != null)
        {
            try
            {
                out.write(text.getBytes());
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeWriteBytes(byte[] buffer, int offset, int count)
    {
        if (out != null)
        {
            try
            {
                Log.d(LOGTAG, "pokeWriteBytes: offset=" + offset + " len=" + count);

                out.write(buffer, offset, count);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeWriteBuffer(ByteBuffer buffer, int offset, int count)
    {
        if ((out != null) && (buffer != null) && buffer.hasArray())
        {
            try
            {
                out.write("\n--\n".getBytes());
                out.write(buffer.array(), offset, count);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeCloseFile()
    {
        if (out != null)
        {
            try
            {
                out.close();
            }
            catch (Exception ignore)
            {
            }

            out = null;
        }
    }

    public static void logDat(String text)
    {
        Log.d(LOGTAG, "text=" + text);
    }

    public static void testDat()
    {
        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, "pm.20160721.144623.txt");

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

        Log.d(LOGTAG, "testDat: post=" + (bis - von));
        ProtoBufferDecode decode = new ProtoBufferDecode(data, von, bis);
        JSONObject protos = PokemonProto.getProtos();
        decode.setProtos(protos);
        JSONObject json = decode.decode(".POGOProtos.Networking.Envelopes.RequestEnvelope");

        //tuneUp(json);
        Log.d(LOGTAG,"testDat: " + Json.toPretty(json));

        /*
        decode = new ProtoBufferDecode(data, bis + 4, data.length);
        protos = PokemonProto.getProtos();
        decode.setProtos(protos);
        json = decode.decode(".POGOProtos.Networking.Envelopes.ResponseEnvelope");
        Log.d(LOGTAG,"testDat: " + Json.toPretty(json));
        */
    }

    private static void tuneUp(JSONObject json)
    {
        try
        {
            json.remove("unknown6@.POGOProtos.Networking.Envelopes.Unknown6");

            JSONObject request = json.getJSONObject("requests@.POGOProtos.Networking.Requests.Request");
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
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
