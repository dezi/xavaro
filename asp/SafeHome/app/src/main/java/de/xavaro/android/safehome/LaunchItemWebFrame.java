package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemWebFrame extends LaunchItem
{
    private final static String LOGTAG = LaunchItemWebFrame.class.getSimpleName();

    public LaunchItemWebFrame(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (! config.has("name"))
        {
            if (Simple.equals(subtype, "newspaper"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigNewspaper);
            }

            if (Simple.equals(subtype, "magazine"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigMagazine);
            }

            if (Simple.equals(subtype, "pictorial"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigPictorial);
            }

            if (Simple.equals(subtype, "shopping"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigShopping);
            }

            if (Simple.equals(subtype, "erotics"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigErotics);
            }

            if (Simple.equals(subtype, "inetdir"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigInternet);
            }
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("ioc")) launchWebFrame();
    }

    private void launchWebFrame()
    {
        if (config.has("name"))
        {
            String url = Json.getString(config, "url");
            String name = Json.getString(config, "name");

            LaunchFrameWebFrame webframe = new LaunchFrameWebFrame(context);
            webframe.setLoadURL(config, name, url);

            ((HomeActivity) context).addViewToBackStack(webframe);
        }
        else
        {
            if (directory == null)
            {
                directory = new LaunchGroupWebStream(context, this);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }

            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }
}
