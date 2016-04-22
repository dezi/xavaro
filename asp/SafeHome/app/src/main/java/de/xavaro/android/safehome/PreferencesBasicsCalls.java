package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsCalls extends PreferenceFragments.WeblibFragmentStub
{
    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.title = Simple.getTrans(R.string.pref_basic_calls);
        header.iconRes = GlobalConfigs.IconResCallImportant;
        header.fragment = PreferencesBasicsCalls.class.getName();

        return header;
    }

    public PreferencesBasicsCalls()
    {
        super();

        type = "calls";
        subtype = "important";
        iscalls = true;
        iconres = GlobalConfigs.IconResCallImportant;
        summaryres = R.string.pref_basic_calls_summary;
        keyprefix = type + "." + subtype;
        masterenable = Simple.getTrans(R.string.pref_basic_calls_enable);
        residKeys = R.array.pref_where_keys;
        residVals = R.array.pref_where_vals;
    }
}