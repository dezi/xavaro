package de.xavaro.android.common;

import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActivityListViewAdapter extends PinnedListViewAdapter
{
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
            view = new LinearLayout(Simple.getActContext());
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setOrientation(LinearLayout.HORIZONTAL);
            Simple.setPadding(view, 10, 10, 10, 10);
            view.setBackgroundColor(0xffffffff);

            TextView dateView = new TextView(Simple.getActContext());
            dateView.setLayoutParams(Simple.layoutParamsWW());
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

        ((TextView) view.findViewById(android.R.id.text1)).setText(date);

        return view;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent)
    {
        LinearLayout view = null;

        if (convertView == null)
        {
            view = new LinearLayout(Simple.getActContext());
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setOrientation(LinearLayout.HORIZONTAL);
            Simple.setPadding(view, 10, 10, 10, 10);
            view.setBackgroundColor(0xffccccff);

            TextView dateView = new TextView(Simple.getActContext());
            dateView.setLayoutParams(Simple.layoutParamsWW());
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
        JSONArray recs = Json.getArray(day, "recs");
        JSONObject rec = Json.getObject(recs, position);
        String text = Json.getString(rec, "text");

        ((TextView) view.findViewById(android.R.id.text1)).setText(text);

        return view;
    }
}