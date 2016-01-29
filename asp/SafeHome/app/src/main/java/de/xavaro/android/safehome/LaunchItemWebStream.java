package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemWebStream extends LaunchItem implements ProxyPlayer.Callback
{
    private final static String LOGTAG = LaunchItemWebStream.class.getSimpleName();

    public LaunchItemWebStream(Context context)
    {
        super(context);
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean isPlayingAudio;
    private boolean isPlayingVideo;

    private ArrayList<LaunchItemWebStream> isPlayingParents = new ArrayList<>();

    @Override
    protected void setConfig()
    {
        if (Simple.equals(type, "ipradio"))
        {
            icon.setImageResource(GlobalConfigs.IconResIPRadio);
        }

        if (Simple.equals(type, "iptelevision"))
        {
            icon.setImageResource(GlobalConfigs.IconResIPTelevision);
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("ipradio"      )) launchIPRadio();
        if (type.equals("iptelevision" )) launchIPTelevision();

        if (type.equals("audioplayer"  )) launchAudioPlayer();
        if (type.equals("videoplayer"  )) launchVideoPlayer();
    }

    @Override
    protected void onMyOverlayClick()
    {
        if (isPlayingMedia)
        {
            ProxyPlayer pp = ProxyPlayer.getInstance();

            if (pp.isPlaying())
            {
                pp.playerPause();
            }
            else
            {
                pp.playerResume();
            }
        }
    }

    private void launchIPTelevision()
    {
        if (directory == null) directory = new LaunchGroupWebStream(context, this, "webiptv");
        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchIPRadio()
    {
        if (directory == null) directory = new LaunchGroupWebStream(context, this, "webradio");
        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchAudioPlayer()
    {
        if (handler == null) handler = new Handler();

        String audiourl = Json.getString(config, "audiourl");
        if (audiourl == null) return;

        ProxyPlayer.getInstance().setAudioUrl(context, audiourl, this);
        isPlayingAudio = true;

        bubbleControls();
    }

    private void launchVideoPlayer()
    {
        if (handler == null) handler = new Handler();

        String videourl = Json.getString(config, "videourl");
        if (videourl == null) return;

        ProxyPlayer.getInstance().setVideoUrl(context, videourl, this);
        isPlayingVideo = true;

        bubbleControls();
    }

    private void bubbleControls()
    {
        isPlayingParents = new ArrayList<>();

        LaunchItem bubble = this;

        while (bubble != null)
        {
            if (bubble instanceof LaunchItemWebStream)
            {
                isPlayingParents.add((LaunchItemWebStream) bubble);
            }

            if (bubble.getLaunchGroup() == null) break;
            bubble = bubble.getLaunchGroup().getLaunchItem();
        }
    }

    //region ProxyPlayer.Callback interface.

    public void onPlaybackPrepare()
    {
        clearAndPost(playbackPrepare);
    }

    public void onPlaybackStartet()
    {
        clearAndPost(playbackStartet);
    }

    public void onPlaybackPaused()
    {
        clearAndPost(playbackPaused);
    }

    public void onPlaybackResumed()
    {
        clearAndPost(playbackResumed);
    }

    public void onPlaybackFinished()
    {
        clearAndPost(playbackFinished);
    }

    public void onPlaybackMeta(String meta)
    {
        Log.d(LOGTAG, "onPlaybackMeta: " + meta);
    }

    private void clearAndPost(Runnable start)
    {
        handler.removeCallbacks(playbackPrepare);
        handler.removeCallbacks(playbackStartet);
        handler.removeCallbacks(playbackPaused);
        handler.removeCallbacks(playbackResumed);
        handler.removeCallbacks(playbackFinished);

        handler.postDelayed(start, 5);
    }

    //
    // Required handlers for thread change.
    //

    private final Runnable playbackPrepare = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackPrepare");

            for (LaunchItemWebStream li : isPlayingParents)
            {
                li.setPlaybackPrepare();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackPrepare();
            }
        }
    };

    private final Runnable playbackStartet = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackStartet");

            for (LaunchItemWebStream li : isPlayingParents)
            {
                li.setPlaybackStartet();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackStartet();
            }
        }
    };

    private final Runnable playbackPaused = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackPaused");

            for (LaunchItemWebStream li : isPlayingParents)
            {
                li.setPlaybackPaused();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackPaused();
            }
        }
    };

    private final Runnable playbackResumed = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackResumed");

            for (LaunchItemWebStream li : isPlayingParents)
            {
                li.setPlaybackResumed();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackResumed();
            }
        }
    };

    private final Runnable playbackFinished = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackFinished");

            for (LaunchItemWebStream li : isPlayingParents)
            {
                li.setPlaybackFinished();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackFinished();
            }
        }
    };

    //endregion ProxiPlayer callback interface.

    //region Media playback control.

    private boolean isPlayingMedia;

    private void setPlaybackPrepare()
    {
        Log.d(LOGTAG, "setPlaybackPrepare:" + label.getText());

        isPlayingMedia = true;

        setSpinner(true);
    }

    private void setPlaybackStartet()
    {
        Log.d(LOGTAG, "setPlaybackStartet:" + label.getText());

        setSpinner(false);

        oversize.width = layout.width / 3;
        oversize.height = layout.height / 3;

        overicon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.player_pause_190x190));
        overicon.setVisibility(VISIBLE);
        overlay.setVisibility(VISIBLE);
    }

    private void setPlaybackPaused()
    {
        overicon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.player_play_190x190));
    }

    private void setPlaybackResumed()
    {
        overicon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.player_pause_190x190));
    }

    private void setPlaybackFinished()
    {
        setSpinner(false);

        oversize.width = layout.width / 4;
        oversize.height = layout.height / 4;

        overicon.setVisibility(INVISIBLE);
        overlay.setVisibility(INVISIBLE);

        isPlayingMedia = false;
    }

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
                spinner = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
                spinner.getIndeterminateDrawable().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
                spinner.setPadding(40, 40, 40, 80);
            }

            this.addView(spinner);
        }
    }

    //endregion Media playback control.
}
