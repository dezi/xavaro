package de.xavaro.android.common;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

public class ImageSmartView extends ImageView
{
    private static final String LOGTAG = ImageSmartView.class.getSimpleName();

    private String restag;
    private boolean circle;

    private int width;
    private int height;

    private int left, top, right, bottom;

    private boolean reffed;

    private final Paint npaint = new Paint();
    private final Rect srcrect = new Rect();
    private final Rect dstrect = new Rect();

    public ImageSmartView(Context context)
    {
        super(context);
    }

    @Override
    public void setImageResource(int resid)
    {
        setImageResource("" + resid);
    }

    public void setImageResource(String restag)
    {
        setImageResource(restag, false);
    }

    public void setImageResource(String restag, boolean circle)
    {
        if (Simple.equals(this.restag, restag)) return;

        //
        // Images in preferences get recycled. Check
        // resource id against old id and release and
        // reclame images if required.
        //

        if (this.restag != null)
        {
            if (reffed)
            {
                ImageSmartCache.releaseImage(this.restag, width, height, this.circle);
                reffed = false;
            }

            this.restag = null;

            invalidate();
        }

        this.restag = restag;
        this.circle = circle;

        if ((width > 0) && (height > 0))
        {
            ImageSmartCache.claimImage(this.restag, width, height, this.circle);
            reffed = true;
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        if (reffed)
        {
            ImageSmartCache.releaseImage(restag, width, height, circle);
            reffed = false;
        }

        super.setPadding(left, top, right, bottom);

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        requestLayout();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        //
        // Stop any image detach runner now.
        //

        Simple.removePost(detachRunner);

        if ((width > 0) && (height > 0) && ! reffed)
        {
            ImageSmartCache.claimImage(restag, width, height, circle);
            reffed = true;
        }
    }

    protected final Runnable detachRunner = new Runnable()
    {
        @Override
        public void run()
        {
            if (reffed)
            {
                ImageSmartCache.releaseImage(restag, width, height, circle);
                reffed = false;
            }
        }
    };

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        //
        // Schedule a final release on image to make sure,
        // if view is only detached for rearrangement
        // the image does not get delallocted.
        //

        Simple.makePost(detachRunner, 1000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int newwidth = MeasureSpec.getSize(widthMeasureSpec) - left - right;
        int newheight = MeasureSpec.getSize(heightMeasureSpec) - top - bottom;

        if ((newwidth != width) || (newheight != height))
        {
            if (reffed)
            {
                ImageSmartCache.releaseImage(restag, width, height, circle);
                reffed = false;
            }
        }

        width = newwidth;
        height = newheight;

        dstrect.set(left, top, left + width, top + height);

        Log.d(LOGTAG, "onMeasure: "
                + width + "x" + height + ":"
                + left + ":" + top + ":"
                + right + ":" + bottom
                + "=" + this.restag);

        if (! reffed)
        {
            ImageSmartCache.claimImage(restag, width, height, circle);
            reffed = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Bitmap bitmap = ImageSmartCache.getCachedBitmap(restag, width, height, circle);

        if (bitmap != null)
        {
            srcrect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, srcrect, dstrect, npaint);
        }
        else
        {
            canvas.drawColor(Color.TRANSPARENT);
        }
    }
}
