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
            NicedPreferences.NiceCheckboxPreference cp;
            NicedPreferences.NiceGalleryPreference gp;

            //
            // Settings.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Einstellungen");
            preferences.add(pc);

            cp = new NicedPreferences.NiceCheckboxPreference(context);

            cp.setKey(keyprefix + ".camera");
            cp.setTitle("Kamera freigeben");
            cp.setEnabled(enabled);

            preferences.add(cp);

            cp = new NicedPreferences.NiceCheckboxPreference(context);

            cp.setKey(keyprefix + ".screenshot");
            cp.setTitle("Screenshot freigeben");
            cp.setEnabled(enabled);

            preferences.add(cp);

            //
            // Galeries.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Bildgalerien");
            preferences.add(pc);

            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.camera");
            gp.setTitle("Kamera");
            gp.setEnabled(enabled);

            preferences.add(gp);

            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.camera");
            gp.setTitle("Screenshots");
            gp.setEnabled(enabled);

            preferences.add(gp);

            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.download");
            gp.setTitle("Download");
            gp.setEnabled(enabled);

            preferences.add(gp);

            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.whatsapp");
            gp.setTitle("Whatsapp");
            gp.setEnabled(enabled);

            preferences.add(gp);

            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.family");
            gp.setTitle("Familie");
            gp.setEnabled(enabled);

            preferences.add(gp);

            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.misc");
            gp.setTitle("Sonstiges");
            gp.setEnabled(enabled);

            preferences.add(gp);
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
            NicedPreferences.NiceGalleryPreference gp;

            //
            // Directories.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Audioverzeichnisse");
            preferences.add(pc);

            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.music");
            gp.setTitle("Musik");
            gp.setEnabled(enabled);

            preferences.add(gp);
            gp = new NicedPreferences.NiceGalleryPreference(context);

            gp.setKey(keyprefix + ".directory.spoken");
            gp.setTitle("Hörbücher");
            gp.setEnabled(enabled);

            preferences.add(gp);
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
