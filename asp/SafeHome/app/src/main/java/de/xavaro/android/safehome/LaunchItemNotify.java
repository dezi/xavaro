package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import de.xavaro.android.common.NotificationService;
import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.SimpleStorage;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

public class LaunchItemNotify extends LaunchItem
{
    private final static String LOGTAG = LaunchItemNotify.class.getSimpleName();

    protected final ArrayList<String> dupscount = new ArrayList<>();

    protected int mainnews;
    protected int totalnews;

    public LaunchItemNotify(Context context)
    {
        super(context);
    }

    protected String getSubscribeNotificationType(JSONObject config)
    {
        String typetag = null;

        String type = Json.getString(config, "type");
        String subtype = Json.getString(config, "subtype");

        if (Simple.equals(type, "phone"))
        {
            if (Simple.equals(subtype, "text")) typetag = "smsmms";
            if (Simple.equals(subtype, "voip")) typetag = "phonecall";
        }

        if (Simple.equals(type, "skype") ||
                Simple.equals(type, "xavaro") ||
                Simple.equals(type, "whatsapp") ||
                Simple.equals(type, "twitter") ||
                Simple.equals(type, "facebook") ||
                Simple.equals(type, "instagram") ||
                Simple.equals(type, "googleplus"))
        {
            typetag = type;
        }

        return typetag;
    }

    protected String getSubscribeNotificationPfid(JSONObject config)
    {
        String pfidtag = null;

        String type = Json.getString(config, "type");

        if (Simple.equals(type, "phone")) pfidtag = Json.getString(config, "phonenumber");
        if (Simple.equals(type, "skype")) pfidtag = Json.getString(config, "skypename");
        if (Simple.equals(type, "xavaro")) pfidtag = Json.getString(config, "identity");
        if (Simple.equals(type, "whatsapp")) pfidtag = Json.getString(config, "waphonenumber");

        if (Simple.equals(type, "twitter") ||
                Simple.equals(type, "facebook") ||
                Simple.equals(type, "instagram") ||
                Simple.equals(type, "googleplus"))
        {
            pfidtag = Json.getString(config, "pfid");;
        }

        return pfidtag;
    }

    protected void subscribeNotification(JSONObject config, Runnable callback)
    {
        String typetag = getSubscribeNotificationType(config);
        String pfidtag = getSubscribeNotificationPfid(config);

        if ((typetag == null) || (pfidtag == null)) return;

        NotificationService.subscribe(typetag, pfidtag, callback);
    }

    protected void unsubscribeNotification(JSONObject config, Runnable callback)
    {
        String typetag = getSubscribeNotificationType(config);
        String pfidtag = getSubscribeNotificationPfid(config);

        if ((typetag == null) || (pfidtag == null)) return;

        NotificationService.unsubscribe(typetag, pfidtag, callback);
    }

    @Nullable
    protected String getNotificationKey(JSONObject config)
    {
        String typetag = getSubscribeNotificationType(config);
        String pfidtag = getSubscribeNotificationPfid(config);

        if ((typetag == null) || (pfidtag == null)) return null;

        return typetag + "." + pfidtag;
    }

    protected int getSubscribeNotificationMessage(JSONObject config)
    {
        String typetag = getSubscribeNotificationType(config);

        int singular = R.string.simple_call;

        if (typetag != null)
        {
            switch (typetag)
            {
                case "smsmms":
                case "xavaro":
                case "whatsapp":
                    singular = R.string.simple_message;
                    break;

                case "twitter":
                case "facebook":
                case "instagram":
                case "googleplus":
                    singular = R.string.simple_news;
                    break;
            }
        }

        return singular;
    }

    protected int getNotificationCount(JSONObject config)
    {
        String typetag = getSubscribeNotificationType(config);
        String pfidtag = getSubscribeNotificationPfid(config);

        if ((typetag == null) || (pfidtag == null)) return -1;

        if (Simple.equals(typetag, "twitter") ||
                Simple.equals(typetag, "facebook") ||
                Simple.equals(typetag, "instagram") ||
                Simple.equals(typetag, "googleplus"))
        {
            return SimpleStorage.getInt("socialfeednews", typetag + ".count." + pfidtag);
        }

        return SimpleStorage.getInt("notifications", typetag + ".count." + pfidtag);
    }

