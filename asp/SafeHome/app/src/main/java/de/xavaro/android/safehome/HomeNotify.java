package de.xavaro.android.safehome;

import android.support.annotation.Nullable;
import android.annotation.SuppressLint;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeNotify extends HomeFrame
{
    private static final String LOGTAG = HomeNotify.class.getSimpleName();

    private static HomeNotify instance;

    public static HomeNotify getInstance()
    {
        return instance;
    }

    protected LinearLayout contentFrame;

    protected ArrayList<HomeEvent> slots = new ArrayList<>();

    protected HomeEvent topFrame;
    protected HomeEvent event1Frame;
    protected HomeEvent event2Frame;

    protected int padh = Simple.getDevicePixels(Simple.isTablet() ? 16 : 8);
    protected int padv = Simple.getDevicePixels(Simple.isTablet() ?  4 : 2);
    protected int size;
    protected int tops;

    protected final ArrayList<LaunchItem> candidatesLaunch = new ArrayList<>();

    public HomeNotify(Context context)
    {
        super(context);

        instance = this;

        Log.d(LOGTAG,"HomeNotify: creating.");

        layoutParams.width = Simple.MP;
        layoutParams.height = HomeActivity.notifySize - 16;
        layoutParams.gravity = Gravity.TOP;

        layoutParams.leftMargin = Simple.DP(8);
        layoutParams.topMargin = Simple.DP(8);
        layoutParams.rightMargin = Simple.isPortrait() ? Simple.DP(8) : HomeActivity.peopleSize;
        layoutParams.bottomMargin = Simple.DP(8);

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
        slots.add(topFrame);

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
        slots.add(event1Frame);

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
        slots.add(event2Frame);

        CommonConfigs.UpdateNotifications = new Runnable()
        {
            @Override
            public void run()
            {
                manageNotifications();
            }
        };
    }

    public void setConfig(JSONObject config)
    {
        candidatesLaunch.clear();

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

                if (launchItem instanceof NotifyIntent.NotifyService)
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

        Simple.removePost(manageNotificationsRun);
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
        layoutNormal.height = HomeActivity.notifySize - Simple.DP(16);
        layoutNormal.rightMargin = Simple.isPortrait() ? Simple.DP(8) : HomeActivity.peopleSize;

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

            for (int inx = 1; inx < slots.size(); inx++)
            {
                slots.get(inx).setLayoutHeight(size);
            }
        }
    }

    public void onManageNotifications()
    {
        Simple.removePost(manageNotificationsRun);
        Simple.makePost(manageNotificationsRun, 100);
    }

    @Nullable
    protected NotifyIntent getNextIntent(ArrayList<NotifyIntent> intents)
    {
        int bestIndex = -1;
        int bestLevel = -1;

        String bestDate = null;

        for (int inx = 0; inx < intents.size(); inx++)
        {
            NotifyIntent intent = intents.get(inx);

            if ((intent.checkCondition != null) &&
                    ! intent.checkCondition.onCheckNotifyCondition(intent))
            {
                //
                // Intent is not required anymore.
                //

                NotifyManager.removeNotification(intent);
                intents.remove(inx--);

                continue;
            }

            if ((intent.importance >= bestLevel) &&
                ((bestDate == null) || (Simple.compareTo(intent.dst, bestDate) > 0)))
            {
                bestIndex = inx;
                bestLevel = intent.importance;
                bestDate = intent.dst;
            }
        }

        return (bestIndex >= 0) ? intents.remove(bestIndex) : null;
    }

    protected void manageNotifications()
    {
        synchronized (LOGTAG)
        {
            ArrayList<NotifyIntent> intents = new ArrayList<>();

            //
            // Collect pending intents.
            //

            ArrayList<NotifyIntent> pendings = NotifyManager.getPendingIntents();
            for (NotifyIntent pending : pendings) intents.add(pending);

            //
            // Add notify intents from all lauch items.
            //

            for (LaunchItem launchItem : candidatesLaunch)
            {
                NotifyIntent intent = ((NotifyIntent.NotifyService) launchItem).onGetNotifiyIntent();
                if (intent == null) continue;

                intent.iconFrame = launchItem;
                intents.add(intent);
            }

            //
            // Display best candidates.
            //

            for (int inx = 0; inx < slots.size(); inx++)
            {
                NotifyIntent intent = getNextIntent(intents);
                slots.get(inx).setNotifyIntent(intent);
            }
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
