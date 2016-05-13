package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private ScrollView friendsFrame;
    private LinearLayout.LayoutParams peopleLayout;
    private LinearLayout peopleList;

    private LayoutParams friendsLayout;

    public HomeBottom(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(Simple.MP, Simple.MP);
        setLayoutParams(layoutParams);

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

        friendsLayout = new LayoutParams(Simple.MP, Simple.MP);
        friendsFrame = new ScrollView(context);
        friendsFrame.setLayoutParams(friendsLayout);
        this.addView(friendsFrame);

        peopleLayout = new LinearLayout.LayoutParams(Simple.WC, Simple.WC);
        peopleList = new LinearLayout(context);
        peopleList.setLayoutParams(peopleLayout);
        friendsFrame.addView(peopleList);
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
        peopleList.removeAllViews();

        extractConfig(config);

        for (int inx = 0; inx < contacts.length(); inx++)
        {
            JSONObject contact = Json.getObject(contacts, inx);
            if (contact == null) continue;

            LaunchItem li = LaunchItem.createLaunchItem(getContext(), null, contact);

            li.setFrameLess();
            li.setSize(layoutSize, layoutSize);

            peopleList.addView(li);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT)
        {
            layoutParams.width = Simple.MP;
            layoutParams.height = layoutSize;
            layoutParams.gravity = Gravity.BOTTOM;

            friendsLayout.leftMargin = 0;
            friendsLayout.rightMargin = 0;
            friendsLayout.topMargin = 0;
            friendsLayout.bottomMargin = 0;

            if (alertLaunchItem != null)
            {
                alertLaunchItem.setGravity(Gravity.LEFT);
                friendsLayout.rightMargin = layoutSize;
            }

            if (voiceLaunchItem != null)
            {
                voiceLaunchItem.setGravity(Gravity.RIGHT);
                friendsLayout.leftMargin = layoutSize;
            }

            peopleLayout.width = Simple.WC;
            peopleLayout.height = Simple.MP;
            peopleList.setOrientation(LinearLayout.HORIZONTAL);
        }
        else
        {
            layoutParams.width = layoutSize;
            layoutParams.height = Simple.MP;
            layoutParams.gravity = Gravity.RIGHT;

            friendsLayout.leftMargin = 0;
            friendsLayout.rightMargin = 0;
            friendsLayout.topMargin = 0;
            friendsLayout.bottomMargin = 0;

            if (alertLaunchItem != null)
            {
                alertLaunchItem.setGravity(Gravity.TOP);
                friendsLayout.topMargin = layoutSize;
            }

            if (voiceLaunchItem != null)
            {
                voiceLaunchItem.setGravity(Gravity.BOTTOM);
                friendsLayout.bottomMargin = layoutSize;
            }

            peopleLayout.width = Simple.MP;
            peopleLayout.height = Simple.WC;
            peopleList.setOrientation(LinearLayout.VERTICAL);
        }
    }
}
