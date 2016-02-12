package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import de.xavaro.android.common.Simple;

public class LaunchItemToday extends LaunchItem
{
    private final static String LOGTAG = LaunchItemToday.class.getSimpleName();

    public LaunchItemToday(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        icon.setImageResource(GlobalConfigs.IconResToday);
    }

    @Override
    protected void onMyClick()
    {
        // @formatter:off
        if (type.equals("today")) launchToday();
        // @formatter:on
    }

    LaunchFrameToday todayFrame;

    private void launchToday()
    {
        if (todayFrame == null)
        {
            todayFrame = new LaunchFrameToday(context);
        }

        ((HomeActivity) context).addViewToBackStack(todayFrame);
    }
}
