package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import de.xavaro.android.common.ActivityListViewAdapter;
import de.xavaro.android.common.PinnedListView;

public class LaunchFrameToday extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameToday.class.getSimpleName();

    private PinnedListView listView;
    private ActivityListViewAdapter activityList;

    public LaunchFrameToday(Context context, LaunchItem parent)
    {
        super(context, parent);

        setBackgroundColor(GlobalConfigs.LaunchPageBackgroundColor);

        listView = new PinnedListView(getContext());
        activityList = new ActivityListViewAdapter();
        listView.setAdapter(activityList);
        addView(listView);
    }
}