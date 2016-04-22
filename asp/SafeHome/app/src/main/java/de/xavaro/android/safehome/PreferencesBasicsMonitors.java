package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsMonitors extends PreferenceFragments.WeblibFragmentStub
{
    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.title = Simple.getTrans(R.string.pref_basic_monitoring);
        header.iconRes = CommonConfigs.IconResMonitoring;
        header.fragment = PreferencesBasicsMonitors.class.getName();

        return header;
    }

    public PreferencesBasicsMonitors()
    {
        super();

        type = "calls";
        subtype = "monitors";
        iscalls = true;
        iconres = CommonConfigs.IconResMonitoring;
        summaryres = R.string.pref_basic_monitoring_summary;
        keyprefix = type + "." + subtype;
        masterenable = Simple.getTrans(R.string.pref_basic_monitoring_enable);
        residKeys = R.array.pref_where_keys;
        residVals = R.array.pref_where_vals;
    }
}