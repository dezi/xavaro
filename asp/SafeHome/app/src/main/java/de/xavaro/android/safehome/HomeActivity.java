package de.xavaro.android.safehome;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.json.JSONObject;

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
import de.xavaro.android.common.WebAppCache;
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
    private FrameLayout launchFrame;

    private HomeNotify notifyScreen;
    private HomeLaunch launchScreen;
    private HomePeople peopleScreen;
    private HomeSocial socialScreen;
    
    private LaunchGroupRoot launchGroup;
    private JSONObject launchConfig;

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

        //
        // Allow cross fuck domain HTTP shit.
        //

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //
        // Set common cookie store between webkit and java.net
        //

        WebCookie.initCookies();

        //
        // Check version of web app cache.
        //

        WebAppCache.checkWebAppCache();

        //
        // Build views.
        //

        topScreen = new FrameLayout(this);
        topScreen.setSystemUiVisibility(topScreen.getSystemUiVisibility() + UI_HIDE);
        topScreen.setBackgroundColor(0xfffbfbfb);
        setContentView(topScreen);

        int personsize = Simple.getDevicePixels(160);
        int peoplesize = Simple.getDevicePixels(200);

        notifyScreen = new HomeNotify(this);
        topScreen.addView(notifyScreen);

        launchScreen = new HomeLaunch(this);
        topScreen.addView(launchScreen);

        socialScreen = new HomeSocial(this);
        topScreen.addView(socialScreen);

        peopleScreen = new HomePeople(this);
        peopleScreen.setSize(peoplesize, personsize);
        topScreen.addView(peopleScreen);

        launchFrame = launchScreen.getPayloadFrame();

        ArchievementManager.reset("alertcall.shortclick");

        startService(new Intent(this, KioskService.class));
        startService(new Intent(this, CommService.class));
        startService(new Intent(this, OopsService.class));

        startService(new Intent(this, GCMRegistrationService.class));
        startService(new Intent(this, AccessibilityService.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        topScreen.setOnSystemUiVisibilityChangeListener(this);

        socialScreen.setTitle("Neuigkeiten");
        socialScreen.setConfig(null);

        launchScreen.setTitle("Mein Safehome");
        launchScreen.setConfig(null);

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

        if ((launchConfig == null) || CommonStatic.settingschanged)
        {
            if (launchGroup != null)
            {
                launchFrame.removeAllViews();
                backStack.clear();
                launchGroup = null;

                System.gc();
            }

            launchConfig = LaunchGroupRoot.getConfig();
            launchGroup = new LaunchGroupRoot(this);

            notifyScreen.setConfig(launchConfig);
            peopleScreen.setConfig(launchConfig);

            launchGroup.setConfig(null, launchConfig);

            launchFrame.addView(launchGroup);

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
                launchFrame.removeView((FrameLayout) lastview);
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

    private long backPressedTime;
    private int backPressedCount;

    private ArrayList<Object> backStack = new ArrayList<>();

    public void addViewToBackStack(Object view)
    {
        if (((ViewGroup) view).getParent() == null)
        {
            launchFrame.addView((FrameLayout) view);
            backStack.add(view);

            if (videoSurface != null) videoSurface.bringToFront();
        }
    }

    public void addView(Object view, ViewGroup.LayoutParams params)
    {
        launchFrame.addView((FrameLayout) view, params);
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

                launchFrame.removeView((FrameLayout) lastview);
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
                    launchFrame.removeView((FrameLayout) lastview);
                    backStack.remove(backStack.size() - 1);

                    ((BackKeyClient) lastview).onBackKeyExecuted();
                }
            }
            else
            {
                launchFrame.removeView((FrameLayout) lastview);
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