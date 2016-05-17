package de.xavaro.android.safehome;

import android.content.Context;
import android.widget.LinearLayout;

import de.xavaro.android.common.Simple;

public class HomeEvent extends LinearLayout
{
    LayoutParams layoutParams;

    public HomeEvent(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(Simple.MP, 0);
        setLayoutParams(layoutParams);
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
}
