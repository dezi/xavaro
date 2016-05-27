package de.xavaro.android.safehome;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

public class LaunchItemNotify extends LaunchItem
{
    private final static String LOGTAG = LaunchItemNotify.class.getSimpleName();

    public LaunchItemNotify(Context context)
    {
        super(context);
    }

    protected final ArrayList<String> dupscount = new ArrayList<>();

    protected int mainnews;
    protected int totalnews;

    private void subscribeLaunchItems(JSONObject config, boolean subscribe)
    {
        JSONArray launchItems = Json.getArray(config, "launchitems");

        if (launchItems != null)
        {
            for (int inx = 0; inx < launchItems.length(); inx++)
            {
                JSONObject liconfig = Json.getObject(launchItems, inx);
                if (liconfig == null) continue;

                if (subscribe)
                {
                    LaunchItem.subscribeNotification(liconfig, onNotification);
                }
                else
                {
                    LaunchItem.unsubscribeNotification(liconfig, onNotification);
                }

                if (liconfig.has("launchitems"))
                {
                    subscribeLaunchItems(liconfig, subscribe);
                }
            }
        }
    }

    private void countEventsLaunchItems(JSONObject config, String mainitemkey)
    {
        JSONArray launchItems = Json.getArray(config, "launchitems");

        if (launchItems != null)
        {
            for (int inx = 0; inx < launchItems.length(); inx++)
            {
                JSONObject liconfig = Json.getObject(launchItems, inx);
                if (liconfig == null) continue;

                String subitemkey = LaunchItem.getNotificationKey(liconfig);

                if (! dupscount.contains(subitemkey))
                {
                    dupscount.add(subitemkey);

                    int subnews = LaunchItem.getNotificationCount(liconfig);
                    if (subnews < 0) continue;

                    totalnews += subnews;

                    if (Simple.equals(mainitemkey, subitemkey))
                    {
                        mainnews += subnews;
                    }
                }

                if (liconfig.has("launchitems")) countEventsLaunchItems(liconfig, mainitemkey);
            }
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        if (config.has("launchitems"))
        {
            subscribeLaunchItems(config, true);
        }
        else
        {
            if (config.has("pfid") && !isNoFunction())
            {
                LaunchItem.subscribeNotification(config, onNotification);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if (config.has("launchitems"))
        {
            subscribeLaunchItems(config, false);
        }
        else
        {
            if (config.has("pfid") && !isNoFunction())
            {
                LaunchItem.unsubscribeNotification(config, onNotification);
            }
        }
    }

    protected final Runnable onNotification = new Runnable()
    {
        @Override
        public void run()
        {
            if (config.has("launchitems"))
            {
                mainnews = 0;
                totalnews = 0;
                dupscount.clear();

                String mainitemkey = LaunchItem.getNotificationKey(config);
                countEventsLaunchItems(config, mainitemkey);

                if (totalnews >= 0)
                {
                    int mesgresid = LaunchItem.getSubscribeNotificationMessage(config);

                    if (totalnews != mainnews)
                    {
                        //
                        // We have notifications from different areas.
                        // Adjust message to be "news".
                        //

                        mesgresid = R.string.simple_news;
                    }

                    String message = totalnews + " " + Simple.getTrans(
                            (totalnews == 1) ? mesgresid : Simple.getPlural(mesgresid));

                    notifyText.setText(message);
                    notifyText.setVisibility((totalnews == 0) ? GONE : VISIBLE);
                }
            }
            else
            {
                int count = LaunchItem.getNotificationCount(config);

                if (count >= 0)
                {
                    int mesgresid = LaunchItem.getSubscribeNotificationMessage(config);

                    String message = count + " " + Simple.getTrans(
                            (count == 1) ? mesgresid : Simple.getPlural(mesgresid));

                    notifyText.setText(message);
                    notifyText.setVisibility((count == 0) ? GONE : VISIBLE);
                }
            }
        }
    };
}
