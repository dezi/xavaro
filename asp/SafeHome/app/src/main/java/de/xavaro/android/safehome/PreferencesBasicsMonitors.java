package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;

import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsMonitors extends PreferenceFragments.EnableFragmentStub
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

        keyprefix = "monitors";
        iconres = CommonConfigs.IconResMonitoring;
        summaryres = R.string.pref_basic_monitoring_summary;
        masterenable = Simple.getTrans(R.string.pref_basic_monitoring_enable);
    }

    @Override
    public void registerAll(Context context)
    {
        super.registerAll(context);

        NicedPreferences.NiceCategoryPreference cp;
        NicedPreferences.NiceSwitchPreference sp;
        NicedPreferences.NiceListPreference lp;
        NicedPreferences.NiceEditTextPreference ep;

        CharSequence[] wherePrefText = Simple.getTransArray(R.array.pref_basic_monitoring_where_vals);
        CharSequence[] wherePrefKeys = Simple.getTransArray(R.array.pref_basic_monitoring_where_keys);

        //
        // Activities status monitoring.
        //

        String todayprefix = keyprefix + ".today";

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setIcon(GlobalConfigs.IconResToday);
        cp.setTitle(R.string.pref_basic_monitoring_today);
        cp.setSummary(R.string.pref_basic_monitoring_today_summary);
        cp.setEnabled(enabled);
        preferences.add(cp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(todayprefix + ".mode");
        lp.setTitle(R.string.pref_basic_monitoring_today_where);
        lp.setEntries(wherePrefText);
        lp.setEntryValues(wherePrefKeys);
        lp.setDefaultValue("inact");
        lp.setEnabled(enabled);

        preferences.add(lp);

        //
        // Prepaid status monitoring.
        //

        String prepaidprefix = keyprefix + ".prepaid";

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setIcon(CommonConfigs.IconResPrepaid);
        cp.setTitle(R.string.pref_basic_monitoring_prepaid);
        cp.setSummary(R.string.pref_basic_monitoring_prepaid_summary);
        cp.setEnabled(enabled);
        preferences.add(cp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey(prepaidprefix + ".mode");
        lp.setTitle(R.string.pref_basic_monitoring_prepaid_where);
        lp.setEntries(wherePrefText);
        lp.setEntryValues(wherePrefKeys);
        lp.setDefaultValue("inact");
        lp.setEnabled(enabled);

        preferences.add(lp);

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey(prepaidprefix + ".balance");
        ep.setTitle(R.string.pref_basic_monitoring_prepaid_balance);
        ep.setIsPhonenumber();
        ep.setEnabled(enabled);
        ep.setDefaultValue("*100#");

        if (Simple.getSharedPrefString(ep.getKey()) == null)
        {
            Simple.setSharedPrefString(ep.getKey(),"*100#");
        }

        preferences.add(ep);

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey(prepaidprefix + ".charge");
        ep.setTitle(R.string.pref_basic_monitoring_prepaid_charge);
        ep.setIsPhonenumber();
        ep.setEnabled(enabled);
        ep.setDefaultValue("*101*#");

        if (Simple.getSharedPrefString(ep.getKey()) == null)
        {
            Simple.setSharedPrefString(ep.getKey(),"*101*#");
        }

        preferences.add(ep);

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

        String batteryprefix = keyprefix + ".battery";

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
        lp.setEntries(wherePrefText);
        lp.setEntryValues(wherePrefKeys);
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