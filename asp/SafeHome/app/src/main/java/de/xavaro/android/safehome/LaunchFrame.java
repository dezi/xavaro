package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import de.xavaro.android.common.Simple;

public class LaunchFrame extends FrameLayout
{
    private static final String LOGTAG = LaunchFrame.class.getSimpleName();

    protected LaunchItem parent;

    public LaunchFrame(Context context)
    {
        super(context);
    }

    public LaunchFrame(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LaunchFrame(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setParent(LaunchItem parent)
    {
        this.parent = parent;
    }

    public void onBackKeyExecuted()
    {
        Log.d(LOGTAG, "onBackKeyExecuted");
    }
}
