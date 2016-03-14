package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;

public class PreferencesMedia
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
            keyprefix = "media.image";
            masterenable = "Bilder freischalten";
        }

        private ArrayList<NicedPreferences.NiceCheckboxPreference> contacts = new ArrayList<>();

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceCheckboxPreference cp;
            NicedPreferences.NiceSwitchPreference sp;
            NicedPreferences.NiceListPreference lp;

            //
            // Settings.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Einstellungen");
            preferences.add(pc);

            sp = new NicedPreferences.NiceSwitchPreference(context);

            sp.setKey(keyprefix + ".camera");
            sp.setTitle("Kamera");
            sp.setEnabled(enabled);

            preferences.add(sp);

            sp = new NicedPreferences.NiceSwitchPreference(context);

            sp.setKey(keyprefix + ".screenshot");
            sp.setTitle("Screenshots");
            sp.setEnabled(enabled);

            preferences.add(sp);

            sp = new NicedPreferences.NiceSwitchPreference(context);

            sp.setKey(keyprefix + ".delete");
            sp.setTitle("Löschen");
            sp.setEnabled(enabled);

            preferences.add(sp);

            //
            // Galeries.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Bildgalerien");
            preferences.add(pc);

            String[] keys =  Simple.getTransArray(R.array.pref_media_image_directories_keys);
            String[] vals =  Simple.getTransArray(R.array.pref_media_image_directories_vals);

            for (int inx = 0; (inx < keys.length) && (inx < vals.length); inx++)
            {
                lp = new NicedPreferences.NiceListPreference(context);

                lp.setKey(keyprefix + ".directory." + keys[ inx ]);
                lp.setEntries(R.array.pref_media_image_where_vals);
                lp.setEntryValues(R.array.pref_media_image_where_keys);
                lp.setDefaultValue("inact");
                lp.setTitle(vals[ inx ]);
                lp.setEnabled(enabled);

                preferences.add(lp);
            }

            //
            // Send images.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Einfaches Versenden");
            preferences.add(pc);

            sp = new NicedPreferences.NiceSwitchPreference(context);

            sp.setKey(keyprefix + ".simplesend.enable");
            sp.setTitle("Freischalten");
            sp.setEnabled(enabled);

            sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    for (NicedPreferences.NiceCheckboxPreference cb : contacts)
                    {
                        cb.setEnabled((boolean) newValue);
                    }

                    return true;
                }
            });

            preferences.add(sp);

            boolean simpleenabled = Simple.getSharedPrefBoolean(sp.getKey());

            JSONObject rcs = RemoteContacts.getAllContacts();

            if (rcs != null)
            {
                Iterator<String> keysIterator = rcs.keys();

                while (keysIterator.hasNext())
                {
                    String ident = keysIterator.next();
                    String name = RemoteContacts.getDisplayName(ident);

                    cp = new NicedPreferences.NiceCheckboxPreference(context);

                    cp.setKey(keyprefix + ".simplesend.contact." + ident);
                    cp.setTitle(name);
                    cp.setEnabled(enabled && simpleenabled);

                    contacts.add(cp);
                    preferences.add(cp);
                }
            }
        }
    }

    //endregion Media images preferences

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
            NicedPreferences.NiceSwitchPreference sp;
            NicedPreferences.NiceListPreference lp;

            //
            // Settings.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Einstellungen");
            preferences.add(pc);

            sp = new NicedPreferences.NiceSwitchPreference(context);

            sp.setKey(keyprefix + ".delete");
            sp.setTitle("Löschen");
            sp.setEnabled(enabled);

            preferences.add(sp);

            //
            // Galeries.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Video-Ordner");
            preferences.add(pc);

            String[] keys =  Simple.getTransArray(R.array.pref_media_video_directories_keys);
            String[] vals =  Simple.getTransArray(R.array.pref_media_video_directories_vals);

            for (int inx = 0; (inx < keys.length) && (inx < vals.length); inx++)
            {
                lp = new NicedPreferences.NiceListPreference(context);

                lp.setKey(keyprefix + ".directory." + keys[ inx ]);
                lp.setEntries(R.array.pref_media_video_where_vals);
                lp.setEntryValues(R.array.pref_media_video_where_keys);
                lp.setDefaultValue("inact");
                lp.setTitle(vals[ inx ]);
                lp.setEnabled(enabled);

                preferences.add(lp);
            }
        }
    }

    //endregion Media video preferences

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
            NicedPreferences.NiceListPreference lp;

            //
            // Directories.
            //

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Audioverzeichnisse");
            preferences.add(pc);

            /*
            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".directory.music");
            lp.setTitle("Musik");
            lp.setEnabled(enabled);

            preferences.add(lp);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".directory.spoken");
            lp.setTitle("Hörbücher");
            lp.setEnabled(enabled);

            preferences.add(lp);
            */
        }
    }

    //endregion Media audio preferences

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
