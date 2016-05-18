package de.xavaro.android.safehome;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.util.Log;

import de.xavaro.android.common.Simple;

public class HomeEvent extends FrameLayout
{
    private static final String LOGTAG = HomeEvent.class.getSimpleName();

    private LinearLayout.LayoutParams layoutParams;
    private FrameLayout.LayoutParams followParams;
    private HomeButton followButton;

    public HomeEvent(Context context)
    {
        super(context);

        layoutParams = new LinearLayout.LayoutParams(Simple.MP, 0);
        setLayoutParams(layoutParams);
        setPadding(0, 0, Simple.getDevicePixels(16), 0);

        followParams = new FrameLayout.LayoutParams(200, 40, Gravity.END + Gravity.CENTER_VERTICAL);
        followButton = new HomeButton(context);
        followButton.setLayoutParams(followParams);
        followButton.setGravity(Gravity.CENTER_VERTICAL + Gravity.CENTER_HORIZONTAL);
        followButton.setText("Ja doch");

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
}
