package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends HomeFrame
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    private WebAppView webView;

    private String titleText;
    private int titleColor;

    public HomeSocial(Context context)
    {
        super(context);

        layoutParams.width = Simple.MP;
        layoutParams.height = Simple.MP;
        layoutParams.gravity = Gravity.LEFT;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = notifySize;
        layoutParams.rightMargin = launchWid + 8 + (Simple.isPortrait() ? 8 : peopleSize);
        layoutParams.bottomMargin = Simple.isPortrait() ? peopleSize : 8;

        layoutNormal = new LayoutParams(layoutParams);
    }

    public void setConfig(JSONObject config)
    {
        if (webView == null)
        {
            webView = new WebAppView(getContext());
            webView.loadWebView("instaface", "main");

            if (webView.social != null)
            {
                webView.social.setNewsPostRunner(newsPostRunner);
            }

            payloadFrame.addView(webView);
        }
    }

    @Override
    public void setTitle(String title)
    {
        super.setTitle(title);

        titleText = title;
        titleColor = titleView.getCurrentTextColor();
    }

    protected void newsPostUpdateTitle()
    {
        if ((webView != null) && (webView.social != null))
        {
            int newsTotalCount = webView.social.getNewsTotalCount();

            String message = "" + newsTotalCount;

            if (newsTotalCount == 0)
            {
                message = Simple.getFirstCap(Simple.getTrans(R.string.simple_none));

                titleView.setTextColor(titleColor);
            }
            else
            {
                titleView.setTextColor(Color.RED);
            }

            message += " " + titleText;
            titleView.setText(message);

            Log.d(LOGTAG, "===================================>>>>>>>>>> fucka " + message);
        }
    }

    protected final Runnable newsPostRunner = new Runnable()
    {
        @Override
        public void run()
        {
            newsPostUpdateTitle();
        }
    };

    @Override
    protected void onChangeOrientation()
    {
        layoutNormal.topMargin = notifySize;
        layoutNormal.rightMargin = launchWid + 8 + (Simple.isPortrait() ? 8 : peopleSize);
        layoutNormal.bottomMargin = Simple.isPortrait() ? peopleSize : 8;

        if (! fullscreen)
        {
            layoutParams.topMargin = layoutNormal.topMargin;
            layoutParams.rightMargin = layoutNormal.rightMargin;
            layoutParams.bottomMargin = layoutNormal.bottomMargin;
        }
    }
}
