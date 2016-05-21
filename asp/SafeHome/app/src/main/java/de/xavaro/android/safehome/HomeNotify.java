package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyIntent;
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
    protected int size;
    protected int tops;

    protected final ArrayList<LaunchItem> candidatesLaunch = new ArrayList<>();

    public HomeNotify(Context context)
    {
        super(context);

        Log.d(LOGTAG,"HomeNotify: creating.");

        layoutParams.width = Simple.MP;
        layoutParams.height = notifySize - 16;
        layoutParams.gravity = Gravity.TOP;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = 8;
        layoutParams.rightMargin = Simple.isPortrait() ? 8 : peopleSize;
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

        topFrame = new HomeEvent(context, true);
        contentFrame.addView(topFrame);

        barFrame = new FrameLayout(context);
        barFrame.setLayoutParams(barFrameLayout);
        barFrame.setPadding(padh, padv, padh, padv);
        barRuler = new FrameLayout(context);
        barRuler.setLayoutParams(barRulerLayout);
        barRuler.setBackgroundColor(0xffcccccc);
        barFrame.addView(barRuler);
        contentFrame.addView(barFrame);

        event1Frame = new HomeEvent(context, false);
        contentFrame.addView(event1Frame);

        barFrame = new FrameLayout(context);
        barFrame.setLayoutParams(barFrameLayout);
        barFrame.setPadding(padh, padv, padh, padv);
        barRuler = new FrameLayout(context);
        barRuler.setBackgroundColor(0xffcccccc);
        barRuler.setLayoutParams(barRulerLayout);
        barFrame.addView(barRuler);
        contentFrame.addView(barFrame);

        event2Frame = new HomeEvent(context, false);
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

            String notify = Json.getString(li, "notify");

            if (Simple.equals(notify, "only") || Simple.equals(notify, "both"))
            {
                LaunchItem launchItem = LaunchItem.createLaunchItem(getContext(), null, li);

                if (launchItem instanceof NotifyIntent.NotifiyService)
                {
                    launchItem.setFrameLess(true);
                    launchItem.setTextLess(true);
                    candidatesLaunch.add(launchItem);
                }

                if (Simple.equals(notify, "only")) lis.remove(inx--);
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
        layoutNormal.rightMargin = Simple.isPortrait() ? 8 : peopleSize;

        if (! fullscreen)
        {
            layoutParams.height = layoutNormal.height;
            layoutParams.rightMargin = layoutNormal.rightMargin;
        }

        //
        // Get current height minus rulers.
        //

        if (Simple.isPortrait())
        {
            size = (payloadFrame.getHeight() - (padv + padv + 2) * 2) / 4;
            tops = size * 2 + size % 4;
        }
        else
        {
            size = (payloadFrame.getHeight() - (padv + padv + 2) * 2) / 4;
            tops = size * 2 + size % 4;
        }

        //
        // Important: Only set dimensions if different from
        // actual state because it will fire a chain of new
        // resize events if not.
        //

        if (topFrame.getLayoutHeight() != tops)
        {
            topFrame.setLayoutHeight(tops);
            if (topLaunch != null) topLaunch.setSize(tops, tops);

            event1Frame.setLayoutHeight(size);
            if (event1Launch != null) event1Launch.setSize(size, size);

            event2Frame.setLayoutHeight(size);
            if (event2Launch != null) event2Launch.setSize(size, size);
        }
    }

    protected LaunchItem setupNotification(LaunchItem launchItem, HomeEvent eventFrame)
    {
        eventFrame.setNotifyIntent(null);
        if (launchItem == null) return null;

        NotifyIntent intent = ((NotifyIntent.NotifiyService) launchItem).onGetNotifiyIntent();

        if (intent == null)
        {
            candidatesLaunch.add(launchItem);
            return null;
        }

        launchItem.setSize(eventFrame.getHeight(), eventFrame.getHeight());

        eventFrame.setNotifyIntent(intent);
        eventFrame.addView(launchItem);

        return launchItem;
    }

    @Nullable
    protected LaunchItem getNextCandidate()
    {
        int count = candidatesLaunch.size();

        for (int inx = 0; inx < count; inx++)
        {
            LaunchItem launchItem = candidatesLaunch.remove(0);

            NotifyIntent intent = ((NotifyIntent.NotifiyService) launchItem).onGetNotifiyIntent();
            if (intent != null) return launchItem;

            candidatesLaunch.add(launchItem);
        }

        return null;
    }

    protected void manageNotifications()
    {
        if (topLaunch != null)
        {
            Simple.removeFromParent(topLaunch);
            topFrame.setNotifyIntent(null);
            candidatesLaunch.add(topLaunch);
            topLaunch = null;
        }

        if (event1Launch != null)
        {
            Simple.removeFromParent(event1Launch);
            topLaunch = setupNotification(event1Launch, topFrame);
            event1Launch = null;
        }

        if (event2Launch != null)
        {
            Simple.removeFromParent(event2Launch);
            event1Launch = setupNotification(event2Launch, event1Frame);
            event2Launch = null;
        }

        if (topLaunch == null)
        {
            topLaunch = setupNotification(getNextCandidate(), topFrame);
        }

        if (event1Launch == null)
        {
            event1Launch = setupNotification(getNextCandidate(), event1Frame);
        }

        if (event2Launch == null)
        {
            event2Launch = setupNotification(getNextCandidate(), event2Frame);
        }
    }

    protected final Runnable manageNotificationsRun = new Runnable()
    {
        @Override
        public void run()
        {
            manageNotifications();

            Simple.makePost(manageNotificationsRun, 60 * 1000);
        }
    };
}
