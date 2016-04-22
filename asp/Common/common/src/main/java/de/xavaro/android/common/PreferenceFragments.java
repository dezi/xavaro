package de.xavaro.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
        protected boolean iscalls;
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

            //
            // Preflight and sort configs.
            //

            Iterator<String> keysIterator = config.keys();
            JSONArray configs = new JSONArray();
            boolean hasregion = false;

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

                Json.put(webitem, "website", website);
                Json.put(configs, webitem);

                if (Json.has(webitem, "region")) hasregion = true;
            }

            //
            // Get locale regions if required.
            //

            Map<String, String> regionMap = null;

            if (hasregion)
            {
                String lang = Simple.getLocaleLanguage();
                int resid = Simple.getResArrayIdentifier(lang + "_regions_keys");
                regionMap = Simple.getTransMap(resid);

                configs = Json.sort(configs, "region", false);
            }

            //
            // Build preferences.
            //

            NicedPreferences.NiceCategoryPreference pc = null;
            NicedPreferences.NiceEditTextPreference ep;
            NicedPreferences.NiceListPreference lp;
            NicedPreferences.NiceScorePreference sp;

            for (int cinx = 0; cinx < configs.length(); cinx++)
            {
                JSONObject webitem = Json.getObject(configs, cinx);
                if (webitem == null) continue;

                String website = Json.getString(webitem, "website");
                if (website == null) continue;

                String label = Json.getString(webitem, "label");
                String summary = Json.getString(webitem, "summary");
                String region = Json.getString(webitem, "region");

                if (! webitem.has("channels"))
                {
                    String key = null;

                    if (isplaystore)
                    {
                        Drawable drawable = CacheManager.getAppIcon(website);

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

                    if (iscalls)
                    {
                        String iconurl = Json.getString(webitem, "icon");
                        if (iconurl == null) continue;

                        Drawable drawable = WebLib.getIconDrawable(type, iconurl);

                        pc = new NicedPreferences.NiceCategoryPreference(context);
                        pc.setTitle(label);
                        pc.setIcon(drawable);
                        pc.setEnabled(enabled);

                        preferences.add(pc);

                        key = keyprefix + ".subtype:" + website;

                        lp = new NicedPreferences.NiceListPreference(context);

                        lp.setKey(key);
                        lp.setTitle("Funktion");
                        lp.setSummary(summary);
                        lp.setEntries(residVals);
                        lp.setEntryValues(residKeys);
                        lp.setEnabled(enabled);

                        preferences.add(lp);
                        activekeys.add(lp.getKey());

                        ep = new NicedPreferences.NiceEditTextPreference(context);

                        ep.setKey(keyprefix + ".nametag:" + website);
                        ep.setTitle("Bezeichnung");
                        ep.setSummary(summary);
                        ep.setEnabled(enabled);

                        String defaulttext = Json.getString(webitem, "defaulttext");
                        if (defaulttext != null) ep.setDefaultValue(defaulttext);

                        preferences.add(ep);
                        activekeys.add(ep.getKey());

                        ep = new NicedPreferences.NiceEditTextPreference(context);

                        String calltext = Json.getString(webitem, "calltext");
                        if (calltext == null) calltext = "Telefon";

                        ep.setKey(keyprefix + ".phonenumber:" + website);
                        ep.setTitle(calltext);
                        ep.setIsPhonenumber();
                        ep.setSummary(summary);
                        ep.setEnabled(enabled);

                        String defaultvalue = Json.getString(webitem, "defaultvalue");
                        if (defaultvalue != null) ep.setDefaultValue(defaultvalue);

                        preferences.add(ep);
                        activekeys.add(ep.getKey());

                        String loadtext = Json.getString(webitem, "loadtext");
                        String loadvalue = Json.getString(webitem, "loadvalue");

                        if ((loadtext != null) && (loadvalue != null))
                        {
                            ep = new NicedPreferences.NiceEditTextPreference(context);

                            ep.setKey(keyprefix + ".prepaidload:" + website);
                            ep.setTitle(loadtext);
                            ep.setIsPhonenumber();
                            ep.setSummary(summary);
                            ep.setEnabled(enabled);
                            ep.setDefaultValue(loadvalue);

                            preferences.add(ep);
                            activekeys.add(ep.getKey());
                        }
                    }

                    if (! (iscalls || isplaystore))
                    {
                        String iconurl = Json.getString(webitem, "icon");
                        if (iconurl == null) continue;

                        if ((regionMap != null) && (region != null)
                                && ((pc == null) || ! region.equals(pc.getKey())))
                        {
                            //
                            // Build a region category header.
                            //

                            pc = new NicedPreferences.NiceCategoryPreference(context);
                            pc.setKey(region);
                            pc.setTitle(regionMap.get(region));
                            pc.setEnabled(enabled);
                            preferences.add(pc);
                        }

                        Drawable drawable = CacheManager.getWebIcon(website, iconurl);

                        key = keyprefix + ".website:" + website;

                        lp = new NicedPreferences.NiceListPreference(context);

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

                    if ((key != null) && ! Simple.hasSharedPref(key))
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

        @Override
        protected void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceSwitchPreference sw;

            sw = new NicedPreferences.NiceSwitchPreference(context);

            sw.setKey(keyprefix + ".enable");
            sw.setTitle(masterenable);
            sw.setDefaultValue(false);
            //sw.setIcon((iconres > 0) ? Simple.getDrawable(iconres) : icondraw);
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
        protected final SharedPreferences sharedPrefs = Simple.getSharedPrefs();
        protected final Handler handler = new Handler();

        protected ArrayList<Preference> preferences = new ArrayList<>();
        protected PreferenceScreen root;
        protected String keyprefix;
        protected Drawable icondraw;
        protected int iconres;

        protected void registerAll(Context context)
        {
        }

        @Override
        @SuppressWarnings("ResourceType")
        public void onStart()
        {
            super.onStart();

            //StaticUtils.dumpViewsChildren(getActivity().getWindow().getDecorView());

            final int breadcrumb_section = 0x1020342;
            final int left_icon = 0x1020032;

            View view = getActivity().getWindow().getDecorView();
            if (view == null) return;

            view = view.findViewById(breadcrumb_section);
            if (view == null) return;

            view = view.findViewById(left_icon);
            if ((view == null) || ! (view instanceof ImageView)) return;

            ImageView icon = (ImageView) view;

            if (iconres != 0) icon.setImageResource(iconres);
            if (icondraw != null) icon.setImageDrawable(icondraw);

            icon.setVisibility(View.VISIBLE);
            icon.setLayoutParams(new LinearLayout.LayoutParams(Simple.WC, Simple.MP));
            icon.setAdjustViewBounds(true);
            icon.setPadding(8, 8, 0, 8);

            view = (View) view.getParent();

            view = (View) view.getParent();
            view = (View) view.getParent();
            view.setBackgroundColor(0xdddddddd);

            view = (View) view.getParent();

            view.setLayoutParams(new LinearLayout.LayoutParams(Simple.MP, Simple.WC));
            view.setPadding(8, 4, 8, 4);

            view = (View) view.getParent();
            StaticUtils.dumpViewsChildren(view);
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
