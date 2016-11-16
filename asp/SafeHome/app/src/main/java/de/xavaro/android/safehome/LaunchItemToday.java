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

        if (Simple.getSharedPrefBoolean("monitors.enable"))
        {
            String mode = Simple.getSharedPrefString("monitors.today.mode");

            JSONObject launchitem = new JSONObject();

            Json.put(launchitem, "type", "today");
            Json.put(launchitem, "label", Simple.getTrans(R.string.today_label));
            Json.put(launchitem, "order", 100);

            if (Simple.equals(mode, "home"))
            {
                Json.put(launchitems, launchitem);
            }
        }

        return launchitems;
    }

    public LaunchItemToday(Context context)
    {
        super(context);
    }

    private TextView wdayView;
    private TextView timeView;
    private TextView dateView;

    @Override
    protected void setConfig()
    {
        icon.setImageResource(GlobalConfigs.IconResToday);

        wdayView = new TextView(getContext());
        wdayView.setGravity(Gravity.CENTER_HORIZONTAL);
        wdayView.setTypeface(null, Typeface.BOLD);
        wdayView.setTextColor(Color.WHITE);

        addView(wdayView);

        timeView = new TextView(getContext());
        timeView.setGravity(Gravity.CENTER_HORIZONTAL);
        timeView.setTypeface(null, Typeface.BOLD);
        timeView.setTextColor(Color.WHITE);

        addView(timeView);

        dateView = new TextView(getContext());
        dateView.setGravity(Gravity.CENTER_HORIZONTAL);
        dateView.setTypeface(null, Typeface.BOLD);
        dateView.setTextColor(Color.WHITE);

        addView(dateView);

        updateTime.run();
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);

        //
        // Original font sizes based on 200 pixels height.
        //

        float scale = Simple.getNormalPixels(height - icon.getPaddingBottom()) / 200.0f;

        wdayView.setTextSize(Simple.getDeviceTextSize(28f * scale));
        timeView.setTextSize(Simple.getDeviceTextSize(56f * scale));
        dateView.setTextSize(Simple.getDeviceTextSize(24f * scale));

        Simple.setPadding(wdayView, 0, Math.round( 20 * scale), 0, 0);
        Simple.setPadding(timeView, 0, Math.round( 50 * scale), 0, 0);
        Simple.setPadding(dateView, 0, Math.round(120 * scale), 0, 0);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("today")) launchToday();
    }

    private Runnable updateTime = new Runnable()
    {
        @Override
        public void run()
        {
            long now = Simple.nowAsTimeStamp();
            String dom = Simple.getLocalDayOfMonth(now) + ". " + Simple.getLocalMonth(now);

            wdayView.setText(Simple.getLocalDayOfWeek(now));
            timeView.setText(Simple.getLocal24HTime(now));
            dateView.setText(dom);

            postDelayed(updateTime, 1000);
        }
    };

    private void launchToday()
    {
        LaunchFrameToday todayFrame = new LaunchFrameToday(context, this);
        ((HomeActivity) context).addViewToBackStack(todayFrame);
    }
}
