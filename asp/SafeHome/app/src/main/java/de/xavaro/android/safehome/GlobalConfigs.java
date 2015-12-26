package de.xavaro.android.safehome;

//
// Global static access to features.
//

import android.content.Context;

public class GlobalConfigs
{
    //region Standard apps

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
    // Famous package names
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

    //region Server adresses

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
    public static final boolean  BetaVersion        = true;
    public static final String   BetaServerName     = "www.xavaro.de";
    // @formatter:on

    //endregion

    //region Colors and styles

    // @formatter:off
    public static final int LaunchPageBackgroundColor  = 0xffffffee;
    public static final int LaunchArrowBackgroundColor = 0x40ddddcc;

    public static final int VideoSurfaceDisabledButton = 0x88888884;
    public static final int VideoSurfaceEnabledButton  = 0xddddddd6;
    public static final int VideoSurfaceSelectedButton = 0xfffffff8;
    // @formatter:on

    //endregion

    //region Global icons

    // @formatter:off
    public static final int IconResFireWall           = R.drawable.firewall_256x256;
    public static final int IconResWhatsApp           = R.drawable.whatsapp_480x480;
    public static final int IconResWhatsAppVoip       = R.drawable.whatsapp_voip_480x480;
    public static final int IconResWhatsAppChat       = R.drawable.whatsapp_chat_480x480;
    public static final int IconResSkype              = R.drawable.skype_256x256;
    public static final int IconResSkypeVoip          = R.drawable.skype_voip_256x256;
    public static final int IconResSkypeChat          = R.drawable.skype_chat_256x256;
    public static final int IconResSkypeVideo         = R.drawable.skype_vica_256x256;
    public static final int IconResIPTelevision       = R.drawable.television_275x275;
    public static final int IconResIPRadio            = R.drawable.radio_400x400;
    public static final int IconResWebConfigNewspaper = R.drawable.newspaper_480x480;
    public static final int IconResWebConfigMagazine  = R.drawable.magazine_128x128;
    public static final int IconResWebConfigShopping  = R.drawable.shopping_440x440;
    public static final int IconResWebConfigErotics   = R.drawable.erotica_280x280;
    // @formatter:on

    //endregion
}
