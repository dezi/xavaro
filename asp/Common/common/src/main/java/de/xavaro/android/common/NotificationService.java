package de.xavaro.android.common;

import android.content.Intent;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.util.Log;

public class NotificationService extends NotificationListenerService
{
    private static final String LOGTAG = NotificationService.class.getSimpleName();

    public static boolean checkAvailable()
    {
        try
        {
            String binds = Settings.Secure.getString(
                    Simple.getContentResolver(),
                    "enabled_notification_listeners");

            return (binds != null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return true;
    }

    public static boolean checkEnabled()
    {
        return checkEnabled(false);
    }

    public static boolean checkEnabled(boolean verbose)
    {
        try
        {
            String binds = Settings.Secure.getString(
                    Simple.getContentResolver(),
                    "enabled_notification_listeners");

            boolean found = (binds != null) && binds.contains(NotificationService.class.getName());

            if (verbose)
            {
                Log.d(LOGTAG, "checkEnabled=" + binds);
                Log.d(LOGTAG, "checkEnabled=" + found);
            }

            return found;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static final Runnable selectNotificationsSettings = new Runnable()
    {
        @Override
        public void run()
        {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            ProcessManager.launchIntent(intent);
        }
    };

    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();

        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = (String) extras.getCharSequence("android.text");
        String ticker = (String) sbn.getNotification().tickerText;

        Log.d(LOGTAG, "onNotificationPosted: Bundle:" + extras.toString());
        Log.d(LOGTAG, "onNotificationPosted: Package:" + pack);
        Log.d(LOGTAG, "onNotificationPosted: Ticker:" + ticker);
        Log.d(LOGTAG, "onNotificationPosted: Title:" + title);
        Log.d(LOGTAG, "onNotificationPosted: Text:" + text);
        Log.d(LOGTAG, "onNotificationPosted: Key:" + sbn.getKey());
        Log.d(LOGTAG, "onNotificationPosted: GroupKey:" + sbn.getGroupKey());
        Log.d(LOGTAG, "onNotificationPosted: Tag:" + sbn.getTag());
        Log.d(LOGTAG, "onNotificationRemoved: Id:" + sbn.getId());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();

        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = (String) extras.getCharSequence("android.text");
        String ticker = (String) sbn.getNotification().tickerText;

        Log.d(LOGTAG, "onNotificationRemoved: Bundle:" + extras.toString());
        Log.d(LOGTAG, "onNotificationRemoved: Package:" + pack);
        Log.d(LOGTAG, "onNotificationRemoved: Ticker:" + ticker);
        Log.d(LOGTAG, "onNotificationRemoved: Title:" + title);
        Log.d(LOGTAG, "onNotificationRemoved: Text:" + text);
        Log.d(LOGTAG, "onNotificationRemoved: Key:" + sbn.getKey());
        Log.d(LOGTAG, "onNotificationRemoved: GroupKey:" + sbn.getGroupKey());
        Log.d(LOGTAG, "onNotificationRemoved: Tag:" + sbn.getTag());
        Log.d(LOGTAG, "onNotificationRemoved: Id:" + sbn.getId());
    }
}