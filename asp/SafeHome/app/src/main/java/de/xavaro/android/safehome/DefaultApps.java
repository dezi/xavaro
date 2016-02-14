package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import de.xavaro.android.common.OopsService;

public class DefaultApps
{
    private static final String LOGTAG = DefaultApps.class.getSimpleName();

    public static String getAppLable(Context context)
    {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = null;

        try
        {
            ai = pm.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        }
        catch (final PackageManager.NameNotFoundException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return (String) (ai != null ? pm.getApplicationLabel(ai) : "Unknown");
    }

    public static String getDefaultHome(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

    public static void setDefaultHome(Context context)
    {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, FakeHome.class);
        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);

        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    @Nullable
    public static String getDefaultHomeLabel(Context context)
    {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) return null;
        String packageName = res.activityInfo.packageName;

        try
        {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return (String) pm.getApplicationLabel(ai);
        }
        catch (PackageManager.NameNotFoundException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static boolean isDefaultHome(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo != null) && res.activityInfo.packageName.equals(context.getPackageName());
    }

    //
    // Retrieve package name handling assist button press.
    //

    public static String getDefaultAssist(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

    public static void setDefaultAssist(Context context)
    {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, FakeAssist.class);
        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent startMain = new Intent(Intent.ACTION_ASSIST);
        startMain.addCategory(Intent.CATEGORY_DEFAULT);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);

        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    @Nullable
    public static String getDefaultAssistLabel(Context context)
    {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) return null;
        String packageName = res.activityInfo.packageName;

        try
        {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return (String) pm.getApplicationLabel(ai);
        }
        catch (PackageManager.NameNotFoundException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @SuppressWarnings("unused")
    public static boolean isDefaultAssist(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo != null) && res.activityInfo.packageName.equals(context.getPackageName());
    }
}
