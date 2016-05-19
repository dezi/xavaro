package de.xavaro.android.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.system.ErrnoException;
import android.telephony.PhoneNumberUtils;
import android.util.DisplayMetrics;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

//
// SDK version specific utility methods.
//

public class VersionUtils
{
    @Nullable
    public static String formatPhoneNumber(String phone)
    {
        if (phone == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return PhoneNumberUtils.formatNumber(phone, Simple.getLocaleCountry());
        }

        //noinspection deprecation
        return PhoneNumberUtils.formatNumber(phone);
    }

    //
    // Get SDK compliant drawable resource.
    //

    public static Drawable getDrawableFromResources(Context context,int id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return context.getResources().getDrawable(id, null);
        }

        //noinspection deprecation
        return context.getResources().getDrawable(id);
    }

    //
    // Get SDK compliant drawable resource.
    //

    public static Drawable getIconFromApplication(String packageName)
    {
        try
        {
            Context context = Simple.getAnyContext();
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

    private static int getBestDensity()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            return DisplayMetrics.DENSITY_XXXHIGH;
        }

        return DisplayMetrics.DENSITY_XHIGH;
    }

    //
    // Get error number form exception.
    //

    public static int getErrno(Exception exception)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Throwable errnoex = exception.getCause();

            if (errnoex instanceof ErrnoException)
            {
                return ((ErrnoException) errnoex).errno;
            }
        }

        return -1;
    }
}
