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

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Pokemongo extends FrameLayout
{
    private static final String LOGTAG = "POKEDEZI #10";

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

        lat += 0.0003;

        Log.d(LOGTAG, "lat=" + location.getLatitude() + " lng=" + location.getLongitude());
    }

    private static OutputStream out;

    public static void pokeOpenFile(String url, String headers)
    {
        pokeCloseFile();

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
            out.write("\r".getBytes());
            out.write(headers.getBytes());
            out.write("\r".getBytes());
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
}
