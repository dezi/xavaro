package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebLib;

public class LaunchItemNextPrev extends LaunchItem
{
    private final static String LOGTAG = LaunchItemNextPrev.class.getSimpleName();

    public LaunchItemNextPrev(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (type.equals("prev")) icon.setImageResource(CommonConfigs.IconResPrev);
        if (type.equals("next")) icon.setImageResource(CommonConfigs.IconResNext);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("prev")) launchPrev();
        if (type.equals("next")) launchNext();
    }

    private void launchPrev()
    {
        if (parent != null) parent.animatePrevPage();
    }

    private void launchNext()
    {
        if (parent != null) parent.animateNextPage();
    }
}
