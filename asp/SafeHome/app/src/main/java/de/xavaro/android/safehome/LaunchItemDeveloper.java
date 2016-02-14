package de.xavaro.android.safehome;

import android.content.Context;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VersionUtils;

public class LaunchItemDeveloper extends LaunchItem
{
    private final static String LOGTAG = LaunchItemDeveloper.class.getSimpleName();

    public LaunchItemDeveloper(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        icon.setImageResource(GlobalConfigs.IconResTesting);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("developer")) launchDeveloper();
    }

    LaunchFrameDeveloper developerFrame;

    private void launchDeveloper()
    {
        if (subtype != null)
        {
            if (developerFrame == null)
            {
                developerFrame = new LaunchFrameDeveloper(context);
                developerFrame.setSubtype(subtype);
            }

            ((HomeActivity) context).addViewToBackStack(developerFrame);
        }
        else
        {
            if (directory == null)
            {
                directory = new LaunchGroupDeveloper(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }

            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }
}
