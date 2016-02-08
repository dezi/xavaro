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
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.Simple;

public class LaunchFrameDeveloper extends LaunchFrame
{
    private static final String LOGTAG = HealthFrame.class.getSimpleName();

    private String subtype;
    private ScrollView scrollview;
    private TextView jsonListing;
    private ImageView schwalbe;

    public LaunchFrameDeveloper(Context context)
    {
        super(context);

        myInit();
    }

    private void myInit()
    {
        scrollview = new ScrollView(getContext());
        scrollview.setBackgroundColor(0xffffc080);
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
        JSONObject root = PersistManager.getRoot();
        jsonListing.setText(Json.toPretty(root));
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
            return true;
        }
    };
}