package de.xavaro.android.common;

//
// Class for storing application global variables.
//

import android.util.Log;

public class CommonStatic
{
    private static final String LOGTAG = CommonStatic.class.getSimpleName();

    //region Google cloud messaging stuff

    public static String gcm_token;

    //enregion Google cloud messaging stuff

    //region Current focus status and active activity

    public static String activity;
    public static boolean focused;
    public static boolean initialized;
    public static boolean lostfocus;

    public static void setFocused(String activity, boolean hasFocus)
    {
        Log.d(LOGTAG, "setFocused: " + activity + "=" + hasFocus);

        CommonStatic.activity = activity;
        CommonStatic.focused = hasFocus;
        CommonStatic.lostfocus |= ! hasFocus;
        CommonStatic.initialized |= hasFocus;

        if (hasFocus) ProcessManager.clearOneShotApps();
    }

    //endregion Current focus status and active activity

    //region Current internet

    public static String myIPaddress;
    public static String gwIPaddress;
    public static int natPrivatePort;
    public static int natPublicPort;

    //endregion
}
