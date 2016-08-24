package de.xavaro.android.safehome;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import de.xavaro.android.common.CaptureOverlay;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsPermissions extends PreferenceFragments.BasicFragmentStub
{
    private static final String LOGTAG = PreferencesBasicsPermissions.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_basic_permissions;
        header.iconRes = CommonConfigs.IconResPermissions;
        header.fragment = PreferencesBasicsPermissions.class.getName();

        return header;
    }

    public PreferencesBasicsPermissions()
    {
        super();

        iconres = CommonConfigs.IconResPermissions;
        summaryres = R.string.pref_basic_permissions_summary;
    }

    public NicedPreferences.NiceListPreference PrefContactsProfRead;
    public NicedPreferences.NiceListPreference PrefContactsRead;
    public NicedPreferences.NiceListPreference PrefContactsWrite;
    public NicedPreferences.NiceListPreference PrefPhoneCall;
    public NicedPreferences.NiceListPreference PrefPhoneRead;
    public NicedPreferences.NiceListPreference PrefStorageRead;
    public NicedPreferences.NiceListPreference PrefStorageWrite;
    public NicedPreferences.NiceListPreference PrefRecordAudio;
    public NicedPreferences.NiceListPreference PrefSystemAlertWindow;

    public NicedPreferences.NiceListPreference PrefBluetoothRead;
    public NicedPreferences.NiceListPreference PrefBluetoothAdmin;

    public NicedPreferences.NiceListPreference PrefNetworkInternet;
    public NicedPreferences.NiceListPreference PrefNetworkState;
    public NicedPreferences.NiceListPreference PrefNetworkWifiState;
    public NicedPreferences.NiceListPreference PrefNetworkGCM;

    public NicedPreferences.NiceListPreference PrefOtherVibrate;
    public NicedPreferences.NiceListPreference PrefOtherWakelock;
    public NicedPreferences.NiceListPreference PrefOtherReceiveBoot;

    public void registerAll(Context context)
    {
        super.registerAll(context);

        NicedPreferences.NiceInfoPreference ip;
        NicedPreferences.NiceListPreference lp;

        String permprefix = "permissions";

        CharSequence[] permStatusText = Simple.getTransArray(R.array.pref_basic_permissions_status_vals);
        CharSequence[] permStatusKeys = Simple.getTransArray(R.array.pref_basic_permissions_status_keys);

        //
        // Contacts permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_contacts);
        ip.setSummary(R.string.pref_basic_permissions_contacts_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".contacts.profread");
        lp.setTitle(R.string.pref_basic_permissions_contacts_profread);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission("android.permission.READ_PROFILE");
            }
        });

        preferences.add(lp);
        PrefContactsProfRead = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".contacts.read");
        lp.setTitle(R.string.pref_basic_permissions_contacts_read);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.READ_CONTACTS);
            }
        });

        preferences.add(lp);
        PrefContactsRead = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".contacts.write");
        lp.setTitle(R.string.pref_basic_permissions_contacts_write);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.WRITE_CONTACTS);
            }
        });

        preferences.add(lp);
        PrefContactsWrite = lp;

        //
        // Phone permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_phone);
        ip.setSummary(R.string.pref_basic_permissions_phone_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".phone.read");
        lp.setTitle(R.string.pref_basic_permissions_phone_read);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.READ_PHONE_STATE);
            }
        });

        preferences.add(lp);
        PrefPhoneRead = lp;


        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".phone.call");
        lp.setTitle(R.string.pref_basic_permissions_phone_call);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.CALL_PHONE);
            }
        });

        preferences.add(lp);
        PrefPhoneCall = lp;

        //
        // Storage permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_storage);
        ip.setSummary(R.string.pref_basic_permissions_storage_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".storage.read");
        lp.setTitle(R.string.pref_basic_permissions_storage_read);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        preferences.add(lp);
        PrefStorageRead = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".storage.write");
        lp.setTitle(R.string.pref_basic_permissions_storage_write);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        preferences.add(lp);
        PrefStorageWrite = lp;

        //
        // Microphone permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_record_audio);
        ip.setSummary(R.string.pref_basic_permissions_record_audio_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".record.audio");
        lp.setTitle(R.string.pref_basic_permissions_record_audio_allow);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.RECORD_AUDIO);
            }
        });

        preferences.add(lp);
        PrefRecordAudio = lp;

        //
        // System alerts permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_system_alert);
        ip.setSummary(R.string.pref_basic_permissions_system_alert_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".system.alert_window");
        lp.setTitle(R.string.pref_basic_permissions_system_alert_window);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
            }
        });

        preferences.add(lp);
        PrefSystemAlertWindow = lp;

        //
        // Bluetooth permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_bluetooth);
        ip.setSummary(R.string.pref_basic_permissions_bluetooth_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".bluetooth.read");
        lp.setTitle(R.string.pref_basic_permissions_bluetooth_read);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.BLUETOOTH);
            }
        });

        preferences.add(lp);
        PrefBluetoothRead = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".bluetooth.admin");
        lp.setTitle(R.string.pref_basic_permissions_bluetooth_admin);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.BLUETOOTH_ADMIN);
            }
        });

        preferences.add(lp);
        PrefBluetoothAdmin = lp;

        //
        // Network permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_network);
        ip.setSummary(R.string.pref_basic_permissions_network_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".network.internet");
        lp.setTitle(R.string.pref_basic_permissions_network_internet);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.INTERNET);
            }
        });

        preferences.add(lp);
        PrefNetworkInternet = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".network.state");
        lp.setTitle(R.string.pref_basic_permissions_network_state);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.ACCESS_NETWORK_STATE);
            }
        });

        preferences.add(lp);
        PrefNetworkState = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".network.wifistate");
        lp.setTitle(R.string.pref_basic_permissions_network_wifistate);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.ACCESS_NETWORK_STATE);
            }
        });

        preferences.add(lp);
        PrefNetworkWifiState = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".network.gcmreceive");
        lp.setTitle(R.string.pref_basic_permissions_network_gcmreceive);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission("com.google.android.c2dm.permission.RECEIVE");
            }
        });

        preferences.add(lp);
        PrefNetworkGCM = lp;

        //
        // Miscelanous permissions.
        //

        ip = new NicedPreferences.NiceInfoPreference(context);
        ip.setTitle(R.string.pref_basic_permissions_other);
        ip.setSummary(R.string.pref_basic_permissions_other_summary);
        preferences.add(ip);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".other.vibrate");
        lp.setTitle(R.string.pref_basic_permissions_other_vibrate);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.VIBRATE);
            }
        });

        preferences.add(lp);
        PrefOtherVibrate = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".other.wakelock");
        lp.setTitle(R.string.pref_basic_permissions_other_wakelock);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.WAKE_LOCK);
            }
        });

        preferences.add(lp);
        PrefOtherWakelock = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".other.receiveboot");
        lp.setTitle(R.string.pref_basic_permissions_other_receiveboot);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                requestPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED);
            }
        });

        preferences.add(lp);
        PrefOtherReceiveBoot = lp;

        //
        // Run preferences monitor.
        //

        checkStatus.run();
    }

    private void requestPermission(String permission)
    {
        if (checkPermission(permission).equals("grant")) return;

        Log.d(LOGTAG, "requestPermission: " + permission);

        //
        // Execute request delayed to get rid
        // of overlay before calling settings.
        //

        if (CaptureOverlay.getInstance() != null)
        {
            CaptureOverlay.getInstance().detachFromScreen();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + Simple.getAppContext().getPackageName()));

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Simple.getAppContext().startActivity(intent);

                return;
            }
        }

        //
        // Workaround for bug in Android permission system.
        //

        if (permission.equals("android.permission.READ_PROFILE"))
        {
            permission = Manifest.permission.READ_CONTACTS;
        }

        final String cbpermission = permission;

        Simple.makePost(new Runnable()
        {
            @Override
            public void run()
            {
                ActivityCompat.requestPermissions(Simple.getActContext(), new String[]{ cbpermission }, 4711);
            }
        }, 1000);
    }

    private String checkPermission(String permission)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW))
            {
                return Settings.canDrawOverlays(Simple.getAppContext()) ? "grant" : "deny";
            }
        }

        //
        // Bug in Android:
        //
        // When "android.permission.READ_PROFILE" is granted in manifest, the
        // check permission method returns true. This is not correct, unless
        // the user has granted access to the contacts permissions.
        //

        if (permission.equals("android.permission.READ_PROFILE"))
        {
            int res1 = ContextCompat.checkSelfPermission(Simple.getActContext(), permission);
            int res2 = ContextCompat.checkSelfPermission(Simple.getActContext(), Manifest.permission.READ_CONTACTS);

            boolean ok = ((res1 == PackageManager.PERMISSION_GRANTED) && (res2 == PackageManager.PERMISSION_GRANTED));
            return ok ? "grant" : "deny";
        }

        //
        // Check normal permission.
        //

        int res = ContextCompat.checkSelfPermission(Simple.getActContext(), permission);
        return (res == PackageManager.PERMISSION_GRANTED) ? "grant" : "deny";
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Simple.removePost(checkStatus);
    }

    private final Runnable checkStatus = new Runnable()
    {
        @Override
        public void run()
        {
            PrefContactsProfRead.setValue(checkPermission("android.permission.READ_PROFILE"));
            PrefContactsRead.setValue(checkPermission(Manifest.permission.READ_CONTACTS));
            PrefContactsWrite.setValue(checkPermission(Manifest.permission.WRITE_CONTACTS));

            PrefPhoneRead.setValue(checkPermission(Manifest.permission.READ_PHONE_STATE));
            PrefPhoneCall.setValue(checkPermission(Manifest.permission.CALL_PHONE));

            PrefStorageRead.setValue(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
            PrefStorageWrite.setValue(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));

            PrefRecordAudio.setValue(checkPermission(Manifest.permission.RECORD_AUDIO));

            PrefSystemAlertWindow.setValue(checkPermission(Manifest.permission.SYSTEM_ALERT_WINDOW));

            PrefBluetoothRead.setValue(checkPermission(Manifest.permission.BLUETOOTH));
            PrefBluetoothAdmin.setValue(checkPermission(Manifest.permission.BLUETOOTH_ADMIN));

            PrefNetworkInternet.setValue(checkPermission(Manifest.permission.INTERNET));
            PrefNetworkState.setValue(checkPermission(Manifest.permission.ACCESS_NETWORK_STATE));
            PrefNetworkWifiState.setValue(checkPermission(Manifest.permission.ACCESS_WIFI_STATE));
            PrefNetworkGCM.setValue(checkPermission("com.google.android.c2dm.permission.RECEIVE"));

            PrefOtherVibrate.setValue(checkPermission(Manifest.permission.VIBRATE));
            PrefOtherWakelock.setValue(checkPermission(Manifest.permission.WAKE_LOCK));
            PrefOtherReceiveBoot.setValue(checkPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED));

            Simple.makePost(checkStatus, 1000);
        }
    };

}
