package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VoiceIntent;

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

        if (type.equals("facebook"))
        {
            if (config.has("subtype"))
            {
                String fbid = Json.getString(config, "fbid");
                File profile = ProfileImages.getFacebookProfileImageFile(fbid);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), false);
                    targetIcon = overicon;
                }

                targetIcon.setImageResource(CommonConfigs.IconResSocialFacebook);
            }
            else
            {
                icon.setImageResource(CommonConfigs.IconResSocialFacebook);
            }
        }

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("facebook")) launchFacebook();
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (super.onExecuteVoiceIntent(voiceintent, index))
        {
            if (type.equals("facebook"   )) launchFacebook();

            return true;
        }

        return false;
    }

    private void launchFacebook()
    {
        if (config.has("fbid"))
        {
            final LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context);
            webappFrame.setWebAppName("instaface");
            webappFrame.setParent(this);

            if (webappFrame.getWebAppView().facebook != null)
            {
                String fbid = Json.getString(config, "fbid");
                String name = Json.getString(config, "label");
                String type = Json.getString(config, "subtype");

                webappFrame.getWebAppView().facebook.setTarget(fbid, name, type);
            }

            ((HomeActivity) context).addViewToBackStack(webappFrame);

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroupSocial.FacebookGroup(context);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
