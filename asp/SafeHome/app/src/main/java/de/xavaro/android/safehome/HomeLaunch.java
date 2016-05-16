package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONObject;

import de.xavaro.android.common.ImageSmartView;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeLaunch extends HomeFrame
{
    private int peopleSize;
    private int socialSize;
    private int notifySize;

    public HomeLaunch(Context context)
    {
        super(context);
    }

    public void setSize(int peopleSize, int socialSize, int notifySize)
    {
        this.peopleSize = peopleSize;
        this.socialSize = socialSize;
        this.notifySize = notifySize;

        layoutParams.width = Simple.MP;
        layoutParams.height = Simple.MP;

        layoutParams.leftMargin = socialSize;
        layoutParams.topMargin = notifySize;
        layoutParams.rightMargin = isPortrait() ? 8 : peopleSize;
        layoutParams.bottomMargin = isPortrait() ? peopleSize : 8;
    }

    public void setConfig(JSONObject config)
    {
    }

    @Override
    protected void onToogleFullscreen()
    {
        bringToFront();
    }

    @Override
    protected void onChangeOrientation()
    {
        if (isPortrait())
        {
            layoutParams.rightMargin = 8;
            layoutParams.bottomMargin = peopleSize;
        }

        if (isLandscape())
        {
            layoutParams.rightMargin = peopleSize;
            layoutParams.bottomMargin = 8;
        }
    }
}
