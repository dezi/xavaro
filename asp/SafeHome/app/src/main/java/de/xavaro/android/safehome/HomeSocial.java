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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends FrameLayout
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    private int headspace = Simple.getDevicePixels(40);

    private int layoutSize;
    private int buddySize;
    private int notifySize;

    private LayoutParams layoutParams;
    private LayoutParams titleLayout;
    private TextView titleText;
    private LayoutParams innerLayout;
    private FrameLayout innerFrame;
    private FrameLayout payloadFrame;
    private WebAppView webView;

    private int orientation;
    private boolean fullscreen;

    public HomeSocial(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);
        setPadding(8, 0, 0, 8);

        innerLayout = new LayoutParams(Simple.MP, Simple.MP);
        innerLayout.topMargin = headspace;

        innerFrame = new FrameLayout(context);
        addView(innerFrame);

        titleLayout = new LayoutParams(Simple.MP, headspace);

        titleText = new TextView(context);
        titleText.setLayoutParams(titleLayout);
        titleText.setText("Neuigkeiten");
        titleText.setTextSize(headspace * 2 / 3);
        titleText.setPadding(16, 0, 0, 0);

        titleText.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                toogleFullscreen();
            }
        });

        addView(titleText);

        innerFrame.setLayoutParams(innerLayout);
        innerFrame.setBackground(Simple.getRoundedBorders());
        innerFrame.setPadding(8, 8, 8, 8);

        payloadFrame = new FrameLayout(context);
        innerFrame.addView(payloadFrame);

        webView = new WebAppView(getContext());
        webView.loadWebView("instaface", "main");
        if (webView.social != null) webView.social.setMode("news");

        payloadFrame.addView(webView);

        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    private void toogleFullscreen()
    {
        bringToFront();

        if (fullscreen)
        {
            setPadding(8, 0, 0, 8);

            layoutParams.width = layoutSize;

            layoutParams.topMargin = notifySize;
            layoutParams.rightMargin = 8;
            layoutParams.bottomMargin = buddySize;

            innerLayout.topMargin = headspace;
            innerFrame.setBackgroundColor(Color.TRANSPARENT);
            innerFrame.setBackground(Simple.getRoundedBorders());

            titleLayout.height = headspace;
            titleText.setBackgroundColor(Color.TRANSPARENT);
            titleText.setGravity(Gravity.START);

            fullscreen = false;
        }
        else
        {
            setPadding(0, 0, 0, 0);

            layoutParams.width = Simple.MP;

            layoutParams.topMargin = 0;
            layoutParams.rightMargin = 0;
            layoutParams.bottomMargin = 0;

            innerLayout.topMargin = headspace * 2;
            innerFrame.setBackground(null);
            innerFrame.setBackgroundColor(0xffffffff);

            titleLayout.height = headspace * 2;
            titleText.setBackgroundColor(0xffcccccc);
            titleText.setGravity(Gravity.CENTER);

            fullscreen = true;
        }
    }

    public void setSize(int layoutSize, int buddySize, int notifySize)
    {
        this.layoutSize = layoutSize;
        this.buddySize = buddySize;
        this.notifySize = notifySize;

        layoutParams.width = layoutSize;
        layoutParams.height = Simple.MP;
        layoutParams.gravity = Gravity.LEFT;

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
