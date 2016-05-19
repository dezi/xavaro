package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import de.xavaro.android.common.CacheManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemApps extends LaunchItem
{
    private final static String LOGTAG = LaunchItemApps.class.getSimpleName();

    public LaunchItemApps(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        ImageView targetIcon = icon;

        if (config.has("apkname"))
        {
            String apkname = Json.getString(config, "apkname");
            String iconpath = CacheManager.getAppIconPath(apkname);

            CommonConfigs.weLikeThis(apkname);

            if (Simple.isAppInstalled(apkname))
            {
                icon.setImageResource(iconpath);
            }
            else
            {
                String psiconpath = CacheManager.getAppIconPath(CommonConfigs.packagePlaystore);

                icon.setImageResource(psiconpath);
                overicon.setImageResource(iconpath);
                overlay.setVisibility(VISIBLE);
            }
        }
        else
        {
            if (Simple.equals(subtype, "discounter"))
            {
                icon.setImageResource(GlobalConfigs.IconResAppsDiscounter);
            }
        }

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);
    }

    @Override
    protected void onMyClick()
    {
        if (subtype.equals("discounter")) launchDiscounter();
    }

    private void launchDiscounter()
    {
        if (config.has("apkname"))
        {
            String apkname = Json.getString(config, "apkname");

            if (Simple.isAppInstalled(apkname))
            {
                Simple.launchApp(apkname);
            }
            else
            {
                Simple.installAppFromPlaystore(apkname);
            }
        }
        else
        {
            if (directory == null)
            {
                directory = new LaunchGroupWebStream(context, this);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }

            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }
}
