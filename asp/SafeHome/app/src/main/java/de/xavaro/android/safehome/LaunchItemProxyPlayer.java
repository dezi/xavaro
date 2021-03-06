package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import de.xavaro.android.common.MediaProxy;

public class LaunchItemProxyPlayer extends LaunchItem implements MediaProxy.Callback
{
    private final static String LOGTAG = LaunchItemProxyPlayer.class.getSimpleName();

    public LaunchItemProxyPlayer(Context context)
    {
        super(context);
    }

    protected ArrayList<LaunchItemProxyPlayer> isPlayingParents = new ArrayList<>();

    @Override
    protected void onMyOverlayClick()
    {
        if (isPlayingMedia)
        {
            MediaProxy pp = MediaProxy.getInstance();

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

    protected void bubbleControls()
    {
        isPlayingParents = new ArrayList<>();

        LaunchItem bubble = this;

        while (bubble != null)
        {
            if (bubble instanceof LaunchItemProxyPlayer)
            {
                isPlayingParents.add((LaunchItemProxyPlayer) bubble);
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

            for (LaunchItemProxyPlayer li : isPlayingParents)
            {
                li.setPlaybackPrepare();
            }
        }
    };

    private final Runnable playbackStartet = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackStartet");

            for (LaunchItemProxyPlayer li : isPlayingParents)
            {
                li.setPlaybackStartet();
            }
        }
    };

    private final Runnable playbackPaused = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackPaused");

            for (LaunchItemProxyPlayer li : isPlayingParents)
            {
                li.setPlaybackPaused();
            }
        }
    };

    private final Runnable playbackResumed = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackResumed");

            for (LaunchItemProxyPlayer li : isPlayingParents)
            {
                li.setPlaybackResumed();
            }
        }
    };

    private final Runnable playbackFinished = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackFinished");

            for (LaunchItemProxyPlayer li : isPlayingParents)
            {
                li.setPlaybackFinished();
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

        overicon.setImageResource(R.drawable.player_pause_190x190);
        overicon.setVisibility(VISIBLE);
        overlay.setVisibility(VISIBLE);
    }

    private void setPlaybackPaused()
    {
        overicon.setImageResource(R.drawable.player_play_190x190);
    }

    private void setPlaybackResumed()
    {
        overicon.setImageResource(R.drawable.player_pause_190x190);
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

        if (visible && ! MediaProxy.getInstance().isLocalFile())
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
