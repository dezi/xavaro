package de.xavaro.android.safehome;

import android.content.Context;

import de.xavaro.android.common.KnownFileManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class LaunchItemMediaImage extends LaunchItemMedia
{
    private final static String LOGTAG = LaunchItemMediaImage.class.getSimpleName();

    public LaunchItemMediaImage(Context context)
    {
        super(context);

        mediatype = "image";
    }

    @Override
    protected void onMyClick()
    {
        if (Simple.equals(subtype, "image")) launchImage();
    }

    private void launchImage()
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

            // todo display image.

            return;
        }

        if (directory == null)
        {
            if (config.has("mediadir"))
            {
                if (numberItems == 0)
                {
                    String message = "Es sind keine Bilder enthalten.";
                    Speak.speak(message);

                    return;
                }
                else
                {
                    String mediadir = Json.getString(config, "mediadir");
                    directory = new LaunchGroupMediaImage(context, mediadir);
                }
            }
            else
            {
                directory = new LaunchGroupMediaImage(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
