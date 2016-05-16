package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends HomeFrame
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    public HomeSocial(Context context)
    {
        super(context);

        layoutParams.width = Simple.MP;
        layoutParams.height = Simple.MP;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = notifySize;
        layoutParams.rightMargin = launchWid + 8 + (isPortrait() ? 8 : peopleSize);
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
        layoutNormal.topMargin = notifySize;
        layoutNormal.rightMargin = launchWid + 8 + (isPortrait() ? 8 : peopleSize);
        layoutNormal.bottomMargin = isPortrait() ? peopleSize : 8;

        if (! fullscreen)
        {
            layoutParams.topMargin = layoutNormal.topMargin;
            layoutParams.rightMargin = layoutNormal.rightMargin;
            layoutParams.bottomMargin = layoutNormal.bottomMargin;
        }
    }
}
