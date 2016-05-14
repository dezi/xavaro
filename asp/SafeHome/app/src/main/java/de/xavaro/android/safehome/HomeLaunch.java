package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeLaunch extends FrameLayout
{
    private int buddySize;
    private int socialSize;
    private int notifySize;

    private int orientation;

    private LayoutParams layoutParams;
    private TextView titleText;
    private LayoutParams innerLayout;
    private FrameLayout innerFrame;
    private FrameLayout payloadFrame;

    public HomeLaunch(Context context)
    {
        super(context);

        int headspace = Simple.getDevicePixels(40);

        layoutParams = new LayoutParams(Simple.MP, Simple.MP);
        setLayoutParams(layoutParams);
        setPadding(8, 0, 8, 8);

        titleText = new TextView(context);
        titleText.setLayoutParams(new LayoutParams(Simple.MP, headspace));
        titleText.setText("Mein Safehome");
        titleText.setTextSize(headspace * 2 / 3);
        titleText.setPadding(16, 0, 0, 0);
        addView(titleText);

        innerLayout = new LayoutParams(Simple.MP, Simple.MP);
        innerLayout.topMargin = headspace;

        innerFrame = new FrameLayout(context);
        addView(innerFrame);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(16);
        gd.setColor(0xffffffff);
        gd.setStroke(2, 0xffcccccc);

        innerFrame.setLayoutParams(innerLayout);
        innerFrame.setBackground(gd);
        innerFrame.setPadding(8, 8, 8, 8);

        payloadFrame = new FrameLayout(context);
        innerFrame.addView(payloadFrame);

        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    public void setSize(int buddySize, int socialSize, int notifySize)
    {
        this.buddySize = buddySize;
        this.socialSize = socialSize;
        this.notifySize = notifySize;

        layoutParams.topMargin = notifySize;
        layoutParams.leftMargin = socialSize;
    }

    public FrameLayout getPayloadFrame()
    {
        return payloadFrame;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if ((orientation != Configuration.ORIENTATION_PORTRAIT) &&
                (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT))
        {
            layoutParams.rightMargin = 0;
            layoutParams.bottomMargin = buddySize;

            orientation = Configuration.ORIENTATION_PORTRAIT;
        }

        if ((orientation != Configuration.ORIENTATION_LANDSCAPE) &&
                (Simple.getOrientation() == Configuration.ORIENTATION_LANDSCAPE))
        {
            layoutParams.rightMargin = buddySize;
            layoutParams.bottomMargin = 0;

            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
    }
}
