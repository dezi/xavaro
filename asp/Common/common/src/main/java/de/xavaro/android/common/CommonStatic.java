package de.xavaro.android.common;

//
// Class for storing application global variables.
//

import java.util.ArrayList;

public class CommonStatic
{
    public static final ArrayList<String> oneshotApps = new ArrayList<>();

    public static void addOneShotApp(String packagename)
    {
        oneshotApps.add(packagename);
    }

    public static void clearOneShotApps()
    {
        oneshotApps.clear();
    }
}
