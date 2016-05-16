package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONObject;

import de.xavaro.android.common.ImageSmartView;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public abstract class HomeFrame extends FrameLayout
{
    private static final String LOGTAG = HomeFrame.class.getSimpleName();

    protected int headspace = Simple.getDevicePixels(40);

    protected LayoutParams layoutParams;
    protected LayoutParams titleLayout;
    protected ImageSmartView titleClose;
    protected TextView titleText;
    protected LayoutParams innerLayout;
    protected FrameLayout innerFrame;
    protected FrameLayout innerClick;
    protected FrameLayout payloadFrame;

    protected boolean fullscreen;

    protected int animationSteps;

    public HomeFrame(Context context)
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

        innerClick = new FrameLayout(context);
        innerClick.setOnClickListener(onClickListener);
        addView(innerClick);
    }

    public FrameLayout getPayloadFrame()
    {
        return payloadFrame;
    }

    public void setTitle(String title)
    {
        if (title != null)
        {
            titleText.setText(title);
            titleText.setVisibility(VISIBLE);
            innerLayout.topMargin = headspace;
        }
    }

    protected boolean isPortrait()
    {
        return (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT);
    }

    protected boolean isLandscape()
    {
        return (Simple.getOrientation() == Configuration.ORIENTATION_LANDSCAPE);
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
            onChangeOrientation();
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (animationSteps == 0) Simple.makePost(changeOrientation);
    }

    protected abstract void onToogleFullscreen();
    protected abstract void onChangeOrientation();
}
