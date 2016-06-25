package de.xavaro.android.common;

import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.view.MotionEvent;
import android.content.Context;
import android.util.Log;

public class CaptureOverlay extends FrameLayout
{
    private static final String LOGTAG = CaptureOverlay.class.getSimpleName();

    private static CaptureOverlay instance;

    public static CaptureOverlay getInstance()
    {
        if (instance == null) instance = new CaptureOverlay(Simple.getAppContext());

        return instance;
    }

    private WindowManager.LayoutParams overlayParam;
    private int isAttached;

    private static final int handHotXspot = 210;
    private static final int handHotYspot =  75;

    private FrameLayout.LayoutParams handLayout;
    private ImageSmartView handTap;
    private int handSize;
    private int handXoff;
    private int handYoff;

    private CaptureOverlay(Context context)
    {
        super(context);

        setBackgroundColor(0x22880000);

        overlayParam = new WindowManager.LayoutParams(
                Simple.MP, Simple.MP,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        handSize = 128;
        handXoff = (handHotXspot * handSize / 512);
        handYoff = (handHotYspot * handSize / 512);

        handLayout = new FrameLayout.LayoutParams(handSize, handSize);

        handTap = new ImageSmartView(getContext());
        handTap.setLayoutParams(handLayout);
        handTap.setImageResource(R.drawable.hand_blue_tap_512x512);
        handTap.setVisibility(GONE);

        addView(handTap);
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
        if (ev.getAction() == MotionEvent.ACTION_HOVER_MOVE)
        {
            handLayout.topMargin  = ((int) ev.getY()) - handYoff;
            handLayout.leftMargin = ((int) ev.getX()) - handXoff;

            handTap.setLayoutParams(handLayout);
            handTap.setVisibility(VISIBLE);
        }

        Log.d(LOGTAG, "registerGenericMotionEvent: " + ev);
    }

    public void registerTouchEvent (MotionEvent ev)
    {
        if (ev.getAction() == MotionEvent.ACTION_MOVE)
        {
            handLayout.topMargin  = ((int) ev.getY()) - handYoff;
            handLayout.leftMargin = ((int) ev.getX()) - handXoff;

            handTap.setLayoutParams(handLayout);
            handTap.setVisibility(VISIBLE);
        }

        Log.d(LOGTAG, "registerTouchEvent: " + ev);
    }
}
