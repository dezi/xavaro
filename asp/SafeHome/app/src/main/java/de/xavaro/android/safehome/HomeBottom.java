package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeBottom extends FrameLayout
{
    private static final String LOGTAG = HomeBottom.class.getSimpleName();

    private int layoutSize;

    private JSONArray contacts;

    private LayoutParams layoutParams;

    private LaunchItem alertLaunchItem;
    private LaunchItem voiceLaunchItem;
    private LinearLayout.LayoutParams peopleLayout;
    private LinearLayout peopleView;

    private ScrollView vertFrame;
    private HorizontalScrollView horzFrame;
    private LayoutParams vertLayout;
    private LayoutParams horzLayout;
    private int orientation;

    private ArrayList<LaunchItem> peopleList = new ArrayList<>();

    public HomeBottom(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(Simple.MP, Simple.MP);
        setLayoutParams(layoutParams);
        //setBackgroundColor(0x88880000);

        JSONObject alertConfig = Json.getObject(LaunchItemAlertcall.getConfig(), 0);

        if (alertConfig != null)
        {
            alertLaunchItem = LaunchItem.createLaunchItem(context, null, alertConfig);
            alertLaunchItem.setFrameLess();
            this.addView(alertLaunchItem);
        }

        JSONObject voiceConfig = Json.getObject(LaunchItemVoice.getConfig(), 0);

        if (voiceConfig != null)
        {
            voiceLaunchItem = LaunchItem.createLaunchItem(context, null, voiceConfig);
            voiceLaunchItem.setFrameLess();
            this.addView(voiceLaunchItem);
        }

        horzLayout = new LayoutParams(Simple.MP, Simple.MP);
        horzFrame = new HorizontalScrollView(context);
        horzFrame.setLayoutParams(horzLayout);
        addView(horzFrame);

        vertLayout = new LayoutParams(Simple.MP, Simple.MP);
        vertFrame = new ScrollView(context);
        vertFrame.setLayoutParams(vertLayout);
        addView(vertFrame);

        peopleLayout = new LinearLayout.LayoutParams(Simple.WC, Simple.WC);
        peopleView = new LinearLayout(context);
        peopleView.setLayoutParams(peopleLayout);

        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    public void setSize(int pixels)
    {
        layoutSize = pixels;

        if (alertLaunchItem != null) alertLaunchItem.setSize(layoutSize, layoutSize);
        if (voiceLaunchItem != null) voiceLaunchItem.setSize(layoutSize, layoutSize);
    }

    public void setConfig(JSONObject config)
    {
        contacts = new JSONArray();
        peopleView.removeAllViews();

        extractConfig(config);

        for (int inx = 0; inx < contacts.length(); inx++)
        {
            JSONObject contact = Json.getObject(contacts, inx);
            if (contact == null) continue;

            LaunchItem li = LaunchItem.createLaunchItem(getContext(), null, contact);

            li.setFrameLess();
            li.setSize(layoutSize, layoutSize);

            peopleList.add(li);
            peopleView.addView(li);
        }
    }

    private void extractConfig(JSONObject config)
    {
        JSONArray lis = Json.getArray(config, "launchitems");
        if (lis == null) return;

        for (int inx = 0; inx < lis.length(); inx++)
        {
            JSONObject li = Json.getObject(lis, inx);
            String subtype = Json.getString(li, "subtype");
            if (subtype == null) continue;

            if (Simple.equals(subtype, "chat")
                    || Simple.equals(subtype, "text")
                    || Simple.equals(subtype, "voip")
                    || Simple.equals(subtype, "vica"))
            {
                Json.put(contacts, li);
            }
        }
    }

    private Runnable changeOrientation = new Runnable()
    {
        @Override
        public void run()
        {
            if ((orientation != Configuration.ORIENTATION_PORTRAIT) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT))
            {
                int width = ((View) getParent()).getWidth();
                int items = ((int) Math.floor(width / layoutSize)) - 1;
                int margin = (width - (items * layoutSize)) / 2;

                layoutParams.width = Simple.MP;
                layoutParams.height = layoutSize;
                layoutParams.gravity = Gravity.BOTTOM;

                if (alertLaunchItem != null)
                {
                    alertLaunchItem.setGravity(Gravity.LEFT);
                    horzLayout.leftMargin = margin;
                }

                if (voiceLaunchItem != null)
                {
                    voiceLaunchItem.setGravity(Gravity.RIGHT);
                    horzLayout.rightMargin = margin;
                }

                vertFrame.removeAllViews();
                horzFrame.addView(peopleView);

                peopleLayout.width = Simple.WC;
                peopleLayout.height = Simple.MP;
                peopleView.setOrientation(LinearLayout.HORIZONTAL);

                orientation = Configuration.ORIENTATION_PORTRAIT;
            }

            if ((orientation != Configuration.ORIENTATION_LANDSCAPE) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_LANDSCAPE))
            {
                int height = ((View) getParent()).getHeight();
                int items = ((int) Math.floor(height / layoutSize)) - 1;
                int margin = (height - (items * layoutSize)) / 2;

                layoutParams.width = layoutSize;
                layoutParams.height = Simple.MP;
                layoutParams.gravity = Gravity.RIGHT;

                if (alertLaunchItem != null)
                {
                    alertLaunchItem.setGravity(Gravity.TOP);
                    vertLayout.topMargin = margin;
                }

                if (voiceLaunchItem != null)
                {
                    voiceLaunchItem.setGravity(Gravity.BOTTOM);
                    vertLayout.bottomMargin = margin;
                }

                horzFrame.removeAllViews();
                vertFrame.addView(peopleView);

                peopleLayout.width = Simple.MP;
                peopleLayout.height = Simple.WC;
                peopleView.setOrientation(LinearLayout.VERTICAL);

                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Simple.makePost(changeOrientation);
    }
}
