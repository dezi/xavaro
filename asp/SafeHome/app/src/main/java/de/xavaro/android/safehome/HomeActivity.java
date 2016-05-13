package de.xavaro.android.safehome;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;
import java.util.ArrayList;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.AppInfoHandler;
import de.xavaro.android.common.BackKeyClient;
import de.xavaro.android.common.BackKeyMaster;
import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.GCMRegistrationService;
import de.xavaro.android.common.MediaSurface;
import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.VoiceIntentResolver;
import de.xavaro.android.common.WebCookie;

public class HomeActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener,
        MediaSurface.VideoSurfaceHandler,
        BackKeyMaster,
        AppInfoHandler,
        VoiceIntentResolver
{
    private static final String LOGTAG = HomeActivity.class.getSimpleName();

    public static HomeActivity instance;

    public static HomeActivity getInstance()
    {
        return instance;
    }

    private FrameLayout topScreen;
    private FrameLayout videoSurface;
    private HomeLaunch launchScreen;
    private HomeBottom bottomScreen;
    private LaunchGroupRoot launchGroup;

    private boolean wasPaused = false;
    private boolean lostFocus = true;

    private final Handler handler = new Handler();

    private static final int UI_HIDE = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Simple.setActContext(this);

        super.onCreate(savedInstanceState);

        instance = this;

        int basesize = Simple.getDevicePixels(160);

        topScreen = new FrameLayout(this);
        topScreen.setSystemUiVisibility(topScreen.getSystemUiVisibility() + UI_HIDE);
        setContentView(topScreen);

        launchScreen = new HomeLaunch(this);
        launchScreen.setSize(basesize);
        topScreen.addView(launchScreen);

        bottomScreen = new HomeBottom(this);
        bottomScreen.setSize(basesize);
        topScreen.addView(bottomScreen);

        ArchievementManager.reset("alertcall.shortclick");

        startService(new Intent(this, KioskService.class));
        startService(new Intent(this, CommService.class));
        startService(new Intent(this, OopsService.class));

        startService(new Intent(this, GCMRegistrationService.class));
        startService(new Intent(this, AccessibilityService.class));

        //
        // Allow cross fuck domain HTTP shit.
        //

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //
        // Set common cookie store between webkit and java.net
        //

        WebCookie.initCookies();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        topScreen.setOnSystemUiVisibilityChangeListener(this);

        //
        // Debug update external contacts.
        //

        File contacts = new File(Simple.getExternalFilesDir(), "contacts.json");
        Simple.putFileContent(contacts, Json.toPretty(ContactsHandler.getJSONData(this)));

        if (! AccessibilityService.checkEnabled())
        {
            Simple.makeAlert("Accessibility Service not enabled.");
        }
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

            topScreen.setSystemUiVisibility(UI_HIDE);
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

        Log.d(LOGTAG, "onStart...");

        if ((launchGroup == null) || CommonStatic.settingschanged)
        {
            if (launchGroup != null)
            {
                launchScreen.removeAllViews();
                backStack.clear();
                launchGroup = null;

                System.gc();
            }

            launchGroup = new LaunchGroupRoot(this);
            launchGroup.setConfig(null, LaunchGroupRoot.getConfig());
            launchScreen.addView(launchGroup);

            CommonStatic.settingschanged = false;
        }

        DitUndDat.InternetState.subscribe(this);

        ArchievementManager.show("howto.open.settings");

        //
        // Check for launch intent.
        //

        Intent intent = getIntent();

        if (Simple.equals(intent.getAction(), "de.xavaro.android.safehome.LAUNCHITEM"))
        {
            final String type = intent.getStringExtra("type");
            final String subtype = intent.getStringExtra("subtype");

            final Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    launchGroup.activateLaunchItem(type, subtype);
                }
            };

            handler.postDelayed(runner, 100);
        }
    }

    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume...");

        super.onResume();

        Simple.setActContext(this);

        if (wasPaused && ! lostFocus)
        {
            //
            // Home press in own app.
            //

            while (backStack.size() > 0)
            {
                Object lastview = backStack.remove(backStack.size() - 1);
                launchScreen.removeView((FrameLayout) lastview);
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

        DitUndDat.InternetState.unsubscribe(this);

        Speak.shutdown();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        Log.d(LOGTAG, "onWindowFocusChanged=" + hasFocus);

        super.onWindowFocusChanged(hasFocus);

        CommonStatic.setFocused(HomeActivity.class.getSimpleName(), hasFocus);

        if (hasFocus) handler.postDelayed(makeFullscreen, 500);

        lostFocus = ! hasFocus;
    }

    //region app info handling

    public String getBetaVersion()
    {
        return GlobalConfigs.BetaVersion;
    }

    public String getBetaVersionDate()
    {
        return GlobalConfigs.BetaVersionDate;
    }

    //endregion app info handling

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
        if (((ViewGroup) view).getParent() == null)
        {
            launchScreen.addView((FrameLayout) view);
            backStack.add(view);

            if (videoSurface != null) videoSurface.bringToFront();
        }
    }

    public void addView(Object view, ViewGroup.LayoutParams params)
    {
        launchScreen.addView((FrameLayout) view, params);
    }

    public void addVideoSurface(FrameLayout video)
    {
        if (videoSurface == null)
        {
            topScreen.addView(video);
            videoSurface = video;
        }
    }

    public void removeVideoSurface()
    {
        if ((videoSurface != null) &&  (videoSurface.getParent() == topScreen))
        {
            topScreen.removeView(videoSurface);
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
        if (! DefaultApps.isDefaultHome())
        {
            //
            // Finally release user to system.
            //

            super.onBackPressed();
        }
    }

    public void onBackKeyExecuteNow()
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Object lastview = backStack.get(backStack.size() - 1);

                launchScreen.removeView((FrameLayout) lastview);
                backStack.remove(backStack.size() - 1);

                if (lastview instanceof LaunchFrame)
                {
                    ((LaunchFrame) lastview).onBackKeyExecuted();
                }
            }
        });
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

            if (lastview instanceof BackKeyClient)
            {
                if (! ((BackKeyClient) lastview).onBackKeyWanted())
                {
                    launchScreen.removeView((FrameLayout) lastview);
                    backStack.remove(backStack.size() - 1);

                    ((BackKeyClient) lastview).onBackKeyExecuted();
                }
            }
            else
            {
                launchScreen.removeView((FrameLayout) lastview);
                backStack.remove(backStack.size() - 1);
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
            LaunchGroupPreferences.getInstance(this).open();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(LOGTAG,"onActivityResult: request:" + requestCode + " result:" + resultCode);
    }

    @Override
    public void onCollectVoiceIntent(VoiceIntent voiceintent)
    {
        launchGroup.onCollectVoiceIntent(voiceintent);
    }

    @Override
    public void onResolveVoiceIntent(VoiceIntent voiceintent)
    {
        launchGroup.onResolveVoiceIntent(voiceintent);
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        return launchGroup.onExecuteVoiceIntent(voiceintent, index);
    }
}