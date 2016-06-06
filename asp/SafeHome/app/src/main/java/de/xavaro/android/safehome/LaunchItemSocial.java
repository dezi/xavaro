package de.xavaro.android.safehome;

import android.content.Context;
import android.widget.ImageView;
import android.util.Log;

import org.json.JSONArray;

import java.io.File;

import de.xavaro.android.common.SocialGoogleplus;
import de.xavaro.android.common.SocialInstagram;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.SocialTwitter;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

public class LaunchItemSocial extends LaunchItemNotify
{
    private final static String LOGTAG = LaunchItemSocial.class.getSimpleName();

    public LaunchItemSocial(Context context)
    {
        super(context);
    }

    private String platformName;

    @Override
    protected void setConfig()
    {
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

        if (type.equals("tinder"))
        {
            targetIcon.setImageResource(CommonConfigs.IconResSocialTinder);
        }

        if (type.equals("likes"))
        {
            targetIcon.setImageResource(GlobalConfigs.IconResWebConfigInternet);
        }

        if (type.equals("twitter"))
        {
            platformName = SocialTwitter.getInstance().getPlatformName();
            if (! isNoFunction()) targetIcon.setImageResource(CommonConfigs.IconResSocialTwitter);
            if (isNoProfile()) labelText = platformName;
        }

        if (type.equals("facebook"))
        {
            platformName = SocialFacebook.getInstance().getPlatformName();
            if (! isNoFunction()) targetIcon.setImageResource(CommonConfigs.IconResSocialFacebook);
            if (isNoProfile()) labelText = platformName;
        }

        if (type.equals("instagram"))
        {
            platformName = SocialInstagram.getInstance().getPlatformName();
            if (! isNoFunction()) targetIcon.setImageResource(CommonConfigs.IconResSocialInstagram);
            if (isNoProfile()) labelText = platformName;
        }

        if (type.equals("googleplus"))
        {
            platformName = SocialGoogleplus.getInstance().getPlatformName();
            if (! isNoFunction()) targetIcon.setImageResource(CommonConfigs.IconResSocialGoogleplus);
            if (isNoProfile()) labelText = platformName;
        }

        Simple.makePost(onNotification);
    }

    @Override
    protected void onMyClick()
    {
        if (isNoFunction() || (totalnews != mainnews))
        {
            launchDir();
        }
        else
        {
            launchAny();
        }
    }

    protected boolean onMyLongClick()
    {
        Simple.makeClick();

        return launchDir();
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

    private boolean launchDir()
    {
        if (! launchLikes())
        {
            launchAny();
        }

        return true;
    }

    protected boolean launchLikes()
    {
        JSONArray launchItems = Json.getArray(config, "launchitems");

        if (launchItems != null)
        {
            String label = Json.getString(config, "label") + " – " + "Soziale Netzwerke";

            LaunchGroup directory = new LaunchGroup(getContext());
            directory.setTitle(label);
            directory.setConfig(null, launchItems);

            ((HomeActivity) getContext()).addWorkerToBackStack(label, directory);

            return true;
        }

        return false;
    }

    private void launchAny()
    {
        if (type.equals("social"))
        {
            //
            // Launch mode owner plus all related feeds.
            //

            LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context, this);
            webappFrame.setWebAppName("instaface");

            ((HomeActivity) context).addViewToBackStack(webappFrame);

            return;
        }

        if (type.equals("tinder"))
        {
            LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context, this);
            webappFrame.setWebAppName("tinderella");

            ((HomeActivity) context).addViewToBackStack(webappFrame);

            return;
        }

        if (type.equals("likes"))
        {
            launchLikes();

            return;
        }

        if (config.has("pfid"))
        {
            LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context, this)
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

            String name = Json.getString(config, "label");

            if (webappFrame.getWebAppView().social != null)
            {
                String plat = Json.getString(config, "type");
                String pfid = Json.getString(config, "pfid");
                String type = Json.getString(config, "subtype");

                if (Simple.equals(type, "owner"))
                {
                    //
                    // Launch item in the role as a plain user, even
                    // it is belonging to the social owner. Set type
                    // to friend mode, to enable normal display.
                    //

                    type = "friend";
                }

                webappFrame.getWebAppView().social.setPlatform(plat, pfid, name, type);
            }

            String label = name + " – " + platformName;
            ((HomeActivity) context).addWorkerToBackStack(label, webappFrame);

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroupSocial(context);
            directory.setTitle(Json.getString(config, "label"));
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

        ((HomeActivity) context).addWorkerToBackStack(directory.launchTitle, directory);
    }
}
