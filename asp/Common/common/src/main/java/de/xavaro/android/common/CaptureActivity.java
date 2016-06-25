package de.xavaro.android.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.util.Log;

public class CaptureActivity extends AppCompatActivity
{
    private static final String LOGTAG = CaptureActivity.class.getSimpleName();

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
        Log.d(LOGTAG, "dispatchGenericMotionEvent: " + ev);

        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev)
    {
        Log.d(LOGTAG, "dispatchTouchEvent: " + ev);

        return super.dispatchTouchEvent(ev);
    }
}
