package de.xavaro.android.safehome;

import android.support.v7.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener
{
    private final String LOGTAG = "HomeActivity";

    public static KioskService kioskService;

    public static HomeActivity homeActivity;

    public static HomeActivity getInstance()
    {
        return homeActivity;
    }

    private LaunchGroup launchGroup;
    private JSONObject config;
    private FrameLayout topscreen;
    private FrameLayout videoSurface;

    private boolean wasPaused = false;
    private boolean lostFocus = true;

    private final Handler handler = new Handler();

    private static final int UI_HIDE = 0
//          | View.SYSTEM_UI_FLAG_LOW_PROFILE
//          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
//          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//          | View.SYSTEM_UI_FLAG_IMMERSIVE
//          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeActivity = this;

        topscreen = (FrameLayout) findViewById(R.id.top_screen);
        topscreen.setSystemUiVisibility(UI_HIDE);

        launchGroup = new LaunchGroup(this);
        topscreen.addView(launchGroup);

        createConfig();

        startService(new Intent(this, KioskService.class));
        startService(new Intent(this, CommService.class));
        startService(new Intent(this, OopsService.class));

        //
        // Allow cross fuck domain HTTP shit.
        //

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //
        // Set common cookie store between webkit and java.net
        //

        WebCookie.initCookies();

        SystemIdentity.initialize(this);
        SettingsManager.initialize(this);
        ArchievementManager.initialize(this);
        SettingsFragments.initialize(this);
        DitUndDat.SharedPrefs.initialize(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        topscreen.setOnSystemUiVisibilityChangeListener(this);
    }

    private final Runnable makeFullscreen = new Runnable()
    {
        @Override
        public void run()
        {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

            topscreen.setSystemUiVisibility(UI_HIDE);
        }
    };

    @Override
    public void onSystemUiVisibilityChange(int visibility)
    {
        Log.d(LOGTAG, "onSystemUiVisibilityChange:" + visibility);

        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
        {
            handler.postDelayed(makeFullscreen, 2000);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent kioskIntent = new Intent(this, KioskService.class);
        bindService(kioskIntent, kioskConnection, Context.BIND_AUTO_CREATE);

        Intent commIntent = new Intent(this, CommService.class);
        bindService(commIntent, commConnection, Context.BIND_AUTO_CREATE);

        Log.d(LOGTAG, "onStart...");

        UpdateManager.selfUpdate(this, false);
        DitUndDat.InternetState.subscribe(this);

        ArchievementManager.show("howto.open.settings");
    }

    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume...");

        super.onResume();

        if (kioskService != null) kioskService.clearOneShot();

        if (wasPaused && ! lostFocus)
        {
            //
            // Home press in own app.
            //

            while (backStack.size() > 0)
            {
                Object lastview = backStack.remove(backStack.size() - 1);
                topscreen.removeView((FrameLayout) lastview);
            }
        }

        wasPaused = false;
    }

    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause...");

        super.onPause();

        wasPaused = true;
    }

    @Override
    protected void onStop()
    {
        Log.d(LOGTAG, "onStop...");

        super.onStop();

        if (kioskService != null) unbindService(kioskConnection);
        if (CommService.getInstance() != null) unbindService(commConnection);

        DitUndDat.InternetState.unsubscribe(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        Log.d(LOGTAG, "onWindowFocusChanged=" + hasFocus);

        super.onWindowFocusChanged(hasFocus);

        if (kioskService != null) kioskService.setFocused(LOGTAG, hasFocus);

        if (hasFocus) handler.postDelayed(makeFullscreen, 500);

        lostFocus = ! hasFocus;
    }

    //region onBackPressed handling

    //
    // Execute back presses either within internal
    // stack, call configuration if back pressed
    // serveral times or return to android system.
    //

    private long backPressedTime = 0;
    private int backPressedCount = 0;

    private ArrayList backStack = new ArrayList();

    public void addViewToBackStack(Object view)
    {
        topscreen.addView((FrameLayout) view);
        backStack.add(view);

        if (videoSurface != null) videoSurface.bringToFront();
    }

    public void addView(Object view, ViewGroup.LayoutParams params)
    {
        topscreen.addView((FrameLayout) view, params);
    }

    public void addVideoSurface(FrameLayout video)
    {
        if (videoSurface == null)
        {
            topscreen.addView(video);
            videoSurface = video;
        }
    }

    public void removeVideoSurface()
    {
        if (videoSurface != null)
        {
            topscreen.removeView(videoSurface);
            videoSurface = null;
        }
    }

    private final Runnable delayOnBackPressed = new Runnable()
    {
        @Override
        public void run()
        {
            executeOnBackPressed();
        }
    };

    public void executeOnBackPressed()
    {
        if (! DitUndDat.DefaultApps.isDefaultHome(this))
        {
            //
            // Finally release user to system.
            //

            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed()
    {
        //
        // Internal back stack operation.
        //

        if (backStack.size() > 0)
        {
            Object lastview = backStack.get(backStack.size() - 1);

            if (lastview instanceof LaunchGroup)
            {
                topscreen.removeView((FrameLayout) lastview);
                backStack.remove(backStack.size() - 1);
            }

            if (lastview instanceof WebFrame)
            {
                //
                // Give web browser option to do
                // internal back press.
                //

                if (((WebFrame) lastview).doBackPressed())
                {
                    topscreen.removeView((FrameLayout) lastview);
                    backStack.remove(backStack.size() - 1);
                }
            }

            return;
        }

        //
        // Top level back press handling.
        //

        long now = System.currentTimeMillis();

        if ((now - backPressedTime) > 1000)
        {
            backPressedCount = 0;
        }
        else
        {
            backPressedCount += 1;
        }

        backPressedTime = now;

        handler.removeCallbacks(delayOnBackPressed);

        if (backPressedCount >= 4)
        {
            LaunchSettings.getInstance(this).open();
        }
        else
        {
            //
            // Execute back press within given time.
            //

            handler.postDelayed(delayOnBackPressed,1500);
        }
    }

    //endregion onBackPressed handling

    private void createConfig()
    {
        config = StaticUtils.readRawTextResourceJSON(this, R.raw.default_config);

        if ((config == null) || ! config.has("launchgroup"))
        {
            Toast.makeText(this, "Keine <launchgroup> gefunden.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            launchGroup.setConfig(null,config.getJSONObject("launchgroup"));
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    private ServiceConnection kioskConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            KioskService.KioskBinder binder = (KioskService.KioskBinder) service;
            kioskService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            kioskService = null;
        }
    };

    private ServiceConnection commConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            CommService.CommBinder binder = (CommService.CommBinder) service;
            CommService.setInstance(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            CommService.setInstance(null);
        }
    };
}