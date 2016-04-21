package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.CryptUtils;
import de.xavaro.android.common.IdentityManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.ProfileImagesNew;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.WifiLookup;

public class PreferencesBasics
{
    private static final String LOGTAG = PreferencesBasics.class.getSimpleName();

    //region Alertgroup preferences

    public static class AlertgroupFragment extends PreferenceFragments.EnableFragmentStub
            implements Preference.OnPreferenceChangeListener
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Assistenz";
            header.iconRes = GlobalConfigs.IconResAlertgroup;
            header.fragment = AlertgroupFragment.class.getName();

            return header;
        }

        public AlertgroupFragment()
        {
            super();

            iconres = GlobalConfigs.IconResAlertgroup;
            keyprefix = "alertgroup";
            masterenable = "Assistenz freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceNumberPreference np;

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
                pc.setIcon(ProfileImagesNew.getXavaroProfileDrawable(ident, true));
                pc.setEnabled(enabled);

                preferences.add(pc);

                lp = new NicedPreferences.NiceListPreference(context);
                lp.setKey(keyprefix + ".member." + ident);
                lp.setEntries(prefixText);
                lp.setEntryValues(prefixVals);
                lp.setDefaultValue("inactive");
                lp.setTitle("Als Mitglied");
                lp.setEnabled(enabled);

                lp.setOnPreferenceChangeListener(this);

                preferences.add(lp);
                if (! initial) getPreferenceScreen().addPreference(lp);

                ep = new NicedPreferences.NiceEditTextPreference(context);
                ep.setKey(keyprefix + ".skypecallback." + ident);
                ep.setTitle("Skype Rückruf");
                ep.setEmptyText("Inaktiv");
                ep.setEnabled(enabled);

                ep.setOnPreferenceChangeListener(this);

                preferences.add(ep);
                if (! initial) getPreferenceScreen().addPreference(ep);
            }
        }

        private final Handler handler = new Handler();

        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
            Log.d(LOGTAG, "onPreferenceChange:" + preference.getKey() + "=" + newValue.toString());

            handler.postDelayed(updateAlertGroup, 100);

            if (preference instanceof NicedPreferences.NiceListPreference)
            {
                return ((NicedPreferences.NiceListPreference) preference)
                        .onPreferenceChange(preference, newValue);
            }

            return true;
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

    //endregion Alertgroup preferences

    //region Important calls preferences

    public static class ImportantCallsFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Rufnummern";
            header.iconRes = GlobalConfigs.IconResCallImportant;
            header.fragment = ImportantCallsFragment.class.getName();

            return header;
        }

        public ImportantCallsFragment()
        {
            super();

            type = "calls";
            subtype = "important";
            iscalls = true;
            iconres = GlobalConfigs.IconResCallImportant;
            keyprefix = type + "." + subtype;
            masterenable = "Wichtige Rufnummern freischalten";
            residKeys = R.array.pref_where_keys;
            residVals = R.array.pref_where_vals;
        }
    }

    //endregion Important calls preferences
}
