package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.PreferenceFragments;

public class PreferencesWebFrame
{
    //region Webframe newspaper preferences

    public static class WebConfigNewspaperFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Zeitungen";
            header.iconRes = GlobalConfigs.IconResWebConfigNewspaper;
            header.fragment = WebConfigNewspaperFragment.class.getName();

            return header;
        }

        public WebConfigNewspaperFragment()
        {
            super();

            type = "ioc";
            subtype = "newspaper";
            iconres = GlobalConfigs.IconResWebConfigNewspaper;
            keyprefix = type + "." + subtype;
            masterenable = "Online Zeitungen freischalten";
            residKeys = R.array.pref_ioc_where_keys;
            residVals = R.array.pref_ioc_where_vals;
        }
    }

    //endregion Webframe newspaper preferences

    //region Webframe magazine preferences

    public static class WebConfigMagazineFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Magazine";
            header.iconRes = GlobalConfigs.IconResWebConfigMagazine;
            header.fragment = WebConfigMagazineFragment.class.getName();

            return header;
        }

        public WebConfigMagazineFragment()
        {
            super();

            type = "ioc";
            subtype = "magazine";
            iconres = GlobalConfigs.IconResWebConfigMagazine;
            keyprefix = type + "." + subtype;
            masterenable = "Online Magazine freischalten";
            residKeys = R.array.pref_ioc_where_keys;
            residVals = R.array.pref_ioc_where_vals;
        }
    }

    //endregion Webframe magazine preferences

    //region Webframe magazine preferences

    public static class WebConfigPictorialFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Illustrierte";
            header.iconRes = GlobalConfigs.IconResWebConfigPictorial;
            header.fragment = WebConfigPictorialFragment.class.getName();

            return header;
        }

        public WebConfigPictorialFragment()
        {
            super();

            type = "ioc";
            subtype = "pictorial";
            iconres = GlobalConfigs.IconResWebConfigPictorial;
            keyprefix = type + "." + subtype;
            masterenable = "Online Illustrierte freischalten";
            residKeys = R.array.pref_ioc_where_keys;
            residVals = R.array.pref_ioc_where_vals;
        }
    }

    //endregion Webframe magazine preferences

    //region Webframe shopping preferences

    public static class WebConfigShoppingFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Shopping";
            header.iconRes = GlobalConfigs.IconResWebConfigShopping;
            header.fragment = WebConfigShoppingFragment.class.getName();

            return header;
        }

        public WebConfigShoppingFragment()
        {
            super();

            type = "ioc";
            subtype = "shopping";
            iconres = GlobalConfigs.IconResWebConfigShopping;
            keyprefix = type + "." + subtype;
            masterenable = "Online Shopping freischalten";
            residKeys = R.array.pref_ioc_where_keys;
            residVals = R.array.pref_ioc_where_vals;
        }
    }

    //endregion Webframe shopping preferences

    //region Webframe erotics preferences

    public static class WebConfigEroticsFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Erotisches";
            header.iconRes = GlobalConfigs.IconResWebConfigErotics;
            header.fragment = WebConfigEroticsFragment.class.getName();

            return header;
        }

        public WebConfigEroticsFragment()
        {
            super();

            type = "ioc";
            subtype = "erotics";
            iconres = GlobalConfigs.IconResWebConfigErotics;
            keyprefix = type + "." + subtype;
            masterenable = "Online Erotisches freischalten";
            residKeys = R.array.pref_ioc_where_keys;
            residVals = R.array.pref_ioc_where_vals;
        }
    }

    //endregion Webframe erotics preferences
}
