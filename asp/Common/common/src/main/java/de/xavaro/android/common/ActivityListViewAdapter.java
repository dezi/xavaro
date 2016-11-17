package de.xavaro.android.common;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
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
        return null;
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

        return (recs != null) ? recs.length() : 0;
    }

    @Override
    public View getHeadView(int section, View convertView, ViewGroup parent)
    {
        LinearLayout view = null;

        if (convertView == null)
        {
            view = new LinearLayout(parent.getContext());
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setOrientation(LinearLayout.HORIZONTAL);
            Simple.setPadding(view, 10, 10, 10, 10);
            view.setBackgroundColor(0xdddddddd);

            TextView dateView = new TextView(parent.getContext());
            dateView.setLayoutParams(Simple.layoutParamsMW());
            dateView.setGravity(Gravity.CENTER_HORIZONTAL);
            dateView.setTextSize(Simple.getDeviceTextSize(24f));
            dateView.setTypeface(null, Typeface.BOLD);
            dateView.setId(android.R.id.text1);
            view.addView(dateView);
        }
        else
        {
            view = (LinearLayout) convertView;
        }

        JSONObject day = Json.getObject(days, section);
        String date = Json.getString(day, "date");

        TextView dateView = (TextView) view.findViewById(android.R.id.text1);

        if (dateView != null) dateView.setText(date);

        return view;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent)
    {
        LinearLayout view = null;

        if (convertView == null)
        {
            view = new LinearLayout(parent.getContext());
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setOrientation(LinearLayout.HORIZONTAL);
            Simple.setPadding(view, 10, 10, 10, 10);
            view.setBackgroundColor(0xffffffff);

            ImageSmartView iconView = new ImageSmartView(parent.getContext());
            iconView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(80),Simple.DP(80)));
            iconView.setBackgroundColor(0xcccccccc);
            iconView.setId(android.R.id.icon);
            view.addView(iconView);

            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(Simple.layoutParamsWW());
            Simple.setPadding(textView, 10, 0, 0, 0);
            textView.setTextSize(Simple.getDeviceTextSize(24f));
            textView.setTypeface(null, Typeface.BOLD);
            textView.setId(android.R.id.text1);
            view.addView(textView);
        }
        else
        {
            view = (LinearLayout) convertView;
        }

        JSONObject day = Json.getObject(days, section);
        JSONArray recs = Json.getArray(day, "recs");
        JSONObject rec = Json.getObject(recs, position);
        String text = Json.getString(rec, "text");
        String icon = Json.getString(rec, "icon");

        ImageSmartView iconView = (ImageSmartView) view.findViewById(android.R.id.icon);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        if (iconView != null) iconView.setImageResource(icon);
        if (textView != null) textView.setText(text);

        Log.d(LOGTAG, "pupsikate=" + icon);

        return view;
    }
}