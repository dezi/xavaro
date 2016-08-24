package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;

import de.xavaro.android.common.BackKeyClient;
import de.xavaro.android.common.ImageSmartView;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public abstract class HomeFrame extends FrameLayout implements BackKeyClient
{
    private static final String LOGTAG = HomeFrame.class.getSimpleName();

    protected LayoutParams layoutParams;
    protected LayoutParams layoutNormal;

    protected LayoutParams titleLayout;
    protected FrameLayout titleFrame;
    protected ImageSmartView titleClose;
    protected ImageSmartView titleBacka;
    protected TextView titleView;
    protected String titleText;

    protected LayoutParams pageLayout;
    protected LinearLayout pageBullets;

    protected ArrayList<ImageView> pageBulletsImages = new ArrayList<>();

    protected LayoutParams innerLayout;
    protected FrameLayout innerFrame;
    protected FrameLayout innerClick;
    protected FrameLayout payloadFrame;

    protected boolean onlyfullscreen;
    protected boolean fullscreen;
    protected int titleFullSize;

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

        titleFullSize = HomeActivity.titleSpace * (Simple.isTablet() ? 2 : 1);

        int ipad = Simple.getDevicePixels(Simple.isTablet() ? 16 : 4);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);
        setVisibility(INVISIBLE);

        titleLayout = new LayoutParams(Simple.MP, HomeActivity.titleSpace);

        titleFrame = new FrameLayout(context);
        titleFrame.setLayoutParams(titleLayout);

        addView(titleFrame);

        titleView = new TextView(context);
        titleView.setTextSize(Simple.getDeviceTextSize(Simple.getNormalPixels(HomeActivity.titleSpace * 2 / 3)));
        titleView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        titleView.setTextColor(0xff888888);
        titleView.setPadding(ipad, 0, 0, 0);
        titleView.setVisibility(GONE);
        titleFrame.addView(titleView);

        pageLayout = new LayoutParams(Simple.WC, Simple.getDevicePixels(16));
        pageLayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        pageLayout.bottomMargin = Simple.getDevicePixels(8);

        pageBullets = new LinearLayout(context);
        pageBullets.setLayoutParams(pageLayout);
        pageBullets.setVisibility(GONE);
        titleFrame.addView(pageBullets);

        titleClose = new ImageSmartView(context);
        titleClose.setLayoutParams(new LayoutParams(titleFullSize, titleFullSize, Gravity.END));
        titleClose.setImageResource(R.drawable.close_button_313x313);
        titleClose.setPadding(ipad, ipad, ipad, ipad);
        titleClose.setVisibility(GONE);
        titleClose.setOnClickListener(onBackClickListener);
        titleFrame.addView(titleClose);

        titleBacka = new ImageSmartView(context);
        titleBacka.setLayoutParams(new LayoutParams(titleFullSize, titleFullSize, Gravity.START));
        titleBacka.setImageResource(R.drawable.back_button_256x256);
        titleBacka.setPadding(ipad, ipad, ipad, ipad);
        titleBacka.setVisibility(GONE);
        titleBacka.setOnClickListener(onBackClickListener);
        titleFrame.addView(titleBacka);

        innerLayout = new LayoutParams(Simple.MP, Simple.MP);

        innerFrame = new FrameLayout(context);
        innerFrame.setLayoutParams(innerLayout);

        if (Simple.isTablet())
        {
            innerFrame.setBackground(Simple.getRoundedBorders(16, 0xffffffff, 0xffcccccc));
            Simple.setPadding(innerFrame, 8, 8, 8, 8);
        }

        addView(innerFrame);

        payloadFrame = new FrameLayout(context);
        innerFrame.addView(payloadFrame);

        innerClick = new FrameLayout(context);
        innerClick.setOnClickListener(onInnerClickListener);
        addView(innerClick);
    }

    public FrameLayout getPayloadFrame()
    {
        return payloadFrame;
    }

    public void checkPayloadFrame()
    {
        int childs = payloadFrame.getChildCount();

        if (childs > 0)
        {
            Object view = payloadFrame.getChildAt(childs - 1);

            if (view instanceof LaunchGroup)
            {
                ((LaunchGroup) view).adjustArrows();
            }
        }
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

    public void setActivePage(int act, int max, String subtitle)
    {
        if (subtitle == null)
        {
            titleView.setText(titleText);
        }
        else
        {
            titleView.setText(subtitle);
        }

        int dp16 = Simple.getDevicePixels(16);

        while (pageBulletsImages.size() > max)
        {
            ImageView bullet = pageBulletsImages.remove(0);
            Simple.removeFromParent(bullet);
        }

        while (pageBulletsImages.size() < max)
        {
            ImageView bullet = new ImageView(getContext());
            bullet.setLayoutParams(new LayoutParams(dp16, dp16));
            pageBulletsImages.add(bullet);
            pageBullets.addView(bullet);
        }

        for (int inx = 0; inx < max; inx++)
        {
            ImageView bullet = pageBulletsImages.get(inx);

            bullet.setImageResource(inx == act
                    ? R.drawable.bullet_black_32x32
                    : R.drawable.bullet_white_32x32);
        }

        pageBullets.setVisibility(((max > 1) && isFullscreen()) ? VISIBLE : GONE);
    }

    public void setTitle(String title)
    {
        titleText = title;

        if (title != null)
        {
            titleView.setText(title);
            titleView.setVisibility(VISIBLE);
            innerLayout.topMargin = fullscreen ? titleFullSize : HomeActivity.titleSpace;
        }
    }

    protected final OnClickListener onInnerClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            onToogleFullscreen();
        }
    };


    protected final OnClickListener onBackClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            HomeActivity.getInstance().onBackPressed();
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
                HomeActivity.notifySize = hei - HomeActivity.launchHei
                        - (Simple.isPortrait() ? HomeActivity.peopleSize : 8);

                onChangeOrientation();

                setVisibility(VISIBLE);
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (animationSteps == 0)
        {
            if (fullscreen)
            {
                if (Simple.hasNavigationBar())
                {
                    if (Simple.isPortrait())
                    {
                        layoutParams.rightMargin = 0;
                        layoutParams.bottomMargin = Simple.getNavigationBarHeight();
                    }
                    else
                    {
                        layoutParams.rightMargin = Simple.getNavigationBarHeight();
                        layoutParams.bottomMargin = 0;
                    }
                }
            }
            else
            {
                Simple.makePost(changeOrientation);
            }
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
            int org = layoutNormal.width > 0 ? layoutNormal.width : max;
            if (layoutParams.width == Simple.MP) layoutParams.width = max;
            animationWidth = Math.abs(max - org) / animationSteps;
        }

        if ((layoutNormal.gravity == Gravity.TOP) || (layoutNormal.gravity == Gravity.BOTTOM))
        {
            int max = ((View) getParent()).getHeight();
            int org = layoutNormal.height > 0 ? layoutNormal.height : max;
            if (layoutParams.height == Simple.MP) layoutParams.height = max;
            animationHeight = Math.abs(max - org) / animationSteps;
        }

        animationLeft = layoutNormal.leftMargin / animationSteps;
        animationTop = layoutNormal.topMargin / animationSteps;
        animationRight = layoutNormal.rightMargin / animationSteps;
        animationBottom = layoutNormal.bottomMargin / animationSteps;

        Log.d(LOGTAG, "onToogleFullscreen: animationWidth=" + animationWidth);
        Log.d(LOGTAG, "onToogleFullscreen: animationHeight=" + animationHeight);
        Log.d(LOGTAG, "onToogleFullscreen: animationLeft=" + animationLeft);
        Log.d(LOGTAG, "onToogleFullscreen: animationTop=" + animationTop);
        Log.d(LOGTAG, "onToogleFullscreen: animationRight=" + animationRight);
        Log.d(LOGTAG, "onToogleFullscreen: animationBottom=" + animationBottom);

        if (fullscreen)
        {
            titleFrame.setBackgroundColor(Color.TRANSPARENT);

            titleView.setGravity(Gravity.START | Gravity.BOTTOM);
            titleView.setTextColor(0xff888888);

            titleClose.setVisibility(GONE);
            titleBacka.setVisibility(GONE);

            pageBullets.setVisibility(GONE);
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

        innerLayout.topMargin = titleFullSize;
        titleLayout.height = titleFullSize;

        innerClick.setVisibility(GONE);
        innerFrame.setBackground(null);
        innerFrame.setBackgroundColor(0xffffffff);
        innerFrame.setPadding(0, 0, 0, 0);

        titleFrame.setBackgroundColor(0xffcccccc);

        titleView.setGravity(Gravity.CENTER);
        titleView.setVisibility(VISIBLE);
        titleView.setTextColor(0xff444444);

        pageBullets.setVisibility(VISIBLE);

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

                if (Simple.isTablet())
                {
                    innerFrame.setPadding(animationPad, animationPad, animationPad, animationPad);
                }

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

                innerLayout.topMargin = HomeActivity.titleSpace;
                titleLayout.height = HomeActivity.titleSpace;

                innerClick.setVisibility(VISIBLE);
                innerFrame.setBackgroundColor(Color.TRANSPARENT);

                if (Simple.isTablet())
                {
                    innerFrame.setBackground(Simple.getRoundedBorders(16, 0xffffffff, 0xffcccccc));
                    Simple.setPadding(innerFrame, 8, 8, 8, 8);
                }

                fullscreen = false;

                setLayoutParams(layoutParams);
            }
            else
            {
                setFullscreen();
            }
        }
    };

    public boolean isFullscreen()
    {
        return fullscreen;
    }

    @Override
    public boolean onBackKeyWanted()
    {
        Log.d(LOGTAG, "onBackKeyWanted:" + fullscreen);

        if (getVisibility() == VISIBLE)
        {
            int childs = payloadFrame.getChildCount();

            if (childs > 0)
            {
                View content = payloadFrame.getChildAt(childs - 1);

                if (content instanceof BackKeyClient)
                {
                    Log.d(LOGTAG, "onBackKeyWanted: BackKeyClient:" + content);

                    if (((BackKeyClient) content).onBackKeyWanted())
                    {
                        Log.d(LOGTAG, "onBackKeyWanted: BackKeyClient wanted");

                        return true;
                    }

                    ((BackKeyClient) content).onBackKeyExecuted();
                }

                payloadFrame.removeView(content);

                if (--childs > 0)
                {
                    checkPayloadFrame();

                    return true;
                }
            }

            if (isFullscreen() && !onlyfullscreen)
            {
                onToogleFullscreen();

                return true;
            }
        }

        return false;
    }

    @Override
    public void onBackKeyExecuted()
    {
        Log.d(LOGTAG, "onBackKeyExecuted");

        int childs = payloadFrame.getChildCount();
        if (childs == 0) setVisibility(View.GONE);
    }
}
