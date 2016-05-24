package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.PendingIntent;
import android.provider.Settings;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NotificationService extends NotificationListenerService
{
    private static final String LOGTAG = NotificationService.class.getSimpleName();

    private static final Map<String, ArrayList<Runnable>> callbacks = new HashMap<>();

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

        return false;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                ProcessManager.launchIntent(intent);
            }
        }
    };

    private void onWhatsAppMessage(StatusBarNotification sbn, boolean removed)
    {
        Intent intent = getIntent(sbn.getNotification().contentIntent);
        Bundle extras = sbn.getNotification().extras;

        if ((intent == null) || (extras == null)) return;

        //
        // WhatsApp notifications contain user
        // phone number in different locations.
        //

        String whatsappid = intent.hasExtra("jid") ? intent.getExtras().getString("jid") : null;
        if (whatsappid == null) whatsappid = sbn.getTag();

        String sender = extras.getString("android.title");
        String text = extras.getString("android.text");
        String summary = extras.getString("android.summaryText");

        //
        // If the text equals the summary the notification
        // is not a new message but a summary. Drop.
        //

        if (Simple.equals(summary, text)) return;

        Log.d(LOGTAG, "onWhatsAppMessage: ------------------");
        Log.d(LOGTAG, "onWhatsAppMessage: removed=" + removed);
        Log.d(LOGTAG, "onWhatsAppMessage: whatsappid=" + whatsappid);
        Log.d(LOGTAG, "onWhatsAppMessage: sender=" + sender);
        Log.d(LOGTAG, "onWhatsAppMessage: text=" + text);

        if (removed)
        {
            SimpleStorage.put("notifications", "whatsapp" + ".count." + whatsappid, 0);
            SimpleStorage.remove("notifications", "whatsapp" + ".texts." + whatsappid);
        }
        else
        {
            SimpleStorage.addInt("notifications", "whatsapp" + ".count." + whatsappid, 1);
            SimpleStorage.addArray("notifications", "whatsapp" + ".texts." + whatsappid, text);
        }

        SimpleStorage.put("notifications", "whatsapp" + ".stamp." + whatsappid, Simple.nowAsISO());

        doCallbacks("whatsapp");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();

        if (Simple.equals(pack, "com.whatsapp"))
        {
            onWhatsAppMessage(sbn, false);
            return;
        }

        dumpNotification("onNotificationPosted", sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();

        if (Simple.equals(pack, "com.whatsapp"))
        {
            onWhatsAppMessage(sbn, true);
            return;
        }

        dumpNotification("onNotificationRemoved", sbn);
    }

    @Nullable
    private Intent getIntent(PendingIntent pendingIntent)
    {
        if (pendingIntent != null)
        {
            //
            // Retrieve a pending intent using reflection.
            //

            try
            {
                Method getIntent = PendingIntent.class.getDeclaredMethod("getIntent");
                return (Intent) getIntent.invoke(pendingIntent);
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return null;
    }

    private void dumpExtras(String type, Bundle extras)
    {
        if (extras == null)
        {
            Log.d(LOGTAG, type + ": has no extras");

            return;
        }

        Set<String> keys = extras.keySet();

        for (String key : keys)
        {
            Object keyval = extras.get(key);
            if (keyval == null) continue;

            Log.d(LOGTAG, type + ": " + key + ":" + keyval.toString());
        }
    }

    private void dumpNotification(String type, StatusBarNotification sbn)
    {
        Bundle extras = sbn.getNotification().extras;

        Log.d(LOGTAG, type + ": ---------------------------");
        Log.d(LOGTAG, type + ": Package:" + sbn.getPackageName());
        Log.d(LOGTAG, type + ": Ticker:" + sbn.getNotification().tickerText);
        Log.d(LOGTAG, type + ": Title:" + extras.getString("android.title"));
        Log.d(LOGTAG, type + ": Text:" + extras.getCharSequence("android.text"));
        Log.d(LOGTAG, type + ": Tag:" + sbn.getTag());
        Log.d(LOGTAG, type + ": Id:" + sbn.getId());
        Log.d(LOGTAG, type + ": isOngoing:" + sbn.isOngoing());

        dumpExtras(type + ": extras", extras);

        Intent pi = getIntent(sbn.getNotification().contentIntent);
        if (pi != null) dumpExtras(type + ": intent", pi.getExtras());
    }

    public static void subscribe(String appname, Runnable runner)
    {
        synchronized (callbacks)
        {
            ArrayList<Runnable> runners = callbacks.get(appname);
            if (runners == null) runners = new ArrayList<>();
            if (! runners.contains(runner)) runners.add(runner);
        }
    }

    public static void unsubscribe(String appname, Runnable runner)
    {
        synchronized (callbacks)
        {
            ArrayList<Runnable> runners = callbacks.get(appname);
            if (runners == null) runners = new ArrayList<>();
            if (runners.contains(runner)) runners.remove(runner);
        }
    }

    private static void doCallbacks(String appname)
    {
        synchronized (callbacks)
        {
            ArrayList<Runnable> runners = callbacks.get(appname);

            if (runners != null)
            {
                for (Runnable runner : runners)
                {
                    Simple.makePost(runner);
                }
            }
        }
    }
}