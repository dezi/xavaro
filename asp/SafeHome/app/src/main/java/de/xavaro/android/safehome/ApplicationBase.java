package de.xavaro.android.safehome;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import de.xavaro.android.common.ProtoBufferDecode;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialGoogleplus;
import de.xavaro.android.common.SocialInstagram;
import de.xavaro.android.common.SocialTwitter;
import de.xavaro.android.common.SystemIdentity;
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

        //
        // Make application context available.
        //

        Simple.setAppContext(getApplicationContext());

        //
        // Set thread policy for internet accesses.
        //

        Simple.setThreadPolicy();

        //
        // Set common cookie store between webkit and java.net
        //

        WebCookie.initCookies();

        //
        // Check version of web app cache.
        //

        WebAppCache.checkWebAppCache();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTerminate()
    {
        Log.d(LOGTAG, "onTerminate");

        super.onTerminate();
    }
}
