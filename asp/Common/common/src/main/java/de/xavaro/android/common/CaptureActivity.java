package de.xavaro.android.common;

import android.content.Intent;
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
        CaptureRecorder.getInstance().onCreate();
    }

    @Override
    protected void onDestroy()
    {
        CaptureRecorder.getInstance().onDestroy();
        CaptureOverlay.getInstance().detachFromScreen();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        CaptureRecorder.getInstance().onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
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
