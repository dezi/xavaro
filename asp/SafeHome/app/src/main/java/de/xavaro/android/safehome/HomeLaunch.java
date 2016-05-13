package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeLaunch extends FrameLayout
{
    public HomeLaunch(Context context)
    {
        super(context);

        init();
    }

    public HomeLaunch(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    public HomeLaunch(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    private LayoutParams layoutParams;

    private void init()
    {
        layoutParams = new LayoutParams(Simple.MP, Simple.MP);
        setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT)
        {
            layoutParams.rightMargin = 0;
            layoutParams.bottomMargin = Simple.getDevicePixels(200);
        }
        else
        {
            layoutParams.rightMargin = Simple.getDevicePixels(200);
            layoutParams.bottomMargin = 0;
        }
    }
}
