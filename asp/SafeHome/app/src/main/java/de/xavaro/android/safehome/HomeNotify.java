package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeNotify extends HomeFrame
{
    private static final String LOGTAG = HomeNotify.class.getSimpleName();

    protected LinearLayout contentFrame;

    protected HomeEvent topFrame;
    protected HomeEvent event1Frame;
    protected HomeEvent event2Frame;

    protected LaunchItem topLaunch;
    protected LaunchItem event1Launch;
    protected LaunchItem event2Launch;

    protected int padh = Simple.getDevicePixels(16);
    protected int padv = Simple.getDevicePixels(4);

    protected final ArrayList<LaunchItem> candidatesLaunch = new ArrayList<>();

    public HomeNotify(Context context)
    {
        super(context);

        layoutParams.width = Simple.MP;
        layoutParams.height = notifySize - 16;
        layoutParams.gravity = Gravity.TOP;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = 8;
        layoutParams.rightMargin = isPortrait() ? 8 : peopleSize;
        layoutParams.bottomMargin = 8;

        layoutNormal = new LayoutParams(layoutParams);

        //
        // Notify content areas.
        //

        setDisablefullscreen();

        LayoutParams barFrameLayout = new LayoutParams(Simple.MP, Simple.WC);
        LayoutParams barRulerLayout = new LayoutParams(Simple.MP, Simple.getDevicePixels(2));
        FrameLayout barFrame;
        FrameLayout barRuler;

        contentFrame = new LinearLayout(context);
        contentFrame.setOrientation(LinearLayout.VERTICAL);
        payloadFrame.addView(contentFrame);

        topFrame = new HomeEvent(context);
        contentFrame.addView(topFrame);

        barFrame = new FrameLayout(context);
        barFrame.setLayoutParams(barFrameLayout);
        barFrame.setPadding(padh, padv, padh, padv);
        barRuler = new FrameLayout(context);
        barRuler.setLayoutParams(barRulerLayout);
        barRuler.setBackgroundColor(0xffcccccc);
        barFrame.addView(barRuler);
        contentFrame.addView(barFrame);

        event1Frame = new HomeEvent(context);
        contentFrame.addView(event1Frame);

        barFrame = new FrameLayout(context);
        barFrame.setLayoutParams(barFrameLayout);
        barFrame.setPadding(padh, padv, padh, padv);
        barRuler = new FrameLayout(context);
        barRuler.setBackgroundColor(0xffcccccc);
        barRuler.setLayoutParams(barRulerLayout);
        barFrame.addView(barRuler);
        contentFrame.addView(barFrame);

        event2Frame = new HomeEvent(context);
        contentFrame.addView(event2Frame);
    }

    public void setConfig(JSONObject config)
    {
        extractConfig(config);
    }

    private void extractConfig(JSONObject config)
    {
        JSONArray lis = Json.getArray(config, "launchitems");
        if (lis == null) return;

        for (int inx = 0; inx < lis.length(); inx++)
        {
            JSONObject li = Json.getObject(lis, inx);

            if (Json.has(li, "launchitems"))
            {
                extractConfig(li);
                continue;
            }

            String type = Json.getString(li, "type");
            String subitem = Json.getString(li, "subitem");

            if (Simple.equals(type, "beta") ||
                    Simple.equals(type, "today") ||
                    Simple.equals(type, "battery") ||
                    (Simple.equals(type, "calls") && Simple.equals(subitem, "prepaid")))
            {
                LaunchItem launchItem = LaunchItem.createLaunchItem(getContext(), null, li);
                launchItem.setFrameLess();
                candidatesLaunch.add(launchItem);
                lis.remove(inx--);
            }
        }
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        Simple.makePost(manageNotificationsRun, 1000);
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        Simple.removePost(manageNotificationsRun);
    }

    @Override
    protected void onChangeOrientation()
    {
        layoutNormal.height = notifySize - 16;
        layoutNormal.rightMargin = isPortrait() ? 8 : peopleSize;

        if (! fullscreen)
        {
            layoutParams.height = layoutNormal.height;
            layoutParams.rightMargin = layoutNormal.rightMargin;
        }

        //
        // Get current height minus rulers.
        //

        int size = (payloadFrame.getHeight() + ((padv + padv + 2)  * 2)) / 4;

        //
        // Important: Only set dimensions if different from
        // actual state because it will fire a chain of new
        // resize events if not.
        //

        if (topFrame.getLayoutHeight() != (size * 2))
        {
            topFrame.setLayoutHeight(size * 2);
            if (topLaunch != null) topLaunch.setSize(size * 2, size * 2);

            event1Frame.setLayoutHeight(size);
            if (event1Launch != null) event1Launch.setSize(size, size);

            event2Frame.setLayoutHeight(size);
            if (event2Launch != null) event2Launch.setSize(size, size);
        }
    }

    protected void manageNotifications()
    {
        if (candidatesLaunch.size() == 0) return;

        int size = payloadFrame.getHeight() / 4;

        if (topLaunch != null) Simple.removeFromParent(topLaunch);

        topLaunch = candidatesLaunch.remove(0);
        topLaunch.setSize(size * 2, size * 2);
        candidatesLaunch.add(topLaunch);

        topFrame.addView(topLaunch);
    }

    protected final Runnable manageNotificationsRun = new Runnable()
    {
        @Override
        public void run()
        {
            manageNotifications();

            Simple.makePost(manageNotificationsRun, 5000);
        }
    };
}
