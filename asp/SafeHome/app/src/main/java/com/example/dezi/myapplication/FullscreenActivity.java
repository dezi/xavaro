package com.example.dezi.myapplication;

import android.app.AlertDialog;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;
import android.util.Log;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.app.Activity;
import android.view.WindowManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity
{
    private static final int UI_IS_HIDDEN
            = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;

    private static final int UI_HIDE
            = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//          | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//          | View.SYSTEM_UI_FLAG_IMMERSIVE
//          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d("dezi", "onCreate...");

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        toggle();
                    }
                });

        mContentView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener()
                {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        Log.d("dezi", "onSystemUiVisibilityChange:" + visibility);

                        /*
                        if ((visibility & UI_IS_HIDDEN) != UI_IS_HIDDEN)
                        {
                            Log.d("dezi", "onSystemUiVisibilityChange => hide");

                            mHideHandler.postDelayed(mHidePart2Runnable, 0);
                        }
                        */
                    }
                });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        findViewById(R.id.dummy_button).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Log.d("dezi", "onClick (button)...");

                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);

                        Log.d("dezi", "onClick (button) done.");
                    }
                });

        findViewById(R.id.whatsapp_button).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Log.d("dezi", "onClick (whatsapp_button)...");

                        Uri uri = Uri.parse("smsto:" + "4915152493345");
                        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        sendIntent.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                        sendIntent.setPackage("com.whatsapp");
                        startActivity(Intent.createChooser(sendIntent, ""));

                        Log.d("dezi", "onClick (whatsapp_button) done.");
                    }
                });

        findViewById(R.id.choose_launcher).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Log.d("dezi", "onClick (choose_launcher)...");

                        PackageManager pm = getPackageManager();
                        ComponentName cn = new ComponentName(FullscreenActivity.this, FakeHome.class);
                        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);

                        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

                        Log.d("dezi", "onClick (choose_launcher) done.");
                    }
                });

        findViewById(R.id.choose_assist).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Log.d("dezi", "onClick (choose_assist)...");

                        PackageManager pm = getPackageManager();
                        ComponentName cn = new ComponentName(FullscreenActivity.this, FakeAssist.class);
                        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                        Intent startMain = new Intent(Intent.ACTION_ASSIST);
                        startMain.addCategory(Intent.CATEGORY_DEFAULT);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);

                        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

                        Log.d("dezi", "onClick (choose_assist) done.");
                    }
                });

        startService(new Intent(this, KioskService.class));

        isDefaultHome();

        isDefaultAssist();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        Log.d("dezi", "onPostCreate...");
    }

    private boolean isDefaultHome()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = getPackageManager().resolveActivity(intent, 0);

        if (res.activityInfo == null) return false;

        Log.d("dezi","Default home = " + res.activityInfo.packageName);

        return res.activityInfo.packageName.equals(getPackageName());
    }

    private boolean isDefaultAssist()
    {
        Intent intent = new Intent(Intent.ACTION_ASSIST);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        ResolveInfo res = getPackageManager().resolveActivity(intent, 0);

        if (res.activityInfo == null) return false;

        Log.d("dezi","Default assist = " + res.activityInfo.packageName);

        return res.activityInfo.packageName.equals(getPackageName());
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            if (AUTO_HIDE)
            {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }

            Log.d("dezi", "OnTouchListener...");

            return false;
        }
    };

    private void toggle()
    {
        if (mVisible)
        {
            hide();
        } else
        {
            show();
        }
    }

    private void hide()
    {
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable()
    {
        @Override
        public void run()
        {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

            mContentView.setSystemUiVisibility(UI_HIDE);
        }
    };

    private void show()
    {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable()
    {
        @Override
        public void run()
        {
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onResume()
    {
        Log.d("dezi", "onResume...");

        super.onResume();
    }

    @Override
    public void onPause()
    {
        Log.d("dezi", "onPause...");

        super.onPause();
    }

    @Override
    public void onStop()
    {
        Log.d("dezi", "onStop...");

        super.onStop();
    }

    @Override
    public void onBackPressed()
    {
        Log.d("dezi", "onBackPressed...");

        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        Log.d("dezi", "onWindowFocusChanged=" + hasFocus);

        super.onWindowFocusChanged(hasFocus);
    }
}