package de.xavaro.android.safehome;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import de.xavaro.android.common.Facebook;
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

        Simple.setFontScale();

        Log.d(LOGTAG, "font SCALE=" + getResources().getConfiguration().fontScale);
        Log.d(LOGTAG, "density DPI=" + getResources().getConfiguration().densityDpi);

        Facebook.initialize(this);
    }

    public final Runnable setFontScale = new Runnable()
    {
        @Override
        public void run()
        {
            //
            // On LG phone some application fucks with the font scale setting.
            // This leads to unreadable system dialogs. We set it to default
            // value if screwed up.
            //

            Simple.setFontScale();
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        Log.d(LOGTAG, "onConfigurationChanged: font SCALE=" + newConfig.fontScale);

        Simple.makePost(setFontScale);
    }

    @Override
    public void onTerminate()
    {
        Log.d(LOGTAG, "onTerminate");

        super.onTerminate();
    }
}
