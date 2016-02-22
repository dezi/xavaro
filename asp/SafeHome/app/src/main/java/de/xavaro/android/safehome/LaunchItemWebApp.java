package de.xavaro.android.safehome;

import android.content.Context;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.WebApp;

public class LaunchItemWebApp extends LaunchItem
{
    private final static String LOGTAG = LaunchItemWebApp.class.getSimpleName();

    public LaunchItemWebApp(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (subtype != null)
        {
            Json.put(config, "label", WebApp.getLabel(subtype));
            icon.setImageDrawable(WebApp.getAppIcon(subtype));
        }
        else
        {
            icon.setImageResource(GlobalConfigs.IconResWebApps);
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("webapp")) launchWebapp();
    }

    @Override
    public void onBackKeyExecuted()
    {
        if (subtype != null) webappFrame = null;
    }

    private LaunchFrameWebApp webappFrame;

    private void launchWebapp()
    {
        if (subtype != null)
        {
            if (webappFrame == null)
            {
                webappFrame = new LaunchFrameWebApp(context);
                webappFrame.setWebAppName(subtype);
                webappFrame.setParent(this);
            }

            ((HomeActivity) context).addViewToBackStack(webappFrame);
        }
        else
        {
            if (directory == null)
            {
                directory = new LaunchGroupWebApps(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }

            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }
}
