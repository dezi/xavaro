package de.xavaro.android.common;

import android.preference.PreferenceActivity;
import android.view.MotionEvent;
import android.os.Bundle;
import android.util.Log;

public class CaptureSettings extends PreferenceActivity
{
    private static final String LOGTAG = CaptureSettings.class.getSimpleName();

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
