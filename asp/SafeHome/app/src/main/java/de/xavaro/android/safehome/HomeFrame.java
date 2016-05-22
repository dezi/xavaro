package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.ImageSmartView;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public abstract class HomeFrame extends FrameLayout
{
    private static final String LOGTAG = HomeFrame.class.getSimpleName();

    protected int titleSpace = Simple.getDevicePixels(40);
    protected int peopleSize = Simple.getDevicePixels(200);
    protected int notifySize = 0;

    protected int launchWid = CommonConfigs.LaunchItemSize * 2 + 32;
    protected int launchHei = CommonConfigs.LaunchItemSize * 2 + 32 + titleSpace;

    protected LayoutParams layoutParams;
    protected LayoutParams layoutNormal;

    protected LayoutParams titleLayout;
    protected ImageSmartView titleClose;
    protected ImageSmartView titleBacka;
    protected TextView titleView;

    protected LayoutParams innerLayout;
    protected FrameLayout innerFrame;
    protected FrameLayout innerClick;
    protected FrameLayout payloadFrame;

    protected boolean fullscreen;

    protected int animationSteps;
    protected int animationWidth;
    protected int animationHeight;
    protected int animationLeft;
    protected int animationTop;
    protected int animationRight;
    protected int animationBottom;
    protected int animationPad;

    public HomeFrame(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);
        setVisibility(INVISIBLE);

        titleLayout = new LayoutParams(Simple.MP, titleSpace);

        titleView = new TextView(context);
        titleView.setLayoutParams(titleLayout);
        titleView.setTextSize(titleSpace * 2 / 3);
        titleView.setTextColor(0xff888888);
        titleView.setPadding(16, 0, 0, 0);
        titleView.setOnClickListener(onClickListener);
        titleView.setVisibility(GONE);
        addView(titleView);

        titleClose = new ImageSmartView(context);
        titleClose.setLayoutParams(new LayoutParams(titleSpace * 2, titleSpace * 2, Gravity.END));
        titleClose.setImageResource(R.drawable.close_button_313x313);
        titleClose.setPadding(15, 15, 15, 15);
        titleClose.setVisibility(GONE);
        addView(titleClose);

        titleBacka = new ImageSmartView(context);
        titleBacka.setLayoutParams(new LayoutParams(titleSpace * 2, titleSpace * 2, Gravity.START));
        titleBacka.setImageResource(R.drawable.back_button_256x256);
        titleBacka.setPadding(15, 15, 15, 15);
        titleBacka.setVisibility(GONE);
        addView(titleBacka);

        innerLayout = new LayoutParams(Simple.MP, Simple.MP);

        innerFrame = new FrameLayout(context);
        innerFrame.setLayoutParams(innerLayout);
        innerFrame.setBackground(Simple.getRoundedBorders(16, 0xffffffff, 0xffcccccc));
        innerFrame.setPadding(8, 8, 8, 8);
        addView(innerFrame);

        payloadFrame = new FrameLayout(context);
        innerFrame.addView(payloadFrame);

        innerClick = new FrameLayout(context);
        innerClick.setOnClickListener(onClickListener);
        addView(innerClick);
    }

    public FrameLayout getPayloadFrame()
    {
        return payloadFrame;
    }

    public void setDisablefullscreen()
    {
        if (innerClick != null)
        {
            innerClick.setOnClickListener(null);
            removeView(innerClick);
            innerClick = null;
        }
    }

    public void setTitle(String title)
    {
        if (title != null)
        {
            titleView.setText(title);
            titleView.setVisibility(VISIBLE);
            innerLayout.topMargin = titleSpace * (fullscreen ? 2 : 1);
        }
    }

    protected final OnClickListener onClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            onToogleFullscreen();
        }
    };

    private Runnable changeOrientation = new Runnable()
    {
        @Override
        public void run()
        {
            int wid = ((View) getParent()).getWidth();
            int hei = ((View) getParent()).getHeight();

            if ((wid != 0) && (hei != 0))
            {
                notifySize = hei - launchHei - (Simple.isPortrait() ? peopleSize : 8);

                onChangeOrientation();
                setVisibility(VISIBLE);
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if ((animationSteps == 0) && ! fullscreen)
        {
            Simple.makePost(changeOrientation);
        }
    }

    protected void onToogleFullscreen()
    {
        //
        // Animation steps should correspond to inner
        // padding size to make things smooth.
        //

        animationSteps = 8;

        animationPad = fullscreen ? animationSteps : 0;

        animationWidth = 0;
        animationHeight = 0;

        if ((layoutNormal.gravity == Gravity.LEFT) || (layoutNormal.gravity == Gravity.RIGHT))
        {
            int max = ((View) getParent()).getWidth();
            if (layoutParams.width == Simple.MP) layoutParams.width = max;
            animationWidth = Math.abs(max - layoutNormal.width) / animationSteps;
        }

        if ((layoutNormal.gravity == Gravity.TOP) || (layoutNormal.gravity == Gravity.BOTTOM))
        {
            int max = ((View) getParent()).getHeight();
            if (layoutParams.height == Simple.MP) layoutParams.height = max;
            animationHeight = Math.abs(max - layoutNormal.height) / animationSteps;
        }

        animationLeft = layoutNormal.leftMargin / animationSteps;
        animationTop = layoutNormal.topMargin / animationSteps;
        animationRight = layoutNormal.rightMargin / animationSteps;
        animationBottom = layoutNormal.bottomMargin / animationSteps;

        if (fullscreen)
        {
            titleView.setBackgroundColor(Color.TRANSPARENT);
            titleView.setGravity(Gravity.START);
            titleView.setTextColor(0xff888888);
            titleClose.setVisibility(GONE);
            titleBacka.setVisibility(GONE);
        }

        bringToFront();

        Simple.makePost(toggleAnimator);
    }

    protected void onChangeOrientation()
    {
    }

    public void setFullscreen()
    {
        layoutParams.width = Simple.MP;
        layoutParams.height = Simple.MP;

        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 0;
        layoutParams.rightMargin = 0;
        layoutParams.bottomMargin = 0;

        innerLayout.topMargin = titleSpace * 2;
        titleLayout.height = titleSpace * 2;

        innerClick.setVisibility(GONE);
        innerFrame.setBackground(null);
        innerFrame.setBackgroundColor(0xffffffff);
        innerFrame.setPadding(0, 0, 0, 0);

        titleView.setBackgroundColor(0xffcccccc);
        titleView.setGravity(Gravity.CENTER);
        titleView.setVisibility(VISIBLE);
        titleView.setTextColor(0xff444444);
        titleClose.setVisibility(VISIBLE);
        titleBacka.setVisibility(VISIBLE);

        setLayoutParams(layoutParams);

        fullscreen = true;
    }

    protected final Runnable toggleAnimator = new Runnable()
    {
        @Override
        public void run()
        {
            if (animationSteps > 0)
            {
                if (fullscreen)
                {
                    layoutParams.width -= animationWidth;
                    layoutParams.height -= animationHeight;

                    layoutParams.leftMargin += animationLeft;
                    layoutParams.topMargin += animationTop;
                    layoutParams.rightMargin += animationRight;
                    layoutParams.bottomMargin += animationBottom;

                    animationPad++;
                }
                else
                {
                    layoutParams.width += animationWidth;
                    layoutParams.height += animationHeight;

                    layoutParams.leftMargin -= animationLeft;
                    layoutParams.topMargin -= animationTop;
                    layoutParams.rightMargin -= animationRight;
                    layoutParams.bottomMargin -= animationBottom;

                    animationPad--;
                }

                innerFrame.setPadding(animationPad, animationPad, animationPad, animationPad);

                setLayoutParams(layoutParams);

                animationSteps -= 1;

                Simple.makePost(toggleAnimator, 0);

                return;
            }

            if (fullscreen)
            {
                layoutParams.width = layoutNormal.width;
                layoutParams.height = layoutNormal.height;

                layoutParams.leftMargin = layoutNormal.leftMargin;
                layoutParams.topMargin = layoutNormal.topMargin;
                layoutParams.rightMargin = layoutNormal.rightMargin;
                layoutParams.bottomMargin = layoutNormal.bottomMargin;

                innerLayout.topMargin = titleSpace;
                titleLayout.height = titleSpace;

                innerClick.setVisibility(VISIBLE);
                innerFrame.setBackgroundColor(Color.TRANSPARENT);
                innerFrame.setBackground(Simple.getRoundedBorders(16, 0xffffffff, 0xffcccccc));
                innerFrame.setPadding(8, 8, 8, 8);

                fullscreen = false;

                setLayoutParams(layoutParams);
            }
            else
            {
                setFullscreen();
            }
        }
    };
}
