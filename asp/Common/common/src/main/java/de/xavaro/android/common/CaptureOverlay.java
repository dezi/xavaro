package de.xavaro.android.common;

import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.content.Context;

public class CaptureOverlay extends FrameLayout
{
    private static final String LOGTAG = CaptureOverlay.class.getSimpleName();

    private static CaptureOverlay instance;

    public static CaptureOverlay getInstance()
    {
        if (instance == null) instance = new CaptureOverlay(Simple.getActContext());

        return instance;
    }

    private WindowManager.LayoutParams overlayParam;
    private int isAttached;

    private CaptureOverlay(Context context)
    {
        super(context);

        setBackgroundColor(0x22880000);

        overlayParam = new WindowManager.LayoutParams(
                Simple.MP, Simple.MP,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        overlayParam.gravity = Gravity.LEFT | Gravity.TOP;
    }

    public void attachToScreen()
    {
        if (isAttached++ == 0)
        {
            Simple.getWindowManager().addView(this, overlayParam);
        }
    }

    public void detachFromScreen()
    {
        if (--isAttached == 0)
        {
            Simple.getWindowManager().removeView(this);
        }
    }

    public void registerGenericMotionEvent (MotionEvent ev)
    {
        Log.d(LOGTAG, "registerGenericMotionEvent: " + ev);
    }

    public void registerTouchEvent (MotionEvent ev)
    {
        Log.d(LOGTAG, "registerTouchEvent: " + ev);
    }

}
