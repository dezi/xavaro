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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.ImageSmartView;
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
    private ImageSmartView titleClose;
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
        titleText.setOnClickListener(onClickListener);

        addView(titleText);

        titleClose = new ImageSmartView(context);
        titleClose.setLayoutParams(new LayoutParams(headspace * 2, headspace * 2, Gravity.END));
        titleClose.setImageResource(R.drawable.close_button_313x313);
        titleClose.setPadding(15, 15, 15, 15);
        titleClose.setVisibility(GONE);
        addView(titleClose);

        innerFrame.setLayoutParams(innerLayout);
        innerFrame.setBackground(Simple.getRoundedBorders());
        innerFrame.setPadding(8, 8, 8, 8);
        innerFrame.setOnClickListener(onClickListener);

        payloadFrame = new FrameLayout(context);
        innerFrame.addView(payloadFrame);

        webView = new WebAppView(getContext());
        webView.loadWebView("instaface", "main");
        if (webView.social != null) webView.social.setMode("news");

        payloadFrame.addView(webView);

        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    private final OnClickListener onClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            toogleFullscreen();
        }
    };

    private final Runnable toggleAnimator = new Runnable()
    {
        @Override
        public void run()
        {
            if (fullscreen)
            {
                setPadding(8, 0, 0, 8);
                layoutParams.width = layoutSize;
                layoutParams.rightMargin = 8;

                layoutParams.topMargin = notifySize;
                layoutParams.bottomMargin = isPortrait() ? buddySize : 0;
                innerLayout.topMargin = headspace;
                titleLayout.height = headspace;

                innerFrame.setOnClickListener(onClickListener);
                innerFrame.setBackgroundColor(Color.TRANSPARENT);
                innerFrame.setBackground(Simple.getRoundedBorders());
                titleText.setBackgroundColor(Color.TRANSPARENT);
                titleText.setGravity(Gravity.START);
                titleClose.setVisibility(GONE);

                fullscreen = false;
            }
            else
            {
                setPadding(0, 0, 0, 0);
                layoutParams.width = Simple.MP;
                layoutParams.rightMargin = 0;

                layoutParams.topMargin = 0;
                layoutParams.bottomMargin = 0;
                innerLayout.topMargin = headspace * 2;
                titleLayout.height = headspace * 2;

                innerFrame.setOnClickListener(null);
                innerFrame.setBackground(null);
                innerFrame.setBackgroundColor(0xffffffff);
                titleText.setBackgroundColor(0xffcccccc);
                titleText.setGravity(Gravity.CENTER);
                titleClose.setVisibility(VISIBLE);

                fullscreen = true;
            }
        }
    };

    private void toogleFullscreen()
    {
        bringToFront();

        Simple.makePost(toggleAnimator);
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

    private boolean isPortrait()
    {
        return (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT);
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
