package de.xavaro.android.safehome;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.CaptureActivity;
import de.xavaro.android.common.AppWorkerHandler;
import de.xavaro.android.common.NotificationService;
import de.xavaro.android.common.AppInfoHandler;
import de.xavaro.android.common.BackKeyClient;
import de.xavaro.android.common.BackKeyMaster;
import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.GCMRegistrationService;
import de.xavaro.android.common.MediaSurface;
import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.CaptureRecorder;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.VoiceIntentResolver;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class HomeActivity extends CaptureActivity implements
        View.OnSystemUiVisibilityChangeListener,
        MediaSurface.VideoSurfaceHandler,
        VoiceIntentResolver,
        AppWorkerHandler,
        AppInfoHandler,
        BackKeyMaster
{
    private static final String LOGTAG = HomeActivity.class.getSimpleName();

    public static HomeActivity instance;
    public static HomeActivity getInstance()
    {
        return instance;
    }

    public static int titleSpace;
    public static int notifySize;
    public static int personSize;
    public static int peopleSize;
    public static int launchWid;
    public static int launchHei;

    private FrameLayout topScreen;
    private FrameLayout videoSurface;
    private FrameLayout launchFrame;

    private HomeNotify notifyScreen;
    private HomeLaunch launchScreen;
    private HomePeople peopleScreen;
    private HomeSocial socialScreen;
    private HomeWorker workerScreen;

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

        Log.d(LOGTAG, "========================> onCreate");

        instance = this;

        Simple.setThreadPolicy();

        //
        // Layout basic sizes.
        //

        Log.d(LOGTAG,"onCreate: getDeviceWidth=" + Simple.getDeviceWidth());
        Log.d(LOGTAG,"onCreate: getDeviceHeight=" + Simple.getDeviceHeight());

        Log.d(LOGTAG,"onCreate: getActionBarHeight=" + Simple.getActionBarHeight());
        Log.d(LOGTAG,"onCreate: getStatusBarHeight=" + Simple.getStatusBarHeight());
        Log.d(LOGTAG,"onCreate: getNavigationBarHeight=" + Simple.getNavigationBarHeight());

        Log.d(LOGTAG,"onCreate: getDensity=" + Simple.getDensity());
        Log.d(LOGTAG,"onCreate: getDensityDPI=" + Simple.getDensityDPI());
        Log.d(LOGTAG,"onCreate: getScaledDensity=" + Simple.getScaledDensity());

        //
        // Determinate person size in people navigation bar.
        //

        if (Simple.isTablet())
        {
            CommonStatic.LaunchItemSize = Simple.getDeviceSmallestSize() / 4;
        }
        else
        {
            CommonStatic.LaunchItemSize = (Simple.getDeviceSmallestSize() - 64) / 3;
        }

        personSize = Simple.getDeviceSmallestSize() / 5;
        peopleSize = Simple.getNavigationBarHeight() + (personSize * 6 / 4);

        titleSpace = Simple.getDevicePixels(40);
        notifySize = 0;

        launchWid = CommonStatic.LaunchItemSize * 2 + Simple.DP(32);
        launchHei = CommonStatic.LaunchItemSize * 2 + Simple.DP(32) + HomeActivity.titleSpace;

        //
        // Build views.
        //

        topScreen = new FrameLayout(this);
        topScreen.setSystemUiVisibility(topScreen.getSystemUiVisibility() + UI_HIDE);
        topScreen.setBackgroundColor(0xfffbfbfb);
        setContentView(topScreen);

        notifyScreen = new HomeNotify(this);
        topScreen.addView(notifyScreen);

        launchScreen = new HomeLaunch(this);
        topScreen.addView(launchScreen);

        peopleScreen = new HomePeople(this);
        topScreen.addView(peopleScreen);

        socialScreen = new HomeSocial(this);
        topScreen.addView(socialScreen);

        workerScreen = new HomeWorker(this);
        topScreen.addView(workerScreen);

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

        Log.d(LOGTAG, "========================> onPostCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        topScreen.setOnSystemUiVisibilityChangeListener(this);

        socialScreen.setTitle("Neuigkeiten");
        socialScreen.setConfig(null);

        launchScreen.setTitle("Mein Safehome");
        launchScreen.setConfig(null);

        //
        // Check important settings.
        //

        NotificationService.checkStatus();
        AccessibilityService.checkStatus();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        Log.d(LOGTAG, "========================> onDestroy");

        //
        // Make sure we are not a zombie.
        //

        System.exit(0);
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

    @Override
    public void onStartWorker(JSONObject config)
    {
        LaunchItem launchItem = LaunchItem.createLaunchItem(this, null, config);
        launchItem.onMyClick();
    }

    //region App info handling

    public String getBetaVersion()
    {
        return GlobalConfigs.BetaVersion;
    }

    public String getBetaVersionDate()
    {
        return GlobalConfigs.BetaVersionDate;
    }

    //endregion App info handling

    //region onBackPressed handling

    //
    // Execute back presses either within internal
    // stack, call configuration if back pressed
    // serveral times or return to android system.
    //

    private long backPressedTime;
    private int backPressedCount;

    private ArrayList<Object> backStack = new ArrayList<>();

    public void addWorkerToBackStack(String title, ViewGroup view)
    {
        workerScreen.getPayloadFrame().addView(view);
        workerScreen.setVisibility(View.VISIBLE);
        workerScreen.setTitle(title);
        workerScreen.bringToFront();

        if (! backStack.contains(workerScreen)) backStack.add(workerScreen);

        if (videoSurface != null) videoSurface.bringToFront();
    }

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

    private final Runnable delayOnBackPressedFinal = new Runnable()
    {
        @Override
        public void run()
        {
            executeOnBackPressedFinal();
        }
    };

    public void executeOnBackPressedFinal()
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
                if (backStack.size() > 0)
                {
                    Object lastview = backStack.remove(backStack.size() - 1);

                    if (lastview instanceof HomeWorker)
                    {
                        ((HomeWorker) lastview).getPayloadFrame().removeAllViews();
                        ((HomeWorker) lastview).setVisibility(View.GONE);
                    }
                    else
                    {
                        launchFrame.removeView((FrameLayout) lastview);

                        if (lastview instanceof LaunchFrame)
                        {
                            ((LaunchFrame) lastview).onBackKeyExecuted();
                        }
                    }
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

        Log.d(LOGTAG, "==========================>onBackPressed: 1=" + backStack.size());

        if (backStack.size() > 0)
        {
            for (Object fl : backStack)
            {
                Log.d(LOGTAG, "==========================>onBackPressed: 1=" + fl.toString());
            }

            Object lastview = backStack.get(backStack.size() - 1);

            if (lastview instanceof BackKeyClient)
            {
                Log.d(LOGTAG, "==========================>onBackPressed: 1wanted=?");

                if (! ((BackKeyClient) lastview).onBackKeyWanted())
                {
                    Log.d(LOGTAG, "==========================>onBackPressed: 1wanted=false");

                    if (! (lastview instanceof HomeWorker))
                    {
                        Simple.removeFromParent((ViewGroup) lastview);
                    }

                    ((BackKeyClient) lastview).onBackKeyExecuted();

                    backStack.remove(backStack.size() - 1);
                }
            }
            else
            {
                Simple.removeFromParent((ViewGroup) lastview);
                backStack.remove(backStack.size() - 1);
            }

            if (backStack.size() == 0)
            {
                launchScreen.checkPayloadFrame();
                workerScreen.checkPayloadFrame();
            }
            else
            {
                lastview = backStack.get(backStack.size() - 1);

                Log.d(LOGTAG, "==========================>onBackPressed: lastview=" + lastview);

                if (lastview instanceof LaunchGroup)
                {
                    ((LaunchGroup) lastview).adjustArrows();
                }
            }

            return;
        }

        Log.d(LOGTAG, "==========================>onBackPressed: 2");
        if (socialScreen.onBackKeyWanted()) return;
        Log.d(LOGTAG, "==========================>onBackPressed: 3");
        if (launchScreen.onBackKeyWanted()) return;
        Log.d(LOGTAG, "==========================>onBackPressed: 4");

        //
        // Top level back press handling.
        //

        long now = System.currentTimeMillis();

        if ((now - backPressedTime) > 750)
        {
            backPressedCount = 0;
        }
        else
        {
            backPressedCount += 1;
        }

        backPressedTime = now;

        handler.removeCallbacks(delayOnBackPressedFinal);

        if (backPressedCount >= 4)
        {
            LaunchGroupPreferences.getInstance(this).open();
        }
        else
        {
            //
            // Execute back press within given time.
            //

            handler.postDelayed(delayOnBackPressedFinal,750);
        }
    }

    //endregion onBackPressed handling

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(LOGTAG, "onActivityResult: request:" + requestCode + " result:" + resultCode);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCollectVoiceIntent(VoiceIntent voiceintent)
    {
        launchGroup.onCollectVoiceIntent(voiceintent);
        peopleScreen.onCollectVoiceIntent(voiceintent);
    }

    @Override
    public void onResolveVoiceIntent(VoiceIntent voiceintent)
    {
        launchGroup.onResolveVoiceIntent(voiceintent);
        peopleScreen.onResolveVoiceIntent(voiceintent);
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        return launchGroup.onExecuteVoiceIntent(voiceintent, index)
                || peopleScreen.onExecuteVoiceIntent(voiceintent, index);
    }
}