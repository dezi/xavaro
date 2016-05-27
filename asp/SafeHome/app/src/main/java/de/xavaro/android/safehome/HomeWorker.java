package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import de.xavaro.android.common.BackKeyClient;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeWorker extends HomeFrame
{
    private static final String LOGTAG = HomeWorker.class.getSimpleName();

    public HomeWorker(Context context)
    {
        super(context);

        layoutParams.width = HomeActivity.launchWid;
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
    protected void onToogleFullscreen()
    {
        View content = payloadFrame.getChildAt(0);

        if (content instanceof BackKeyClient)
        {
            ((BackKeyClient) content).onBackKeyExecuted();
        }

        payloadFrame.removeAllViews();

        setVisibility(GONE);
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
}
