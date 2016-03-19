package de.xavaro.android.safehome;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.MediaProxy;

public class LaunchItemWebStream extends LaunchItemProxyPlayer
{
    private final static String LOGTAG = LaunchItemWebStream.class.getSimpleName();

    public LaunchItemWebStream(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (subtype == null)
        {
            if (Simple.equals(type, "iprd"))
            {
                icon.setImageResource(GlobalConfigs.IconResIPRadio);
            }

            if (Simple.equals(type, "iptv"))
            {
                icon.setImageResource(GlobalConfigs.IconResIPTelevision);
            }
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("iprd")) launchIPRadio();
        if (type.equals("iptv")) launchIPTelevision();

        if (type.equals("audioplayer")) launchAudioPlayer();
        if (type.equals("videoplayer")) launchVideoPlayer();
    }

    private void launchIPTelevision()
    {
        if (directory == null)
        {
            directory = new LaunchGroupWebStream(context, this);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchIPRadio()
    {
        if (directory == null)
        {
            directory = new LaunchGroupWebStream(context, this);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchAudioPlayer()
    {
        if (handler == null) handler = new Handler();

        String audiourl = Json.getString(config, "audiourl");
        if (audiourl == null) return;

        MediaProxy.getInstance().setAudioUrl(audiourl, this);

        bubbleControls();
    }

    private void launchVideoPlayer()
    {
        if (handler == null) handler = new Handler();

        String videourl = Json.getString(config, "videourl");
        if (videourl == null) return;

        Log.d(LOGTAG, "launchVideoPlayer:" + videourl);

        MediaProxy.getInstance().setVideoUrl(videourl, this);

        bubbleControls();
    }
}
