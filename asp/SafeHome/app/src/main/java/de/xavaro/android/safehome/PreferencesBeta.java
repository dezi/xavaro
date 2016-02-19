package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;

import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Simple;

public class PreferencesBeta
{
    //region Developer preferences

    public static class DeveloperFragment extends SettingsFragments.EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Entwickler";
            header.iconRes = GlobalConfigs.IconResTesting;
            header.fragment = DeveloperFragment.class.getName();

            return header;
        }

        public DeveloperFragment()
        {
            super();

            iconres = GlobalConfigs.IconResTesting;
            keyprefix = "developer";
            masterenable = "Entwickler freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Browser");

            preferences.add(pc);

            //
            // Icon location preference.
            //

            String[] keys =  Simple.getTransArray(R.array.pref_beta_where_keys);
            String[] vals =  Simple.getTransArray(R.array.pref_beta_where_vals);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.settings");
            lp.setIcon(GlobalConfigs.IconResPersist);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Settings");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.preferences");
            lp.setIcon(GlobalConfigs.IconResSettingsSafehome);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Preferences");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.contacts");
            lp.setIcon(GlobalConfigs.IconResContacts);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Contacts");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.sdcard");
            lp.setIcon(GlobalConfigs.IconResStorageSDCard);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("SD-Card");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.known");
            lp.setIcon(GlobalConfigs.IconResStorageKnown);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Known");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.cache");
            lp.setIcon(GlobalConfigs.IconResStorageCache);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Cache");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.rcontacts");
            lp.setIcon(GlobalConfigs.IconResCommChatUser);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Remote Contacts");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.rgroups");
            lp.setIcon(GlobalConfigs.IconResCommChatGroup);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Remote Groups");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.identities");
            lp.setIcon(GlobalConfigs.IconResAdministrator);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Identities");
            lp.setEnabled(enabled);

            preferences.add(lp);
        }
    }
}
