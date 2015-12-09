package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CacheManager
{
    private final static String LOGTAG = "CacheManager";

    @Nullable
    public static Bitmap getThumbnail(Context context,String filename)
    {
        File file = new File(context.getCacheDir(), filename);

        if (file.exists())
        {
            Log.d(LOGTAG,"getThumbnail: load " + file.toString());

            try
            {
                FileInputStream input = new FileInputStream(file);

                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                input.close();

                Log.d(LOGTAG, "getThumbnail: load done ");

                return myBitmap;

            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG,ex);
            }
        }

        return null;
    }

    @Nullable
    public static Bitmap cacheThumbnail(Context context,String filename,String src)
    {
        File file = new File(context.getCacheDir(), filename);

        if (! file.exists())
        {
            Log.d(LOGTAG,"cacheThumbnail: fetch " + filename);

            try
            {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(file);

                byte[] bytes = new byte[ 4096 ];
                int xfer;

                while ((xfer = input.read(bytes)) > 0)
                {
                    output.write(bytes, 0, xfer);
                }

                output.close();
                input.close();
            }
            catch (Exception ex)
            {
                if (file.exists()) file.delete();

                return null;
            }

            Log.d(LOGTAG,"cacheThumbnail: fetch done");
        }

        return getThumbnail(context,filename);
    }
}
