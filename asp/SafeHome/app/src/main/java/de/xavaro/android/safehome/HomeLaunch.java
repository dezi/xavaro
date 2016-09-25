package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.Social;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
@SuppressWarnings("ResourceType")
public class HomeLaunch extends HomeFrame
{
    public HomeLaunch(Context context)
    {
        super(context);

        layoutParams.width = Simple.isTablet() ? HomeActivity.launchWid : Simple.MP;
        layoutParams.height = HomeActivity.launchHei;
        layoutParams.gravity = Gravity.RIGHT;

        layoutParams.leftMargin = Simple.DP(8);
        layoutParams.topMargin = HomeActivity.notifySize;
        layoutParams.rightMargin = Simple.isPortrait() ? Simple.DP(8) : HomeActivity.peopleSize;
        layoutParams.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : Simple.DP(8);

        layoutNormal = new LayoutParams(layoutParams);
    }

    @SuppressWarnings("UnusedParameters")
    public void setConfig(JSONObject config)
    {
        boolean social = Social.isAnyEnabled();

        layoutParams.width = (Simple.isTablet() && social) ? HomeActivity.launchWid : Simple.MP;
        layoutNormal.width = layoutParams.width;
    }

    @Override
    protected void onChangeOrientation()
    {
        layoutNormal.topMargin = HomeActivity.notifySize;
        layoutNormal.rightMargin = Simple.isPortrait() ? Simple.DP(8) : HomeActivity.peopleSize;
        layoutNormal.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : Simple.DP(8);

        if (! fullscreen)
        {
            layoutParams.topMargin = layoutNormal.topMargin;
            layoutParams.rightMargin = layoutNormal.rightMargin;
            layoutParams.bottomMargin = layoutNormal.bottomMargin;
        }
    }

    @Override
    public boolean onBackKeyWanted()
    {
        if (isFullscreen())
        {
            onToogleFullscreen();
            return true;
        }

        return false;
    }
}
