package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

public class ImageSmartCache
{
    private static final String LOGTAG = ImageSmartCache.class.getSimpleName();

    private static final ArrayList<CacheDesc> cacheDescs = new ArrayList<>();

    public static void claimImage(String restag, int width, int height)
    {
        if (restag == null) return;

        synchronized (cacheDescs)
        {
            for (CacheDesc cacheDesc : cacheDescs)
            {
                if (Simple.equals(cacheDesc.restag, restag) &&
                        (cacheDesc.width == width) &&
                        (cacheDesc.height == height))
                {
                    cacheDesc.refcount++;

                    return;
                }
            }

            CacheDesc cacheDesc = new CacheDesc();

            cacheDesc.restag = restag;
            cacheDesc.width = width;
            cacheDesc.height = height;
            cacheDesc.refcount = 1;

            cacheDesc.bitmap = getBitmap(restag, width, height);

            Log.d(LOGTAG, "claimImage: aquired: " + restag + ":" + width + "x" + height);

            cacheDescs.add(cacheDesc);
        }
    }

    public static void releaseImage(String restag, int width, int height)
    {
        if (restag == null) return;

        synchronized (cacheDescs)
        {
            for (int inx = 0; inx < cacheDescs.size(); inx++)
            {
                CacheDesc cacheDesc = cacheDescs.get(inx);

                if (Simple.equals(cacheDesc.restag, restag) &&
                        (cacheDesc.width == width) &&
                        (cacheDesc.height == height))
                {
                    cacheDesc.refcount--;

                    if (cacheDesc.refcount == 0)
                    {
                        if (cacheDesc.bitmap != null)
                        {
                            cacheDesc.bitmap.recycle();
                            cacheDesc.bitmap = null;
                        }

                        Log.d(LOGTAG, "releaseImage: released: " + restag + ":" + width + "x" + height);

                        cacheDescs.remove(inx);
                    }

                    return;
                }
            }
        }
    }

    @Nullable
    public static Bitmap getCachedBitmap(String restag, int width, int height)
    {
        if (restag == null) return null;

        synchronized (cacheDescs)
        {
            for (CacheDesc cacheDesc : cacheDescs)
            {
                if (Simple.equals(cacheDesc.restag, restag) &&
                        (cacheDesc.width == width) &&
                        (cacheDesc.height == height))
                {
                    return cacheDesc.bitmap;
                }
            }
        }

        return null;
    }

    private static void adjustOptions(BitmapFactory.Options options, int width, int height)
    {
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;

        width = width << 1;
        height = height << 1;

        int bmwidth = options.outWidth;
        int bmheight = options.outHeight;

        while (((bmwidth >> 1) >= width) && ((bmheight >> 1) >= height))
        {
            options.inSampleSize++;

            bmwidth = bmwidth >> 1;
            bmheight = bmheight >> 1;
        }
    }

    @Nullable
    private static Bitmap getBitmap(String restag, int width, int height)
    {
        //
        // In the first step we just like to get the size of bitmap.
        //

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap tmp = null;

        int resid = Simple.parseNumber(restag);

        if (resid > 0)
        {
            BitmapFactory.decodeResource(Simple.getAnyContext().getResources(), resid, options);
            adjustOptions(options, width, height);
            tmp = BitmapFactory.decodeResource(Simple.getAnyContext().getResources(), resid, options);
        }
        else
        {
            Log.d(LOGTAG, "getBitmap:" + restag);

            byte data[] = WebApp.getAppIconData(restag);

            if (data != null)
            {
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                adjustOptions(options, width, height);
                tmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            }
        }

        if (tmp == null) return null;

        Bitmap end;

        if ((tmp.getWidth() < (width << 1)) || (tmp.getHeight() < (height << 1)))
        {
            //
            // Bitmap is too small to be anti aliased.
            //

            end = Bitmap.createScaledBitmap(tmp, width, height, true);
        }
        else
        {
            //
            // Anti alias bitmap.
            //

            end = StaticUtils.downscaleAntiAliasBitmap(tmp, width, height);
        }

        tmp.recycle();

        return end;
    }

    private static class CacheDesc
    {
        public String restag;
        public int width;
        public int height;

        public int refcount;
        public Bitmap bitmap;
    }
}
