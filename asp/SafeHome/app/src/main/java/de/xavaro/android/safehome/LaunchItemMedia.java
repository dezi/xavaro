package de.xavaro.android.safehome;

import android.content.Context;

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
        if (config.has("directory"))
        {
        }
        else
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
        if (directory == null) directory = new LaunchGroupMedia.ImageGroup(context);

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
