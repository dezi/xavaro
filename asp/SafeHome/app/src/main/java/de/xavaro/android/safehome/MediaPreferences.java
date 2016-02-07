package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;

import de.xavaro.android.common.NicedPreferences;

public class MediaPreferences
{
    //region Media image preferences

    public static class MediaImageFragment extends SettingsFragments.EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Bilder";
            header.iconRes = GlobalConfigs.IconResMediaImage;
            header.fragment = MediaImageFragment.class.getName();

            return header;
        }

        public MediaImageFragment()
        {
            super();

            iconres = GlobalConfigs.IconResMediaImage;
            keyprefix = "media.images";
            masterenable = "Bilder freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceNumberPreference np;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceDatePreference dp;
        }
    }

    //endregion Media images preferences

    //region Media audio preferences

    public static class MediaAudioFragment extends SettingsFragments.EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Audio";
            header.iconRes = GlobalConfigs.IconResMediaAudio;
            header.fragment = MediaAudioFragment.class.getName();

            return header;
        }

        public MediaAudioFragment()
        {
            super();

            iconres = GlobalConfigs.IconResMediaAudio;
            keyprefix = "media.audio";
            masterenable = "Audio freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceNumberPreference np;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceDatePreference dp;
        }
    }

    //endregion Media audio preferences

    //region Media video preferences

    public static class MediaVideoFragment extends SettingsFragments.EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Video";
            header.iconRes = GlobalConfigs.IconResMediaVideo;
            header.fragment = MediaVideoFragment.class.getName();

            return header;
        }

        public MediaVideoFragment()
        {
            super();

            iconres = GlobalConfigs.IconResMediaVideo;
            keyprefix = "media.video";
            masterenable = "Video freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceNumberPreference np;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceDatePreference dp;
        }
    }

    //endregion Media video preferences

    //region Media ebook preferences

    public static class MediaEbookFragment extends SettingsFragments.EnablePreferenceFragment
    {
        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Bücher";
            header.iconRes = GlobalConfigs.IconResMediaEbook;
            header.fragment = MediaEbookFragment.class.getName();

            return header;
        }

        public MediaEbookFragment()
        {
            super();

            iconres = GlobalConfigs.IconResMediaEbook;
            keyprefix = "media.ebook";
            masterenable = "Bücher freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceNumberPreference np;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceDatePreference dp;
        }
    }

    //endregion Media video preferences
}
