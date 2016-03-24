package de.xavaro.android.safehome;

import android.content.Context;
import android.widget.FrameLayout;
import android.util.AttributeSet;
import android.util.Log;

import de.xavaro.android.common.BackKeyClient;

public class LaunchFrame extends FrameLayout implements BackKeyClient
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

    public boolean onBackKeyWanted()
    {
        //
        // To be overwritten.
        //

        Log.d(LOGTAG, "onBackKeyWanted");

        return false;
    }

    public void onBackKeyExecuted()
    {
        //
        // To be overwritten.
        //

        Log.d(LOGTAG, "onBackKeyExecuted");
    }
}
