package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
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

    @Override
    public void registerAll(Context context)
    {
        super.registerAll(context);

        //
        // Confirmed connects.
        //

        registerRemotes(context, true);
    }

    private final ArrayList<String> remoteContacts = new ArrayList<>();

    private void registerRemotes(Context context, boolean initial)
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

        NicedPreferences.NiceCategoryPreference pc;
        NicedPreferences.NiceListPreference lp;
        NicedPreferences.NiceEditTextPreference ep;

        String[] prefixText = Simple.getTransArray(R.array.pref_alertgroup_vals);
        String[] prefixVals = Simple.getTransArray(R.array.pref_alertgroup_keys);

        String xpath = "RemoteContacts/identities";
        JSONObject rcs = PersistManager.getXpathJSONObject(xpath);
        if (rcs == null) return;

        Iterator<String> keysIterator = rcs.keys();

        while (keysIterator.hasNext())
        {
            String ident = keysIterator.next();

            if (remoteContacts.contains(ident)) continue;
            remoteContacts.add(ident);

            String name = RemoteContacts.getDisplayName(ident);

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle(name);
            pc.setIcon(ProfileImages.getXavaroProfileDrawable(ident, true));
            pc.setEnabled(enabled);

            preferences.add(pc);

            lp = new NicedPreferences.NiceListPreference(context);
            lp.setKey(keyprefix + ".member." + ident);
            lp.setEntries(prefixText);
            lp.setEntryValues(prefixVals);
            lp.setDefaultValue("inactive");
            lp.setTitle(R.string.pref_basic_assistance_asmember);
            lp.setEnabled(enabled);

            lp.setOnPreferenceChangeListener(this);

            preferences.add(lp);
            if (! initial) getPreferenceScreen().addPreference(lp);

            ep = new NicedPreferences.NiceEditTextPreference(context);
            ep.setKey(keyprefix + ".skypecallback." + ident);
            ep.setTitle(R.string.pref_basic_assistance_skypecall);
            ep.setEmptyText("Inaktiv");
            ep.setEnabled(enabled);

            ep.setOnPreferenceChangeListener(this);

            preferences.add(ep);
            if (! initial) getPreferenceScreen().addPreference(ep);
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
        }

        handler.postDelayed(updateAlertGroup, 100);

        return retval;
    }

    public final Runnable updateAlertGroup = new Runnable()
    {
        @Override
        public void run()
        {
            RemoteGroups.updateGroupFromPreferences(keyprefix);
        }
    };
}
