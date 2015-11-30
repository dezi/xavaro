package de.xavaro.android.safehome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity
{
    private final String LOGTAG = "HomeActivity";

    private LaunchGroup launchGroup;
    private JSONObject config;
    private KioskService kioskService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        launchGroup = new LaunchGroup(this);
        FrameLayout topscreen = (FrameLayout) findViewById(R.id.top_screen);
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
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (kioskService != null) unbindService(kioskConnection);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        Log.d(LOGTAG, "onWindowFocusChanged=" + hasFocus);

        if (kioskService != null) kioskService.setFocused(LOGTAG,hasFocus);
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