    protected String getNotificationStamp(JSONObject config)
    {
        String typetag = getSubscribeNotificationType(config);
        String pfidtag = getSubscribeNotificationPfid(config);

        if ((typetag == null) || (pfidtag == null)) return null;

        if (Simple.equals(typetag, "twitter") ||
                Simple.equals(typetag, "facebook") ||
                Simple.equals(typetag, "instagram") ||
                Simple.equals(typetag, "googleplus"))
        {
            return SimpleStorage.getString("socialfeednews", typetag + ".stamp." + pfidtag);
        }

        return SimpleStorage.getString("notifications", typetag + ".stamp." + pfidtag);
    }

    protected void resetNotificationCount(JSONObject config)
    {
        String typetag = getSubscribeNotificationType(config);
        String pfidtag = getSubscribeNotificationPfid(config);

        if ((typetag == null) || (pfidtag == null)) return;

        if (Simple.equals(typetag, "twitter") ||
                Simple.equals(typetag, "facebook") ||
                Simple.equals(typetag, "instagram") ||
                Simple.equals(typetag, "googleplus"))
        {
            SimpleStorage.put("socialfeednews", typetag + ".count." + pfidtag, 0);
        }
        else
        {
            SimpleStorage.put("notifications", typetag + ".count." + pfidtag, 0);
        }

        NotificationService.doCallbacks(typetag, pfidtag);
    }

    protected void showLaunchItemInWorker(JSONObject config)
    {
        LaunchItem launchItem = LaunchItem.createLaunchItem(context, null, config);
        launchItem.onMyClick();

        resetNotificationCount(config);
    }

    protected void subscribeLaunchItems(JSONObject config, boolean subscribe)
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
                    subscribeNotification(liconfig, onNotification);
                }
                else
                {
                    unsubscribeNotification(liconfig, onNotification);
                }
            }
        }
    }

    protected void countEventsLaunchItems(JSONObject config, String mainitemkey)
    {
        JSONArray launchItems = Json.getArray(config, "launchitems");

        if (launchItems != null)
        {
            for (int inx = 0; inx < launchItems.length(); inx++)
            {
                final JSONObject liconfig = Json.getObject(launchItems, inx);
                if (liconfig == null) continue;

                if (liconfig.has("launchitems"))
                {
                    countEventsLaunchItems(liconfig, mainitemkey);
                    continue;
                }

                String subitemkey = getNotificationKey(liconfig);

                if (! dupscount.contains(subitemkey))
                {
                    dupscount.add(subitemkey);

                    int subnews = getNotificationCount(liconfig);

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
                            String pfid = getSubscribeNotificationPfid(liconfig);

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

                            int typeresid = getSubscribeNotificationMessage(liconfig);

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

                            intent.dst = getNotificationStamp(liconfig);
                            intent.title = message;
                            intent.followText = "Aufrufen";
                            intent.declineText = "Ignorieren";
                            intent.iconpath = profile.toString();
                            intent.iconcircle = ! issocial;
                            intent.importance = NotifyIntent.REMINDER;

                            intent.followRunner = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    showLaunchItemInWorker(liconfig);
                                }
                            };

                            intent.declineRunner = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    resetNotificationCount(liconfig);
                                }
                            };

                            NotifyManager.addNotification(intent);

                            HomeNotify.getInstance().onManageNotifications();
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
                subscribeNotification(config, onNotification);
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
                unsubscribeNotification(config, onNotification);
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

                String mainitemkey = getNotificationKey(config);
                countEventsLaunchItems(config, mainitemkey);

                if (totalnews >= 0)
                {
                    int mesgresid = getSubscribeNotificationMessage(config);

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
                int count = getNotificationCount(config);

                if (count >= 0)
                {
                    int mesgresid = getSubscribeNotificationMessage(config);

                    String message = count + " " + Simple.getTrans(
                            (count == 1) ? mesgresid : Simple.getPlural(mesgresid));

                    notifyText.setText(message);
                    notifyText.setVisibility((count == 0) ? GONE : VISIBLE);
                }
            }
        }
    };
}
