package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;

import de.xavaro.android.common.ActivityListViewAdapter;
import de.xavaro.android.common.HealthData;
import de.xavaro.android.common.PinnedListView;

@SuppressLint("ViewConstructor")
public class LaunchFrameToday extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameToday.class.getSimpleName();

    public LaunchFrameToday(Context context, LaunchItem parent)
    {
        super(context, parent);

        setBackgroundColor(Color.WHITE);

        PinnedListView listView = new PinnedListView(getContext());
        addView(listView);

        ActivityListViewAdapter adapter = new ActivityListViewAdapter();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
        listView.setOnItemLongClickListener(adapter);
    }
}