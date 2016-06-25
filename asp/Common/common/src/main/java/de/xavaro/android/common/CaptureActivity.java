package de.xavaro.android.common;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.os.Bundle;

public class CaptureActivity extends AppCompatActivity
{
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        CaptureOverlay.getInstance().attachToScreen();
    }

    @Override
    protected void onDestroy()
    {
        CaptureOverlay.getInstance().detachFromScreen();

        super.onDestroy();
    }

    @Override
    public boolean dispatchGenericMotionEvent (MotionEvent ev)
    {
        CaptureOverlay.getInstance().registerGenericMotionEvent(ev);

        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev)
    {
        CaptureOverlay.getInstance().registerTouchEvent(ev);

        return super.dispatchTouchEvent(ev);
    }
}
