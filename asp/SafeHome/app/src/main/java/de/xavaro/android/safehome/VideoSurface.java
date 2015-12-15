package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

//
// Video play surface layout connected to HomeActivity.
//

public class VideoSurface extends FrameLayout implements SurfaceHolder.Callback, View.OnTouchListener
{
    private static final String LOGTAG = VideoSurface.class.getSimpleName();

    private static VideoSurface videoSurface;

    public static VideoSurface getInstance()
    {
        if (videoSurface == null)
        {
            videoSurface = new VideoSurface(HomeActivity.getInstance());
        }

        return videoSurface;
    }

    private Context context;

    private LayoutParams normalParams;
    private LayoutParams fullScreenParams;

    private FrameLayout surfaceLayout;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private int xRatio = 16;
    private int yRatio =  9;

    public VideoSurface(Context context)
    {
        super(context);

        myInit(context);
    }

    public VideoSurface(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public VideoSurface(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    private void myInit(Context context)
    {
        this.context = context;

        normalParams = new LayoutParams(400, 240);
        normalParams.leftMargin = 40;
        normalParams.topMargin  = 400;

        surfaceLayout = new FrameLayout(context);
        surfaceLayout.setBackgroundColor(0x8800ff00);

        surfaceView = new SurfaceView(context);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        surfaceLayout.addView(surfaceView);
        surfaceLayout.setOnTouchListener(this);

        this.addView(surfaceLayout,normalParams);

        HomeActivity.getInstance().addVideoSurface(this);
    }

    private int xStartMargin;
    private int yStartMargin;

    private int xStartTouch;
    private int yStartTouch;

    private boolean isFullscreen;

    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        int xscreen = (int) motionEvent.getRawX();
        int yscreen = (int) motionEvent.getRawY();

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            xStartMargin = normalParams.leftMargin;
            yStartMargin = normalParams.topMargin;

            xStartTouch = xscreen;
            yStartTouch = yscreen;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
        {
            if (! isFullscreen)
            {
                if ((Math.abs(xscreen - xStartTouch) >= 10) || (Math.abs(yscreen - yStartTouch) >= 10))
                {
                    normalParams.leftMargin = xStartMargin + xscreen - xStartTouch;
                    normalParams.topMargin = yStartMargin + yscreen - yStartTouch;

                    surfaceLayout.setLayoutParams(normalParams);
                }
            }
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            if ((Math.abs(xscreen - xStartTouch) < 10) && (Math.abs(yscreen - yStartTouch) < 10))
            {
                ViewParent parent = view.getParent();

                if ((parent != null) && (parent instanceof FrameLayout))
                {
                    int parentWidth  = ((FrameLayout) parent).getWidth();
                    int parentHeight = ((FrameLayout) parent).getHeight();

                    int newWidth = parentWidth;
                    int newHeight = newWidth * yRatio / xRatio;

                    fullScreenParams = new FrameLayout.LayoutParams(newWidth,newHeight);
                    fullScreenParams.leftMargin = (parentWidth - newWidth) / 2;
                    fullScreenParams.topMargin = (parentHeight - newHeight) / 2;

                    if (isFullscreen)
                    {
                        surfaceLayout.setLayoutParams(normalParams);
                        setBackgroundColor(Color.TRANSPARENT);

                        isFullscreen = false;
                    }
                    else
                    {
                        DitUndDat.Animator animator = new DitUndDat.Animator();

                        animator.setDuration(500);
                        animator.setLayout(surfaceLayout, normalParams, fullScreenParams);
                        animator.setColor(this, Color.TRANSPARENT, Color.BLACK);

                        this.startAnimation(animator);

                        isFullscreen = true;
                    }

                    Log.d(LOGTAG, "onTouch: CLICK " + newWidth + "/" + newHeight);
                }
            }
        }

        return true;
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(LOGTAG, "surfaceCreated");

        ProxyPlayer.getInstance().setDisplay(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)
    {
        Log.d(LOGTAG,"surfaceChanged");
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(LOGTAG,"surfaceChanged");
    }
}
