package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class LaunchFrameWebApp extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameWebApp.class.getSimpleName();

    private String webappname;

    public LaunchFrameWebApp(Context context)
    {
        super(context);
    }

    public LaunchFrameWebApp(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LaunchFrameWebApp(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setWebAppName(String webappname)
    {
        this.webappname = webappname;

        this.setBackgroundColor(0x8000ff00);
    }
}
