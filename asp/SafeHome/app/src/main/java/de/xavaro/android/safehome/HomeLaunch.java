package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeLaunch extends HomeFrame
{
    public HomeLaunch(Context context)
    {
        super(context);

        layoutParams.width = Simple.isTablet() ? HomeActivity.launchWid : Simple.MP;
        layoutParams.height = HomeActivity.launchHei;
        layoutParams.gravity = Gravity.RIGHT;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = HomeActivity.notifySize;
        layoutParams.rightMargin = Simple.isPortrait() ? 8 : HomeActivity.peopleSize;
        layoutParams.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : 8;

        layoutNormal = new LayoutParams(layoutParams);
    }

    public void setConfig(JSONObject config)
    {
    }

    @Override
    protected void onChangeOrientation()
    {
        layoutNormal.topMargin = HomeActivity.notifySize;
        layoutNormal.rightMargin = Simple.isPortrait() ? 8 : HomeActivity.peopleSize;
        layoutNormal.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : 8;

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
