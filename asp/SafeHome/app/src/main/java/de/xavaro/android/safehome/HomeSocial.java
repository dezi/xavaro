package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends FrameLayout
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    private int layoutSize;
    private int buddySize;
    private int notifySize;

    private LayoutParams layoutParams;
    private FrameLayout innerFrame;
    private FrameLayout payloadFrame;
    private WebAppView webView;

    private int orientation;

    public HomeSocial(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);
        setPadding(8, 0, 0, 8);

        innerFrame = new FrameLayout(context);
        addView(innerFrame);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(16);
        gd.setColor(0xffffffff);
        gd.setStroke(2, 0xffcccccc);

        innerFrame.setBackground(gd);
        innerFrame.setPadding(8, 8, 8, 8);

        payloadFrame = new FrameLayout(context);
        innerFrame.addView(payloadFrame);

        webView = new WebAppView(getContext());
        webView.loadWebView("instaface", "main");
        if (webView.social != null) webView.social.setMode("news");

        payloadFrame.addView(webView);

        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    public void setSize(int layoutSize, int buddySize, int notifySize)
    {
        this.layoutSize = layoutSize;
        this.buddySize = buddySize;
        this.notifySize = notifySize;

        layoutParams.width = layoutSize;
        layoutParams.height = Simple.MP;
        layoutParams.gravity = Gravity.LEFT;

        layoutParams.rightMargin = 0;
        layoutParams.topMargin = notifySize;
    }

    public void setConfig(JSONObject config)
    {
    }

    private Runnable changeOrientation = new Runnable()
    {
        @Override
        public void run()
        {
            if ((orientation != Configuration.ORIENTATION_PORTRAIT) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT))
            {
                layoutParams.bottomMargin = buddySize;

                orientation = Configuration.ORIENTATION_PORTRAIT;
            }

            if ((orientation != Configuration.ORIENTATION_LANDSCAPE) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_LANDSCAPE))
            {
                layoutParams.bottomMargin = 0;

                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Simple.makePost(changeOrientation);
    }
}
