package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeBottom extends FrameLayout
{
    public HomeBottom(Context context)
    {
        super(context);

        init();
    }

    public HomeBottom(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    public HomeBottom(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    private LayoutParams layoutParams;

    private void init()
    {
        layoutParams = new LayoutParams(Simple.MP, 200);
        setLayoutParams(layoutParams);

        setBackgroundColor(0xcccccccc);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT)
        {
            layoutParams.width = Simple.MP;
            layoutParams.height = Simple.getDevicePixels(200);
            layoutParams.gravity = Gravity.BOTTOM;
        }
        else
        {
            layoutParams.width = Simple.getDevicePixels(200);
            layoutParams.height = Simple.MP;
            layoutParams.gravity = Gravity.RIGHT;
        }
    }
}
