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

    @Override
    protected void setConfig()
    {
        boolean found = false;

        if (config.has("mediadir"))
        {
            String mediadir = Json.getString(config, "mediadir");
            File mediapath = Simple.getMediaPath(mediadir);

            if ((mediapath != null) && mediapath.isDirectory())
            {
                JSONArray images = Simple.getDirectorySortedByAge(
                        mediapath, new Simple.ImageFileFilter());

                if ((images != null) && (images.length() > 0))
                {
                    JSONObject newest = Json.getObject(images, images.length() - 1);
                    String file = Json.getString(newest, "file");
                    icon.setImageDrawable(Simple.getDrawableThumbnailFromFile(file, 200));

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
        if (config.has("mediadir"))
        {

            return;
        }

        if (directory == null) directory = new LaunchGroupMedia.ImageGroup(context);

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
