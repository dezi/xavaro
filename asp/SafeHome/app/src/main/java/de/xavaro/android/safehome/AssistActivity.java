package de.xavaro.android.safehome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import de.xavaro.android.common.Simple;

public class AssistActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assist);

        Simple.setAppContext(this);
    }
}
