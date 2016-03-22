package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemToday extends LaunchItem
{
    private final static String LOGTAG = LaunchItemToday.class.getSimpleName();

    public static JSONArray getConfig()
    {
        JSONArray launchitems = new JSONArray();

        JSONObject launchitem = new JSONObject();

        Json.put(launchitem, "type", "today");
        Json.put(launchitem, "label", Simple.getTrans(R.string.today_label));
        Json.put(launchitem, "order", 100);

        Json.put(launchitems, launchitem);

        return launchitems;
    }

    public LaunchItemToday(Context context)
    {
        super(context);
    }

    private TextView dayView;
    private TextView timeView;

    @Override
    protected void setConfig()
    {
        icon.setImageResource(GlobalConfigs.IconResToday);

        dayView = new TextView(getContext());
        dayView.setLayoutParams(Simple.layoutParamsMW());
        dayView.setPadding(0, 20, 0, 0);
        dayView.setTextSize(Simple.getDeviceTextSize(22f));
        dayView.setGravity(Gravity.CENTER_HORIZONTAL);
        dayView.setTextColor(Color.WHITE);
        dayView.setTypeface(null, Typeface.BOLD);

        addView(dayView);

        timeView = new TextView(getContext());
        timeView.setLayoutParams(Simple.layoutParamsMM());
        timeView.setPadding(0, 20, 0, icon.getPaddingBottom());
        timeView.setTextSize(Simple.getDeviceTextSize(44f));
        timeView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        timeView.setTextColor(Color.WHITE);
        timeView.setTypeface(null, Typeface.BOLD);

        addView(timeView);

        updateTime.run();
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
            long now = Simple.nowAsTimeStamp();
            dayView.setText(Simple.getLocalDay(now));
            timeView.setText(Simple.getLocal24HTime(now));

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
