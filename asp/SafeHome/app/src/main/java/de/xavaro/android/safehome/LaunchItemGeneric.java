package de.xavaro.android.safehome;

import android.content.Context;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.ProcessManager;

public class LaunchItemGeneric extends LaunchItem
{
    private final static String LOGTAG = LaunchItemGeneric.class.getSimpleName();

    public LaunchItemGeneric(Context context)
    {
        super(context);
    }

    @Override
    protected void onMyClick()
    {
        // @formatter:off
        if (type.equals("genericapp")) launchGenericApp();
        if (type.equals("directory" )) launchDirectory();
        // @formatter:on
    }

    private void launchGenericApp()
    {
        String packagename = Json.getString(config, "packagename");
        ProcessManager.launchApp(packagename);
    }

    private void launchDirectory()
    {
        if (directory == null)
        {
            directory = new LaunchGroup(context);
            directory.setConfig(this, Json.getObject(config, "launchgroup"));
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
