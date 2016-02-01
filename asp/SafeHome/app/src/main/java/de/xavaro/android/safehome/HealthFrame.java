package de.xavaro.android.safehome;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class HealthFrame extends LaunchFrame
{
    private static final String LOGTAG = HealthFrame.class.getSimpleName();

    private String subtype;
    private ScrollView scrollview;
    private TextView jsonListing;
    private ImageView schwalbe;

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

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                Simple.getActionBarHeight(),
                Simple.getActionBarHeight(),
                Gravity.END + Gravity.BOTTOM);

        lp.setMargins(8, 8, 8, 8);

        schwalbe = new ImageView(getContext());
        schwalbe.setLayoutParams(lp);
        schwalbe.setImageResource(R.drawable.sendmessage_430x430);

        schwalbe.setLongClickable(true);
        schwalbe.setOnClickListener(onSchwalbeClick);
        schwalbe.setOnLongClickListener(onSchwalbeLongClick);

        addView(schwalbe);
    }

    public void setSubtype(String subtype)
    {
        this.subtype = subtype;

        loadContent();
    }

    private void loadContent()
    {
        JSONObject status = HealthData.getStatus(subtype);
        JSONArray records = HealthData.getRecords(subtype);

        JSONObject all = new JSONObject();
        Json.put(all, "status", status);
        Json.put(all, "records", records);

        jsonListing.setText(Json.toPretty(all));
    }

    private View.OnClickListener onSchwalbeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            loadContent();
        }
    };

    private View.OnLongClickListener onSchwalbeLongClick = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View view)
        {
            HealthData.clearStatus(subtype);
            HealthData.clearRecords(subtype);

            loadContent();

            return true;
        }
    };
}