package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends HomeFrame
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    private int layoutSize;
    private int peopleSize;
    private int notifySize;

    public HomeSocial(Context context)
    {
        super(context);
    }

    public void setSize(int layoutSize, int peopleSize, int notifySize)
    {
        this.layoutSize = layoutSize;
        this.peopleSize = peopleSize;
        this.notifySize = notifySize;

        layoutParams.width = layoutSize;
        layoutParams.height = Simple.MP;
        layoutParams.gravity = Gravity.LEFT;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = notifySize;
        layoutParams.rightMargin = 8;
        layoutParams.bottomMargin = isPortrait() ? peopleSize : 8;
    }

    public void setConfig(JSONObject config)
    {
        WebAppView webView = new WebAppView(getContext());
        webView.loadWebView("instaface", "main");

        payloadFrame.addView(webView);
    }

    private final Runnable toggleAnimator = new Runnable()
    {
        @Override
        public void run()
        {
            if (animationSteps > 0)
            {
                if (fullscreen)
                {
                    layoutParams.leftMargin  = 8;
                    layoutParams.rightMargin = 8;

                    layoutParams.width -= animationWidth;
                    layoutParams.topMargin += animationTop;
                    layoutParams.bottomMargin += animationBottom;
                }
                else
                {
                    layoutParams.leftMargin = 0;
                    layoutParams.rightMargin = 0;

                    layoutParams.width += animationWidth;
                    layoutParams.topMargin -= animationTop;
                    layoutParams.bottomMargin -= animationBottom;
                }

                setLayoutParams(layoutParams);

                animationSteps -= 1;
                Simple.makePost(toggleAnimator, 0);

                return;
            }

            if (fullscreen)
            {
                layoutParams.width = layoutSize;
                layoutParams.topMargin = notifySize;
                layoutParams.bottomMargin = isPortrait() ? peopleSize : 0;

                innerLayout.topMargin = headspace;
                titleLayout.height = headspace;

                innerClick.setVisibility(VISIBLE);
                innerFrame.setBackgroundColor(Color.TRANSPARENT);
                innerFrame.setBackground(Simple.getRoundedBorders());
                innerFrame.setPadding(8, 8, 8, 8);

                fullscreen = false;
            }
            else
            {
                layoutParams.width = Simple.MP;
                layoutParams.topMargin = 0;
                layoutParams.bottomMargin = 0;

                innerLayout.topMargin = headspace * 2;
                titleLayout.height = headspace * 2;

                innerClick.setVisibility(GONE);
                innerFrame.setBackground(null);
                innerFrame.setBackgroundColor(0xffffffff);
                innerFrame.setPadding(0, 0, 0, 0);
                titleText.setBackgroundColor(0xffcccccc);
                titleText.setGravity(Gravity.CENTER);
                titleClose.setVisibility(VISIBLE);

                fullscreen = true;
            }

            setLayoutParams(layoutParams);
        }
    };

    private int animationTop;
    private int animationBottom;
    private int animationWidth;

    @Override
    protected void onToogleFullscreen()
    {
        bringToFront();

        animationSteps = 10;

        animationTop = notifySize / animationSteps;
        animationBottom = (isPortrait() ? peopleSize : 0) / animationSteps;
        animationWidth = ((View) getParent()).getWidth() / animationSteps;

        if (layoutParams.width == Simple.MP) layoutParams.width = ((View) getParent()).getWidth();

        if (fullscreen)
        {
            titleText.setBackgroundColor(Color.TRANSPARENT);
            titleText.setGravity(Gravity.START);
            titleClose.setVisibility(GONE);
        }

        Simple.makePost(toggleAnimator);
    }

    @Override
    protected void onChangeOrientation()
    {
        if (isPortrait())
        {
            layoutParams.bottomMargin = peopleSize;
        }

        if (isLandscape())
        {
            layoutParams.bottomMargin = 8;
        }
    }
}
