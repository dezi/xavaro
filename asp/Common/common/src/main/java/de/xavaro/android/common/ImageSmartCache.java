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

    public static void claimImage(int resid, int width, int height)
    {
        synchronized (cacheDescs)
        {
            for (CacheDesc cacheDesc : cacheDescs)
            {
                if ((cacheDesc.resid == resid) &&
                        (cacheDesc.width == width) &&
                        (cacheDesc.height == height))
                {
                    cacheDesc.refcount++;

                    return;
                }
            }

            CacheDesc cacheDesc = new CacheDesc();

            cacheDesc.resid = resid;
            cacheDesc.width = width;
            cacheDesc.height = height;
            cacheDesc.refcount = 1;

            cacheDesc.bitmap = getBitmap(resid, width, height);

            Log.d(LOGTAG, "claimImage: aquired: " + resid + ":" + cacheDesc.bitmap.getWidth() + "x" + cacheDesc.bitmap.getHeight());

            cacheDescs.add(cacheDesc);
        }
    }

    public static void releaseImage(int resid, int width, int height)
    {
        synchronized (cacheDescs)
        {
            for (int inx = 0; inx < cacheDescs.size(); inx++)
            {
                CacheDesc cacheDesc = cacheDescs.get(inx);

                if ((cacheDesc.resid == resid) &&
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

                        cacheDescs.remove(inx);
                    }
                    
                    return;
                }
            }
        }
    }

    @Nullable
    public static Bitmap getCachedBitmap(int resid, int width, int height)
    {
        synchronized (cacheDescs)
        {
            for (CacheDesc cacheDesc : cacheDescs)
            {
                if ((cacheDesc.resid == resid) &&
                        (cacheDesc.width == width) &&
                        (cacheDesc.height == height))
                {
                    return cacheDesc.bitmap;
                }
            }
        }

        return null;
    }

    public static Bitmap getBitmap(int resid, int width, int height)
    {
        Bitmap tmp = BitmapFactory.decodeResource(Simple.getAnyContext().getResources(), resid);
        return Bitmap.createScaledBitmap(tmp, width, height, true);
    }

    private static class CacheDesc
    {
        public int resid;
        public int width;
        public int height;

        public int refcount;
        public Bitmap bitmap;
    }
}
