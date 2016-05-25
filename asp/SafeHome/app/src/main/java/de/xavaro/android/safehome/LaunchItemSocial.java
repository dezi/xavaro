package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.NotificationService;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SimpleStorage;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.SocialGoogleplus;
import de.xavaro.android.common.SocialInstagram;
import de.xavaro.android.common.SocialTwitter;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.Json;

public class LaunchItemSocial extends LaunchItem
{
    private final static String LOGTAG = LaunchItemSocial.class.getSimpleName();

    public LaunchItemSocial(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        boolean isFunc = ! Json.getBoolean(config, "nofunc");

        ImageView targetIcon = icon;

        if (config.has("pfid"))
        {
            String pfid = Json.getString(config, "pfid");
            File profile = ProfileImages.getSocialUserImageFile(type, pfid);

            if ((profile != null) && ! isNoProfile())
            {
                icon.setImageResource(profile.toString(), false);
                overlay.setVisibility(VISIBLE);
                targetIcon = overicon;
            }
        }

        if (type.equals("social"))
        {
            targetIcon.setImageResource(CommonConfigs.IconResSocial);
        }

        if (type.equals("twitter"))
        {
            platformName = SocialTwitter.getInstance().getPlatformName();
            if (isFunc) targetIcon.setImageResource(CommonConfigs.IconResSocialTwitter);
            if (isNoProfile()) labelText = platformName;
        }

        if (type.equals("facebook"))
        {
            platformName = SocialFacebook.getInstance().getPlatformName();
            if (isFunc) targetIcon.setImageResource(CommonConfigs.IconResSocialFacebook);
            if (isNoProfile()) labelText = platformName;
        }

        if (type.equals("instagram"))
        {
            platformName = SocialInstagram.getInstance().getPlatformName();
            if (isFunc) targetIcon.setImageResource(CommonConfigs.IconResSocialInstagram);
            if (isNoProfile()) labelText = platformName;
        }

        if (type.equals("googleplus"))
        {
            platformName = SocialGoogleplus.getInstance().getPlatformName();
            if (isFunc) targetIcon.setImageResource(CommonConfigs.IconResSocialGoogleplus);
            if (isNoProfile()) labelText = platformName;
        }

        setFeeds();

        Simple.makePost(onNotification);
    }

    private JSONArray allFeeds;
    private String platformName;

    private void setFeeds()
    {
        allFeeds = new JSONArray();

        if (type.equals("social"))
        {
            if (SocialTwitter.getInstance().isReady())
            {
                Json.append(allFeeds, SocialTwitter.getInstance().getUserFeeds(true));
            }

            if (SocialFacebook.getInstance().isReady())
            {
                Json.append(allFeeds, SocialFacebook.getInstance().getUserFeeds(true));
            }

            if (SocialInstagram.getInstance().isReady())
            {
                Json.append(allFeeds, SocialInstagram.getInstance().getUserFeeds(true));
            }

            if (SocialGoogleplus.getInstance().isReady())
            {
                Json.append(allFeeds, SocialGoogleplus.getInstance().getUserFeeds(true));
            }
        }
        else
        {
            if (config.has("pfid"))
            {
                JSONObject feed = new JSONObject();

                Json.copy(feed, "id",   config, "pfid");
                Json.copy(feed, "name", config, "label");
                Json.copy(feed, "type", config, "subtype");
                Json.copy(feed, "plat", config, "type");

                allFeeds.put(feed);
            }
        }

        for (int inx = 0; inx < allFeeds.length(); inx++)
        {
            JSONObject feed = Json.getObject(allFeeds, inx);
            if (feed == null) continue;

            Log.d(LOGTAG, "setFeeds: "
                    + Json.getString(feed, "plat") + "="
                    + Json.getString(feed, "name") + "="
                    + Json.getString(feed, "id"));
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        Simple.makePost(feednews);

        if (config.has("pfid"))
        {
            LaunchItem.subscribeNotification(config, onNotification);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        Simple.removePost(feednews);

        if (config.has("pfid"))
        {
            LaunchItem.subscribeNotification(config, onNotification);
        }
    }

    protected final Runnable onNotification = new Runnable()
    {
        @Override
        public void run()
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
    };

    @Override
    protected void onMyClick()
    {
        launchAny();
    }

    protected boolean onMyLongClick()
    {
        Simple.makeClick();

        JSONArray launchItems = Json.getArray(config, "launchitems");

        if (launchItems != null)
        {
            LaunchGroup directory = new LaunchGroup(getContext());
            directory.setConfig(null, launchItems);

            String label = Json.getString(config, "label") + " – " + "Soziale Netzwerke";
            ((HomeActivity) getContext()).addWorkerToBackStack(label, directory);

            return true;
        }

        launchAny();

        return true;
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (super.onExecuteVoiceIntent(voiceintent, index))
        {
            launchAny();

            return true;
        }

        return false;
    }

    private void launchAny()
    {
        if (type.equals("social"))
        {
            LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context);
            webappFrame.setWebAppName("instaface");
            webappFrame.setParent(this);

            ((HomeActivity) context).addViewToBackStack(webappFrame);

            return;
        }

        if (config.has("pfid"))
        {
            LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context)
            {
                @Override
                protected void onDetachedFromWindow()
                {
                    super.onDetachedFromWindow();
                    getWebAppView().destroy();

                    Log.d(LOGTAG, "onDetachedFromWindow: destroyed web view.");
                }
            };

            webappFrame.setWebAppName("instaface");
            webappFrame.setParent(this);

            String name = Json.getString(config, "label");

            if (webappFrame.getWebAppView().social != null)
            {
                String plat = Json.getString(config, "type");
                String pfid = Json.getString(config, "pfid");
                String type = Json.getString(config, "subtype");

                webappFrame.getWebAppView().social.setPlatform(plat, pfid, name, type);
            }

            String label = name + " – " + platformName;
            ((HomeActivity) context).addWorkerToBackStack(label, webappFrame);

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroupSocial(context);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private final Runnable feednews = new Runnable()
    {
        @Override
        public void run()
        {
            int newposts = 0;

            if (allFeeds != null)
            {
                for (int inx = 0; inx < allFeeds.length(); inx++)
                {
                    JSONObject feed = Json.getObject(allFeeds, inx);
                    String platform = Json.getString(feed, "plat");
                    String feedpfid = Json.getString(feed, "id");
                    if ((platform == null) || (feedpfid == null)) continue;

                    newposts += SimpleStorage.getInt("socialfeednews", platform + ".count." + feedpfid);
                }
            }

            String label = Json.getString(config, "label");
            Log.d(LOGTAG, "feednews:" + type + ":" + label + "=" + newposts);

            Simple.makePost(feednews, 60 * 1000);
        }
    };
}
