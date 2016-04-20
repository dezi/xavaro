package de.xavaro.android.common;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class MessageService extends AccessibilityService
{
    private static final String LOGTAG = MessageService.class.getSimpleName();
    private static final ArrayList<MessageServiceCallback> callbacks = new ArrayList<>();

    public static void subscribe(MessageServiceCallback callback)
    {
        synchronized (callbacks)
        {
            if (!callbacks.contains(callback)) callbacks.add(callback);
        }
    }

    public static void unsubscribe(MessageServiceCallback callback)
    {
        synchronized (callbacks)
        {
            if (callbacks.contains(callback)) callbacks.remove(callback);
        }
    }

    public static void unsubscribeAll()
    {
        synchronized (callbacks)
        {
            callbacks.clear();
        }
    }

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

        final JSONObject message = new JSONObject();

        Json.put(message, "text", text);
        Json.put(message, "app", app);
        Json.put(message, "type", type);

        Simple.makePost(new Runnable()
        {
            @Override
            public void run()
            {
                doCallbacks(message);
            }
        });
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

    public void doCallbacks(JSONObject message)
    {
        synchronized (callbacks)
        {
            for (MessageServiceCallback callback : callbacks)
            {
                int action = callback.onMessageReceived(message);

                if (action == GLOBAL_ACTION_BACK)
                {
                    performGlobalAction(action);
                }
            }
        }
    }

    public interface MessageServiceCallback
    {
        int onMessageReceived(JSONObject message);
    }
}
