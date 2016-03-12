package de.xavaro.android.safehome;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.AttributeSet;
import android.util.Log;

import de.xavaro.android.common.Animator;
import de.xavaro.android.common.VersionUtils;
import de.xavaro.android.common.VideoQuality;

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

    private final int xRatio = 16;
    private final int yRatio =  9;

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

    private FrameLayout topArea;

    private FrameLayout playButton;
    private FrameLayout offButton;

    private FrameLayout qualityLQButton;
    private FrameLayout qualitySDButton;
    private FrameLayout qualityHQButton;
    private FrameLayout qualityHDButton;

    private void myInit(Context context)
    {
        setOnTouchListener(this);

        normalParams = new LayoutParams(400, 240);
        normalParams.leftMargin = 40;
        normalParams.topMargin  = 400;

        fullScreenParams = new LayoutParams(400, 240);
        fullScreenParams.leftMargin = 40;
        fullScreenParams.topMargin  = 400;

        topArea = new FrameLayout(context);
        topArea.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 80));
        topArea.setPadding(10, 10, 10, 10);
        topArea.setVisibility(INVISIBLE);

        this.addView(topArea);

        offButton = new FrameLayout(context);
        offButton.setLayoutParams(new LayoutParams(60, 60, Gravity.START + Gravity.TOP));
        offButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_shutdown_190x190));

        topArea.addView(offButton);

        offButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ProxyPlayer.getInstance().playerReset();
            }
        });

        playButton = new FrameLayout(context);
        playButton.setLayoutParams(new LayoutParams(60, 60, Gravity.END + Gravity.TOP));
        playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_play_190x190));

        topArea.addView(playButton);

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

        FrameLayout outer = new FrameLayout(context);
        outer.setLayoutParams(new LayoutParams(320, 80, Gravity.CENTER_HORIZONTAL));
        outer.setPadding(10, 0, 10, 0);

        FrameLayout inner = new FrameLayout(context);
        inner.setLayoutParams(new LayoutParams(150, 60, Gravity.CENTER_HORIZONTAL));
        inner.setPadding(5, 0, 5, 0);

        outer.addView(inner);
        topArea.addView(outer);

        qualityHDButton = buttonWithText(context, "HD", Gravity.START);
        outer.addView(qualityHDButton);

        qualityHQButton = buttonWithText(context, "HQ", Gravity.START);
        inner.addView(qualityHQButton);

        qualitySDButton = buttonWithText(context, "SD", Gravity.END);
        inner.addView(qualitySDButton);

        qualityLQButton = buttonWithText(context, "LQ", Gravity.END);
        outer.addView(qualityLQButton);

        surfaceLayout = new FrameLayout(context);
        surfaceView = new SurfaceView(context);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceLayout.addView(surfaceView);
        surfaceLayout.setOnTouchListener(this);

        this.addView(surfaceLayout, normalParams);
    }

    private final View.OnClickListener qualityButtonOnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            ProxyPlayer pp = ProxyPlayer.getInstance();

            int options = pp.getAvailableQualities();
            int select = 0;

            if ((view == qualityLQButton) && (options & VideoQuality.LQ) != 0)
            {
                select = VideoQuality.LQ;
            }

            if ((view == qualitySDButton) && (options & VideoQuality.SD) != 0)
            {
                select = VideoQuality.SD;
            }

            if ((view == qualityHQButton) && (options & VideoQuality.HQ) != 0)
            {
                select = VideoQuality.HQ;
            }

            if ((view == qualityHDButton) && (options & VideoQuality.HD) != 0)
            {
                select = VideoQuality.HD;
            }

            if (select > 0)
            {
                pp.setCurrentQuality(select);

                pp.playerRestart();
            }
        }
    };

    private FrameLayout buttonWithText(Context context, String text, int gravity)
    {
        FrameLayout button = new FrameLayout(context);
        button.setLayoutParams(new LayoutParams(60, 60, gravity));
        button.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_empty_190x190));

        TextView textview = new TextView(context);
        textview.setTypeface(null, Typeface.BOLD);
        textview.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        textview.setTextColor(GlobalConfigs.VideoSurfaceDisabledButton);
        textview.setPadding(0, 5, 0, 0);
        textview.setTextSize(32f);
        textview.setText(text);
        button.addView(textview);

        button.setOnClickListener(qualityButtonOnClick);

        return button;
    }

    private void setButtonTextColor(FrameLayout button,int color)
    {
        View child = button.getChildAt(0);

        if (child instanceof TextView) ((TextView) child).setTextColor(color);
    }

    private void setButtonStates()
    {
        ProxyPlayer pp = ProxyPlayer.getInstance();

        int current = pp.getCurrentQuality();
        int options = pp.getAvailableQualities();

        int dis = GlobalConfigs.VideoSurfaceDisabledButton;
        int ena = GlobalConfigs.VideoSurfaceEnabledButton;
        int sel = GlobalConfigs.VideoSurfaceSelectedButton;

        if ((options & VideoQuality.LQ) == 0)
        {
            setButtonTextColor(qualityLQButton,dis);
        }
        else
        {
            setButtonTextColor(qualityLQButton,(current == VideoQuality.LQ) ? sel : ena);
        }

        if ((options & VideoQuality.SD) == 0)
        {
            setButtonTextColor(qualitySDButton,dis);
        }
        else
        {
            setButtonTextColor(qualitySDButton,(current == VideoQuality.SD) ? sel : ena);
        }

        if ((options & VideoQuality.HQ) == 0)
        {
            setButtonTextColor(qualityHQButton,dis);
        }
        else
        {
            setButtonTextColor(qualityHQButton,(current == VideoQuality.HQ) ? sel : ena);
        }

        if ((options & VideoQuality.HD) == 0)
        {
            setButtonTextColor(qualityHDButton,dis);
        }
        else
        {
            setButtonTextColor(qualityHDButton,(current == VideoQuality.HD) ? sel : ena);
        }
    }

    //region View.OnTouchListener interface.

    private int xStartMargin;
    private int yStartMargin;

    private int xStartTouch;
    private int yStartTouch;

    private final Runnable animationFinished = new Runnable()
    {
        @Override
        public void run()
        {
            topArea.setVisibility(VISIBLE);
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

                    fullScreenParams.width = newWidth;
                    fullScreenParams.height = newHeight;
                    fullScreenParams.leftMargin = (parentWidth - newWidth) / 2;
                    fullScreenParams.topMargin = (parentHeight - newHeight) / 2;

                    if (isFullscreen)
                    {
                        topArea.setVisibility(INVISIBLE);

                        surfaceLayout.setLayoutParams(normalParams);
                        setBackgroundColor(Color.TRANSPARENT);

                        isFullscreen = false;
                    }
                    else
                    {
                        Animator animator = new Animator();

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

    //region Orientation change handling.

    private boolean orientationChanged = false;

    @Override
    protected void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);

        orientationChanged = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && isFullscreen && orientationChanged)
        {
            getHandler().post(adjustFullscreen);

        }

        orientationChanged = false;
    }

    private final Runnable adjustFullscreen = new Runnable()
    {
        @Override
        public void run()
        {
            if (isFullscreen)
            {
                int newWidth = getWidth();
                int newHeight = newWidth * yRatio / xRatio;

                fullScreenParams.width = newWidth;
                fullScreenParams.height = newHeight;
                fullScreenParams.leftMargin = (getWidth() - newWidth) / 2;
                fullScreenParams.topMargin = (getHeight() - newHeight) / 2;

                surfaceLayout.setLayoutParams(fullScreenParams);
            }
        }
    };

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

        if (visible && ! ProxyPlayer.getInstance().isLocalFile())
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
    }

    public void onPlaybackStartet()
    {
        Log.d(LOGTAG, "onPlaybackStartet");

        isPlaying = true;
        setSpinner(false);

        playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_pause_190x190));

        setButtonStates();

        Log.d(LOGTAG, "======================duration=" + ProxyPlayer.getInstance().getMediaPlayer().getDuration());
    }

    public void onPlaybackPaused()
    {
        Log.d(LOGTAG,"onPlaybackPaused");

        isPlaying = false;
        setSpinner(false);

        playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_play_190x190));
    }

    public void onPlaybackResumed()
    {
        Log.d(LOGTAG,"onPlaybackResumed");

        isPlaying = true;
        setSpinner(false);

        playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_pause_190x190));
    }

    public void onPlaybackFinished()
    {
        Log.d(LOGTAG,"onPlaybackFinished");

        isPlaying = false;
        setSpinner(false);

        HomeActivity.getInstance().removeVideoSurface();
    }

    public void onPlaybackMeta(String meta)
    {
        Log.d(LOGTAG,"onPlaybackMeta");
    }

    //endregion
}
