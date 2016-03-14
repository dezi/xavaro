package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;

import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;

public class PreferencesWebStream
{
    //region IP Radio preferences

    public static class IPRadioFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Radio";
            header.iconRes = GlobalConfigs.IconResIPRadio;
            header.fragment = IPRadioFragment.class.getName();

            return header;
        }

        public IPRadioFragment()
        {
            super();

            type = "iprd";
            iconres = GlobalConfigs.IconResIPRadio;
            keyprefix = "iprd";
            masterenable = "Internet Radio freischalten";
        }
    }

    //endregion IP Radio preferences

    //region IP Television preferences

    public static class IPTelevisionFragment extends PreferenceFragments.WeblibFragmentStub
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Fernsehen";
            header.iconRes = GlobalConfigs.IconResIPTelevision;
            header.fragment = IPTelevisionFragment.class.getName();

            return header;
        }

        public IPTelevisionFragment()
        {
            super();

            type = "iptv";
            iconres = GlobalConfigs.IconResIPTelevision;
            keyprefix = "iptv";
            masterenable = "Internet Fernsehen freischalten";
        }
    }

    //endregion IP Television preferences
}
