package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.ImageSmartView;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebAppView;

@SuppressLint("RtlHardcoded")
public class HomeSocial extends FrameLayout
{
    private static final String LOGTAG = HomeSocial.class.getSimpleName();

    private int headspace = Simple.getDevicePixels(40);

    private int layoutSize;
    private int peopleSize;
    private int notifySize;

    private LayoutParams layoutParams;
    private LayoutParams titleLayout;
    private ImageSmartView titleClose;
    private TextView titleText;
    private LayoutParams innerLayout;
    private FrameLayout innerFrame;
    private FrameLayout innerClick;
    private FrameLayout payloadFrame;
    private WebAppView webView;

    private int orientation = Configuration.ORIENTATION_UNDEFINED;
    private boolean fullscreen;

    public HomeSocial(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);

        titleLayout = new LayoutParams(Simple.MP, headspace);

        titleText = new TextView(context);
        titleText.setLayoutParams(titleLayout);
        titleText.setTextSize(headspace * 2 / 3);
        titleText.setPadding(16, 0, 0, 0);
        titleText.setOnClickListener(onClickListener);
        titleText.setVisibility(GONE);
        addView(titleText);

        titleClose = new ImageSmartView(context);
        titleClose.setLayoutParams(new LayoutParams(headspace * 2, headspace * 2, Gravity.END));
        titleClose.setImageResource(R.drawable.close_button_313x313);
        titleClose.setPadding(15, 15, 15, 15);
        titleClose.setVisibility(GONE);
        addView(titleClose);

        innerLayout = new LayoutParams(Simple.MP, Simple.MP);

        innerFrame = new FrameLayout(context);
        innerFrame.setLayoutParams(innerLayout);
        innerFrame.setBackground(Simple.getRoundedBorders());
        innerFrame.setPadding(8, 8, 8, 8);
        addView(innerFrame);

        payloadFrame = new FrameLayout(context);
        innerFrame.addView(payloadFrame);

        webView = new WebAppView(getContext());
        webView.loadWebView("instaface", "main");
        if (webView.social != null) webView.social.setMode("news");

        payloadFrame.addView(webView);

        innerClick = new FrameLayout(context);
        innerClick.setOnClickListener(onClickListener);
        addView(innerClick);
    }

    public void setSize(int layoutSize, int peopleSize, int notifySize)
    {
        this.layoutSize = layoutSize;
        this.peopleSize = peopleSize;
        this.notifySize = notifySize;

        setPadding(8, 0, 0, 8);

        layoutParams.width = layoutSize;
        layoutParams.height = Simple.MP;
        layoutParams.gravity = Gravity.LEFT;
        layoutParams.topMargin = notifySize;
        layoutParams.bottomMargin = isPortrait() ? peopleSize : 0;;
    }

    public void setConfig(String title, JSONObject config)
    {
        if (title != null)
        {
            titleText.setText(title);
            titleText.setVisibility(VISIBLE);
            innerLayout.topMargin = headspace;
        }
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
            if (animationSteps > 0)
            {
                if (fullscreen)
                {
                    setPadding(8, 0, 0, 8);
                    layoutParams.rightMargin = 8;

                    layoutParams.width -= animationWidth;
                    layoutParams.topMargin += animationTop;
                    layoutParams.bottomMargin += animationBottom;
                }
                else
                {
                    setPadding(0, 0, 0, 0);
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

    private int animationSteps;
    private int animationTop;
    private int animationBottom;
    private int animationWidth;

    private void toogleFullscreen()
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

    private boolean isPortrait()
    {
        return (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT);
    }

    private boolean isLandscape()
    {
        return (Simple.getOrientation() == Configuration.ORIENTATION_LANDSCAPE);
    }

    private Runnable changeOrientation = new Runnable()
    {
        @Override
        public void run()
        {
            if ((orientation != Configuration.ORIENTATION_PORTRAIT) && isPortrait())
            {
                layoutParams.bottomMargin = peopleSize;

                orientation = Configuration.ORIENTATION_PORTRAIT;
            }

            if ((orientation != Configuration.ORIENTATION_LANDSCAPE) && isLandscape())
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

        if (animationSteps == 0) Simple.makePost(changeOrientation);
    }
}
