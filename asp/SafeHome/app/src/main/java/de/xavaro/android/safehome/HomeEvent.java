package de.xavaro.android.safehome;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.util.Log;

import de.xavaro.android.common.Simple;

public class HomeEvent extends FrameLayout
{
    private static final String LOGTAG = HomeEvent.class.getSimpleName();

    private LinearLayout.LayoutParams layoutParams;
    private HomeButton followButton;
    private boolean istopevent;

    public HomeEvent(Context context, boolean istopevent)
    {
        super(context);

        this.istopevent = istopevent;

        layoutParams = new LinearLayout.LayoutParams(Simple.MP, 0);
        setLayoutParams(layoutParams);

        followButton = new HomeButton(context, this.istopevent);

        addView(followButton);
    }

    public void setLayoutHeight(int height)
    {
        if (layoutParams.height != height)
        {
            layoutParams.height = height;
            setLayoutParams(layoutParams);
        }
    }

    public int getLayoutHeight()
    {
        return layoutParams.height;
    }

    public void setButtonText(String text)
    {
        followButton.setText(text);
    }
}
