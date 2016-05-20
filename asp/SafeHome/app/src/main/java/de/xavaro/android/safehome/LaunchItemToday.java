package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
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
    private TextView dateView;

    @Override
    protected void setConfig()
    {
        icon.setImageResource(GlobalConfigs.IconResToday);

        dayView = new TextView(getContext());
        dayView.setLayoutParams(Simple.layoutParamsMW());
        dayView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        dayView.setTextColor(Color.WHITE);
        dayView.setTypeface(null, Typeface.BOLD);

        addView(dayView);

        timeView = new TextView(getContext());
        timeView.setLayoutParams(Simple.layoutParamsMM());
        timeView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        timeView.setTextColor(Color.WHITE);
        timeView.setTypeface(null, Typeface.BOLD);

        addView(timeView);

        dateView = new TextView(getContext());
        dateView.setLayoutParams(Simple.layoutParamsMM());
        dateView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        dateView.setTextColor(Color.WHITE);
        dateView.setTypeface(null, Typeface.BOLD);

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

        float scale = (height - icon.getPaddingBottom()) / 200.0f;

        dayView.setTextSize(Simple.getDeviceTextSize(28f * scale));
        timeView.setTextSize(Simple.getDeviceTextSize(56f * scale));
        dateView.setTextSize(Simple.getDeviceTextSize(24f * scale));

        int devpad20 = Simple.getDevicePixels(Math.round(20 * scale));
        int devpad54 = Simple.getDevicePixels(Math.round(54 * scale));

        dayView.setPadding(0, devpad20, 0, 0);
        timeView.setPadding(0, devpad54, 0, 0);
        dateView.setPadding(0, 0, 0, devpad20 + icon.getPaddingBottom());
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
            String dom = Simple.getLocalDayOfMonth(now) + ". " + Simple.getLocalMonth(now);

            dayView.setText(Simple.getLocalDayOfWeek(now));
            timeView.setText(Simple.getLocal24HTime(now));
            dateView.setText(dom);

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
