package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.content.Context;
import android.util.Log;

import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsAdmin extends PreferenceFragments.BasicFragmentStub
{
    private static final String LOGTAG = PreferencesBasicsAdmin.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_basic_admin;
        header.iconRes = GlobalConfigs.IconResAdministrator;
        header.fragment = PreferencesBasicsAdmin.class.getName();

        return header;
    }

    public PreferencesBasicsAdmin()
    {
        super();

        iconres = GlobalConfigs.IconResAdministrator;
    }

    private NicedPreferences.NiceListPreference accessibilityPref;
    private NicedPreferences.NiceEditTextPreference homeButtonPref;
    private NicedPreferences.NiceEditTextPreference assistButtonPref;

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.d(LOGTAG, "onDestroy");

        Simple.removePost(monitorSettings);
    }

    @Override
    public void registerAll(Context context)
    {
        super.registerAll(context);

        NicedPreferences.NiceCategoryPreference cp;
        NicedPreferences.NiceEditTextPreference ep;
        NicedPreferences.NiceListPreference lp;

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_admin_access);
        cp.setSummary(R.string.pref_basic_admin_access_summary);
        preferences.add(cp);

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey("admin.password");
        ep.setTitle(R.string.pref_basic_admin_password);
        ep.setIsPassword();

        if (!sharedPrefs.getString(ep.getKey(), "").equals(""))
        {
            ArchievementManager.archieved("configure.settings.password");
        }
        else
        {
            ArchievementManager.revoke("configure.settings.password");
        }

        preferences.add(ep);

        //
        // System services.
        //

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_admin_services);
        preferences.add(cp);

        CharSequence[] prefText = Simple.getTransArray(R.array.pref_basic_admin_service_vals);
        CharSequence[] prefKeys = Simple.getTransArray(R.array.pref_basic_admin_service_keys);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey("admin.accessibility.service");
        lp.setTitle(R.string.pref_basic_admin_accessibility_service);
        lp.setEntries(prefText);
        lp.setEntryValues(prefKeys);
        lp.setDefaultValue("inact");
        lp.setOnclick(AccessibilityService.selectAccessibility);

        preferences.add(lp);
        accessibilityPref = lp;

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey("admin.kioskmode.service");
        lp.setTitle(R.string.pref_basic_admin_kioskmode_service);
        lp.setEntries(prefText);
        lp.setEntryValues(prefKeys);
        lp.setDefaultValue("inact");

        preferences.add(lp);
        accessibilityPref = lp;

        //
        // System buttons.
        //

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_admin_buttons);
        cp.setSummary(R.string.pref_basic_admin_buttons_summary);
        preferences.add(cp);

        //
        // Home button application.
        //

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey("admin.home.button");
        ep.setTitle(R.string.pref_basic_admin_home_button);
        ep.setOnclick(selectHome);

        preferences.add(ep);
        homeButtonPref = ep;

        //
        // Assist button application.
        //

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey("admin.assist.button");
        ep.setTitle(R.string.pref_basic_admin_assistance_button);
        ep.setOnclick(selectAssist);

        preferences.add(ep);
        assistButtonPref = ep;

        lp = new NicedPreferences.NiceListPreference(context);

        CharSequence[] menueText = {"Android-System", "SafeHome"};
        CharSequence[] menueVals = {"android", "safehome"};

        lp.setKey("admin.recent.button");
        lp.setTitle(R.string.pref_basic_admin_menu_button);
        lp.setEntries(menueText);
        lp.setEntryValues(menueVals);
        lp.setDefaultValue("safehome");

        preferences.add(lp);

        Simple.makePost(monitorSettings);
    }

    private final Runnable selectHome = new Runnable()
    {
        @Override
        public void run()
        {
            DefaultApps.setDefaultHome();
        }
    };

    private final Runnable selectAssist = new Runnable()
    {
        @Override
        public void run()
        {
            DefaultApps.setDefaultAssist();
        }
    };

    private final Runnable monitorSettings = new Runnable()
    {
        @Override
        public void run()
        {
            String access = AccessibilityService.checkEnabled() ? "active" : "inactive";

            if (! Simple.equals(access, Simple.getSharedPrefString("admin.accessibility.service")))
            {
                Simple.setSharedPrefString("admin.accessibility.service", access);
                accessibilityPref.setValue(access);

                if (Simple.equals(access, "active"))
                {
                    ArchievementManager.archieved("configure.settings.accessibility");
                }
                else
                {
                    ArchievementManager.revoke("configure.settings.accessibility");
                }
            }

            String home = DefaultApps.getDefaultHomeLabel();

            if (! Simple.equals(home, Simple.getSharedPrefString("admin.home.button")))
            {
                Simple.setSharedPrefString("admin.home.button", home);
                homeButtonPref.setText(home);

                if (Simple.equals(home, Simple.getAppName()))
                {
                    ArchievementManager.archieved("configure.settings.homebutton");
                }
                else
                {
                    ArchievementManager.revoke("configure.settings.homebutton");
                }
            }

            String assist = DefaultApps.getDefaultAssistLabel();

            if (! Simple.equals(assist, Simple.getSharedPrefString("admin.assist.button")))
            {
                Simple.setSharedPrefString("admin.assist.button", assist);
                assistButtonPref.setText(assist);

                if (Simple.equals(assist, Simple.getAppName()))
                {
                    ArchievementManager.archieved("configure.settings.assistbutton");
                }
                else
                {
                    ArchievementManager.revoke("configure.settings.assistbutton");
                }
            }

            Simple.makePost(monitorSettings, 1000);
        }
    };
}
