package de.xavaro.android.safehome;

import android.content.Context;
import android.widget.ImageView;

import java.io.File;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.ProfileImages;
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

        if (type.equals("twitter"))
        {
            if (config.has("pfid"))
            {
                String pfid = Json.getString(config, "pfid");
                File profile = ProfileImages.getSocialUserImageFile("twitter", pfid);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), false);
                    targetIcon = overicon;
                }
            }

            targetIcon.setImageResource(CommonConfigs.IconResSocialTwitter);
        }

        if (type.equals("facebook"))
        {
            if (config.has("pfid"))
            {
                String pfid = Json.getString(config, "pfid");
                File profile = ProfileImages.getSocialUserImageFile("facebook", pfid);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), false);
                    targetIcon = overicon;
                }
            }

            targetIcon.setImageResource(CommonConfigs.IconResSocialFacebook);
        }

        if (type.equals("instagram"))
        {
            if (config.has("pfid"))
            {
                String pfid = Json.getString(config, "pfid");
                File profile = ProfileImages.getSocialUserImageFile("instagram", pfid);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), false);
                    targetIcon = overicon;
                }
            }

            targetIcon.setImageResource(CommonConfigs.IconResSocialInstagram);
        }

        if (type.equals("googleplus"))
        {
            if (config.has("pfid"))
            {
                String pfid = Json.getString(config, "pfid");
                File profile = ProfileImages.getSocialUserImageFile("googleplus", pfid);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), false);
                    targetIcon = overicon;
                }
            }

            targetIcon.setImageResource(CommonConfigs.IconResSocialGoogleplus);
        }

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);
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

                webappFrame.getWebAppView().social.setTarget(plat, pfid, name, type);
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
}
