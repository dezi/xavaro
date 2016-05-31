package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends HomeFrame
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    private WebAppView webView;

    public HomeSocial(Context context)
    {
        super(context);

        layoutParams.width = Simple.MP;
        layoutParams.height = Simple.MP;
        layoutParams.gravity = Gravity.LEFT;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = HomeActivity.notifySize;
        layoutParams.rightMargin = HomeActivity.launchWid + 8 + (Simple.isPortrait() ? 8 : HomeActivity.peopleSize);
        layoutParams.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : 8;

        layoutNormal = new LayoutParams(layoutParams);
    }

    public void setConfig(JSONObject config)
    {
        if (Simple.isTablet() && (webView == null))
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

    protected void newsPostUpdateTitle()
    {
        if ((webView != null) && (webView.social != null))
        {
            int newsTotalCount = webView.social.getNewsTotalCount();

            String message = newsTotalCount + " " + "Neuigkeiten";

            if (newsTotalCount == 0)
            {
                message = Simple.getFirstCap(Simple.getTrans(R.string.simple_none));
                message += " " + "Neuigkeiten";

                titleText.setTextColor(fullscreen ? 0xff444444 : 0xff888888);
            }
            else
            {
                if (newsTotalCount == 1)
                {
                    message = newsTotalCount + " " + "Neuigkeit";
                }

                titleText.setTextColor(Color.RED);
            }

            titleText.setText(message);
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
        layoutNormal.topMargin = HomeActivity.notifySize;
        layoutNormal.rightMargin = HomeActivity.launchWid + 8 + (Simple.isPortrait() ? 8 : HomeActivity.peopleSize);
        layoutNormal.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : 8;

        if (! fullscreen)
        {
            layoutParams.topMargin = layoutNormal.topMargin;
            layoutParams.rightMargin = layoutNormal.rightMargin;
            layoutParams.bottomMargin = layoutNormal.bottomMargin;
        }
    }
}
