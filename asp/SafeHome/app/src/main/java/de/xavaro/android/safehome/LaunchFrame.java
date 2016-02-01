package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class LaunchFrame extends FrameLayout
{
    private static final String LOGTAG = LaunchFrame.class.getSimpleName();

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
}
