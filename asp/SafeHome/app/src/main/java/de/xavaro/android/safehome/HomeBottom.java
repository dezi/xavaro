package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeBottom extends FrameLayout
{
    private int layoutSize;

    private LayoutParams layoutParams;

    private LaunchItem alertLaunchItem;
    private LaunchItem voiceLaunchItem;
    private FrameLayout friendsFrame;

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
        friendsFrame = new FrameLayout(context);
        friendsFrame.setLayoutParams(friendsLayout);
        this.addView(friendsFrame);
    }

    public void setSize(int pixels)
    {
        layoutSize = pixels;

        if (alertLaunchItem != null) alertLaunchItem.setSize(layoutSize, layoutSize);
        if (voiceLaunchItem != null) voiceLaunchItem.setSize(layoutSize, layoutSize);
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
        }
    }
}
