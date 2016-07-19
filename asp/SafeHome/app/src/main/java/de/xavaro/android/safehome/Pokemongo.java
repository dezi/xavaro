package de.xavaro.android.safehome;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Pokemongo
{
    private static final String LOGTAG = "POKEDEZI #8";

    private void updateLocation(Location location)
    {
        deziLocation(location);

        String bla = "fasel";

        logDat(bla);
        byte[] data = new byte[ 1 ];
        int offset = 1;
        int size = 2;

        pokeOpenFile("test", "headers");
        pokeWriteText("test2");
        pokeWriteBytes(data, offset, size);
        pokeCloseFile();
    }

    private static double lat = 53.0;
    private static double lon = 10.0;

    public static void deziLocation(Location location)
    {
        location.setLatitude(lat);
        location.setLongitude(lon);

        lat += 0.0001;

        Log.d(LOGTAG, "lat=" + location.getLatitude() + " lng=" + location.getLongitude());
    }

    private static OutputStream out;

    public static void pokeOpenFile(String url, String headers)
    {
        DateFormat df = new SimpleDateFormat("yyyyMMdd'.'HHmmss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String filename = "pm." + df.format(new Date()) + ".txt";

        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, filename);

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
