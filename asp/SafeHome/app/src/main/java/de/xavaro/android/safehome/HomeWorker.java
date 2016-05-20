package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeWorker extends HomeFrame
{
    public HomeWorker(Context context)
    {
        super(context);

        layoutParams.width = launchWid;
        layoutParams.height = launchHei;
        layoutParams.gravity = Gravity.RIGHT;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = notifySize;
        layoutParams.rightMargin = Simple.isPortrait() ? 8 : peopleSize;
        layoutParams.bottomMargin = Simple.isPortrait() ? peopleSize : 8;

        layoutNormal = new LayoutParams(layoutParams);
    }

    public void setConfig(JSONObject config)
    {
    }

    @Override
    protected void onToogleFullscreen()
    {
        payloadFrame.removeAllViews();
        setVisibility(GONE);
    }

    @Override
    protected void onChangeOrientation()
    {
        layoutNormal.topMargin = notifySize;
        layoutNormal.rightMargin = Simple.isPortrait() ? 8 : peopleSize;
        layoutNormal.bottomMargin = Simple.isPortrait() ? peopleSize : 8;

        if (! fullscreen)
        {
            layoutParams.topMargin = layoutNormal.topMargin;
            layoutParams.rightMargin = layoutNormal.rightMargin;
            layoutParams.bottomMargin = layoutNormal.bottomMargin;
        }
    }
}
