package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;

import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.NicedPreferences;
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

    @Override
    public void registerAll(Context context)
    {
        super.registerAll(context);

        NicedPreferences.NiceCategoryPreference cp;
        NicedPreferences.NiceSwitchPreference sp;
        NicedPreferences.NiceListPreference lp;

        //
        // Prepaid status monitoring.
        //

        String prepaidprefix = "monitors.prepaid";

        CharSequence[] preWarnText = Simple.getTransArray(R.array.pref_basic_monitoring_prepaid_warn_vals);
        CharSequence[] preWarnKeys = Simple.getTransArray(R.array.pref_basic_monitoring_prepaid_warn_keys);

        CharSequence[] preRepeatText = Simple.getTransArray(R.array.pref_basic_monitoring_prepaid_repeat_vals);
        CharSequence[] preRepeatKeys = Simple.getTransArray(R.array.pref_basic_monitoring_prepaid_repeat_keys);

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey(prepaidprefix + ".automatic");
        sp.setTitle(R.string.pref_basic_monitoring_prepaid_auto);
        sp.setEnabled(enabled);

        preferences.add(sp);

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey(prepaidprefix + ".aftercall");
        sp.setTitle(R.string.pref_basic_monitoring_prepaid_call);
        sp.setEnabled(enabled);

        preferences.add(sp);

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey(prepaidprefix + ".record");
        sp.setTitle(R.string.pref_basic_monitoring_record);
        sp.setEnabled(enabled);

        preferences.add(sp);

        cp = new NicedPreferences.NiceSeparatorPreference(context);
        cp.setTitle(R.string.pref_basic_monitoring_warnings);
        cp.setEnabled(enabled);
        preferences.add(cp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(prepaidprefix + ".remind");
        lp.setTitle(R.string.pref_basic_monitoring_remind);
        lp.setEntries(preWarnText);
        lp.setEntryValues(preWarnKeys);
        lp.setDefaultValue("never");
        lp.setEnabled(enabled);

        preferences.add(lp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(prepaidprefix + ".warn");
        lp.setTitle(R.string.pref_basic_monitoring_warn);
        lp.setEntries(preWarnText);
        lp.setEntryValues(preWarnKeys);
        lp.setDefaultValue("never");
        lp.setEnabled(enabled);

        preferences.add(lp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(prepaidprefix + ".assistance");
        lp.setTitle(R.string.pref_basic_monitoring_assistance);
        lp.setEntries(preWarnText);
        lp.setEntryValues(preWarnKeys);
        lp.setDefaultValue("never");
        lp.setEnabled(enabled);

        preferences.add(lp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(prepaidprefix + ".repeat");
        lp.setTitle(R.string.pref_basic_monitoring_repeat);
        lp.setEntries(preRepeatText);
        lp.setEntryValues(preRepeatKeys);
        lp.setDefaultValue("once");
        lp.setEnabled(enabled);

        preferences.add(lp);

        //
        // Battery status monitoring.
        //

        String batteryprefix = "monitors.battery";

        CharSequence[] battPrefText = Simple.getTransArray(R.array.pref_basic_monitoring_battery_where_vals);
        CharSequence[] battPrefKeys = Simple.getTransArray(R.array.pref_basic_monitoring_battery_where_keys);

        CharSequence[] battWarnText = Simple.getTransArray(R.array.pref_basic_monitoring_battery_warn_vals);
        CharSequence[] battWarnKeys = Simple.getTransArray(R.array.pref_basic_monitoring_battery_warn_keys);

        CharSequence[] battRepeatText = Simple.getTransArray(R.array.pref_basic_monitoring_battery_repeat_vals);
        CharSequence[] battRepeatKeys = Simple.getTransArray(R.array.pref_basic_monitoring_battery_repeat_keys);

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setIcon(CommonConfigs.IconResBattery);
        cp.setTitle(R.string.pref_basic_monitoring_battery);
        cp.setSummary(R.string.pref_basic_monitoring_battery_summary);
        cp.setEnabled(enabled);
        preferences.add(cp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(batteryprefix + ".mode");
        lp.setTitle(R.string.pref_basic_monitoring_battery_where);
        lp.setEntries(battPrefText);
        lp.setEntryValues(battPrefKeys);
        lp.setDefaultValue("inact");
        lp.setEnabled(enabled);

        preferences.add(lp);

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey(batteryprefix + ".record");
        sp.setTitle(R.string.pref_basic_monitoring_record);
        sp.setEnabled(enabled);

        preferences.add(sp);

        cp = new NicedPreferences.NiceSeparatorPreference(context);
        cp.setTitle(R.string.pref_basic_monitoring_warnings);
        cp.setEnabled(enabled);
        preferences.add(cp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(batteryprefix + ".remind");
        lp.setTitle(R.string.pref_basic_monitoring_remind);
        lp.setEntries(battWarnText);
        lp.setEntryValues(battWarnKeys);
        lp.setDefaultValue("never");
        lp.setEnabled(enabled);

        preferences.add(lp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(batteryprefix + ".warn");
        lp.setTitle(R.string.pref_basic_monitoring_warn);
        lp.setEntries(battWarnText);
        lp.setEntryValues(battWarnKeys);
        lp.setDefaultValue("never");
        lp.setEnabled(enabled);

        preferences.add(lp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(batteryprefix + ".assistance");
        lp.setTitle(R.string.pref_basic_monitoring_assistance);
        lp.setEntries(battWarnText);
        lp.setEntryValues(battWarnKeys);
        lp.setDefaultValue("never");
        lp.setEnabled(enabled);

        preferences.add(lp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(batteryprefix + ".repeat");
        lp.setTitle(R.string.pref_basic_monitoring_repeat);
        lp.setEntries(battRepeatText);
        lp.setEntryValues(battRepeatKeys);
        lp.setDefaultValue("once");
        lp.setEnabled(enabled);

        preferences.add(lp);
    }
}