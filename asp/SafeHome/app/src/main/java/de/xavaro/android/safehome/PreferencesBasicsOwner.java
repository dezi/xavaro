package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.util.Log;

import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.ProfileImagesNew;
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
    }

    public void registerAll(Context context)
    {
        super.registerAll(context);

        NicedPreferences.NiceCategoryPreference pc;
        NicedPreferences.NiceEditTextPreference et;
        NicedPreferences.NiceSwitchPreference sp;
        NicedPreferences.NiceListPreference lp;

        pc = new NicedPreferences.NiceInfoPreference(context);
        pc.setTitle(R.string.pref_basic_owner_personal);
        pc.setSummary(R.string.pref_basic_owner_personal_summary);
        pc.setIcon(ProfileImagesNew.getOwnerProfileDrawable(true));
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

        et.setKey("owner.firstname");
        et.setTitle(R.string.pref_basic_owner_firstname);

        preferences.add(et);

        et = new NicedPreferences.NiceEditTextPreference(context);

        et.setKey("owner.givenname");
        et.setTitle(R.string.pref_basic_owner_givenname);

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

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey("owner.usephoto");
        sp.setTitle(R.string.pref_basic_owner_profileimage_share);
        sp.setDefaultValue(true);

        preferences.add(sp);
    }
}
