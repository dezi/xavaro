package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

public class PreferencesWebFrame
{
    //region Webframe newspaper preferences

    public static class WebConfigNewspaperFragment extends SettingsFragments.JSONConfigFragment
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

            root = "webconfig";
            subtype = "newspaper";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigNewspaper;
            keyprefix = "webconfig.newspaper";
            masterenable = "Online Zeitungen freischalten";
        }
    }

    //endregion Webframe newspaper preferences

    //region Webframe magazine preferences

    public static class WebConfigMagazineFragment extends SettingsFragments.JSONConfigFragment
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

            root = "webconfig";
            subtype = "magazine";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigMagazine;
            keyprefix = "webconfig.magazine";
            masterenable = "Online Magazine freischalten";
        }
    }

    //endregion Webframe magazine preferences

    //region Webframe magazine preferences

    public static class WebConfigPictorialFragment extends SettingsFragments.JSONConfigFragment
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

            root = "webconfig";
            subtype = "pictorial";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigPictorial;
            keyprefix = "webconfig.pictorial";
            masterenable = "Online Illustrierte freischalten";
        }
    }

    //endregion Webframe magazine preferences

    //region Webframe shopping preferences

    public static class WebConfigShoppingFragment extends SettingsFragments.JSONConfigFragment
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

            root = "webconfig";
            subtype = "shopping";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigShopping;
            keyprefix = "webconfig.shopping";
            masterenable = "Online Shopping freischalten";
        }
    }

    //endregion Webframe shopping preferences

    //region Webframe erotics preferences

    public static class WebConfigEroticsFragment extends SettingsFragments.JSONConfigFragment
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

            root = "webconfig";
            subtype = "erotics";
            jsonres = R.raw.default_webconfig;
            iconres = GlobalConfigs.IconResWebConfigErotics;
            keyprefix = "webconfig.erotics";
            masterenable = "Online Erotisches freischalten";
        }
    }

    //endregion Webframe erotics preferences
}
