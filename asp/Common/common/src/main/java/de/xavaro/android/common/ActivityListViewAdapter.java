package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.View;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActivityListViewAdapter extends PinnedListViewAdapter
{
    private static final String LOGTAG = ActivityListViewAdapter.class.getSimpleName();

    private JSONArray days;

    public ActivityListViewAdapter()
    {
        super();

        days = ActivityManager.getInstance().loadDays();
    }
    
    @Override
    public Object getItem(int section, int position)
    {
        JSONObject day = Json.getObject(days, section);
        JSONArray recs = Json.getArray(day, "recs");

        return (recs == null) ? null : Json.getObject(recs, position);
    }

    @Override
    public long getItemId(int section, int position)
    {
        return 0;
    }

    @Override
    public int getSectionCount()
    {
        return days.length();
    }

    @Override
    public int getCountForSection(int section)
    {
        JSONObject day = Json.getObject(days, section);
        JSONArray recs = Json.getArray(day, "recs");

        return (recs == null) ? 0 : recs.length();
    }

    @Override
    public View getHeadView(int section, View convertView, ViewGroup parent)
    {
        LinearLayout view;

        if (convertView == null)
        {
            view = new LinearLayout(parent.getContext());
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setOrientation(LinearLayout.HORIZONTAL);
            Simple.setPadding(view, 10, 10, 10, 10);
            view.setBackgroundColor(0x448888ff);

            TextView dateView = new TextView(parent.getContext());
            dateView.setLayoutParams(Simple.layoutParamsMW());
            dateView.setGravity(Gravity.CENTER_HORIZONTAL);
            dateView.setTextSize(Simple.getDeviceTextSize(24f));
            dateView.setTypeface(null, Typeface.BOLD);
            dateView.setId(android.R.id.content);
            view.addView(dateView);
        }
        else
        {
            view = (LinearLayout) convertView;
        }

        JSONObject day = Json.getObject(days, section);
        String date = Json.getString(day, "date");

        TextView dateView = (TextView) view.findViewById(android.R.id.content);

        if (dateView != null) dateView.setText(date);

        return view;
    }

    @Override
    @SuppressLint("RtlHardcoded")
    public View getItemView(int section, int position, View convertView, ViewGroup parent)
    {
        FrameLayout view;

        if (convertView == null)
        {
            view = new FrameLayout(parent.getContext());
            view.setLayoutParams(Simple.layoutParamsMW());

            LinearLayout contFrame = new LinearLayout(parent.getContext());
            contFrame.setLayoutParams(Simple.layoutParamsWW());
            contFrame.setOrientation(LinearLayout.HORIZONTAL);
            view.addView(contFrame);

            FrameLayout iconFrame = new FrameLayout(parent.getContext());
            iconFrame.setLayoutParams(Simple.layoutParamsXX(Simple.DP(100),Simple.DP(100)));
            Simple.setPadding(iconFrame, 10, 10, 10, 10);
            contFrame.addView(iconFrame);

            ImageSmartView iconView = new ImageSmartView(parent.getContext());
            iconView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(80),Simple.DP(80)));
            iconView.setBackgroundColor(0xffffffff);
            iconView.setId(android.R.id.icon1);
            iconFrame.addView(iconView);

            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(Simple.layoutParamsWW());
            Simple.setPadding(textView, 10, 4, 10, 16);
            textView.setTextSize(Simple.getDeviceTextSize(22f));
            textView.setTypeface(null, Typeface.BOLD);
            textView.setId(android.R.id.text1);
            contFrame.addView(textView);

            ImageSmartView alertView = new ImageSmartView(parent.getContext());
            alertView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(50), Simple.DP(50), Gravity.RIGHT | Gravity.TOP));
            alertView.setId(android.R.id.icon2);
            Simple.setPadding(alertView, 0, 10, 10, 0);
            view.addView(alertView);

            TextView timeView = new TextView(parent.getContext());
            timeView.setLayoutParams(Simple.layoutParamsWW(Gravity.RIGHT | Gravity.BOTTOM));
            timeView.setTextSize(Simple.getDeviceTextSize(16f));
            timeView.setId(android.R.id.text2);
            Simple.setPadding(timeView, 0, 0, 15, 0);
            view.addView(timeView);
        }
        else
        {
            view = (FrameLayout) convertView;
        }

        view.setBackgroundColor(((position % 2) == 0) ? 0x44ffffff : 0x44dddddd);

        JSONObject day = Json.getObject(days, section);
        JSONArray recs = Json.getArray(day, "recs");
        JSONObject rec = Json.getObject(recs, position);
        String date = Json.getString(rec, "date");
        String text = Json.getString(rec, "text");
        String icon = Json.getString(rec, "icon");
        String mode = Json.getString(rec, "mode");

        String time = Simple.getLocal24HTime(date);

        if ((text != null) && ! text.endsWith(".")) text += ".";

        int duration = Json.getInt(rec, "duration");

        if (duration > 0)
        {
            int mins = duration / 60;

            time = ((mins == 0) ? "<1" : ("~" + mins)) + " min, " + time;
        }

        ImageSmartView iconView = (ImageSmartView) view.findViewById(android.R.id.icon1);
        ImageSmartView alertView = (ImageSmartView) view.findViewById(android.R.id.icon2);

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        TextView timeView = (TextView) view.findViewById(android.R.id.text2);

        if (iconView != null) iconView.setImageResource(icon);
        if (textView != null) textView.setText(text);
        if (timeView != null) timeView.setText(time);

        if (alertView != null)
        {
            if (Simple.equals(mode, "warning"))
            {
                alertView.setImageResource(CommonConfigs.IconResWarning);
            }
            else
            {
                alertView.setImageResource(0);
            }
        }

        return view;
    }
}