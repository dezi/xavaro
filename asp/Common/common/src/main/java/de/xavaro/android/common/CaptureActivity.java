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
        return CaptureOverlay.getInstance().registerGenericMotionEvent(ev)
                || super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev)
    {
        return CaptureOverlay.getInstance().registerTouchEvent(ev)
                || super.dispatchTouchEvent(ev);
    }
}
