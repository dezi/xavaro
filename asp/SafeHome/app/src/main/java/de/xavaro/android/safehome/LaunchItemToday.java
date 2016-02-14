package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.xavaro.android.common.Simple;

public class LaunchItemToday extends LaunchItem
{
    private final static String LOGTAG = LaunchItemToday.class.getSimpleName();

    public LaunchItemToday(Context context)
    {
        super(context);
    }

    private TextView timeView;

    @Override
    protected void setConfig()
    {
        icon.setImageResource(GlobalConfigs.IconResToday);

        timeView = new TextView(getContext());
        timeView.setLayoutParams(Simple.layoutParamsMM());
        timeView.setPadding(0, 0, 0, icon.getPaddingBottom());
        timeView.setTextSize(44f);
        timeView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        timeView.setTextColor(Color.WHITE);
        timeView.setTypeface(null, Typeface.BOLD);

        addView(timeView);

        post(updateTime);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("today")) launchToday();
    }

    LaunchFrameToday todayFrame;

    private Runnable updateTime = new Runnable()
    {
        @Override
        public void run()
        {
            timeView.setText(Simple.getLocal24HTime(Simple.nowAsTimeStamp()));

            postDelayed(updateTime, 1000);
        }
    };

    private void launchToday()
    {
        if (todayFrame == null)
        {
            todayFrame = new LaunchFrameToday(context);
        }

        ((HomeActivity) context).addViewToBackStack(todayFrame);
    }
}
