package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.BackKeyClient;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeWorker extends HomeFrame implements BackKeyClient
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

    public boolean onBackKeyWanted()
    {
        //
        // To be overwritten.
        //

        Log.d(LOGTAG, "onBackKeyWanted");

        return false;
    }

    public void onBackKeyExecuted()
    {
        //
        // To be overwritten.
        //

        Log.d(LOGTAG, "onBackKeyExecuted");
    }
}
