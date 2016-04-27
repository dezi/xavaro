package de.xavaro.android.safehome;

import android.app.Application;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
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

        //
        // Make application context available.
        //

        Simple.setAppContext(getApplicationContext());

        //
        // Ignore users system setting regarding font scaling.
        //

        Configuration configuration = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        configuration.fontScale = 1.0f;
        metrics.scaledDensity = configuration.fontScale * metrics.density;

        getResources().updateConfiguration(configuration, metrics);

        Log.d(LOGTAG, "density DPI=" + getResources().getConfiguration().densityDpi);
    }

    @Override
    public void onTerminate()
    {
        Log.d(LOGTAG, "onTerminate");

        super.onTerminate();
    }
}
