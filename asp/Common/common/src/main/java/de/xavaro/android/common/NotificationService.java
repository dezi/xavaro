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

    public static void checkStatus()
    {
        if (isRequested() && ! checkEnabled())
        {
            NotifyIntent alert = new NotifyIntent();

            alert.title = Simple.getTrans(R.string.notification_service_alert_text);
            alert.importance = NotifyIntent.URGENT;
            alert.iconres = CommonConfigs.IconResSettingsAndroid;

            alert.followText = Simple.getTrans(R.string.notification_service_alert_button);
            alert.followRunner = selectNotificationsSettings;

            alert.declineText = Simple.getTrans(R.string.notification_service_alert_about);
            alert.declineRunner = aboutNotificationsSettings;

            alert.checkCondition = checkNotificationsSettings;

            NotifyManager.addNotification(alert);
        }
    }

    public static boolean isRequested()
    {
        return Simple.getSharedPrefBoolean("admin.notifications.enabled");
    }

    public static boolean checkAvailable()
    {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2);
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

    public static final NotifyChecker checkNotificationsSettings = new NotifyChecker()
    {
        @Override
        public boolean onCheckNotifyCondition(NotifyIntent intent)
        {
            return (isRequested() && ! checkEnabled());
        }
    };

    public static final Runnable aboutNotificationsSettings = new Runnable()
    {
        @Override
        public void run()
        {
            Simple.makeAlert(
                    Simple.getTrans(R.string.pref_basic_safety_notifications_summary),
                    Simple.getTrans(R.string.pref_basic_safety_notifications_service));
        }
    };

    public static final Runnable selectNotificationsSettings = new Runnable()
    {
        @Override
        public void run()
        {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            ProcessManager.launchIntent(intent);
        }
    };

    private void onPhoneCall(StatusBarNotification sbn, boolean removed)
    {
        Bundle extras = sbn.getNotification().extras;

        if (extras == null) return;

        String sender = extras.getString("android.text");
        String title = extras.getString("android.title");

        Log.d(LOGTAG, "onPhoneCall: ---------------------------" + removed);
        Log.d(LOGTAG, "onPhoneCall: Package:" + sbn.getPackageName());
        Log.d(LOGTAG, "onPhoneCall: Title:" + title);
        Log.d(LOGTAG, "onPhoneCall: Sender:" + sender);

        if ((sender == null) || (title == null)) return;

        Log.d(LOGTAG, "onPhoneCall: Sender:" + sender);

        String phone = ProfileImages.getPhoneFromName(sender);

        if (phone == null) return;

        if (removed)
        {
            SimpleStorage.put("notifications", "phonecall" + ".count." + phone, 0);
            SimpleStorage.remove("notifications", "phonecall" + ".texts." + phone);
        }
        else
        {
            String[] parts = title.split(" ");
            int calls = Simple.parseNumber(parts[ 0 ]);

            SimpleStorage.put("notifications", "phonecall" + ".count." + phone, calls);
        }

        SimpleStorage.put("notifications", "phonecall" + ".stamp." + phone, Simple.nowAsISO());

        doCallbacks("phonecall", phone);
    }

    private void onSMSMessage(StatusBarNotification sbn, boolean removed)
    {
        Bundle extras = sbn.getNotification().extras;

        if (extras == null) return;

        String sender = extras.getString("android.title");
        String ticker = (String) sbn.getNotification().tickerText;

        Log.d(LOGTAG, "onSMSMessage: ---------------------------" + removed);
        Log.d(LOGTAG, "onSMSMessage: Package:" + sbn.getPackageName());
        Log.d(LOGTAG, "onSMSMessage: Ticker:" + ticker);
        Log.d(LOGTAG, "onSMSMessage: Text:" + extras.getCharSequence("android.text"));

        if ((sender == null) || (ticker == null)) return;

        Log.d(LOGTAG, "onSMSMessage: Sender:" + sender);

        if (! ticker.startsWith(sender + ": ")) return;

        String text = ticker.substring(sender.length() + 2);
        String phone = ProfileImages.getPhoneFromName(sender);

        Log.d(LOGTAG, "onSMSMessage: Message:" + text);
        Log.d(LOGTAG, "onSMSMessage: Phone:" + phone);

        if (phone == null) return;

        //
        // The SMS service removes an old notification when
        // a new notification is setup. We cannot tell, if
        // the remove is due to clearing the list as such or
        // due to another message beeing notified.
        //
        // So we perform the removal with a delay. If in the
        // meantime a new message comes in, the phone number
        // ist removed from the removal list.
        //

        Simple.removePost(onSMSMessageRemoveAll);

        if (removed)
        {
            synchronized (removeSMSPhones)
            {
                if (! removeSMSPhones.contains(phone))
                {
                    removeSMSPhones.add(phone);
                }
            }
        }
        else
        {
            synchronized (removeSMSPhones)
            {
                if (removeSMSPhones.contains(phone))
                {
                    removeSMSPhones.add(phone);
                }
            }

            SimpleStorage.addInt("notifications", "smsmms" + ".count." + phone, 1);
            SimpleStorage.addArray("notifications", "smsmms" + ".texts." + phone, text);
            SimpleStorage.put("notifications", "smsmms" + ".stamp." + phone, Simple.nowAsISO());

            doCallbacks("smsmms", phone);
        }

        //
        // Make sure, all removals are delayed by the given interval.
        //

        Simple.makePost(onSMSMessageRemoveAll, 1000);
    }

    private final ArrayList<String> removeSMSPhones = new ArrayList<>();

    private final Runnable onSMSMessageRemoveAll = new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (removeSMSPhones)
            {
                for (String phone : removeSMSPhones)
                {
                    SimpleStorage.put("notifications", "smsmms" + ".count." + phone, 0);
                    SimpleStorage.remove("notifications", "smsmms" + ".texts." + phone);
                    SimpleStorage.put("notifications", "smsmms" + ".stamp." + phone, Simple.nowAsISO());

                    doCallbacks("smsmms", phone);
                }

                removeSMSPhones.clear();
            }
        }
    };

    private void onSkypeCall(StatusBarNotification sbn, boolean removed)
    {
        Intent intent = getIntent(sbn.getNotification().contentIntent);
        Bundle extras = sbn.getNotification().extras;

        if ((intent == null) || (extras == null)) return;

        String skypename = intent.hasExtra("com.skype.identitiy")
                ? intent.getExtras().getString("com.skype.identitiy")
                : null;

        if (skypename == null) return;

        Log.d(LOGTAG, "onSkypeCall: ------------------" + removed);
        Log.d(LOGTAG, "onSkypeCall: removed=" + removed);
        Log.d(LOGTAG, "onSkypeCall: skypename=" + skypename);

        if (removed)
        {
            SimpleStorage.put("notifications", "skype" + ".count." + skypename, 0);
            SimpleStorage.remove("notifications", "skype" + ".texts." + skypename);
        }
        else
        {
            SimpleStorage.addInt("notifications", "skype" + ".count." + skypename, 1);
        }

        SimpleStorage.put("notifications", "skype" + ".stamp." + skypename, Simple.nowAsISO());

        doCallbacks("skype", skypename);
    }

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
        if (whatsappid == null) return;

        if (whatsappid.endsWith("@s.whatsapp.net"))
        {
            whatsappid = whatsappid.substring(0, whatsappid.length() - 15);
        }

        if (! whatsappid.startsWith("+"))
        {
            whatsappid = "+" + whatsappid;
        }

        String sender = extras.getString("android.title");
        String text = extras.getString("android.text");
        String summary = extras.getString("android.summaryText");

        //
        // If the text equals the summary the notification
        // is not a new message but a summary. Drop.
        //

        if (Simple.equals(summary, text)) return;

        Log.d(LOGTAG, "onWhatsAppMessage: ------------------" + removed);
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

        doCallbacks("whatsapp", whatsappid);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();

        if (Simple.equals(pack, "com.skype.raider"))
        {
            onSkypeCall(sbn, false);
            return;
        }

        if (Simple.equals(pack, "com.whatsapp"))
        {
            onWhatsAppMessage(sbn, false);
            return;
        }

        if (Simple.equals(pack, "com.android.mms"))
        {
            onSMSMessage(sbn, false);
            return;
        }

        if (Simple.equals(pack, "com.android.server.telecom"))
        {
            onPhoneCall(sbn, false);
            return;
        }

        dumpNotification("onNotificationPosted", sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();

        if (Simple.equals(pack, "com.skype.raider"))
        {
            onSkypeCall(sbn, true);
            return;
        }

        if (Simple.equals(pack, "com.whatsapp"))
        {
            onWhatsAppMessage(sbn, true);
            return;
        }

        if (Simple.equals(pack, "com.android.mms"))
        {
            onSMSMessage(sbn, true);
            return;
        }

        if (Simple.equals(pack, "com.android.server.telecom"))
        {
            onPhoneCall(sbn, false);
            return;
        }

        dumpNotification("onNotificationRemoved", sbn);
    }

    @Nullable
    private static Intent getIntent(PendingIntent pendingIntent)
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

    private static void dumpExtras(String type, Bundle extras)
    {
        if (extras == null)
        {
            Log.d(LOGTAG, type + ": has no extras");

            return;
        }

        try
        {
            Set<String> keys = extras.keySet();

            for (String key : keys)
            {
                Object keyval = extras.get(key);
                if (keyval == null) continue;

                Log.d(LOGTAG, type + ": " + key + ":" + keyval.toString());
            }
        }
        catch (Exception ignore)
        {
            //
            // Silently ignore.
            //
        }
    }

    private static void dumpNotification(String type, StatusBarNotification sbn)
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

        Intent intent = getIntent(sbn.getNotification().contentIntent);

        if (intent != null)
        {
            Log.d(LOGTAG, type + ": intent: data:" + intent.getDataString());

            dumpExtras(type + ": intent", intent.getExtras());
        }
    }

    public static void subscribe(String appname, String pfid, Runnable runner)
    {
        String mapkey = appname + "." + pfid;
        Log.d(LOGTAG, "subscribe: " + mapkey);

        synchronized (callbacks)
        {
            ArrayList<Runnable> runners = callbacks.get(mapkey);

            if (runners == null)
            {
                runners = new ArrayList<>();
                callbacks.put(mapkey, runners);
            }

            if (! runners.contains(runner)) runners.add(runner);
        }
    }

    public static void unsubscribe(String appname, String pfid, Runnable runner)
    {
        String mapkey = appname + "." + pfid;
        Log.d(LOGTAG, "unsubscribe: " + mapkey);

        synchronized (callbacks)
        {
            ArrayList<Runnable> runners = callbacks.get(mapkey);

            if ((runners != null) && runners.contains(runner))
            {
                runners.remove(runner);
            }
        }
    }

    public static void doCallbacks(String appname, String pfid)
    {
        String mapkey = appname + "." + pfid;
        Log.d(LOGTAG, "doCallbacks: " + mapkey);

        synchronized (callbacks)
        {
            ArrayList<Runnable> runners = callbacks.get(mapkey);

            if (runners != null)
            {
                for (Runnable runner : runners)
                {
                    Log.d(LOGTAG, "doCallbacks: " + appname + "=" + pfid);

                    Simple.makePost(runner);
                }
            }
        }
    }
}
