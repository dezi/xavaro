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

        if (CaptureOverlay.getInstance() != null)
        {
            CaptureOverlay.getInstance().attachToScreen();
        }

        if (CaptureRecorder.getInstance() != null)
        {
            CaptureRecorder.getInstance().onCreate();
        }
    }

    @Override
    protected void onDestroy()
    {
        if (CaptureRecorder.getInstance() != null)
        {
            CaptureRecorder.getInstance().onDestroy();
        }

        if (CaptureOverlay.getInstance() != null)
        {
            CaptureOverlay.getInstance().detachFromScreen();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (CaptureRecorder.getInstance() != null)
        {
            CaptureRecorder.getInstance().onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean dispatchGenericMotionEvent (MotionEvent ev)
    {
        if (CaptureOverlay.getInstance() != null)
        {
            return CaptureOverlay.getInstance().registerGenericMotionEvent(ev)
                    || super.dispatchGenericMotionEvent(ev);
        }

        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev)
    {
        if (CaptureOverlay.getInstance() != null)
        {
            return CaptureOverlay.getInstance().registerTouchEvent(ev)
                    || super.dispatchTouchEvent(ev);
        }

        return super.dispatchTouchEvent(ev);
    }
}
