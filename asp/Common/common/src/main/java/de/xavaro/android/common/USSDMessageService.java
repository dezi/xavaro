package de.xavaro.android.common;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;
import android.util.Log;

public class USSDMessageService extends AccessibilityService
{
    private static final String LOGTAG = USSDMessageService.class.getSimpleName();

    @Override
    public void onCreate()
    {
        Log.d(LOGTAG, "onCreate");

        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        String text = event.getText().toString();
        String app = event.getPackageName().toString();
        int type = event.getEventType();

        Log.d(LOGTAG, "onAccessibilityEvent:" + app + "=" + type + "=" + text);

        if (text.contains(", OK")) performGlobalAction(GLOBAL_ACTION_BACK);
    }

    @Override
    public void onInterrupt()
    {
        Log.d(LOGTAG, "onInterrupt");
    }


    @Override
    protected void onServiceConnected()
    {
        super.onServiceConnected();

        Log.d(LOGTAG, "onServiceConnected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;

        setServiceInfo(info);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(LOGTAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }
}
