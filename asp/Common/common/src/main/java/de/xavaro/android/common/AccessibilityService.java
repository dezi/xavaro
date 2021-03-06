package de.xavaro.android.common;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.provider.Settings;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService
{
    private static final String LOGTAG = AccessibilityService.class.getSimpleName();
    private static final ArrayList<MessageServiceCallback> callbacks = new ArrayList<>();

    public static void checkStatus()
    {
        if (isRequested() && ! checkEnabled())
        {
            NotifyIntent alert = new NotifyIntent();

            alert.title = Simple.getTrans(R.string.accessibility_service_alert_text);
            alert.importance = NotifyIntent.URGENT;
            alert.iconres = CommonConfigs.IconResSettingsAndroid;

            alert.followText = Simple.getTrans(R.string.accessibility_service_alert_button);
            alert.followRunner = selectAccessibilitySettings;

            alert.declineText = Simple.getTrans(R.string.accessibility_service_alert_about);
            alert.declineRunner = aboutAccessibilitySettings;

            alert.checkCondition = checkAccessibilitySettings;

            NotifyManager.addNotification(alert);
        }
    }

    public static boolean isRequested()
    {
        return Simple.getSharedPrefBoolean("admin.accessibility.enabled");
    }

    public static boolean checkAvailable()
    {
        try
        {
            Settings.Secure.getInt(
                    Simple.getAnyContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);

            return true;
        }
        catch (Exception ignore)
        {
            //
            // Will throw if feature not available.
            //
        }

        return false;
    }

    public static boolean checkEnabled()
    {
        return checkEnabled(false);
    }

    public static boolean checkEnabled(boolean verbose)
    {
        int enabled;

        try
        {
            enabled = Settings.Secure.getInt(
                    Simple.getAnyContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);

            if (verbose) Log.d(LOGTAG, "checkEnabled=" + enabled);
        }
        catch (Exception ignore)
        {
            if (verbose) Log.d(LOGTAG, "checkEnabled: service not present");

            return false;
        }

        try
        {
            if (enabled != 0)
            {
                String binds = Settings.Secure.getString(
                        Simple.getAnyContext().getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

                boolean found = (binds != null) && binds.contains(AccessibilityService.class.getName());

                if (verbose)
                {
                    Log.d(LOGTAG, "checkEnabled=" + binds);
                    Log.d(LOGTAG, "checkEnabled=" + found);
                }

                return found;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static final NotifyChecker checkAccessibilitySettings = new NotifyChecker()
    {
        @Override
        public boolean onCheckNotifyCondition(NotifyIntent intent)
        {
            return (isRequested() && ! checkEnabled());
        }
    };

    public static final Runnable aboutAccessibilitySettings = new Runnable()
    {
        @Override
        public void run()
        {
            Simple.makeAlert(
                    Simple.getTrans(R.string.pref_basic_safety_accessibility_summary),
                    Simple.getTrans(R.string.pref_basic_safety_accessibility_service));
        }
    };

    public static final Runnable selectAccessibilitySettings = new Runnable()
    {
        @Override
        public void run()
        {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            ProcessManager.launchIntent(intent);
        }
    };

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
        String app = (event.getPackageName() != null) ? event.getPackageName().toString() : "unknown";
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

        checkEnabled(true);

        return super.onStartCommand(intent, flags, startId);
    }

    public void doCallbacks(JSONObject message)
    {
        synchronized (callbacks)
        {
            for (MessageServiceCallback callback : callbacks)
            {
                int action = callback.onAccessibilityMessageReceived(message);

                if (action == GLOBAL_ACTION_BACK)
                {
                    performGlobalAction(action);
                }
            }
        }
    }

    public interface MessageServiceCallback
    {
        int onAccessibilityMessageReceived(JSONObject message);
    }
}
