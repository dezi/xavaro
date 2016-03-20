package de.xavaro.android.safehome;

import android.content.Context;
import android.os.Handler;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.KnownFileManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.MediaProxy;

public class LaunchItemMediaVideo extends LaunchItemMedia
{
    private final static String LOGTAG = LaunchItemMediaVideo.class.getSimpleName();

    public LaunchItemMediaVideo(Context context)
    {
        super(context);

        mediatype = "video";
    }

    @Override
    protected void onMyClick()
    {
        if (Simple.equals(subtype, "video")) launchVideo();
    }

    private void launchVideo()
    {
        if (config.has("mediaitem"))
        {
            String mediaitem = Json.getString(config, "mediaitem");
            boolean known = KnownFileManager.getKnownFileStatus(mediaitem);

            if (! known)
            {
                KnownFileManager.putKnownFileStatus(mediaitem);
                overlay.setVisibility(INVISIBLE);
            }

            if (handler == null) handler = new Handler();

            MediaProxy.getInstance().setVideoFile(mediaitem, this);

            bubbleControls();

            return;
        }

        if (directory == null)
        {
            if (config.has("mediadir"))
            {
                if (numberItems == 0)
                {
                    String message = "Es sind keine Videos enthalten.";
                    Speak.speak(message);

                    return;
                }
                else
                {
                    String mediadir = Json.getString(config, "mediadir");
                    directory = new LaunchGroupMediaVideo(context, mediadir);
                }
            }
            else
            {
                directory = new LaunchGroupMediaVideo(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
