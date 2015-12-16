package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

//
// Video play surface layout connected to HomeActivity.
//

public class VideoSurface extends FrameLayout implements
        SurfaceHolder.Callback,
        View.OnTouchListener,
        ProxyPlayer.Callback
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

    private LayoutParams normalParams;
    private LayoutParams fullScreenParams;

    private FrameLayout surfaceLayout;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private boolean isFullscreen;
    private boolean isPlaying;

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

    private LayoutParams playPosition;
    private FrameLayout playButton;
    private ImageView playImage;

    private LayoutParams offPosition;
    private FrameLayout offButton;
    private ImageView offImage;

    private void myInit(Context context)
    {
        setOnTouchListener(this);

        normalParams = new LayoutParams(400, 240);
        normalParams.leftMargin = 40;
        normalParams.topMargin  = 400;

        offPosition = new LayoutParams(80,80);
        offPosition.gravity = Gravity.START + Gravity.TOP;
        offButton = new FrameLayout(context);
        offButton.setLayoutParams(offPosition);
        offButton.setPadding(10, 10, 10, 10);
        offButton.setVisibility(INVISIBLE);
        offImage = new ImageView(context);
        offButton.addView(offImage);

        this.addView(offButton);

        offImage.setImageDrawable(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_shutdown_190x190));

        offButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ProxyPlayer.getInstance().playerReset();
            }
        });

        playPosition = new LayoutParams(80,80);
        playPosition.gravity = Gravity.END + Gravity.TOP;
        playButton = new FrameLayout(context);
        playButton.setLayoutParams(playPosition);
        playButton.setPadding(10, 10, 10, 10);
        playButton.setVisibility(INVISIBLE);
        playImage = new ImageView(context);
        playButton.addView(playImage);

        this.addView(playButton);

        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (isPlaying)
                {
                    ProxyPlayer.getInstance().playerPause();
                }
                else
                {
                    ProxyPlayer.getInstance().playerResume();
                }
            }
        });

        surfaceLayout = new FrameLayout(context);
        surfaceView = new SurfaceView(context);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceLayout.addView(surfaceView);
        surfaceLayout.setOnTouchListener(this);

        this.addView(surfaceLayout, normalParams);
    }

    //region View.OnTouchListener interface.

    private int xStartMargin;
    private int yStartMargin;

    private int xStartTouch;
    private int yStartTouch;

    private Runnable animationFinished = new Runnable()
    {
        @Override
        public void run()
        {
            offButton.setVisibility(VISIBLE);
            playButton.setVisibility(VISIBLE);
        }
    };

    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (view == this)
        {
            //
            // Kill all clicks in fullscreen mode.
            //

            return isFullscreen;
        }

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
                        offButton.setVisibility(INVISIBLE);
                        playButton.setVisibility(INVISIBLE);
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
                        animator.setFinalCall(animationFinished);

                        this.startAnimation(animator);

                        isFullscreen = true;
                    }

                    Log.d(LOGTAG, "onTouch: CLICK " + newWidth + "/" + newHeight);
                }
            }
        }

        return true;
    }

    //endregion

    //region SurfaceHolder.Callback interface.

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
        Log.d(LOGTAG, "surfaceDestroyed");

        ProxyPlayer.getInstance().setDisplay(null);
    }

    //endregion

    //region ProxyPlayer.Callback interface.

    private ProgressBar spinner;

    private void setSpinner(boolean visible)
    {
        if ((spinner != null) && (spinner.getParent() != null))
        {
            ((ViewGroup) spinner.getParent()).removeView(spinner);
        }

        if (visible)
        {
            if (spinner == null)
            {
                spinner = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);
                spinner.getIndeterminateDrawable().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
                spinner.setPadding(80, 80, 80, 80);
            }

            surfaceLayout.addView(spinner);
        }
    }

    public void onPlaybackPrepare()
    {
        Log.d(LOGTAG, "onPlaybackPrepare");

        HomeActivity.getInstance().addVideoSurface(this);

        setSpinner(true);

        offButton.setVisibility(INVISIBLE);
        playButton.setVisibility(INVISIBLE);
    }

    public void onPlaybackStartet()
    {
        Log.d(LOGTAG, "onPlaybackStartet");

        isPlaying = true;
        setSpinner(false);

        playImage.setImageDrawable(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_pause_190x190));

        offButton.setVisibility(isFullscreen ? VISIBLE : INVISIBLE);
        playButton.setVisibility(isFullscreen ? VISIBLE : INVISIBLE);
    }

    public void onPlaybackPaused()
    {
        Log.d(LOGTAG,"onPlaybackPaused");

        isPlaying = false;
        setSpinner(false);

        playImage.setImageDrawable(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_play_190x190));

        offButton.setVisibility(isFullscreen ? VISIBLE : INVISIBLE);
        playButton.setVisibility(isFullscreen ? VISIBLE : INVISIBLE);
    }

    public void onPlaybackResumed()
    {
        Log.d(LOGTAG,"onPlaybackResumed");

        isPlaying = true;
        setSpinner(false);

        playImage.setImageDrawable(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_pause_190x190));

        offButton.setVisibility(isFullscreen ? VISIBLE : INVISIBLE);
        playButton.setVisibility(isFullscreen ? VISIBLE : INVISIBLE);
    }

    public void onPlaybackFinished()
    {
        Log.d(LOGTAG,"onPlaybackFinished");

        isPlaying = false;
        setSpinner(false);

        offButton.setVisibility(INVISIBLE);
        playButton.setVisibility(INVISIBLE);

        HomeActivity.getInstance().removeVideoSurface();
    }

    public void onPlaybackMeta(String meta)
    {
        Log.d(LOGTAG,"onPlaybackMeta");
    }

    //endregion
}
