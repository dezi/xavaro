package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.PreferenceFragments;

public class PreferencesApps
{
    //region IP Radio preferences

    public static class AppsDiscounterFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Discounter";
            header.iconRes = GlobalConfigs.IconResAppsDiscounter;
            header.fragment = AppsDiscounterFragment.class.getName();

            return header;
        }

        public AppsDiscounterFragment()
        {
            super();

            type = "appstore";
            subcategory = "discounter";
            isplaystore = true;
            iconres = GlobalConfigs.IconResAppsDiscounter;
            keyprefix = "apps.discounter";
            masterenable = "Discounter Apps freischalten";
        }
    }

    //endregion IP Radio preferences
}
