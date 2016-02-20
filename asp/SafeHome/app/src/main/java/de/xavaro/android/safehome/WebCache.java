package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;

public class WebCache
{
    private static final String LOGTAG = WebCache.class.getSimpleName();

    @Nullable
    public static Drawable getImage(String src)
    {
        try
        {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            int length = connection.getContentLength();
            byte[] buffer = new byte[ length ];

            InputStream input = connection.getInputStream();

            int xfer;
            int total = 0;

            while (total < length)
            {
                xfer = input.read(buffer, total, length - total);
                total += xfer;
            }

            input.close();

            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            return new BitmapDrawable(Simple.getResources(), bitmap);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static String getContent(String src)
    {
        try
        {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            int length = connection.getContentLength();
            byte[] buffer = new byte[ length ];

            InputStream input = connection.getInputStream();

            int xfer;
            int total = 0;

            while (total < length)
            {
                xfer = input.read(buffer, total, length - total);
                total += xfer;
            }

            input.close();

            return new String(buffer);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }
}
