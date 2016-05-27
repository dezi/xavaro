package de.xavaro.android.safehome;

import android.content.Context;
import android.widget.ImageView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

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
}
