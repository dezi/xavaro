package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.SocialGoogleplus;
import de.xavaro.android.common.SocialInstagram;
import de.xavaro.android.common.SocialTwitter;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
@SuppressWarnings("ResourceType")
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

        layoutParams.leftMargin = Simple.DP(8);
        layoutParams.topMargin = HomeActivity.notifySize;
        layoutParams.rightMargin = HomeActivity.launchWid + Simple.DP(8) + (Simple.isPortrait() ? Simple.DP(8) : HomeActivity.peopleSize);
        layoutParams.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : Simple.DP(8);

        layoutNormal = new LayoutParams(layoutParams);
    }

    @SuppressWarnings("UnusedParameters")
    public void setConfig(JSONObject config)
    {
        boolean social =
                SocialTwitter.getInstance().isEnabled() ||
                SocialFacebook.getInstance().isEnabled() ||
                SocialInstagram.getInstance().isEnabled() ||
                SocialGoogleplus.getInstance().isEnabled();

        if (Simple.isTablet() && social)
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

            setVisibility(VISIBLE);
        }
        else
        {
            if (webView != null)
            {
                Simple.removeFromParent(webView);

                webView.destroy();
                webView = null;
            }

            setVisibility(GONE);
        }
    }

    private void newsPostUpdateTitle()
    {
        if ((webView != null) && (webView.social != null))
        {
            int newsTotalCount = webView.social.getNewsTotalCount();

            String message = newsTotalCount + " " + "Neuigkeiten";

            if (newsTotalCount == 0)
            {
                message = Simple.getFirstCap(Simple.getTrans(R.string.simple_none));
                message += " " + "Neuigkeiten";

                titleView.setTextColor(fullscreen ? 0xff444444 : 0xff888888);
            }
            else
            {
                if (newsTotalCount == 1)
                {
                    message = newsTotalCount + " " + "Neuigkeit";
                }

                titleView.setTextColor(Color.RED);
            }

            titleView.setText(message);
        }
    }

    private final Runnable newsPostRunner = new Runnable()
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
        layoutNormal.rightMargin = HomeActivity.launchWid + Simple.DP(8) + (Simple.isPortrait() ? Simple.DP(8) : HomeActivity.peopleSize);
        layoutNormal.bottomMargin = Simple.isPortrait() ? HomeActivity.peopleSize : Simple.DP(8);

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
