package de.xavaro.android.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.PixelFormat;
import android.view.InputDevice;
import android.view.View;
import android.view.ViewGroup;
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

    private ImageSmartView handTap;
    private ImageSmartView handTap1;
    private ImageSmartView handTap2;
    private ImageSmartView handTap3;
    private ImageSmartView handMoveLeft;
    private ImageSmartView handMoveRight;
    private ImageSmartView handMoveUp;
    private ImageSmartView handMoveDown;
    private ImageSmartView markerRed;

    private FrameLayout.LayoutParams handLayoutMov;
    private int handSizeMov;
    private int handXoff;
    private int handYoff;

    private FrameLayout.LayoutParams handLayoutTap;
    private int handSizeTap;
    private int handXtap;
    private int handYtap;
    private int tapState;
    private int tapDelay = 80;

    private FrameLayout.LayoutParams markerLayout;
    private int markerScale;
    private int markerXoff;
    private int markerYoff;

    private int maxX;
    private int maxY;
    private int orgX;
    private int orgY;
    private int lastX;
    private int lastY;
    private boolean isMove;

    private CaptureOverlay(Context context)
    {
        super(context);

        setBackgroundColor(0x22880000);

        overlayParam = new WindowManager.LayoutParams(
                Simple.MP, Simple.MP,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        handSizeMov = 128;
        handXoff = (handHotXspot * handSizeMov / 512);
        handYoff = (handHotYspot * handSizeMov / 512);

        handSizeTap = (768 * handSizeMov) / 512;
        handXtap = ((handHotXspot + 256) * handSizeTap / 768);
        handYtap = ((handHotYspot + 256) * handSizeTap / 768);

        handLayoutMov = new FrameLayout.LayoutParams(handSizeMov, handSizeMov);
        handLayoutTap = new FrameLayout.LayoutParams(handSizeTap, handSizeTap);

        handTap = new ImageSmartView(getContext());
        handTap.setLayoutParams(handLayoutMov);
        handTap.setImageResource(R.drawable.hand_blue_tap_512x512);
        handTap.setVisibility(GONE);
        addView(handTap);

        handTap1 = new ImageSmartView(getContext());
        handTap1.setLayoutParams(handLayoutTap);
        handTap1.setImageResource(R.drawable.hand_blue_tap1_768x768);
        handTap1.setVisibility(GONE);
        addView(handTap1);

        handTap2 = new ImageSmartView(getContext());
        handTap2.setLayoutParams(handLayoutTap);
        handTap2.setImageResource(R.drawable.hand_blue_tap2_768x768);
        handTap2.setVisibility(GONE);
        addView(handTap2);

        handTap3 = new ImageSmartView(getContext());
        handTap3.setLayoutParams(handLayoutTap);
        handTap3.setImageResource(R.drawable.hand_blue_tap3_768x768);
        handTap3.setVisibility(GONE);
        addView(handTap3);

        handMoveLeft = new ImageSmartView(getContext());
        handMoveLeft.setLayoutParams(handLayoutMov);
        handMoveLeft.setImageResource(R.drawable.hand_blue_swipe_left_512x512);
        handMoveLeft.setVisibility(GONE);
        addView(handMoveLeft);

        handMoveRight = new ImageSmartView(getContext());
        handMoveRight.setLayoutParams(handLayoutMov);
        handMoveRight.setImageResource(R.drawable.hand_blue_swipe_right_512x512);
        handMoveRight.setVisibility(GONE);
        addView(handMoveRight);

        handMoveUp = new ImageSmartView(getContext());
        handMoveUp.setLayoutParams(handLayoutMov);
        handMoveUp.setImageResource(R.drawable.hand_blue_swipe_up_512x512);
        handMoveUp.setVisibility(GONE);
        addView(handMoveUp);

        handMoveDown = new ImageSmartView(getContext());
        handMoveDown.setLayoutParams(handLayoutMov);
        handMoveDown.setImageResource(R.drawable.hand_blue_swipe_down_512x512);
        handMoveDown.setVisibility(GONE);
        addView(handMoveDown);

        markerScale = 3;
        markerXoff = 200 / markerScale;
        markerYoff = 200 / markerScale;

        markerLayout = new FrameLayout.LayoutParams(640 / markerScale, 400 / markerScale);

        markerRed = new ImageSmartView(getContext());
        markerRed.setLayoutParams(handLayoutMov);
        markerRed.setImageResource(R.drawable.mark_red_640x400);
        markerRed.setVisibility(GONE);
        addView(markerRed);
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

    private final View.OnGenericMotionListener monitorGenericMotionListener = new View.OnGenericMotionListener()
    {
        @Override
        public boolean onGenericMotion(View view, MotionEvent event)
        {
            return registerGenericMotionEvent(event);
        }
    };

    private final View.OnTouchListener monitorTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            return registerTouchEvent(event);
        }
    };

    private void monitorAllTouchesRecurse(View view)
    {
        view.setOnTouchListener(monitorTouchListener);

        if (view instanceof ViewGroup)
        {
            ViewGroup vg = (ViewGroup) view;

            int childcount = vg.getChildCount();

            for (int inx = 0; inx < childcount; inx++)
            {
                monitorAllTouchesRecurse(vg.getChildAt(inx));
            }
        }
    }

    public void monitorAllTouches(Dialog dialog)
    {
        if ((dialog == null) || (dialog.getWindow() == null)) return;

        dialog.getWindow().getDecorView().setOnGenericMotionListener(monitorGenericMotionListener);

        monitorAllTouchesRecurse(dialog.getWindow().getDecorView());
    }

    public boolean registerGenericMotionEvent(MotionEvent ev)
    {
        Log.d(LOGTAG, "registerGenericMotionEvent: " + ev);

        return false;
    }

    public boolean registerTouchEvent(MotionEvent ev)
    {
        if ((ev.getDevice() != null) &&
                ((ev.getDevice().getSources() & InputDevice.SOURCE_MOUSE)
                        == InputDevice.SOURCE_MOUSE))
        {
            if (ev.getAction() == MotionEvent.ACTION_DOWN)
            {
                hideAll();

                markerLayout.topMargin  = ((int) ev.getRawY()) - markerYoff;
                markerLayout.leftMargin = ((int) ev.getRawX()) - markerXoff;

                markerRed.setLayoutParams(markerLayout);
                markerRed.setVisibility(VISIBLE);
            }

            return true;
        }

        int actX = (int) ev.getRawX();
        int actY = (int) ev.getRawY();

        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            isMove = false;

            orgX = maxX = lastX = actX;
            orgY = maxY = lastY = actY;

            handLayoutMov.topMargin  = actY - handYoff;
            handLayoutMov.leftMargin = actX - handXoff;

            handTap.setLayoutParams(handLayoutMov);
            handTap.setVisibility(VISIBLE);

            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE)
        {
            hideAll();

            ImageSmartView img = handTap;

            int diffX = lastX - actX;
            int diffY = lastY - actY;

            if (! isMove)
            {
                isMove = (Math.abs(orgX - actX) > 10) || (Math.abs(orgY - actY) > 10);
            }

            if (isMove)
            {
                if (Math.abs(diffX) > Math.abs(diffY))
                {
                    if (lastX < actX)
                    {
                        img = handMoveRight;
                    }
                    else
                    {
                        img = handMoveLeft;
                    }
                }
                else
                {
                    if (lastY < actY)
                    {
                        img = handMoveDown;
                    }
                    else
                    {
                        img = handMoveUp;
                    }
                }
            }

            handLayoutMov.topMargin  = actY - handYoff;
            handLayoutMov.leftMargin = actX - handXoff;

            img.setLayoutParams(handLayoutMov);
            img.setVisibility(VISIBLE);

            lastX = actX;
            lastY = actY;

            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP)
        {
            if (isMove)
            {
                hideAll();
            }
            else
            {
                handTap.setVisibility(GONE);

                handLayoutTap.topMargin = actY - handYtap;
                handLayoutTap.leftMargin = actX - handXtap;

                handTap1.setLayoutParams(handLayoutTap);
                handTap1.setVisibility(VISIBLE);

                tapState = 1;

                Simple.makePost(animateTap, tapDelay);
            }

            return false;
        }

        Log.d(LOGTAG, "registerTouchEvent: " + ev);

        return false;
    }

    private void hideAll()
    {
        handTap.setVisibility(GONE);
        handTap1.setVisibility(GONE);
        handTap2.setVisibility(GONE);
        handTap3.setVisibility(GONE);
        handMoveLeft.setVisibility(GONE);
        handMoveRight.setVisibility(GONE);
        handMoveUp.setVisibility(GONE);
        handMoveDown.setVisibility(GONE);
        markerRed.setVisibility(GONE);
    }

    private final Runnable animateTap = new Runnable()
    {
        @Override
        public void run()
        {
            if (tapState == 1)
            {
                handTap1.setVisibility(GONE);

                handTap2.setLayoutParams(handLayoutTap);
                handTap2.setVisibility(VISIBLE);

                tapState++;

                Simple.makePost(animateTap, tapDelay);

                return;
            }

            if (tapState == 2)
            {
                handTap2.setVisibility(GONE);

                handTap3.setLayoutParams(handLayoutTap);
                handTap3.setVisibility(VISIBLE);

                tapState++;

                Simple.makePost(animateTap, tapDelay);

                return;
            }

            if (tapState == 3)
            {
                handTap3.setVisibility(GONE);

                tapState = 0;
            }
        }
    };
}
