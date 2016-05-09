package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;
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

            Simple.makePost(feednews, 60 * 1000);
        }
    };
}
