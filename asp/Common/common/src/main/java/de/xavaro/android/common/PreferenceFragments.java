package de.xavaro.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceFragment;
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
        protected String subcategory;
        protected boolean isplaystore;
        protected int residKeys = R.array.pref_where_keys;
        protected int residVals = R.array.pref_where_vals;

        @Override
        protected void registerAll(Context context)
        {
            super.registerAll(context);

            JSONObject config = WebLib.getLocaleConfig(type);
            if (config == null) return;

            if (subtype != null)
            {
                config = Json.getObject(config, subtype);
                if (config == null) return;
            }

            if (subcategory != null)
            {
                config = Json.getObject(config, subcategory);
                if (config == null) return;
            }

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceScorePreference sp;

            Iterator<String> keysIterator = config.keys();

            while (keysIterator.hasNext())
            {
                String website = keysIterator.next();
                if (website.equals("intent") || website.equals("intents")) continue;

                JSONObject webitem = Json.getObject(config, website);
                if (webitem == null) continue;

                if (webitem.has("enabled") && ! Json.getBoolean(webitem, "enabled"))
                {
                    continue;
                }

                String label = Json.getString(webitem, "label");
                String summary = Json.getString(webitem, "summary");
                String region = Json.getString(webitem, "region");

                if (! webitem.has("channels"))
                {
                    Drawable drawable;
                    String key;

                    if (isplaystore)
                    {
                        drawable = CacheManager.getAppIcon(website);

                        key = keyprefix + ".package:" + website;

                        sp = new NicedPreferences.NiceScorePreference(context);

                        sp.setKey(key);
                        sp.setTitle(label);
                        sp.setIcon(drawable);
                        sp.setSummary(summary);
                        sp.setEntries(residVals);
                        sp.setEntryValues(residKeys);
                        sp.setEnabled(enabled);

                        String score = Json.getString(webitem, "score");
                        sp.setScore((score == null) ? -1 : Integer.parseInt(score));
                        sp.setAPKName(website);

                        preferences.add(sp);
                        activekeys.add(sp.getKey());
                    }
                    else
                    {
                        String iconurl = Json.getString(webitem, "icon");
                        if (iconurl == null) continue;

                        drawable = CacheManager.getWebIcon(website, iconurl);

                        key = keyprefix + ".website:" + website;

                        lp = new NicedPreferences.NiceListPreference(context);

                        //Log.d(LOGTAG, "=======================>" + Simple.getResArrayIdentifier("de_regions_keys"));

                        lp.setKey(key);
                        lp.setTitle(label);
                        lp.setIcon(drawable);
                        lp.setSummary(summary);
                        lp.setEntries(residVals);
                        lp.setEntryValues(residKeys);
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                    }

                    if (! Simple.hasSharedPref(key))
                    {
                        boolean def = Json.getBoolean(webitem, "default");
                        Simple.setSharedPrefString(key, def ? "folder" : "inact");
                    }
                }
                else
                {
                    JSONArray channels = Json.getArray(webitem, "channels");
                    if (channels == null) continue;

                    pc = new NicedPreferences.NiceCategoryPreference(context);
                    pc.setTitle(label);
                    pc.setEnabled(enabled);
                    preferences.add(pc);

                    int activecount = 0;

                    for (int inx = 0; inx < channels.length(); inx++)
                    {
                        JSONObject channel = Json.getObject(channels, inx);
                        if (channel == null) continue;

                        label = Json.getString(channel, "label");
                        if (label == null) continue;

                        String iconurl = Json.getString(channel, "icon");
                        String iconname = website + "." + label;
                        Drawable drawable = CacheManager.getWebIcon(iconname, iconurl);
                        boolean developer = Json.getBoolean(channel, "develop");

                        if (developer && ! Simple.getSharedPrefBoolean("developer.enable"))
                        {
                            //
                            // Developer only enabled content.
                            //

                            continue;
                        }

                        lp = new NicedPreferences.NiceListPreference(context);

                        String key = keyprefix + ".channel:" + website + ":" + label;

                        lp.setKey(key);
                        lp.setTitle(label);
                        lp.setIcon(drawable);
                        lp.setSummary(summary);
                        lp.setEntries(residVals);
                        lp.setEntryValues(residKeys);
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());
                        activecount++;

                        if (! Simple.hasSharedPref(key))
                        {
                            boolean def = Json.getBoolean(channel, "default");
                            Simple.setSharedPrefString(key, def ? "folder" : "inact");
                        }
                    }

                    if (activecount == 0)
                    {
                        //
                        // Category does not contain active entries, remove.
                        //

                        preferences.remove(pc);
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
                pref.setEnabled((boolean) obj);
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
