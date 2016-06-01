package de.xavaro.android.safehome;

import android.content.Context;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemDeveloper extends LaunchItem
{
    private final static String LOGTAG = LaunchItemDeveloper.class.getSimpleName();

    public LaunchItemDeveloper(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (subtype != null)
        {
            if (Simple.equals(subtype, "settings"))
            {
                icon.setImageResource(GlobalConfigs.IconResPersist);
            }

            if (Simple.equals(subtype, "preferences"))
            {
                icon.setImageResource(GlobalConfigs.IconResSettingsSafehome);
            }

            if (Simple.equals(subtype, "identities"))
            {
                icon.setImageResource(GlobalConfigs.IconResAdministrator);
            }

            if (Simple.equals(subtype, "contacts"))
            {
                icon.setImageResource(GlobalConfigs.IconResContacts);
            }

            if (Simple.equals(subtype, "rcontacts"))
            {
                icon.setImageResource(GlobalConfigs.IconResCommChatUser);
            }

            if (Simple.equals(subtype, "rgroups"))
            {
                icon.setImageResource(GlobalConfigs.IconResCommChatGroup);
            }

            if (Simple.equals(subtype, "sdcard"))
            {
                icon.setImageResource(GlobalConfigs.IconResStorageSDCard);
            }

            if (Simple.equals(subtype, "cache"))
            {
                icon.setImageResource(GlobalConfigs.IconResStorageCache);
            }

            if (Simple.equals(subtype, "known"))
            {
                icon.setImageResource(GlobalConfigs.IconResStorageKnown);
            }

            if (Simple.equals(subtype, "webappcache"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebApps);
            }

            if (Simple.equals(subtype, "events"))
            {
                icon.setImageResource(GlobalConfigs.IconResEvents);
            }

            if (Simple.equals(subtype, "activity"))
            {
                icon.setImageResource(CommonConfigs.IconResActivity);
            }

            overicon.setImageResource(GlobalConfigs.IconResTesting);
            overlay.setVisibility(VISIBLE);
        }
        else
        {
            icon.setImageResource(GlobalConfigs.IconResTesting);

            if (directory == null)
            {
                directory = new LaunchGroupDeveloper(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("developer")) launchDeveloper();
    }

    protected LaunchFrameDeveloper developerFrame;

    private void launchDeveloper()
    {
        if (subtype != null)
        {
            if (developerFrame == null)
            {
                developerFrame = new LaunchFrameDeveloper(context, this);
                developerFrame.setSubtype(subtype);
            }

            ((HomeActivity) context).addViewToBackStack(developerFrame);
        }
        else
        {
            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }
}
