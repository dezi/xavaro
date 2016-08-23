package de.xavaro.android.safehome;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Owner;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
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

    public NicedPreferences.NiceListPreference PrefContactsRead;
    public NicedPreferences.NiceListPreference PrefContactsWrite;
    public NicedPreferences.NiceListPreference PrefSystemAlertWindow;

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

        lp.setKey(permprefix + ".contacts.read");
        lp.setTitle(R.string.pref_basic_permissions_contacts_read);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        lp.setOnclick(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(LOGTAG, "Manifest.permission.READ_CONTACTS...");

                ActivityCompat.requestPermissions(Simple.getActContext(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        1234);
            }
        });

        preferences.add(lp);
        PrefContactsRead = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".contacts.write");
        lp.setTitle(R.string.pref_basic_permissions_contacts_write);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);

        preferences.add(lp);
        PrefContactsWrite = lp;

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
                ActivityCompat.requestPermissions(Simple.getActContext(),
                        new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                        1234);
            }
        });

        preferences.add(lp);
        PrefSystemAlertWindow = lp;

        //
        // Run preferences monitor.
        //

        checkPretty.run();
    }

    private static void requestPermission(String permission)
    {
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

        ActivityCompat.requestPermissions(Simple.getActContext(), new String[]{permission}, 4711);
    }

    private static String checkPermission(String permission)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW))
            {
                return Settings.canDrawOverlays(Simple.getAppContext()) ? "grant" : "deny";
            }
        }

        int res = ContextCompat.checkSelfPermission(Simple.getActContext(), permission);

        return  (res == PackageManager.PERMISSION_GRANTED) ? "grant" : "deny";
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Simple.removePost(checkPretty);
    }

    private final Runnable checkPretty = new Runnable()
    {
        @Override
        public void run()
        {
            PrefContactsRead.setValue(checkPermission(Manifest.permission.READ_CONTACTS));
            PrefContactsWrite.setValue(checkPermission(Manifest.permission.WRITE_CONTACTS));
            PrefSystemAlertWindow.setValue(checkPermission(Manifest.permission.SYSTEM_ALERT_WINDOW));

            Simple.makePost(checkPretty, 1 * 1000);
        }
    };

}
