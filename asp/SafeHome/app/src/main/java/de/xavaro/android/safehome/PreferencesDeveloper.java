package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.util.Map;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebApp;

public class PreferencesDeveloper
{
    public static class DeveloperFragment extends PreferencesBasics.EnablePreferenceFragment
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

        private void nukeWifiPreference(String wifiname)
        {
            String devwebappsprefix = keyprefix + ".webapps.";

            for (int inx = 0; inx < preferences.size(); inx++)
            {
                Preference preference = preferences.get(inx);
                String key = preference.getKey();
                if (key == null) continue;

                if (key.startsWith(devwebappsprefix) && key.endsWith("." + wifiname))
                {
                    getPreferenceScreen().removePreference(preference);
                    Simple.removeSharedPref(key);
                    preferences.remove(inx--);
                }
            }
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceEditTextPreference ep;
            NicedPreferences.NiceCheckboxPreference cp;

            //
            // Webapp server preferences.
            //

            String wifiprefix = keyprefix + ".webapps.httpbypass.";

            //
            // Make sure, our current wifi is in list.
            //

            if (Simple.isWifiConnected())
            {
                String mywifi = Simple.getWifiName();
                String mywifipref = wifiprefix + mywifi;
                Simple.setSharedPrefBoolean(mywifipref, Simple.getSharedPrefBoolean(mywifipref));
            }

            Map<String, Object> wifiprefs = Simple.getAllPreferences(wifiprefix);

            for (String pref : wifiprefs.keySet())
            {
                final String wifiname = pref.substring(wifiprefix.length());

                pc = new NicedPreferences.NiceCategoryPreference(context);
                pc.setKey(keyprefix + ".webapps.wlancat." + wifiname);
                pc.setTitle("WLAN" + " \"" + wifiname + "\"");

                pc.setOnLongClick(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        nukeWifiPreference(wifiname);
                    }
                });

                preferences.add(pc);

                cp = new NicedPreferences.NiceCheckboxPreference(context);

                cp.setKey(keyprefix + ".webapps.httpbypass." + wifiname);
                cp.setTitle("HTTP-Bypass");
                cp.setEnabled(enabled);

                preferences.add(cp);

                ep = new NicedPreferences.NiceEditTextPreference(context);

                ep.setKey(keyprefix + ".webapps.httpserver." + wifiname);
                ep.setTitle("HTTP-Server");
                ep.setEnabled(enabled);

                preferences.add(ep);

                ep = new NicedPreferences.NiceEditTextPreference(context);

                ep.setKey(keyprefix + ".webapps.httpport." + wifiname);
                ep.setDefaultValue("8000");
                ep.setTitle("HTTP-Port");
                ep.setEnabled(enabled);

                preferences.add(ep);

                cp = new NicedPreferences.NiceCheckboxPreference(context);

                cp.setKey(keyprefix + ".webapps.datacachedisable." + wifiname);
                cp.setTitle("DATA-Cache disable");
                cp.setEnabled(enabled);

                preferences.add(cp);
            }

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Webapps");

            preferences.add(pc);

            String webappname = "testing";

            String[] appkeys =  Simple.getTransArray(R.array.pref_webapps_where_keys);
            String[] appvals =  Simple.getTransArray(R.array.pref_webapps_where_vals);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey("developer" + ".mode." + webappname);
            lp.setEntries(appvals);
            lp.setEntryValues(appkeys);
            lp.setDefaultValue("inact");
            lp.setIcon(WebApp.getAppIcon(webappname));
            lp.setTitle(WebApp.getLabel(webappname));
            lp.setEnabled(enabled);

            preferences.add(lp);

            //
            // Browser preferences.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Browser");

            preferences.add(pc);

            //
            // Icon location preference.
            //

            String[] keys =  Simple.getTransArray(R.array.pref_beta_where_keys);
            String[] vals =  Simple.getTransArray(R.array.pref_beta_where_vals);

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

            lp.setKey(keyprefix + ".browser.settings");
            lp.setIcon(GlobalConfigs.IconResPersist);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Settings");
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

            lp.setKey(keyprefix + ".browser.sdcard");
            lp.setIcon(GlobalConfigs.IconResStorageSDCard);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("SD-Card");
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

            lp.setKey(keyprefix + ".browser.known");
            lp.setIcon(GlobalConfigs.IconResStorageKnown);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Known");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.webappcache");
            lp.setIcon(GlobalConfigs.IconResWebApps);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Webapp Cache");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.events");
            lp.setIcon(GlobalConfigs.IconResEvents);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Events");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".browser.activity");
            lp.setIcon(CommonConfigs.IconResActivity);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("folder");
            lp.setTitle("Activity");
            lp.setEnabled(enabled);

            preferences.add(lp);
        }
    }
}
