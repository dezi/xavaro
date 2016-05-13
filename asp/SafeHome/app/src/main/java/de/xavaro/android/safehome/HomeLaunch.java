package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.FrameLayout;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeLaunch extends FrameLayout
{
    private int layoutSize;

    private LayoutParams layoutParams;

    public HomeLaunch(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(Simple.MP, Simple.MP);
        setLayoutParams(layoutParams);
    }

    public void setSize(int pixels)
    {
        layoutSize = pixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT)
        {
            layoutParams.rightMargin = 0;
            layoutParams.bottomMargin = layoutSize;
        }
        else
        {
            layoutParams.rightMargin = layoutSize;
            layoutParams.bottomMargin = 0;
        }
    }
}
