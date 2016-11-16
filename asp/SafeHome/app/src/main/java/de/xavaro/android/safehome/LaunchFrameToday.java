package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import de.xavaro.android.common.ActivityListViewAdapter;
import de.xavaro.android.common.PinnedListView;

public class LaunchFrameToday extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameToday.class.getSimpleName();

    public LaunchFrameToday(Context context, LaunchItem parent)
    {
        super(context, parent);

        myInit();
    }

    private FrameLayout topscreen;
    private PinnedListView listView;

    private void myInit()
    {
        topscreen = new FrameLayout(getContext());
        topscreen.setBackgroundColor(GlobalConfigs.LaunchPageBackgroundColor);
        addView(topscreen);

        listView = new PinnedListView(getContext());
        ActivityListViewAdapter sectionedAdapter = new ActivityListViewAdapter();
        listView.setAdapter(sectionedAdapter);
        topscreen.addView(listView);
    }
}