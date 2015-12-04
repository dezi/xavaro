package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CacheManager
{
    public static Bitmap cacheThumbnail(Context context,String filename,String src)
    {
        File file = new File(context.getCacheDir(), filename);

        if (! file.exists())
        {
            try
            {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(file);

                byte[] bytes = new byte[4096];
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
        }

        if (file.exists())
        {
            try
            {
                FileInputStream input = new FileInputStream(file);

                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                input.close();

                return myBitmap;

            }
            catch (Exception ex)
            {
                return null;
            }
        }

        return null;
    }
}
