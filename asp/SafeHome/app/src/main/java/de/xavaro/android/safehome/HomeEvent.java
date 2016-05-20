package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.util.Log;

import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

@SuppressLint("ViewConstructor")
public class HomeEvent extends FrameLayout
{
    private static final String LOGTAG = HomeEvent.class.getSimpleName();

    private LinearLayout.LayoutParams layoutParams;

    private FrameLayout.LayoutParams titleParams;
    private TextView titleView;
    private HomeButton declineButton;
    private HomeButton followButton;
    private boolean istopevent;
    private NotifyIntent intent;

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
        titleView.setTextColor(istopevent ? 0xff444444 : 0xff888888);

        addView(titleView);

        followButton = new HomeButton(context, this.istopevent, 1);
        followButton.setVisibility(GONE);
        followButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onFollowButtonClick();
            }
        });

        addView(followButton);

        if (this.istopevent)
        {
            declineButton = new HomeButton(context, this.istopevent, 2);
            declineButton.setVisibility(GONE);
            declineButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onDeclineButtonClick();
                }
            });

            addView(declineButton);
        }
    }

    protected void onFollowButtonClick()
    {
        Log.d(LOGTAG, "onFollowButtonClick:");

        if ((intent != null) && (intent.followRunner != null))
        {
            intent.followRunner.run();
        }
    }

    protected void onDeclineButtonClick()
    {
        Log.d(LOGTAG, "onDeclineButtonClick:");

        if ((intent != null) && (intent.declineRunner != null))
        {
            intent.declineRunner.run();
        }
    }

    public void setNotifyIntent(NotifyIntent intent)
    {
        this.intent = intent;

        if (intent == null)
        {
            setTitleText(null);
            setFollowButtonText(null);
            setDeclineButtonText(null);
        }
        else
        {
            setTitleText(intent.title);
            setFollowButtonText(intent.followText);
            setDeclineButtonText(intent.declineText);

            //
            // Check for spoken text and repeat intervals.
            //

            if (intent.spokenTimePref != null)
            {
                int repeatval = intent.spokenRepeatMinutes * 60 * 1000;
                String date = Simple.getSharedPrefString(intent.spokenTimePref);
                String ddue = Simple.timeStampAsISO(Simple.nowAsTimeStamp() - repeatval);

                if ((date == null) || ((repeatval > 0) && (date.compareTo(ddue) <= 0)))
                {
                    int volume = 50;

                    if (intent.importance == NotifyIntent.WARNING) volume = 75;
                    if (intent.importance == NotifyIntent.ASSISTANCE) volume = 100;

                    Speak.speak(intent.title, volume);
                    Simple.setSharedPrefString(intent.spokenTimePref, Simple.nowAsISO());
                }
            }
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
                titleParams.height = height / 2;

                titleParams.topMargin = Simple.isPortrait() ? height / 10 : 0;
                titleParams.leftMargin = height + height / 10;
                titleParams.rightMargin = Simple.getDevicePixels(16);
            }
            else
            {
                titleParams.height = Simple.MP;

                titleParams.topMargin = 0;
                titleParams.leftMargin = height + height / 8;
                titleParams.rightMargin = Simple.getDevicePixels(16);

                if ((followButton != null) && (followButton.getVisibility() == VISIBLE))
                {
                    titleParams.rightMargin += Simple.getDevicePixels(16) + HomeButton.buttonWidth;
                }
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
        titleView.setMaxWidth(titleView.getWidth());

        float textsize = Simple.getDeviceTextSize(istopevent ? 30f : 24f);

        while (true)
        {
            titleView.setTextSize(textsize);
            titleView.measure(0, 0);

            if (titleView.getMeasuredHeight() <= titleView.getHeight())
            {
                //
                // Text fits view.
                //

                break;
            }

            textsize -= 1.0f;
        }
    }

    public void setFollowButtonText(String text)
    {
        if (followButton != null)
        {
            followButton.setText(text);
            followButton.setVisibility((text == null) ? GONE : VISIBLE);

            if (! istopevent)
            {
                if (text != null)
                {
                    titleParams.rightMargin += Simple.getDevicePixels(32) + HomeButton.buttonWidth;
                }
                else
                {
                    titleParams.rightMargin = Simple.getDevicePixels(16);
                }

                titleView.setLayoutParams(titleParams);
            }
        }
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
