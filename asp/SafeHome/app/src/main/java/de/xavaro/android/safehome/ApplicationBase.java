package de.xavaro.android.safehome;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.StrictMode;
import android.util.Log;

import java.io.File;
import java.util.List;

import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialGoogleplus;
import de.xavaro.android.common.SocialInstagram;
import de.xavaro.android.common.SocialTwitter;
import de.xavaro.android.common.WebAppCache;
import de.xavaro.android.common.WebCookie;

public class ApplicationBase extends Application
{
    private static final String LOGTAG = ApplicationBase.class.getSimpleName();

    @Override
    public void onCreate()
    {
        Log.d(LOGTAG, "onCreate");

        super.onCreate();

        /*
        final PackageManager pm = getPackageManager();

        List<ApplicationInfo> packages =  pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages)
        {
            if (packageInfo.packageName.equals("com.nianticlabs.pokemongo"))
            {
                Log.d(LOGTAG, "Installed package :" + packageInfo.packageName);
                Log.d(LOGTAG, "Apk file path:" + packageInfo.sourceDir);

                Simple.fileCopy(new File(packageInfo.sourceDir),
                        new File(Simple.getMediaPath("download"),
                                "com.nianticlabs.pokemongo.apk"));
            }
        }
        */

        //
        // Make application context available.
        //

        Simple.setAppContext(getApplicationContext());

        //
        // Set thread policy for internet accesses.
        //

        Simple.setThreadPolicy();

        //
        // Ignore users system setting regarding font scaling.
        //

        Simple.setFontScale();

        //
        // Set common cookie store between webkit and java.net
        //

        WebCookie.initCookies();

        //
        // Check version of web app cache.
        //

        WebAppCache.checkWebAppCache();
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
