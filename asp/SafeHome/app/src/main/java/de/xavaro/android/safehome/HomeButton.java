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

    private int buttonWidth = Simple.getDevicePixels(220);
    private int buttonHeight = Simple.getDevicePixels(50);
    private boolean istopevent;

    public HomeButton(Context context, boolean istopevent)
    {
        super(context);

        this.istopevent = istopevent;

        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(buttonWidth, buttonHeight);
        layout.gravity = Gravity.END + (this.istopevent ? Gravity.BOTTOM : Gravity.CENTER_VERTICAL);
        if (this.istopevent) layout.bottomMargin = Simple.getDevicePixels(6);
        layout.rightMargin = Simple.getDevicePixels(16);

        setLayoutParams(layout);
        setBackground(Simple.getRoundedBorders(8, 0xff66afff, 0xff0a80ff));
        setGravity(Gravity.CENTER_VERTICAL + Gravity.CENTER_HORIZONTAL);
        setPadding(0, 0, 0, Simple.getDevicePixels(4));
        setTextSize(Simple.getDeviceTextSize(22f));
        setTypeface(null, Typeface.BOLD);
        setTextColor(0xffffffff);
    }
}
