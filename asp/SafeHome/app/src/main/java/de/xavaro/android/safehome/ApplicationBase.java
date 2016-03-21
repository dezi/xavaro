package de.xavaro.android.safehome;

import android.app.Application;
import android.util.Log;

public class ApplicationBase extends Application
{
    private static final String LOGTAG = ApplicationBase.class.getSimpleName();

    public void onCreate()
    {
        Log.d(LOGTAG, "Here i am...");

        super.onCreate();
    }
}
