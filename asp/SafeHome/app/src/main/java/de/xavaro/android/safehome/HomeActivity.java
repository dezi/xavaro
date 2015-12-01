package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity
{
    private final String LOGTAG = "HomeActivity";

    public KioskService kioskService;

    private LaunchGroup launchGroup;
    private JSONObject config;
    private FrameLayout topscreen;

    private boolean wasPaused = false;
    private boolean lostFocus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        topscreen = (FrameLayout) findViewById(R.id.top_screen);

        launchGroup = new LaunchGroup(this);
        topscreen.addView(launchGroup);

        createConfig();

        startService(new Intent(this, KioskService.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent intent = new Intent(this, KioskService.class);
        bindService(intent, kioskConnection, Context.BIND_AUTO_CREATE);

        Log.d(LOGTAG, "onStart...");
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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        Log.d(LOGTAG, "onWindowFocusChanged=" + hasFocus);

        super.onWindowFocusChanged(hasFocus);

        if (kioskService != null) kioskService.setFocused(LOGTAG, hasFocus);

        lostFocus = ! hasFocus;
    }

    @Override
    public void onBackPressed()
    {
        if (backStack.size() > 0)
        {
            Object lastview = backStack.remove(backStack.size() - 1);
            topscreen.removeView((FrameLayout) lastview);

            return;
        }

        super.onBackPressed();
    }

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
            launchGroup.setConfig(config.getJSONObject("launchgroup"));
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    private ArrayList backStack = new ArrayList();

    public void addLauncherToBackStack(Object view)
    {
        topscreen.addView((FrameLayout) view);
        backStack.add((FrameLayout) view);
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
}