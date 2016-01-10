package de.xavaro.android.common;

//
// Global static access to features.
//

import android.content.Context;

public class CommonConfigs
{
    //region Colors and styles

    // @formatter:off
    public static final int PreferenceTextEnabledColor  = 0xff444444;
    public static final int PreferenceTextDisabledColor = 0x88888888;
    // @formatter:on

    //endregion

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

    //endregion
}
