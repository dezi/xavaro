package de.xavaro.android.safehome;

//
// Global static access to features.
//

import android.content.Context;

public class GlobalConfigs
{
    //region Standard apps.

    //
    // Apps we like in this setup.
    //

    // @formatter:off
    public static boolean likeGooglePlus = true;
    public static boolean likeWhatsApp   = false;
    public static boolean likeFacebook   = true;
    public static boolean likeTwitter    = true;
    public static boolean likeSkype      = false;
    public static boolean likeEmail      = true;
    // @formatter:on

    //
    // Famous package names.
    //

    // @formatter:off
    public static String packageGooglePlus = "com.google.android.apps.plus";
    public static String packageWhatsApp   = "com.whatsapp";
    public static String packageFacebook   = "com.facebook.katana";
    public static String packageTwitter    = "com.twitter.android";
    public static String packageSkype      = "com.skype.raider";
    public static String packageEmail      = null;
    // @formatter:on

    public static void weLikeThis(Context context,String packagename)
    {
        if (packageEmail == null) packageEmail = StaticUtils.getDefaultEmail(context);

        // @formatter:off
        if (packagename.equals(packageWhatsApp  )) likeWhatsApp   = true;
        if (packagename.equals(packageFacebook  )) likeFacebook   = true;
        if (packagename.equals(packageTwitter   )) likeTwitter    = true;
        if (packagename.equals(packageSkype     )) likeSkype      = true;
        if (packagename.equals(packageGooglePlus)) likeGooglePlus = true;
        if (packagename.equals(packageEmail     )) likeEmail      = true;
        // @formatter:on
    }

    //endregion

    //region Server adresses.

    // @formatter:off
    public static final String OopsServerName     = "www.xavaro.de";
    public static final int    OopsServerPort     = 42742;
    public static final int    OopsServerSleepMin = 1000;
    public static final int    OopsServerSleepMax = 1000 * 3600;
    // @formatter:on

    // @formatter:off
    public static final String CommServerName     = "www.xavaro.de";
    public static final int    CommServerPort     = 42742; // 42743
    public static final int    CommServerSleepMin = 100;
    public static final int    CommServerSleepMax = 100 * 1000;
    public static final int    CommServerPingSec  = 1;
    // @formatter:on

    //endregion
}
