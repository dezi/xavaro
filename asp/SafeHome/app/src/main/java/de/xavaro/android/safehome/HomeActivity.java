package de.xavaro.android.safehome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class HomeActivity extends AppCompatActivity
{
    private final String LOGTAG = "HomeActivity";

    private LaunchGroup launchGroup;
    private JSONObject config;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        launchGroup = new LaunchGroup(this);
        FrameLayout topscreen = (FrameLayout) findViewById(R.id.top_screen);
        topscreen.addView(launchGroup);

        createConfig();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void createConfig()
    {
        config = StaticUtils.readRawTextResourceJSON(this,R.raw.default_config);

        if ((config == null) || ! config.has("launchgroup"))
        {
            Toast.makeText(this,"Keine <launchgroup> gefunden.",Toast.LENGTH_LONG).show();

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
}
