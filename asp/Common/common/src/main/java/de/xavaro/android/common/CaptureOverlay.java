package de.xavaro.android.common;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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
        if (instance == null)
        {
            instance = new CaptureOverlay(Simple.getAppContext());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (!Settings.canDrawOverlays(Simple.getAppContext()))
                {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Simple.getAppContext().getPackageName()));

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    Simple.getAppContext().startActivity(intent);

                    instance = null;
                }
            }
        }

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
    private FrameLayout frameRed;

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

    private FrameLayout.LayoutParams frameLayout;

    private int orgX;
    private int orgY;
    private int lastX;
    private int lastY;

    private boolean isMove;
    private boolean isHorz;
    private boolean isVert;

    private CaptureOverlay(Context context)
    {
        super(context);

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

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(16);
        gd.setColor(Color.TRANSPARENT);
        gd.setStroke(8, Color.RED);

        frameLayout = new FrameLayout.LayoutParams(0, 0);

        frameRed = new FrameLayout(getContext());
        frameRed.setLayoutParams(frameLayout);
        frameRed.setBackground(gd);
        frameRed.setVisibility(GONE);

        addView(frameRed);
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

    private boolean isMouseEvent(MotionEvent event)
    {
        return ((event.getDevice() != null) &&
                ((event.getDevice().getSources() & InputDevice.SOURCE_MOUSE)
                        == InputDevice.SOURCE_MOUSE));
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

        View view = dialog.getWindow().getDecorView();
        if (view == null) return;

        view.setOnGenericMotionListener(monitorGenericMotionListener);
        monitorAllTouchesRecurse(view);
    }

    public boolean registerGenericMotionEvent(MotionEvent ev)
    {
        //Log.d(LOGTAG, "registerGenericMotionEvent: " + ev);

        return false;
    }

    public boolean registerTouchEvent(MotionEvent ev)
    {
        int actX = (int) ev.getRawX();
        int actY = (int) ev.getRawY();

        int diffX = lastX - actX;
        int diffY = lastY - actY;

        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            isMove = isHorz = isVert = false;

            orgX = lastX = actX;
            orgY = lastY = actY;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE)
        {
            if (! isMove)
            {
                isHorz = (Math.abs(orgX - actX) > 10);
                isVert = (Math.abs(orgY - actY) > 10);
                isMove = isHorz || isVert;

                if (isMove)
                {
                    //
                    // Gesture is move. Try to cancel tap display.
                    //

                    Simple.removePost(showTap);
                }
            }

            if ((Math.abs(diffX) < 10) && (Math.abs(diffY) < 10))
            {
                //
                // Move is to fine.
                //

                return false;
            }
        }

        if (isMouseEvent(ev))
        {
            //
            // Mouse event.
            //

            if (ev.getAction() == MotionEvent.ACTION_DOWN)
            {
                hideAll();

                orgX = lastX = actX;
                orgY = lastY = actY;
            }

            if (ev.getAction() == MotionEvent.ACTION_MOVE)
            {
                if (isMove)
                {
                    frameLayout.width = actX - orgX;
                    frameLayout.height = actY - orgY;

                    frameLayout.leftMargin = orgX;
                    frameLayout.topMargin  = orgY;

                    frameRed.setLayoutParams(frameLayout);
                    frameRed.setVisibility(VISIBLE);
                }
            }

            if (ev.getAction() == MotionEvent.ACTION_UP)
            {
                if (! isMove)
                {
                    if ((actX == (Simple.getDeviceWidth() - 1)) &&
                        (actY == (Simple.getDeviceHeight() - 1)))
                    {
                        CaptureRecorder.getInstance().toggleRecording();
                    }
                    else
                    {
                        markerLayout.leftMargin = actX - markerXoff;
                        markerLayout.topMargin = actY - markerYoff;

                        markerRed.setLayoutParams(markerLayout);
                        markerRed.setVisibility(VISIBLE);

                        Log.d(LOGTAG, "registerTouchEvent: mouse up: " + actX + ":" + actY);
                    }
                }
            }

            return true;
        }
        else
        {
            //
            // Touch event.
            //

            if (ev.getAction() == MotionEvent.ACTION_DOWN)
            {
                handLayoutMov.leftMargin = actX - handXoff;
                handLayoutMov.topMargin = actY - handYoff;

                Simple.makePost(showTap, 250);

                return false;
            }

            if (ev.getAction() == MotionEvent.ACTION_MOVE)
            {
                ImageSmartView img = handTap;

                if (isMove)
                {
                    hideAll();

                    if (isHorz)
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

                    if (isVert)
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


                handLayoutMov.leftMargin = (isHorz ? actX : orgX) - handSizeMov / 2;
                handLayoutMov.topMargin  = (isVert ? actY : orgY) - handSizeMov / 2;

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
                    Simple.removePost(showTap);
                    handTap.setVisibility(GONE);

                    handLayoutTap.leftMargin = actX - handXtap;
                    handLayoutTap.topMargin = actY - handYtap;

                    handTap1.setLayoutParams(handLayoutTap);
                    handTap1.setVisibility(VISIBLE);

                    tapState = 1;

                    Simple.makePost(animateTap, tapDelay);
                }

                return false;
            }
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
        frameRed.setVisibility(GONE);
    }

    private final Runnable showTap = new Runnable()
    {
        @Override
        public void run()
        {
            handTap.setLayoutParams(handLayoutMov);
            handTap.setVisibility(VISIBLE);
        }
    };

    private final Runnable animateTap = new Runnable()
    {
        @Override
        public void run()
        {
            if (tapState == 1)
            {
                frameRed.setVisibility(GONE);
                markerRed.setVisibility(GONE);

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
