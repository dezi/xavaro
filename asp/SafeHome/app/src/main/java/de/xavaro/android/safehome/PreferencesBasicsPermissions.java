package de.xavaro.android.safehome;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;

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
        lp.setValue(getPermission(Manifest.permission.READ_CONTACTS));

        preferences.add(lp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(permprefix + ".contacts.write");
        lp.setTitle(R.string.pref_basic_permissions_contacts_write);
        lp.setEntries(permStatusText);
        lp.setEntryValues(permStatusKeys);
        lp.setValue(getPermission(Manifest.permission.WRITE_CONTACTS));

        preferences.add(lp);
    }

    private static String getPermission(String permission)
    {
        int res = ContextCompat.checkSelfPermission(Simple.getActContext(), permission);

        return  (res == PackageManager.PERMISSION_GRANTED) ? "deny" : "grant";
    }
}
