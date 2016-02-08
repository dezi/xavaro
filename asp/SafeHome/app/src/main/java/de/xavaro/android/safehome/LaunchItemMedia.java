package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemMedia extends LaunchItem
{
    private final static String LOGTAG = LaunchItemMedia.class.getSimpleName();

    public LaunchItemMedia(Context context)
    {
        super(context);
    }

    private int numberImages;

    @Override
    protected void setConfig()
    {
        boolean found = false;

        if (config.has("mediaitem"))
        {
            String imagefile = Json.getString(config, "mediaitem");
            icon.setImageDrawable(Simple.getDrawableThumbnailFromFile(imagefile, 200));
            found = true;
        }

        if (config.has("mediadir"))
        {
            String mediadir = Json.getString(config, "mediadir");
            File mediapath = Simple.getMediaPath(mediadir);

            if (! mediapath.exists()) mediapath.mkdir();

            if ((mediapath != null) && mediapath.isDirectory())
            {
                JSONArray images = Simple.getDirectorySortedByAge(
                        mediapath, new Simple.ImageFileFilter(), true);

                if ((images != null) && (images.length() > 0))
                {
                    JSONObject newest = Json.getObject(images, 0);
                    String file = Json.getString(newest, "file");
                    icon.setImageDrawable(Simple.getDrawableThumbnailFromFile(file, 200));

                    numberImages = images.length();

                    labelText = Json.getString(config, "label");
                    labelText += " (" + numberImages + ")";
                    setLabelText(labelText);

                    found = true;
                }
            }
        }

        if (! found)
        {
            if (Simple.equals(subtype, "image"))
            {
                icon.setImageResource(GlobalConfigs.IconResMediaImage);
            }
        }
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
            return;
        }

        if (directory == null)
        {
            if (config.has("mediadir"))
            {
                if (numberImages == 0)
                {
                    String message = "Es sind keine Bilder enthalten.";
                    DitUndDat.SpeekDat.speak(message);
                    Simple.makeToast(message);

                    return;
                }
                else
                {
                    String mediadir = Json.getString(config, "mediadir");

                    directory = new LaunchGroupMedia.ImageGroup(context, mediadir);
                }
            }
            else
            {
                directory = new LaunchGroupMedia.ImageGroup(context);
            }
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
