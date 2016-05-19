package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsAssistance extends PreferenceFragments.EnableFragmentStub
        implements Preference.OnPreferenceChangeListener
{
    private static final String LOGTAG = PreferencesBasicsAssistance.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_basic_assistance;
        header.iconRes = GlobalConfigs.IconResAlertgroup;
        header.fragment = PreferencesBasicsAssistance.class.getName();

        return header;
    }

    public PreferencesBasicsAssistance()
    {
        super();

        keyprefix = "alertgroup";
        iconres = GlobalConfigs.IconResAlertgroup;
        summaryres = R.string.pref_basic_assistance_summary;
        masterenable = Simple.getTrans(R.string.pref_basic_assistance_enable);
    }

    private final Map<String, Preference> remoteContacts = new HashMap<>();
    private int baseprefscount;

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.d(LOGTAG, "onDestroy");

        Simple.removePost(monitorContacts);
    }

    @Override
    public void registerAll(Context context)
    {
        super.registerAll(context);

        NicedPreferences.NiceSwitchPreference sp;

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey(keyprefix + ".skypecallback");
        sp.setTitle("Skype Rückruf ermöglichen");
        sp.setEnabled(enabled);

        sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                Simple.makePost(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        registerRemotes(true, true);
                    }
                });

                return true;
            }
        });

        preferences.add(sp);

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey(keyprefix + ".prepaidcallback");
        sp.setTitle("Prepaid SIM überwachen");
        sp.setEnabled(enabled);

        sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                Simple.makePost(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        registerRemotes(true, true);
                    }
                });

                return true;
            }
        });

        preferences.add(sp);

        baseprefscount = preferences.size();

        //
        // Alert group hidden preferences.
        //

        checkAlertGroup();

        //
        // Confirmed connects.
        //

        registerRemotes(false, false);

        Simple.makePost(monitorContacts);
    }

    private final Runnable monitorContacts = new Runnable()
    {
        @Override
        public void run()
        {
            registerRemotes(false, true);

            Simple.makePost(monitorContacts, 1000);
        }
    };

    private void checkAlertGroup()
    {
        SharedPreferences sp = Simple.getSharedPrefs();

        String groupidentkey = keyprefix + ".groupidentity";
        String grouppasspkey = keyprefix + ".passphrase";
        String grouptypekey  = keyprefix + ".type";
        String groupnamekey  = keyprefix + ".name";

        if (! sp.contains(groupidentkey))
        {
            String groupident = UUID.randomUUID().toString();
            String grouppassp = UUID.randomUUID().toString();

            sp.edit().putString(groupidentkey, groupident).apply();
            sp.edit().putString(grouppasspkey, grouppassp).apply();
        }

        String groupname = "Assistenz";
        String grouptype = "alertcall";
        sp.edit().putString(groupnamekey, groupname).apply();
        sp.edit().putString(grouptypekey, grouptype).apply();
    }

    private void registerRemotes(boolean clear, boolean update)
    {
        int clearcount = 0;

        if (clear)
        {
            clearcount = preferences.size() - baseprefscount;

            remoteContacts.clear();
        }

        boolean isskype = Simple.getSharedPrefBoolean(keyprefix + ".skypecallback");
        boolean isprepaid = Simple.getSharedPrefBoolean(keyprefix + ".prepaidcallback");

        NicedPreferences.NiceCategoryPreference pc;
        NicedPreferences.NiceListPreference lp;
        NicedPreferences.NiceEditTextPreference ep;
        NicedPreferences.NiceCheckboxPreference cp;

        String[] prefixText = Simple.getTransArray(R.array.pref_alertgroup_vals);
        String[] prefixVals = Simple.getTransArray(R.array.pref_alertgroup_keys);

        String xpath = "RemoteContacts/identities";
        JSONObject rcs = PersistManager.getXpathJSONObject(xpath);
        if (rcs == null) return;

        Iterator<String> keysIterator = rcs.keys();

        while (keysIterator.hasNext())
        {
            String ident = keysIterator.next();

            if (remoteContacts.containsKey(ident)) continue;

            String name = RemoteContacts.getDisplayName(ident);

            pc = new NicedPreferences.NiceCategoryPreference(Simple.getActContext());
            pc.setTitle(name);
            pc.setIcon(ProfileImages.getProfileDrawable(ident, true));
            pc.setEnabled(enabled);

            preferences.add(pc);
            if (update) getPreferenceScreen().addPreference(pc);

            remoteContacts.put(ident, pc);

            lp = new NicedPreferences.NiceListPreference(Simple.getActContext());
            lp.setKey(keyprefix + ".member." + ident);
            lp.setEntries(prefixText);
            lp.setEntryValues(prefixVals);
            lp.setDefaultValue("inactive");
            lp.setTitle(R.string.pref_basic_assistance_asmember);
            lp.setEnabled(enabled);

            lp.setOnPreferenceChangeListener(this);

            preferences.add(lp);
            if (update) getPreferenceScreen().addPreference(lp);

            String memberval = Simple.getSharedPrefString(lp.getKey());

            if (isskype && Simple.equals(memberval, "invited"))
            {
                ep = new NicedPreferences.NiceEditTextPreference(Simple.getActContext());
                ep.setKey(keyprefix + ".skypecallback." + ident);
                ep.setTitle(R.string.pref_basic_assistance_skypename);
                ep.setEmptyText("…");
                ep.setEnabled(enabled);

                String skypename = Simple.getSharedPrefString(ep.getKey());

                if ((skypename == null) || skypename.isEmpty())
                {
                    skypename = RemoteContacts.getSkypeName(ident);

                    if (skypename != null)
                    {
                        Simple.setSharedPrefString(ep.getKey(), skypename);
                        ep.setText(skypename);
                    }
                }

                ep.setOnPreferenceChangeListener(this);

                preferences.add(ep);
                if (update) getPreferenceScreen().addPreference(ep);

                cp = new NicedPreferences.NiceCheckboxPreference(Simple.getActContext());
                cp.setKey(keyprefix + ".skypeenable." + ident);
                cp.setTitle(R.string.pref_basic_assistance_skypecall);
                cp.setEnabled(enabled);

                cp.setOnPreferenceChangeListener(this);

                preferences.add(cp);
                if (update) getPreferenceScreen().addPreference(cp);
            }

            if (isprepaid && Simple.equals(memberval, "invited"))
            {
                cp = new NicedPreferences.NiceCheckboxPreference(Simple.getActContext());
                cp.setKey(keyprefix + ".prepaidadmin." + ident);
                cp.setTitle(R.string.pref_basic_assistance_prepaidadmin);
                cp.setEnabled(enabled);

                cp.setOnPreferenceChangeListener(this);

                preferences.add(cp);
                if (update) getPreferenceScreen().addPreference(cp);
            }
        }

        if (clear)
        {
            //
            // We remove obsoleted entries at end to avoid
            // flickering and dejustment of preference view.
            //

            while (clearcount > 0)
            {
                Preference pp = preferences.remove(baseprefscount);
                getPreferenceScreen().removePreference(pp);
                clearcount--;
            }
        }
    }

    private final Handler handler = new Handler();

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        Log.d(LOGTAG, "onPreferenceChange:" + preference.getKey() + "=" + newValue.toString());

        if (Simple.equals(preference.getKey(), keyprefix + ".enable"))
        {
            return super.onPreferenceChange(preference, newValue);
        }

        boolean retval = true;

        if (preference instanceof NicedPreferences.NiceListPreference)
        {
            retval = ((NicedPreferences.NiceListPreference) preference)
                    .onPreferenceChange(preference, newValue);

            handler.postDelayed(updateRemotes, 10);
        }

        handler.postDelayed(updateAlertGroup, 100);

        return retval;
    }

    public final Runnable updateRemotes = new Runnable()
    {
        @Override
        public void run()
        {
            registerRemotes(true, true);
        }
    };

    public final Runnable updateAlertGroup = new Runnable()
    {
        @Override
        public void run()
        {
            RemoteGroups.updateGroupFromPreferences(keyprefix);
        }
    };
}
