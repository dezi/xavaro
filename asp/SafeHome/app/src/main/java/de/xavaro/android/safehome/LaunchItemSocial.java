package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import de.xavaro.android.common.CommonConfigs;
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
        ImageView targetIcon = icon;

        if (config.has("pfid"))
        {
            String pfid = Json.getString(config, "pfid");
            File profile = ProfileImages.getSocialUserImageFile(type, pfid);

            if (profile != null)
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
            targetIcon.setImageResource(CommonConfigs.IconResSocialTwitter);
        }

        if (type.equals("facebook"))
        {
            targetIcon.setImageResource(CommonConfigs.IconResSocialFacebook);
        }

        if (type.equals("instagram"))
        {
            targetIcon.setImageResource(CommonConfigs.IconResSocialInstagram);
        }

        if (type.equals("googleplus"))
        {
            targetIcon.setImageResource(CommonConfigs.IconResSocialGoogleplus);
        }

        setFeeds();
    }

    private JSONArray allFeeds;

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
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        Simple.removePost(feednews);
    }

    @Override
    protected void onMyClick()
    {
        launchAny();
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
            final LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context);
            webappFrame.setWebAppName("instaface");
            webappFrame.setParent(this);

            ((HomeActivity) context).addViewToBackStack(webappFrame);

            return;
        }

        if (config.has("pfid"))
        {
            final LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context);
            webappFrame.setWebAppName("instaface");
            webappFrame.setParent(this);

            if (webappFrame.getWebAppView().social != null)
            {
                String plat = Json.getString(config, "type");
                String pfid = Json.getString(config, "pfid");
                String name = Json.getString(config, "label");
                String type = Json.getString(config, "subtype");

                webappFrame.getWebAppView().social.setPlatform(plat, pfid, name, type);
            }

            ((HomeActivity) context).addViewToBackStack(webappFrame);

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
