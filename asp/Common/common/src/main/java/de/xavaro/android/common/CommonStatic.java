package de.xavaro.android.common;

//
// Class for storing application global variables.
//

import android.util.Log;

import java.net.ServerSocket;

public class CommonStatic
{
    private static final String LOGTAG = CommonStatic.class.getSimpleName();

    //region Global layout stuff.

    public static int LaunchItemSize = 190;

    //endregion

    //region Google cloud messaging stuff

    public static String gcm_token;

    //enregion Google cloud messaging stuff

    //region Current focus status and active activity

    public static String activity;
    public static boolean focused;
    public static boolean initialized;
    public static boolean lostfocus;
    public static boolean settingschanged;

    public static void setFocused(String activity, boolean hasFocus)
    {
        Log.d(LOGTAG, "setFocused: " + activity + "=" + hasFocus);

        //
        // The focus events come in in wrong order. The activated
        // activity gains focus before the going activity looses it.
        //

        if (hasFocus || (CommonStatic.activity == null) || CommonStatic.activity.equals(activity))
        {
            CommonStatic.activity = activity;
            CommonStatic.focused = hasFocus;
            CommonStatic.lostfocus |= !hasFocus;
            CommonStatic.initialized |= hasFocus;

            if (hasFocus) ProcessManager.clearOneShotApps();
        }
    }

    //endregion Current focus status and active activity

    //region Current internet

    public static String privateIPaddress;
    public static String publicIPaddress;
    public static String gwIPaddress;
    public static String wifiName;

    //endregion
}
