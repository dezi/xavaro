package de.xavaro.android.common;

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
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.AttributeSet;
import android.util.Log;

//
// Video play surface layout connected to HomeActivity.
//

public class MediaSurface extends FrameLayout implements
        SeekBar.OnSeekBarChangeListener,
        SurfaceHolder.Callback,
        View.OnTouchListener,
        MediaProxy.Callback

{
    private static final String LOGTAG = MediaSurface.class.getSimpleName();

    private static MediaSurface instance;

    public static MediaSurface getInstance()
    {
        if (instance == null)
        {
            instance = new MediaSurface(Simple.getActContext());
        }

        return instance;
    }

    private LayoutParams normalParams;
    private LayoutParams fullScreenParams;

    private FrameLayout surfaceLayout;

    private boolean isFullscreen;
    private boolean isPlaying;

    private final int xRatio = 16;
    private final int yRatio =  9;

    public MediaSurface(Context context)
    {
        super(context);

        myInit(context);
    }

    public MediaSurface(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public MediaSurface(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    private FrameLayout topArea;
    private FrameLayout bottomArea;

    private FrameLayout playButton;

    private FrameLayout qualityLQButton;
    private FrameLayout qualitySDButton;
    private FrameLayout qualityHQButton;
    private FrameLayout qualityHDButton;

    private MediaControl videoControl;

    private void myInit(Context context)
    {
        setOnTouchListener(this);

        normalParams = new LayoutParams(Simple.DP(400), Simple.DP(240));
        normalParams.leftMargin = Simple.DP(40);
        normalParams.topMargin  = Simple.DP(400);

        fullScreenParams = new LayoutParams(Simple.DP(400), Simple.DP(240));
        fullScreenParams.leftMargin = Simple.DP(40);
        fullScreenParams.topMargin  = Simple.DP(400);

        topArea = new FrameLayout(context);
        topArea.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Simple.DP(80), Gravity.TOP));
        Simple.setPadding(topArea, 10, 10, 10, 10);
        topArea.setVisibility(INVISIBLE);

        this.addView(topArea);

        bottomArea = new FrameLayout(context);
        bottomArea.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Simple.DP(80), Gravity.BOTTOM));
        Simple.setPadding(bottomArea, 10, 10, 10, 10);
        bottomArea.setVisibility(INVISIBLE);

        this.addView(bottomArea);

        FrameLayout offButton = new FrameLayout(context);
        offButton.setLayoutParams(new LayoutParams(Simple.DP(60), Simple.DP(60), Gravity.START + Gravity.TOP));
        offButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_shutdown_190x190));

        topArea.addView(offButton);

        offButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                MediaProxy.getInstance().playerReset();
            }
        });

        playButton = new FrameLayout(context);
        playButton.setLayoutParams(new LayoutParams(Simple.DP(60), Simple.DP(60), Gravity.END + Gravity.TOP));
        playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_play_190x190));

        topArea.addView(playButton);

        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (isPlaying)
                {
                    MediaProxy.getInstance().playerPause();
                }
                else
                {
                    MediaProxy.getInstance().playerResume();
                }
            }
        });

        FrameLayout outer = new FrameLayout(context);
        outer.setLayoutParams(new LayoutParams(Simple.DP(320), Simple.DP(80), Gravity.CENTER_HORIZONTAL));
        Simple.setPadding(outer, 10, 0, 10, 0);

        FrameLayout inner = new FrameLayout(context);
        inner.setLayoutParams(new LayoutParams(Simple.DP(150), Simple.DP(80), Gravity.CENTER_HORIZONTAL));
        Simple.setPadding(inner, 5, 0, 5, 0);

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
        SurfaceView surfaceView = new SurfaceView(context);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceLayout.addView(surfaceView);
        surfaceLayout.setOnTouchListener(this);

        this.addView(surfaceLayout, normalParams);

        videoControl = new MediaControl(context);
        videoControl.setOnSeekBarChangeListener(this);
        videoControl.setVisibility(INVISIBLE);
        bottomArea.addView(videoControl);
    }

    private final View.OnClickListener qualityButtonOnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            MediaProxy pp = MediaProxy.getInstance();

            int options = pp.getAvailableQualities();
            int select = 0;

            if ((view == qualityLQButton) && (options & MediaQuality.LQ) != 0)
            {
                select = MediaQuality.LQ;
            }

            if ((view == qualitySDButton) && (options & MediaQuality.SD) != 0)
            {
                select = MediaQuality.SD;
            }

            if ((view == qualityHQButton) && (options & MediaQuality.HQ) != 0)
            {
                select = MediaQuality.HQ;
            }

            if ((view == qualityHDButton) && (options & MediaQuality.HD) != 0)
            {
                select = MediaQuality.HD;
            }

            if (select > 0)
            {
                pp.setDesiredQuality(select);

                pp.playerRestart();
            }
        }
    };

    private FrameLayout buttonWithText(Context context, String text, int gravity)
    {
        FrameLayout button = new FrameLayout(context);
        button.setLayoutParams(new LayoutParams(Simple.DP(60), Simple.DP(60), gravity));
        button.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_empty_190x190));

        TextView textview = new TextView(context);
        textview.setTypeface(null, Typeface.BOLD);
        textview.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        textview.setTextColor(CommonConfigs.VideoSurfaceDisabledButton);
        Simple.setPadding(textview, 0, 5, 0, 0);
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
        MediaProxy pp = MediaProxy.getInstance();

        int current = pp.getCurrentQuality();
        int options = pp.getAvailableQualities();

        int dis = CommonConfigs.VideoSurfaceDisabledButton;
        int ena = CommonConfigs.VideoSurfaceEnabledButton;
        int sel = CommonConfigs.VideoSurfaceSelectedButton;

        if ((options & MediaQuality.LQ) == 0)
        {
            setButtonTextColor(qualityLQButton,dis);
        }
        else
        {
            setButtonTextColor(qualityLQButton,(current == MediaQuality.LQ) ? sel : ena);
        }

        if ((options & MediaQuality.SD) == 0)
        {
            setButtonTextColor(qualitySDButton,dis);
        }
        else
        {
            setButtonTextColor(qualitySDButton,(current == MediaQuality.SD) ? sel : ena);
        }

        if ((options & MediaQuality.HQ) == 0)
        {
            setButtonTextColor(qualityHQButton,dis);
        }
        else
        {
            setButtonTextColor(qualityHQButton,(current == MediaQuality.HQ) ? sel : ena);
        }

        if ((options & MediaQuality.HD) == 0)
        {
            setButtonTextColor(qualityHDButton,dis);
        }
        else
        {
            setButtonTextColor(qualityHDButton,(current == MediaQuality.HD) ? sel : ena);
        }
    }

    private final Runnable progressReader = new Runnable()
    {
        @Override
        public void run()
        {
            int position = MediaProxy.getInstance().getCurrentPosition();
            if (position >= 0) videoControl.setCurrentPosition(position);

            Simple.removePost(progressReader);
            Simple.makePost(progressReader, 1000);
        }
    };

    //region SeekBar.OnSeekBarChangeListener interface

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (fromUser) MediaProxy.getInstance().setCurrentPosition(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }

    //endregion SeekBar.OnSeekBarChangeListener interface

    //region View.OnTouchListener interface

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
            bottomArea.setVisibility(VISIBLE);
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
                        bottomArea.setVisibility(INVISIBLE);

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

    //endregion View.OnTouchListener interface

    //region Orientation change handling

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
            Simple.makePost(adjustFullscreen);

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

    //endregion Orientation change handling

    //region SurfaceHolder.Callback interface

    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(LOGTAG, "surfaceCreated");

        MediaProxy.getInstance().setDisplay(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)
    {
        Log.d(LOGTAG,"surfaceChanged");
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(LOGTAG, "surfaceDestroyed");

        MediaProxy.getInstance().setDisplay(null);
    }

    //endregion SurfaceHolder.Callback interface

    //region ProxyPlayer.Callback interface

    private ProgressBar spinner;

    private void setSpinner(boolean visible)
    {
        if ((spinner != null) && (spinner.getParent() != null))
        {
            ((ViewGroup) spinner.getParent()).removeView(spinner);
        }

        if (visible && ! MediaProxy.getInstance().isLocalFile())
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
        Simple.makePost(onPlaybackPrepareUI);
    }

    public final Runnable onPlaybackPrepareUI = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "onPlaybackPrepare");

            Context activity = Simple.getActContext();

            if (activity instanceof VideoSurfaceHandler)
            {
                ((VideoSurfaceHandler) activity).addVideoSurface(MediaSurface.this);
            }

            setSpinner(true);
        }
    };

    public void onPlaybackStartet()
    {
        Simple.makePost(onPlaybackStartetUI);
    }

    public final Runnable onPlaybackStartetUI = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "onPlaybackStartet");

            isPlaying = true;
            setSpinner(false);

            playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_pause_190x190));

            setButtonStates();

            boolean islocal = MediaProxy.getInstance().isLocalFile();
            Log.d(LOGTAG, "onPlaybackStartet:" + islocal);

            videoControl.setVisibility(islocal ? VISIBLE : INVISIBLE);

            if (islocal)
            {
                videoControl.setDuration(MediaProxy.getInstance().getDuration());

                Simple.removePost(progressReader);
                Simple.makePost(progressReader);
            }
        }
    };

    public void onPlaybackPaused()
    {
        Simple.makePost(onPlaybackPausedUI);
    }

    public final Runnable onPlaybackPausedUI = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "onPlaybackPaused");

            isPlaying = false;
            setSpinner(false);

            playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_play_190x190));
        }
    };

    public void onPlaybackResumed()
    {
        Simple.makePost(onPlaybackResumedUI);
    }

    public final Runnable onPlaybackResumedUI = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "onPlaybackResumed");

            isPlaying = true;
            setSpinner(false);

            playButton.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.player_pause_190x190));
        }
    };

    public void onPlaybackFinished()
    {
        Simple.makePost(onPlaybackFinishedUI);
    }

    public final Runnable onPlaybackFinishedUI = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "onPlaybackFinished");

            isPlaying = false;
            setSpinner(false);

            Simple.removePost(progressReader);

            Context activity = Simple.getActContext();

            if (activity instanceof VideoSurfaceHandler)
            {
                ((VideoSurfaceHandler) activity).removeVideoSurface();
            }

            if (isFullscreen)
            {
                topArea.setVisibility(INVISIBLE);
                bottomArea.setVisibility(INVISIBLE);

                surfaceLayout.setLayoutParams(normalParams);
                setBackgroundColor(Color.TRANSPARENT);

                isFullscreen = false;
            }
        }
    };

    public void onPlaybackMeta(String meta)
    {
    }

    //endregion ProxyPlayer.Callback interface

    public interface VideoSurfaceHandler
    {
        void addVideoSurface(FrameLayout video);
        void removeVideoSurface();
    }
}
