package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.File;

public class ImageSmartCache
{
    private static final String LOGTAG = ImageSmartCache.class.getSimpleName();

    private static final ArrayList<CacheDesc> cacheDescs = new ArrayList<>();

    public static void claimImage(String restag, int width, int height, boolean circle)
    {
        if (restag == null) return;

        synchronized (cacheDescs)
        {
            for (CacheDesc cacheDesc : cacheDescs)
            {
                if (Simple.equals(cacheDesc.restag, restag) &&
                        (cacheDesc.circle == circle) &&
                        (cacheDesc.width == width) &&
                        (cacheDesc.height == height))
                {
                    cacheDesc.refcount++;

                    return;
                }
            }

            CacheDesc cacheDesc = new CacheDesc();

            cacheDesc.restag = restag;
            cacheDesc.circle = circle;
            cacheDesc.width = width;
            cacheDesc.height = height;
            cacheDesc.refcount = 1;

            cacheDesc.bitmap = getBitmap(restag, width, height, circle);

            cacheDescs.add(cacheDesc);
        }
    }

    public static void releaseImage(String restag, int width, int height, boolean circle)
    {
        if (restag == null) return;

        synchronized (cacheDescs)
        {
            for (int inx = 0; inx < cacheDescs.size(); inx++)
            {
                CacheDesc cacheDesc = cacheDescs.get(inx);

                if (Simple.equals(cacheDesc.restag, restag) &&
                        (cacheDesc.circle == circle) &&
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
    public static Bitmap getCachedBitmap(String restag, int width, int height, boolean circle)
    {
        if (restag == null) return null;

        synchronized (cacheDescs)
        {
            for (CacheDesc cacheDesc : cacheDescs)
            {
                if (Simple.equals(cacheDesc.restag, restag) &&
                        (cacheDesc.circle == circle) &&
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
    private static Bitmap getBitmap(String restag, int width, int height, boolean circle)
    {
        String cachename = restag.replace("|", ".");

        int resid = Simple.parseNumber(restag);

        if (resid > 0)
        {
            cachename = "resource." + Simple.getResourceName(resid) + ".icon";
        }

        if (restag.startsWith("/"))
        {
            File file = new File(restag);
            cachename = "file." + file.getName();
        }

        if (! cachename.contains(".")) cachename = "webapp." + cachename + ".icon";

        cachename = width + "x" + height + "." + (circle ? "circle" : "normal") + "." + cachename;

        File cachedir = new File(Simple.getExternalCacheDir(), "screenicons");
        File cachefile = new File(cachedir, cachename);

        if ((! cachedir.exists()) && cachedir.mkdirs())
        {
            Log.d(LOGTAG, "getBitmap: cache dir created.");
        }

        Log.d(LOGTAG, "getBitmap: load: " + cachename);

        if (cachefile.exists())
        {
            byte[] data = Simple.getFileBytes(cachefile);

            if (data != null)
            {
                return BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        }

        Bitmap bitmap = getNewBitmap(restag, width, height, circle);

        if (bitmap != null)
        {
            try
            {
                FileOutputStream out = new FileOutputStream(cachefile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return bitmap;
    }

    @Nullable
    private static Bitmap getNewBitmap(String restag, int width, int height, boolean circle)
    {
        //
        // In the first step we just like to get the size of bitmap.
        //

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap tmp = null;
        byte[] data = null;

        int resid = Simple.parseNumber(restag);

        if (resid > 0)
        {
            //
            // Decode from resources.
            //

            BitmapFactory.decodeResource(Simple.getAnyContext().getResources(), resid, options);
            adjustOptions(options, width, height);
            tmp = BitmapFactory.decodeResource(Simple.getAnyContext().getResources(), resid, options);
        }

        if ((tmp == null) && restag.startsWith("weblib|"))
        {
            String[] parts = restag.split("\\|");

            if (parts.length == 3)
            {
                data = WebLib.getIconData(parts[ 1 ], parts[ 2 ]);
            }
        }

        if ((tmp == null) && (data == null) && restag.startsWith("/"))
        {
            //
            // Decode from local file cache.
            //

            data = Simple.getFileBytes(new File(restag));
        }

        if ((tmp == null) && (data == null))
        {
            data = WebApp.getAppIconData(restag);
        }

        if (data != null)
        {
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            adjustOptions(options, width, height);
            tmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }

        if (tmp == null) return null;

        Bitmap end;

        if ((tmp.getWidth() < (width << 1)) || (tmp.getHeight() < (height << 1)))
        {
            //
            // Bitmap is too small to be anti aliased.
            //

            end = tmp;
        }
        else
        {
            //
            // Anti alias bitmap.
            //

            Log.d(LOGTAG, "getBitmap: anti-alias=" + restag + ":" + width + "x" + height);

            end = StaticUtils.downscaleAntiAliasBitmap(tmp, width, height);
            tmp.recycle();
        }

        if (! circle) return end;

        //
        // Make circle bitmap.
        //

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        Rect srcrect = new Rect(0, 0, end.getWidth(), end.getHeight());
        Rect dstrect = new Rect(0, 0, width, height);
        RectF dstrectF = new RectF(dstrect);

        Bitmap cbm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cbm);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawOval(dstrectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(end, srcrect, dstrect, paint);

        end.recycle();

        return cbm;
    }

    private static class CacheDesc
    {
        public String restag;
        public int width;
        public int height;
        public boolean circle;

        public int refcount;
        public Bitmap bitmap;
    }
}
