package de.xavaro.android.common;

//
// Class for storing application global variables.
//

import android.util.Log;

import java.util.ArrayList;

public class CommonStatic
{
    private static final String LOGTAG = CommonStatic.class.getSimpleName();

    //
    // Current aceptable external apps.
    //

    public static final ArrayList<String> oneshotApps = new ArrayList<>();

    public static void addOneShotApp(String packagename)
    {
        oneshotApps.add(packagename);
    }

    public static void clearOneShotApps()
    {
        oneshotApps.clear();
    }

    //
    // Current focus status and active activity.
    //

    public static boolean focused;
    public static String activity;

    public static void setFocused(String activity, boolean hasFocus)
    {
        Log.d(LOGTAG, "setFocused: " + activity + "=" + hasFocus);

        CommonStatic.activity = activity;
        CommonStatic.focused = hasFocus;
    }
}
