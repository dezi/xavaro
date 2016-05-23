package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.xavaro.android.common.Simple;

public class HomeButton extends TextView
{
    private static final String LOGTAG = HomeButton.class.getSimpleName();

    public static int buttonWidth = Simple.getDevicePixels(Simple.isTablet() ? 220 : 80);
    public static int buttonHeight = Simple.getDevicePixels(Simple.isTablet() ? 42 : 32);

    private boolean istopevent;

    public HomeButton(Context context, boolean istopevent, int buttonnumber)
    {
        super(context);

        this.istopevent = istopevent;

        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(buttonWidth, buttonHeight);
        layout.gravity = Gravity.END + (this.istopevent ? Gravity.BOTTOM : Gravity.CENTER_VERTICAL);
        layout.rightMargin = Simple.getDevicePixels(Simple.isTablet() ? 16 : 8);

        if (this.istopevent)
        {
            layout.bottomMargin = Simple.getDevicePixels(Simple.isTablet() ? 6 : 2);
            layout.rightMargin += (Simple.getDevicePixels(6) + buttonWidth) * (buttonnumber - 1);
        }

        setLayoutParams(layout);
        setBackground(Simple.getRoundedBorders(8, 0xff66afff, 0xff0a80ff));
        setGravity(Gravity.CENTER_VERTICAL + Gravity.CENTER_HORIZONTAL);
        setTextSize(Simple.getDeviceTextSize(Simple.isTablet() ? 22f : 16f));
        setPadding(0, 0, 0, Simple.getDevicePixels(Simple.isTablet() ? 4 : 2));
        setTypeface(null, Typeface.BOLD);
        setTextColor(0xffffffff);
    }
}
