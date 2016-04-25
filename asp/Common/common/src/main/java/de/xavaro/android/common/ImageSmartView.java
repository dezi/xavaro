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
        //Log.d(LOGTAG, "setImageResource: " + restag);

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
        super.setPadding(left, top, right, bottom);

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        //Log.d(LOGTAG, "onAttachedToWindow");

        if ((width > 0) && (height > 0) && ! reffed)
        {
            ImageSmartCache.claimImage(restag, width, height, circle);
            reffed = true;
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        //Log.d(LOGTAG, "onDetachedFromWindow");

        if (reffed)
        {
            ImageSmartCache.releaseImage(restag, width, height, circle);
            reffed = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = MeasureSpec.getSize(widthMeasureSpec) - left - right;
        height = MeasureSpec.getSize(heightMeasureSpec) - top - bottom;

        dstrect.set(left, top, left + width, top + height);

        //Log.d(LOGTAG, "onMeasure: " + width + "x" + height);

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

        //(LOGTAG, "onDraw bitmap:" + bitmap);

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
