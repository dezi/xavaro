package de.xavaro.android.safehome;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import de.xavaro.android.common.Simple;

public class HomeEvent extends LinearLayout
{
    LayoutParams layoutParams;

    public HomeEvent(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(Simple.MP, 0);
        setOrientation(HORIZONTAL);
        setLayoutParams(layoutParams);
        setGravity(Gravity.CENTER_VERTICAL);
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
