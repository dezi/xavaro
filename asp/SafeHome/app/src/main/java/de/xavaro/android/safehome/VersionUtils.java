package de.xavaro.android.safehome;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;

//
// SDK version specific utility methods.
//
public class VersionUtils
{
    //
    // Get SDK compliant drawable resource.
    //
    public static Drawable getDrawableFromResources(Context context,int id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return context.getResources().getDrawable(id,null);
        }

        //noinspection deprecation
        return context.getResources().getDrawable(id);
    }

    //
    // Get SDK compliant drawable resource.
    //
    public static Drawable getIconFromApplication(Context context,String packageName)
    {
        try
        {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            Resources res = context.getPackageManager().getResourcesForApplication(appInfo);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                return res.getDrawableForDensity(appInfo.icon, DisplayMetrics.DENSITY_XXXHIGH, null);
            }

            Configuration appConfig = res.getConfiguration();
            appConfig.densityDpi = VersionUtils.getBestDensity();
            DisplayMetrics dm = res.getDisplayMetrics();
            res.updateConfiguration(appConfig, dm);

            //noinspection deprecation
            return res.getDrawable(appInfo.icon);
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    //
    // Get best density value for SDK version.
    //
    public static int getBestDensity()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            return DisplayMetrics.DENSITY_XXXHIGH;
        }

        return DisplayMetrics.DENSITY_XHIGH;
    }
}
