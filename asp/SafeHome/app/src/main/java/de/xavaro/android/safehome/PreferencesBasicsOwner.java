package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.ContactsContract;

import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Owner;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsOwner extends PreferenceFragments.BasicFragmentStub
{
    private static final String LOGTAG = PreferencesBasicsOwner.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_basic_owner;
        header.iconRes = GlobalConfigs.IconResOwner;
        header.fragment = PreferencesBasicsOwner.class.getName();

        return header;
    }

    public PreferencesBasicsOwner()
    {
        super();

        iconres = GlobalConfigs.IconResOwner;
        summaryres = R.string.pref_basic_owner_summary;
    }

    NicedPreferences.NiceCategoryPreference profileHead;
    NicedPreferences.NiceCheckboxPreference profilePref;
    NicedPreferences.NiceDisplayTextPreference profileFirst;
    NicedPreferences.NiceDisplayTextPreference profileGiven;

    public void registerAll(Context context)
    {
        super.registerAll(context);

        NicedPreferences.NiceCategoryPreference pc;
        NicedPreferences.NiceEditTextPreference et;
        NicedPreferences.NiceListPreference lp;

        //
        // Check android profile for correct values.
        //

        profileHead = new NicedPreferences.NiceInfoPreference(context);
        profileHead.setTitle(R.string.pref_basic_owner_profile);
        profileHead.setSummary(R.string.pref_basic_owner_profile_summary);
        preferences.add(profileHead);

        profilePref = new NicedPreferences.NiceCheckboxPreference(context);
        profilePref.setTitle("Profil bearbeiten");

        profilePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(ContactsContract.Profile.CONTENT_URI);

                ProcessManager.launchIntent(intent);

                return true;
            }
        });

        profilePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                //
                // Disable preference change by click.
                //

                return false;
            }
        });

        preferences.add(profilePref);

        profileFirst = new NicedPreferences.NiceDisplayTextPreference(context);

        profileFirst.setKey("owner.firstname");
        profileFirst.setTitle(R.string.pref_basic_owner_firstname);

        preferences.add(profileFirst);

        profileGiven = new NicedPreferences.NiceDisplayTextPreference(context);

        profileGiven.setKey("owner.givenname");
        profileGiven.setTitle(R.string.pref_basic_owner_givenname);

        preferences.add(profileGiven);

        checkPretty.run();

        //
        // Personal entries.
        //

        pc = new NicedPreferences.NiceInfoPreference(context);
        pc.setTitle(R.string.pref_basic_owner_personal);
        pc.setSummary(R.string.pref_basic_owner_personal_summary);
        preferences.add(pc);

        final CharSequence[] prefixText = { "Keine", "Herr", "Frau" };
        final CharSequence[] prefixVals = { "no",    "mr",   "ms"   };

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey("owner.prefix");
        lp.setEntries(prefixText);
        lp.setEntryValues(prefixVals);
        lp.setDefaultValue("no");
        lp.setTitle(R.string.pref_basic_owner_address);

        preferences.add(lp);

        et = new NicedPreferences.NiceEditTextPreference(context);

        et.setKey("owner.nickname");
        et.setTitle(R.string.pref_basic_owner_nickname);
        et.setDefaultValue("");

        preferences.add(et);

        lp = new NicedPreferences.NiceListPreference(context);

        final CharSequence[] siezenText = { "gesiezt werden", "geduzt werden"};
        final CharSequence[] siezenVals = { "siezen", "duzen" };

        lp.setKey("owner.siezen");
        lp.setEntries(siezenText);
        lp.setEntryValues(siezenVals);
        lp.setDefaultValue("siezen");
        lp.setTitle(R.string.pref_basic_owner_duzen);

        preferences.add(lp);

        //
        // Communication entries.
        //

        pc = new NicedPreferences.NiceInfoPreference(context);
        pc.setTitle(R.string.pref_basic_owner_communication);
        pc.setSummary(R.string.pref_basic_owner_communication_summary);
        preferences.add(pc);

        Simple.removeSharedPref("owner.phonenumber");
        et = new NicedPreferences.NiceEditTextPreference(context);

        et.setKey("owner.phonenumber");
        et.setTitle(R.string.pref_basic_owner_phonenumber);
        et.setEmptyText("…");
        et.setDefaultValue("");
        et.setIsPhonenumber();

        String phonenumber = Simple.getPhoneNumber();
        String prefnumber = Simple.getSharedPrefString(et.getKey());

        if ((phonenumber != null) && ((prefnumber == null) || prefnumber.isEmpty()))
        {
            Simple.setSharedPrefString(et.getKey(), phonenumber);
            et.setText(phonenumber);
        }

        preferences.add(et);

        et = new NicedPreferences.NiceEditTextPreference(context);

        et.setKey("owner.skypename");
        et.setTitle(R.string.pref_basic_owner_skypename);
        et.setEmptyText("…");
        et.setDefaultValue("");

        preferences.add(et);

        //
        // Legacy remove.
        //

        Simple.removeSharedPref("owner.usephoto");
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
            int ispretty = 0;

            String firstname = Owner.getOwnerFirstName();
            if ((firstname != null) && ! firstname.isEmpty())
            {
                Simple.setSharedPrefString("owner.firstname", firstname);
                ispretty++;
            }

            String givenname = Owner.getOwnerGivenName();
            if ((givenname != null) && ! givenname.isEmpty())
            {
                Simple.setSharedPrefString("owner.givenname", givenname);
                ispretty++;
            }

            Drawable profileImage = ProfileImages.getOwnerProfileDrawable(true);
            if (profileImage != null)
            {
                ispretty++;
            }

            profileGiven.setText(givenname);
            profileFirst.setText(firstname);
            profileHead.setIcon(profileImage);
            profilePref.setChecked(ispretty == 3);

            Simple.makePost(checkPretty, 3 * 1000);
        }
    };
}
