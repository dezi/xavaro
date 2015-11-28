package de.xavaro.android.safehome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class HomeActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LaunchGroup lg = new LaunchGroup(this);
        FrameLayout topscreen = (FrameLayout) findViewById(R.id.top_screen);
        topscreen.addView(lg);
    }
}
