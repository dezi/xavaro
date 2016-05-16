package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.view.Gravity;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends HomeFrame
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    private int peopleSize;

    public HomeSocial(Context context)
    {
        super(context);
    }

    public void setSize(int layoutSize, int peopleSize, int notifySize)
    {
        this.peopleSize = peopleSize;

        layoutParams.width = layoutSize - 16;
        layoutParams.height = Simple.MP;
        layoutParams.gravity = Gravity.LEFT;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = notifySize;
        layoutParams.rightMargin = 8;
        layoutParams.bottomMargin = isPortrait() ? peopleSize : 8;

        layoutNormal = new LayoutParams(layoutParams);
    }

    public void setConfig(JSONObject config)
    {
        WebAppView webView = new WebAppView(getContext());
        webView.loadWebView("instaface", "main");

        payloadFrame.addView(webView);
    }

    @Override
    protected void onChangeOrientation()
    {
        if (isPortrait())
        {
            layoutNormal.bottomMargin = peopleSize;
        }

        if (isLandscape())
        {
            layoutNormal.bottomMargin = 8;
        }

        if (! fullscreen)
        {
            layoutParams.bottomMargin = layoutNormal.bottomMargin;
        }
    }
}
