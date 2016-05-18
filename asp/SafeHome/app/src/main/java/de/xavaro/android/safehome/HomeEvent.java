package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import de.xavaro.android.common.Simple;

public class HomeEvent extends FrameLayout
{
    private static final String LOGTAG = HomeEvent.class.getSimpleName();

    private LinearLayout.LayoutParams layoutParams;

    private FrameLayout.LayoutParams titleParams;
    private TextView titleView;
    private HomeButton declineButton;
    private HomeButton followButton;
    private boolean istopevent;

    public HomeEvent(Context context, boolean istopevent)
    {
        super(context);

        this.istopevent = istopevent;

        layoutParams = new LinearLayout.LayoutParams(Simple.MP, 0);
        layoutParams.leftMargin = this.istopevent ? 0 : Simple.getDevicePixels(12);
        setLayoutParams(layoutParams);

        titleView = new TextView(context);
        titleParams = new FrameLayout.LayoutParams(Simple.MP, 0);

        titleView.setLayoutParams(layoutParams);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setTypeface(null, istopevent ? Typeface.BOLD : Typeface.NORMAL);
        titleView.setTextSize(Simple.getDeviceTextSize(istopevent ? 30f : 24f));
        titleView.setTextColor(istopevent ? 0xff444444 : 0xff888888);

        addView(titleView);

        followButton = new HomeButton(context, this.istopevent, 1);
        followButton.setVisibility(GONE);

        addView(followButton);

        if (this.istopevent)
        {
            declineButton = new HomeButton(context, this.istopevent, 2);
            declineButton.setVisibility(GONE);

            addView(declineButton);
        }
    }

    public void setLayoutHeight(int height)
    {
        if (layoutParams.height != height)
        {
            layoutParams.height = height;
            setLayoutParams(layoutParams);

            if (istopevent)
            {
                titleParams.height = height / 3;

                titleParams.topMargin = height / 10;
                titleParams.leftMargin = height + height / 10;
                titleParams.rightMargin = height / 8;
            }
            else
            {
                titleParams.height = Simple.MP;

                titleParams.topMargin = 0;
                titleParams.leftMargin = height + height / 8;
                titleParams.rightMargin = HomeButton.buttonWidth + HomeButton.buttonWidth / 8;
            }

            titleView.setLayoutParams(titleParams);
        }
    }

    public int getLayoutHeight()
    {
        return layoutParams.height;
    }

    public void setTitleText(String text)
    {
        titleView.setText(text);
    }

    public void setFollowButtonText(String text)
    {
        followButton.setText(text);
        followButton.setVisibility((text == null) ? GONE : VISIBLE);
    }

    public void setDeclineButtonText(String text)
    {
        if (declineButton != null)
        {
            declineButton.setText(text);
            declineButton.setVisibility((text == null) ? GONE : VISIBLE);
        }
    }
}
