package de.xavaro.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class PreferenceFragments
{
    private static final String LOGTAG = PreferenceFragments.class.getSimpleName();

    //region WeblibFragmentStub extends EnableFragmentStub

    public static class WeblibFragmentStub extends EnableFragmentStub
    {
        protected String type;
        protected String subtype;

        @Override
        protected void registerAll(Context context)
        {
            super.registerAll(context);

            JSONObject config = WebLib.getLocaleConfig(type);
            if (config == null) return;

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceScorePreference cb;

            Iterator<String> keysIterator = config.keys();

            while (keysIterator.hasNext())
            {
                String website = keysIterator.next();

                JSONObject webitem = Json.getObject(config, website);
                if (webitem == null) continue;

                if (webitem.has("enabled") && !Json.getBoolean(webitem, "enabled"))
                {
                    continue;
                }

                if (!Json.equals(webitem, "subtype", subtype))
                {
                    continue;
                }

                String label = Json.getString(webitem, "label");
                String summary = Json.getString(webitem, "summary");

                if (! webitem.has("channels"))
                {
                    /*
                    key = keyprefix + (isApps ? ".apk." : ".website.") + website;

                    cb = new NicedPreferences.NiceScorePreference(context);

                    cb.setKey(key);
                    cb.setTitle(label);
                    cb.setEnabled(enabled);
                    cb.setScore(score);
                    cb.setAPKName(apkname);

                    if (iconres != 0) cb.setIcon(iconres);
                    if (drawable != null) cb.setIcon(drawable);
                    if (summary != null) cb.setSummary(summary);

                    preferences.add(cb);
                    activekeys.add(cb.getKey());

                    boolean def = webitem.has("default") && webitem.getBoolean("default");

                    if (def && ! sharedPrefs.contains(key))
                    {
                        //
                        // Initially commit shared preference.
                        //

                        sharedPrefs.edit().putBoolean(key, true).apply();
                    }
                    */
                }
                else
                {
                    JSONArray channels = Json.getArray(webitem, "channels");
                    if (channels == null) continue;

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setTitle(label);
                    preferences.add(pc);

                    for (int inx = 0; inx < channels.length(); inx++)
                    {
                        JSONObject channel = Json.getObject(channels, inx);
                        if (channel == null) continue;

                        label = Json.getString(channel, "label");
                        if (label == null) continue;

                        String iconurl = Json.getString(channel, "icon");
                        Bitmap thumbnail = CacheManager.cacheThumbnail(context, iconurl);
                        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), thumbnail);

                        cb = new NicedPreferences.NiceScorePreference(context);

                        String key = keyprefix + ".channel:" + website + ":" + label;

                        cb.setKey(key);
                        cb.setTitle(label);
                        cb.setIcon(drawable);
                        cb.setSummary(summary);
                        cb.setEnabled(enabled);

                        preferences.add(cb);
                        activekeys.add(cb.getKey());

                        boolean def = channel.has("default") && Json.getBoolean(channel, "default");
                        if (! Simple.hasSharedPref(key)) Simple.setSharedPrefBoolean(key, def);
                    }
                }
            }

            //
            // Remove disabled or obsoleted preferences.
            //

            Map<String, ?> exists = Simple.getAllPreferences(keyprefix + ".");

            for (Map.Entry<String, ?> entry : exists.entrySet())
            {
                if (! entry.getKey().startsWith(keyprefix)) continue;
                if (activekeys.contains(entry.getKey())) continue;

                Simple.removeSharedPref(entry.getKey());
            }
        }
    }

    //endregion WeblibFragmentStub extends EnableFragmentStub

    //region EnableFragmentStub extends BasicFragmentStub

    public static class EnableFragmentStub extends BasicFragmentStub
            implements Preference.OnPreferenceChangeListener
    {
        protected final ArrayList<String> activekeys = new ArrayList<>();

        protected Preference enabler;
        protected String masterenable;
        protected boolean enabled;
        protected Drawable icondraw;
        protected int iconres;

        @Override
        protected void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceSwitchPreference sw;

            sw = new NicedPreferences.NiceSwitchPreference(context);

            sw.setKey(keyprefix + ".enable");
            sw.setTitle(masterenable);
            sw.setDefaultValue(false);
            sw.setIcon((iconres > 0) ? Simple.getDrawable(iconres) : icondraw);
            sw.setOnPreferenceChangeListener(this);

            preferences.add(sw);
            activekeys.add(sw.getKey());

            enabler = sw;
            enabled = Simple.getSharedPrefBoolean(keyprefix + ".enable");
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object obj)
        {
            for (Preference pref : preferences)
            {
                if (pref == enabler) continue;

                if (pref instanceof NicedPreferences.NiceCheckboxPreference)
                {
                    pref.setEnabled((boolean) obj);
                }
            }

            return true;
        }
    }

    //endregion EnableFragmentStub extends BasicFragmentStub

    //region BasicFragmentStub extends PreferenceFragment

    public static class BasicFragmentStub extends PreferenceFragment
    {
        protected final ArrayList<Preference> preferences = new ArrayList<>();
        protected final SharedPreferences sharedPrefs = Simple.getSharedPrefs();
        protected final Handler handler = new Handler();

        protected PreferenceScreen root;
        protected String keyprefix;

        protected void registerAll(Context context)
        {
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }
    }

    //endregion BasicFragmentStub extends PreferenceFragment
}
