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
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.PokemonDecode;

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
            public boolean onTouch(View view, MotionEvent event)
            {
                return onTouchCommandWindow(view, event);
            }
        });

        overlayParam = new WindowManager.LayoutParams(
                xsize, ysize,
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

    private final static int xsize = 200;
    private final static int ysize = 200;

    private static boolean onTouchCommandWindow(View view, MotionEvent ev)
    {
        int actX = (int) ev.getX();
        int actY = (int) ev.getY();

        int relX = actX - (xsize / 2);
        int relY = actY - (ysize / 2);

        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {

            // 100 => 0.01 ? 10000

            if ((Math.abs(relX) < 30) && (Math.abs(relY) < 30))
            {
                latMove = 0;
                lonMove = 0;
            }
            else
            {
                latMove = -relY / 100000.0;
                lonMove =  relX / 100000.0;
            }

            Log.d(LOGTAG, "Alert touch down: relX=" + relX + " relY=" + relY);

            Log.d(LOGTAG, "Alert touch down:"
                    + " latMove=" + String.format(Locale.ROOT, "%.6f", latMove)
                    + " lonMove=" + String.format(Locale.ROOT, "%.6f", lonMove));
        }

        return false;
    }

    private static Pokemongo instance;

    private static void initPokemongo(Location location)
    {
        if (instance != null) return;

        if (lat == 0) lat = location.getLatitude();
        if (lon == 0) lon = location.getLongitude();

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

        pokeWriteText(url);
        pokeOpenFile(url);
        pokeWriteBytes(url, data, offset, size);
        pokeWriteBuffer(url, buffer, offset, size);
        pokeCloseFile(url);
    }

    //private static double lat = 0;
    //private static double lon = 0;

    // Hamburg => 53.544107, 9.985271
    private static double lat = 53.544107;
    private static double lon = 9.985271;

    private static double latMove = 0;
    private static double lonMove = 0;

    public static void deziLocation(Location location)
    {
        initPokemongo(location);

        location.setLatitude(lat);
        location.setLongitude(lon);

        lat += latMove;
        lon += lonMove;

        Log.d(LOGTAG, "deziLocation:"
                + " lat=" + location.getLatitude()
                + " lon=" + location.getLongitude()
                + " latMove=" + String.format(Locale.ROOT, "%.6f", latMove)
                + " lonMove=" + String.format(Locale.ROOT, "%.6f", lonMove)
        );
    }

    private static final Map<Integer, OutputStream> outputs = new HashMap<>();
    private static final Map<Integer, byte[]> postdata = new HashMap<>();

    public static void pokeOpenFile(String url)
    {
        int urlhash = System.identityHashCode(url);

        Log.d(LOGTAG, "pokeOpenFile url=" + url + " hash=" + urlhash);

        if (! url.contains("pgorelease.nianticlabs.com")) return;

        DateFormat df = new SimpleDateFormat("yyyyMMdd'.'HHmmss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String filename = "pm." + df.format(new Date()) + ".json";

        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, filename);

        if (extfile.exists())
        {
            Log.d(LOGTAG, "pokeOpenFile toofast... url=" + url + " hash=" + urlhash);

            return;
        }

        //Log.d(LOGTAG, "pokeOpenFile url=" + url + " file=" + extfile.toString());

        try
        {
            OutputStream out = new FileOutputStream(extfile);

            //out.write(url.getBytes());
            //out.write("\n--\n".getBytes());

            outputs.put(urlhash, out);
            postdata.put(urlhash, new byte[ 0 ]);
        }
        catch (Exception ignore)
        {
        }
    }

    public static void pokeWriteText(String text)
    {
        //Log.d(LOGTAG, "pokeWriteText: text=" + text);
    }

    public static void pokeWriteBytes(String url, byte[] buffer, int offset, int count)
    {
        int urlhash = System.identityHashCode(url);

        //Log.d(LOGTAG, "pokeWriteBytes: offset=" + offset + " len=" + count + " hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);
        byte[] request = postdata.get(urlhash);

        if (out != null)
        {
            try
            {
                //Log.d(LOGTAG, "pokeWriteBytes: found... offset=" + offset + " len=" + count);

                //out.write(buffer, offset, count);

                byte[] newdata = new byte[ request.length + (count - offset) ];

                System.arraycopy(request, 0, newdata, 0, request.length);
                System.arraycopy(buffer, offset, newdata, request.length, count - offset);

                postdata.put(urlhash, newdata);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeWriteBuffer(String url, ByteBuffer buffer, int offset, int count)
    {
        int urlhash = System.identityHashCode(url);

        //Log.d(LOGTAG, "pokeWriteBuffer: offset=" + offset + " len=" + count + " hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);
        byte[] request = postdata.get(urlhash);

        if (out != null)
        {
            try
            {
                //Log.d(LOGTAG, "pokeWriteBuffer: found... offset=" + offset + " len=" + count);

                //out.write("\n--\n".getBytes());

                if ((buffer != null) && buffer.hasArray())
                {
                    byte[] response = new byte[ count ];
                    System.arraycopy(buffer.array(), buffer.arrayOffset() + offset, response, 0, count);

                    //out.write(response);

                    PokemonDecode pd = new PokemonDecode();

                    JSONObject result = pd.decode(url, request, response);

                    if (result != null)
                    {
                        String jres = result.toString(2);

                        //out.write("\n--\n".getBytes());
                        out.write(jres.replace("\\/","/").getBytes());

                        evalFortDetails(result);
                    }
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

        //Log.d(LOGTAG, "pokeCloseFile: hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);

        if (out != null)
        {
            try
            {
                //Log.d(LOGTAG, "pokeCloseFile: closed... out=" + out + " hash=" + urlhash);

                out.close();

                outputs.remove(urlhash);
                postdata.remove(urlhash);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    private static void evalFortDetails(JSONObject json)
    {
        try
        {
            JSONObject response = json.getJSONObject("response");
            JSONArray returns = response.getJSONArray("returns@array");

            for (int inx = 0; inx < returns.length(); inx++)
            {
                JSONObject returnobj = returns.getJSONObject(inx);
                String type = returnobj.getString("type");
                if (type.equals(".POGOProtos.Networking.Responses.FortDetailsResponse"))
                {
                    JSONObject data = returnobj.getJSONObject("data");

                    double latloc = data.getDouble("latitude@double");
                    double lonloc = data.getDouble("longitude@double");

                    Log.d(LOGTAG, "Fort encountered: lat=" + latloc + " lon=" + lonloc);

                    lat = latloc;
                    lon = lonloc;
                    latMove = 0;
                    lonMove = 0;
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

    public static void testDat()
    {
        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, "pm.20160723.082357.json");

        byte[] data = Simple.readBinaryFile(extfile);
        Log.d(LOGTAG, "testDat: size=" + data.length);

        int von = -1;
        int bis = -1;
        int eof = -1;

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
                    else
                    {
                        if (eof < 0)
                        {
                            eof = inx - 4;
                        }
                    }
                }
            }
        }

        byte[] request = new byte[ bis - von ];

        System.arraycopy(data, von, request, 0, bis - von);

        bis += 4;

        byte[] response = new byte[ eof - bis ];

        System.arraycopy(data, bis, response, 0, eof - bis);

        PokemonDecode pd = new PokemonDecode();

        JSONObject result = pd.decode("test", request, response);

        Log.d(LOGTAG,"testDat: " + Json.toPretty(result));
    }
}
