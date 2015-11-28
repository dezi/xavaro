package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

//
// Launch item view on home screen.
//

public class LaunchItem extends ViewGroup
{
    private final String LOGTAG = "LaunchItem";

    private Context context;

    private FrameLayout.LayoutParams layout;

    public LaunchItem(Context context)
    {
        super(context);

        myInit(context);
    }

    public LaunchItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public LaunchItem(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    private void myInit(Context context)
    {
        this.context = context;

        setBackgroundColor(0xff0000ff);

        layout = new FrameLayout.LayoutParams(0,0);

        setLayoutParams(layout);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        Log.d(LOGTAG, left + "=" + left + "=" + top + "=" + right + "=" + bottom);
    }

    @Override
    protected void onMeasure(int width, int height)
    {
        super.onMeasure(width, height);
    }

    public void setSize(int width,int height)
    {
        layout.width  = width;
        layout.height = height;

        setLayoutParams(layout);
    }

    public void setPosition(int left,int top)
    {
        layout.leftMargin = left;
        layout.topMargin  = top;

        setLayoutParams(layout);
    }
}
