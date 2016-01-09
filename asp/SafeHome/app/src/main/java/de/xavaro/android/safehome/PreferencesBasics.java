package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;

import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.PreferenceFragments;

public class PreferencesBasics
{
    //region Owner preferences

    public static class OwnerFragment extends PreferenceFragments.BasicFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Eigentümer";
            header.iconRes = GlobalConfigs.IconResOwner;
            header.fragment = OwnerFragment.class.getName();

            return header;
        }

        public void registerAll(Context context)
        {
            super.registerAll(context);

            SettingsNiced.NiceCategoryPreference pc;
            SettingsNiced.NiceEditTextPreference et;
            SettingsNiced.NiceListPreference lp;

            pc = new SettingsNiced.NiceCategoryPreference(context);
            pc.setTitle("Persönliches");
            preferences.add(pc);

            final CharSequence[] prefixText = { "Keine", "Herr", "Frau" };
            final CharSequence[] prefixVals = { "no",    "mr",   "ms"   };

            lp = new SettingsNiced.NiceListPreference(context);

            lp.setKey("owner.prefix");
            lp.setEntries(prefixText);
            lp.setEntryValues(prefixVals);
            lp.setDefaultValue("no");
            lp.setTitle("Anrede");

            preferences.add(lp);

            et = new SettingsNiced.NiceEditTextPreference(context);

            et.setKey("owner.firstname");
            et.setTitle("Vorname");

            preferences.add(et);

            et = new SettingsNiced.NiceEditTextPreference(context);

            et.setKey("owner.givenname");
            et.setTitle("Nachname");

            preferences.add(et);

            lp = new SettingsNiced.NiceListPreference(context);

            final CharSequence[] siezenText =
                    {
                            "gesiezt werden",
                            "geduzt werden",
                            "Hamburger Sie",
                            "Münchner Du"
                    };

            final CharSequence[] siezenVals = { "siezen", "duzen", "hamsie", "mucdu" };

            lp.setKey("owner.siezen");
            lp.setEntries(siezenText);
            lp.setEntryValues(siezenVals);
            lp.setDefaultValue("siezen");
            lp.setTitle("Anwender möchte");

            preferences.add(lp);
        }
    }

    //endregion Owner preferences

    //region Administrator preferences

    public static class AdminFragment extends PreferenceFragments.BasicFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Administrator";
            header.iconRes = GlobalConfigs.IconResAdministrator;
            header.fragment = AdminFragment.class.getName();

            return header;
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            SettingsNiced.NiceEditTextPreference et;
            SettingsNiced.NiceCategoryPreference pc;

            pc = new SettingsNiced.NiceCategoryPreference(context);
            pc.setTitle("Zugang");
            preferences.add(pc);

            et = new SettingsNiced.NiceEditTextPreference(context);

            et.setKey("admin.password");
            et.setTitle("Administrator Passwort (zum Anzeigen clicken)");
            et.setIsPassword();

            if (! sharedPrefs.getString(et.getKey(),"").equals(""))
            {
                ArchievementManager.archieved("configure.settings.password");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.password");
            }

            preferences.add(et);

            pc = new SettingsNiced.NiceCategoryPreference(context);
            pc.setTitle("Abgeschlossenheit");
            preferences.add(pc);

            et = new SettingsNiced.NiceEditTextPreference(context);

            et.setKey("admin.home.button");
            et.setTitle("Anwendung auf dem Home-Button");
            et.setText(DitUndDat.DefaultApps.getDefaultHomeLabel(context));
            et.setOnclick(selectHome);

            if (sharedPrefs.getString(et.getKey(), "").equals(StaticUtils.getAppName(context)))
            {
                ArchievementManager.archieved("configure.settings.homebutton");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.homebutton");
            }

            preferences.add(et);

            et = new SettingsNiced.NiceEditTextPreference(context);

            et.setKey("admin.assist.button");
            et.setTitle("Anwendung auf dem Assistenz-Button");
            et.setText(DitUndDat.DefaultApps.getDefaultAssistLabel(context));
            et.setOnclick(selectAssist);

            if (sharedPrefs.getString(et.getKey(),"").equals(DitUndDat.DefaultApps.getAppLable(context)))
            {
                ArchievementManager.archieved("configure.settings.assistbutton");
            }
            else
            {
                ArchievementManager.revoke("configure.settings.assistbutton");
            }

            preferences.add(et);

            SettingsNiced.NiceListPreference cb = new SettingsNiced.NiceListPreference(context);

            CharSequence[] menueText = { "Android-System", "SafeHome" };
            CharSequence[] menueVals = { "android", "safehome" };

            cb.setKey("admin.recent.button");
            cb.setEntries(menueText);
            cb.setEntryValues(menueVals);
            cb.setDefaultValue("safehome");
            cb.setTitle("Anwendung auf dem Menü-Button");

            preferences.add(cb);
        }

        private final Runnable selectHome = new Runnable()
        {
            @Override
            public void run()
            {
                DitUndDat.DefaultApps.setDefaultHome(context);
            }
        };

        private final Runnable selectAssist = new Runnable()
        {
            @Override
            public void run()
            {
                DitUndDat.DefaultApps.setDefaultAssist(context);
            }
        };
    }

    //endregion Administrator preferences

    //region Community preferences

    public static class CommunityFragment extends PreferenceFragments.BasicFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Bezugspersonen";
            header.iconRes = GlobalConfigs.IconResCommunity;
            header.fragment = CommunityFragment.class.getName();

            return header;
        }

        public void registerAll(Context context)
        {
            super.registerAll(context);
        }
    }

    //endregion Community preferences
}
