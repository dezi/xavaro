package de.xavaro.android.safehome;

import android.content.Context;
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
