package de.xavaro.android.safehome;

import android.content.Intent;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.NotificationService;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;

public class PreferencesBasicsSafety extends PreferenceFragments.BasicFragmentStub
{
    private static final String LOGTAG = PreferencesBasicsSafety.class.getSimpleName();

    public static PreferenceActivity.Header getHeader()
    {
        PreferenceActivity.Header header;

        header = new PreferenceActivity.Header();
        header.titleRes = R.string.pref_basic_safety;
        header.iconRes = GlobalConfigs.IconResAdministrator;
        header.fragment = PreferencesBasicsSafety.class.getName();

        return header;
    }

    public PreferencesBasicsSafety()
    {
        super();

        iconres = GlobalConfigs.IconResAdministrator;
        summaryres = R.string.pref_basic_safety_summary;
    }

    private NicedPreferences.NiceListPreference notificationsPref;
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
        NicedPreferences.NiceSwitchPreference sp;
        NicedPreferences.NiceListPreference lp;

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_safety_access);
        cp.setSummary(R.string.pref_basic_safety_access_summary);
        preferences.add(cp);

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey("admin.password");
        ep.setTitle(R.string.pref_basic_safety_password);
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
        // Notification services.
        //

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_safety_notifications_service);
        cp.setSummary(R.string.pref_basic_safety_notifications_summary);
        preferences.add(cp);

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey("admin.notifications.enabled");
        sp.setTitle(R.string.pref_basic_safety_notifications_enable);
        sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                notificationsPref.setEnabled((Boolean) newValue);
                return true;
            }
        });

        preferences.add(sp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey("admin.notifications.service");
        lp.setTitle(R.string.pref_basic_safety_notifications_activate);
        lp.setEntries(Simple.getTransArray(R.array.pref_basic_safety_notifications_vals));
        lp.setEntryValues(Simple.getTransArray(R.array.pref_basic_safety_notifications_keys));
        lp.setDefaultValue("inactive");
        lp.setOnclick(selectNotifications);
        lp.setEnabled(Simple.getSharedPrefBoolean(sp.getKey()));

        preferences.add(lp);
        notificationsPref = lp;

        //
        // Accessibility services.
        //

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_safety_accessibility_service);
        cp.setSummary(R.string.pref_basic_safety_accessibility_summary);
        preferences.add(cp);

        sp = new NicedPreferences.NiceSwitchPreference(context);

        sp.setKey("admin.accessibility.enabled");
        sp.setTitle(R.string.pref_basic_safety_accessibility_enable);
        sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                accessibilityPref.setEnabled((Boolean) newValue);
                return true;
            }
        });

        preferences.add(sp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey("admin.accessibility.service");
        lp.setTitle(R.string.pref_basic_safety_accessibility_activate);
        lp.setEntries(Simple.getTransArray(R.array.pref_basic_safety_accessibility_vals));
        lp.setEntryValues(Simple.getTransArray(R.array.pref_basic_safety_accessibility_keys));
        lp.setDefaultValue("inactive");
        lp.setOnclick(selectAccessibility);
        lp.setEnabled(Simple.getSharedPrefBoolean(sp.getKey()));

        preferences.add(lp);
        accessibilityPref = lp;

        //
        // Accessibility services.
        //

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_safety_kioskmode_service);
        cp.setSummary(R.string.pref_basic_safety_kioskmode_summary);
        preferences.add(cp);

        lp = new NicedPreferences.NiceListPreference(context);

        lp.setKey("admin.kioskmode.service");
        lp.setTitle(R.string.pref_basic_safety_kioskmode_service);
        lp.setEntries(Simple.getTransArray(R.array.pref_basic_safety_kioskmode_vals));
        lp.setEntryValues(Simple.getTransArray(R.array.pref_basic_safety_kioskmode_keys));
        lp.setDefaultValue("inactive");

        preferences.add(lp);

        //
        // System buttons.
        //

        cp = new NicedPreferences.NiceInfoPreference(context);
        cp.setTitle(R.string.pref_basic_safety_buttons);
        cp.setSummary(R.string.pref_basic_safety_buttons_summary);
        preferences.add(cp);

        //
        // Home button application.
        //

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey("admin.home.button");
        ep.setTitle(R.string.pref_basic_safety_home_button);
        ep.setOnclick(selectHome);

        preferences.add(ep);
        homeButtonPref = ep;

        //
        // Assist button application.
        //

        ep = new NicedPreferences.NiceEditTextPreference(context);

        ep.setKey("admin.assist.button");
        ep.setTitle(R.string.pref_basic_safety_assistance_button);
        ep.setOnclick(selectAssist);

        preferences.add(ep);
        assistButtonPref = ep;

        lp = new NicedPreferences.NiceListPreference(context);

        CharSequence[] menueText = {"Android-System", "SafeHome"};
        CharSequence[] menueVals = {"android", "safehome"};

        lp.setKey("admin.recent.button");
        lp.setTitle(R.string.pref_basic_safety_menu_button);
        lp.setEntries(menueText);
        lp.setEntryValues(menueVals);
        lp.setDefaultValue("safehome");

        preferences.add(lp);

        Simple.makePost(monitorSettings);
    }

    public final static Runnable selectNotifications = new Runnable()
    {
        @Override
        public void run()
        {
            if (NotificationService.checkAvailable())
            {
                NotificationService.selectNotificationsSettings.run();

                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getActContext());

            builder.setTitle(R.string.pref_basic_safety_notifications_unavailable);
            builder.setPositiveButton("Ok", null);

            AlertDialog dialog = builder.create();
            dialog.show();
            Simple.adjustAlertDialog(dialog);
        }
    };

    public final static Runnable selectAccessibility = new Runnable()
    {
        @Override
        public void run()
        {
            if (AccessibilityService.checkAvailable())
            {
                AccessibilityService.selectAccessibilitySettings.run();

                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getActContext());

            builder.setTitle(R.string.pref_basic_safety_accessibility_unavailable);
            builder.setPositiveButton("Ok", null);

            AlertDialog dialog = builder.create();
            dialog.show();
            Simple.adjustAlertDialog(dialog);
        }
    };

    private final Runnable selectHome = new Runnable()
    {
        @Override
        public void run()
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ProcessManager.launchIntent(intent);
            }
            else
            {
                DefaultApps.setDefaultHome();
            }
        }
    };

    private final Runnable selectAssist = new Runnable()
    {
        @Override
        public void run()
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ProcessManager.launchIntent(intent);
                }
                else
                {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ProcessManager.launchIntent(intent);
                }

                /* Test
                Intent intent = new Intent(Intent.ACTION_ASSIST);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ProcessManager.launchIntent(intent);
                */
            }
            else
            {
                DefaultApps.setDefaultAssist();
            }
        }
    };

    private final Runnable monitorSettings = new Runnable()
    {
        @Override
        public void run()
        {
            String notify = "unavailable";

            if (NotificationService.checkAvailable())
            {
                notify = NotificationService.checkEnabled() ? "active" : "inactive";
            }

            if (! Simple.equals(notify, Simple.getSharedPrefString("admin.notifications.service")))
            {
                Simple.setSharedPrefString("admin.notifications.service", notify);
                notificationsPref.setValue(notify);

                if (Simple.equals(notify, "active") || Simple.equals(notify, "unavailable"))
                {
                    ArchievementManager.archieved("configure.settings.notifications");
                }
                else
                {
                    ArchievementManager.revoke("configure.settings.notifications");
                }
            }

            String access = "unavailable";

            if (AccessibilityService.checkAvailable())
            {
                access = AccessibilityService.checkEnabled() ? "active" : "inactive";
            }

            if (! Simple.equals(access, Simple.getSharedPrefString("admin.accessibility.service")))
            {
                Simple.setSharedPrefString("admin.accessibility.service", access);
                accessibilityPref.setValue(access);

                if (Simple.equals(access, "active") || Simple.equals(access, "unavailable"))
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
