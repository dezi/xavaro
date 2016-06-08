package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.view.View;

import org.json.JSONObject;

import de.xavaro.android.common.BackKeyClient;

@SuppressLint("RtlHardcoded")
public class HomeWorker extends HomeFrame
{
    private static final String LOGTAG = HomeWorker.class.getSimpleName();

    public HomeWorker(Context context)
    {
        super(context);

        setFullscreen();

        onlyfullscreen = true;
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
}
