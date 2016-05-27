package de.xavaro.android.safehome;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.ProfileImages;
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

                if (liconfig.has("launchitems"))
                {
                    subscribeLaunchItems(liconfig, subscribe);
                    continue;
                }

                if (subscribe)
                {
                    LaunchItem.subscribeNotification(liconfig, onNotification);
                }
                else
                {
                    LaunchItem.unsubscribeNotification(liconfig, onNotification);
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

                if (liconfig.has("launchitems"))
                {
                    countEventsLaunchItems(liconfig, mainitemkey);
                    continue;
                }

                String subitemkey = LaunchItem.getNotificationKey(liconfig);

                if (! dupscount.contains(subitemkey))
                {
                    dupscount.add(subitemkey);

                    int subnews = LaunchItem.getNotificationCount(liconfig);

                    if (subnews >= 0)
                    {
                        totalnews += subnews;

                        if (Simple.equals(mainitemkey, subitemkey))
                        {
                            mainnews += subnews;
                        }

                        //
                        // Register or unregister the notification intent.
                        //

                        NotifyIntent intent = new NotifyIntent();

                        intent.key = subitemkey;

                        if (subnews == 0)
                        {
                            NotifyManager.removeNotification(intent);
                        }
                        else
                        {
                            String type = Json.getString(liconfig, "type");
                            String pfid = LaunchItem.getSubscribeNotificationPfid(liconfig);

                            boolean issocial;
                            File profile;

                            if (Simple.equals(type, "twitter") ||
                                    Simple.equals(type, "facebook") ||
                                    Simple.equals(type, "instagram") ||
                                    Simple.equals(type, "googleplus"))
                            {
                                profile = ProfileImages.getSocialUserImageFile(type, pfid);
                                issocial = true;
                            }
                            else
                            {
                                profile = ProfileImages.getProfileFile(pfid);
                                issocial = false;
                            }

                            if (profile == null) profile = ProfileImages.getAnonProfileFile();

                            int typeresid = LaunchItem.getSubscribeNotificationMessage(liconfig);

                            String howmuch = "" + subnews;
                            String who = Json.getString(liconfig, "label");

                            int whereid = R.string.notify_where_phone;

                            if (Simple.equals(type, "twitter")) whereid = R.string.notify_where_twitter;
                            if (Simple.equals(type, "facebook")) whereid = R.string.notify_where_facebook;
                            if (Simple.equals(type, "instagram")) whereid = R.string.notify_where_instagram;
                            if (Simple.equals(type, "googleplus")) whereid = R.string.notify_where_googleplus;

                            if (Simple.equals(type, "phone")) whereid = R.string.notify_where_phone;
                            if (Simple.equals(type, "skype")) whereid = R.string.notify_where_skype;
                            if (Simple.equals(type, "xavaro")) whereid = R.string.notify_where_xavaro;
                            if (Simple.equals(type, "whatsapp")) whereid = R.string.notify_where_whatsapp;

                            String where = Simple.getTrans(whereid);
                            String message;

                            if (issocial)
                            {
                                if (subnews == 1)
                                {
                                    message = Simple.getTrans(R.string.notify_social_news_singular,
                                            who, where);
                                }
                                else
                                {
                                    message = Simple.getTrans(R.string.notify_social_news_plural,
                                            howmuch, who, where);
                                }
                            }
                            else
                            {
                                if (typeresid == R.string.simple_call)
                                {
                                    if (subnews == 1)
                                    {
                                        message = Simple.getTrans(R.string.notify_friend_call_singular,
                                                who, where);
                                    }
                                    else
                                    {
                                        message = Simple.getTrans(R.string.notify_friend_call_plural,
                                                howmuch, who, where);
                                    }
                                }
                                else
                                {
                                    if (subnews == 1)
                                    {
                                        message = Simple.getTrans(R.string.notify_friend_message_singular,
                                                who, where);
                                    }
                                    else
                                    {
                                        message = Simple.getTrans(R.string.notify_friend_message_plural,
                                                howmuch, who, where);
                                    }
                                }
                            }

                            intent.title = message;
                            intent.followText = "Aufrufen";
                            intent.declineText = "Wegmachen";
                            intent.iconpath = profile.toString();
                            intent.iconcircle = ! issocial;

                            NotifyManager.addNotification(intent);
                        }
                    }
                }
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
