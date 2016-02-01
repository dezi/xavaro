package de.xavaro.android.safehome;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;

public class HealthFrame extends LaunchFrame
{
    private static final String LOGTAG = HealthFrame.class.getSimpleName();

    private String subtype;
    private ScrollView scrollview;
    private TextView jsonListing;

    public HealthFrame(Context context)
    {
        super(context);

        myInit();
    }

    private void myInit()
    {
        scrollview = new ScrollView(getContext());
        scrollview.setBackgroundColor(0xffffff80);
        addView(scrollview);

        jsonListing = new TextView(getContext());
        jsonListing.setPadding(16, 16, 16, 16);
        jsonListing.setTextSize(18f);
        scrollview.addView(jsonListing);
    }

    public void setSubtype(String subtype)
    {
        this.subtype = subtype;

        JSONObject status = HealthData.getStatus(subtype);
        JSONArray records = HealthData.getRecords(subtype);

        JSONObject all = new JSONObject();
        Json.put(all, "status", status);
        Json.put(all, "records", records);

        jsonListing.setText(Json.toPretty(all));
    }
}