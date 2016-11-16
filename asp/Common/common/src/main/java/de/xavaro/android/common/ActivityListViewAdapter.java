package de.xavaro.android.common;

import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.View;

public class ActivityListViewAdapter extends PinnedListViewAdapter
{
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
        return 7;
    }

    @Override
    public int getCountForSection(int section)
    {
        return 15;
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

        ((TextView) view.findViewById(android.R.id.text1)).setText("Section " + section + " Item " + position);

        return view;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent)
    {
        LinearLayout view = null;

        if (convertView == null)
        {
            view = new LinearLayout(Simple.getActContext());
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setOrientation(LinearLayout.HORIZONTAL);
            Simple.setPadding(view, 10, 10, 10, 10);
            view.setBackgroundColor(0xffccff00);

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

        ((TextView) view.findViewById(android.R.id.text1)).setText("Header for section " + section);

        return view;
    }

}