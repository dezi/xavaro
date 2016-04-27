package de.xavaro.android.safehome;

import android.app.Application;
import android.util.Log;

import de.xavaro.android.common.Simple;

public class ApplicationBase extends Application
{
    private static final String LOGTAG = ApplicationBase.class.getSimpleName();

    @Override
    public void onCreate()
    {
        Log.d(LOGTAG, "onCreate");

        super.onCreate();

        Simple.setAppContext(getApplicationContext());
    }

    @Override
    public void onTerminate()
    {
        Log.d(LOGTAG, "onTerminate");

        super.onTerminate();
    }
}
