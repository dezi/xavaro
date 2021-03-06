package de.xavaro.android.common;

//
// Global static access to features.
//

import android.content.Context;

public class CommonConfigs
{
    //region Global icons

    // @formatter:off
    public static final int IconResAnon             = R.drawable.anon_300x300;
    public static final int IconResNext             = R.drawable.next_600x600;
    public static final int IconResPrev             = R.drawable.prev_600x600;
    public static final int IconResVoice            = R.drawable.voice_256x256;
    public static final int IconResPrepaid          = R.drawable.prepaid_600x600;
    public static final int IconResPrepaidAssist    = R.drawable.prepaid_assist_600x600;
    public static final int IconResVoiceListen      = R.drawable.voice_ear_256x256;
    public static final int IconResActivity         = R.drawable.activity_256x256;
    public static final int IconResBetaVersion      = R.drawable.betaversion_256x256;
    public static final int IconResMonitoring       = R.drawable.monitoring_626x626;
    public static final int IconResBattery          = R.drawable.battery_charge_100_600x600;
    public static final int IconResPermissions      = R.drawable.permissions_220x220;
    public static final int IconResSocial           = R.drawable.social_600x600;
    public static final int IconResSocialTwitter    = R.drawable.social_twitter_400x400;
    public static final int IconResSocialFacebook   = R.drawable.social_facebook_400x400;
    public static final int IconResSocialGoogleplus = R.drawable.social_googleplus_400x400;
    public static final int IconResSocialInstagram  = R.drawable.social_instagram_400x400;
    public static final int IconResSocialTinder     = R.drawable.social_tinder_300x300;
    public static final int IconResSettingsAndroid  = R.drawable.settings_android_512x512;
    public static final int IconResWarning          = R.drawable.warning_300x300;

    // @formatter:on

    //endregion Global icons

    //region UI-Thread runnables

    public static Runnable UpdateNotifications;

    //endregion UI-Thread runnables

    //region Standard apps

    //
    // Apps we like in this setup.
    //

    // @formatter:off
    public static boolean likeGooglePlus = false;
    public static boolean likeWhatsApp   = false;
    public static boolean likeFacebook   = false;
    public static boolean likeTwitter    = false;
    public static boolean likeSkype      = false;
    public static boolean likeEmail      = false;
    // @formatter:on

    public static void weLikeThis(String packagename)
    {
        if (packagename == null) return;

        if (CommonConfigs.packageEmail == null)
        {
            CommonConfigs.packageEmail = Simple.getDefaultEmail();
        }

        // @formatter:off
        if (packagename.equals(CommonConfigs.packageWhatsApp  )) likeWhatsApp   = true;
        if (packagename.equals(CommonConfigs.packageFacebook  )) likeFacebook   = true;
        if (packagename.equals(CommonConfigs.packageTwitter   )) likeTwitter    = true;
        if (packagename.equals(CommonConfigs.packageSkype     )) likeSkype      = true;
        if (packagename.equals(CommonConfigs.packageGooglePlus)) likeGooglePlus = true;
        if (packagename.equals(CommonConfigs.packageEmail     )) likeEmail      = true;
        // @formatter:on
    }

    //endregion

    //region Colors and styles

    // @formatter:off
    public static final int PreferenceTextEnabledColor  = 0xff444444;
    public static final int PreferenceTextDisabledColor = 0x88888888;
    public static final int PreferenceTextButtonColor   = 0xff43932f;

    public static final int VideoSurfaceDisabledButton  = 0x88888884;
    public static final int VideoSurfaceEnabledButton   = 0xddddddd6;
    public static final int VideoSurfaceSelectedButton  = 0xfffffff8;
    // @formatter:on

    //endregion

    //region Famous package names

    // @formatter:off
    public static String packageGooglePlus = "com.google.android.apps.plus";
    public static String packageWhatsApp   = "com.whatsapp";
    public static String packageFacebook   = "com.facebook.katana";
    public static String packageTwitter    = "com.twitter.android";
    public static String packageSkype      = "com.skype.raider";
    public static String packagePlaystore  = "com.android.vending";
    public static String packageEmail      = null;
    // @formatter:on

    //endregion Famous package names

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
    public static final int    CommServerPupsSec  = 30;
    public static final int    CommServerPingSec  = 180;
    // @formatter:on

    // @formatter:off
    public static final String ShareServerName     = "www.xavaro.de";
    public static final int    ShareServerPort     = 80;
    // @formatter:on

    // @formatter:off
    public static final String WebappsServerName   = "www.xavaro.de";
    public static final int    WebappsServerPort   = 80;
    // @formatter:on

    // @formatter:off
    public static final String BetaServerName      = "www.xavaro.de";
    public static final int    BetaServerPort      = 80;
    // @formatter:on

    //endregion
}
