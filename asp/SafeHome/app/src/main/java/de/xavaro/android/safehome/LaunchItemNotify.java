package de.xavaro.android.safehome;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

public class LaunchItemNotify extends LaunchItem
{
    private final static String LOGTAG = LaunchItemNotify.class.getSimpleName();

    public LaunchItemNotify(Context context)
    {
        super(context);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        if (config.has("launchitems"))
        {
            JSONArray launchItems = Json.getArray(config, "launchitems");

            if (launchItems != null)
            {
                for (int inx = 0; inx < launchItems.length(); inx++)
                {
                    JSONObject liconfig = Json.getObject(launchItems, inx);
                    if (liconfig == null) continue;

                    LaunchItem.subscribeNotification(liconfig, onNotification);
                }
            }
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
            JSONArray launchItems = Json.getArray(config, "launchitems");

            if (launchItems != null)
            {
                for (int inx = 0; inx < launchItems.length(); inx++)
                {
                    JSONObject liconfig = Json.getObject(launchItems, inx);
                    if (liconfig == null) continue;

                    LaunchItem.unsubscribeNotification(liconfig, onNotification);
                }
            }
        }
        else
        {
            if (config.has("pfid") && !isNoFunction())
            {
                LaunchItem.unsubscribeNotification(config, onNotification);
            }
        }
    }

    protected int mainnews;
    protected int totalnews;

    protected final Runnable onNotification = new Runnable()
    {
        @Override
        public void run()
        {
            if (config.has("launchitems"))
            {
                JSONArray launchItems = Json.getArray(config, "launchitems");

                if (launchItems != null)
                {
                    String mainitemkey = LaunchItem.getNotificationKey(config);

                    mainnews = 0;
                    totalnews = 0;

                    for (int inx = 0; inx < launchItems.length(); inx++)
                    {
                        JSONObject liconfig = Json.getObject(launchItems, inx);
                        if (liconfig == null) continue;

                        int subnews = LaunchItem.getNotificationCount(liconfig);
                        if (subnews < 0) continue;

                        totalnews += subnews;

                        String subitemkey = LaunchItem.getNotificationKey(liconfig);

                        if (Simple.equals(mainitemkey, subitemkey))
                        {
                            mainnews += subnews;
                        }
                    }

                    if (totalnews >= 0)
                    {
                        String message = totalnews + " " + Simple.getTrans((totalnews == 1)
                                ? R.string.simple_news
                                : R.string.simple_newss);

                        notifyText.setText(message);
                        notifyText.setVisibility((totalnews == 0) ? GONE : VISIBLE);
                    }
                }
            }
            else
            {
                int count = LaunchItem.getNotificationCount(config);

                if (count >= 0)
                {
                    String message = count + " " + Simple.getTrans((count == 1)
                            ? R.string.simple_news
                            : R.string.simple_newss);

                    notifyText.setText(message);
                    notifyText.setVisibility((count == 0) ? GONE : VISIBLE);
                }
            }
        }
    };
}
