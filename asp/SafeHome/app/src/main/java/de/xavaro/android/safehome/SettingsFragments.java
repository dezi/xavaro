package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.CacheManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.VersionUtils;

public class SettingsFragments
{
    private static final String LOGTAG = SettingsFragments.class.getSimpleName();

    private static SharedPreferences sharedPrefs;

    public static void initialize(Context context)
    {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //region App discounter preferences

    public static class AppsDiscounterFragment extends JSONConfigFragment
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

            root = "apps/discounter";
            isApps = true;
            jsonres = R.raw.default_apps;
            iconres = GlobalConfigs.IconResAppsDiscounter;
            keyprefix = "apps.discounter";
            masterenable = "Discounter Apps freischalten";
        }
    }

    //endregion App discounters preferences

    //region JSONConfigFragment stub

    @SuppressWarnings("WeakerAccess")
    public static class JSONConfigFragment extends PreferenceFragment
    {
        private final ArrayList<Preference> preferences = new ArrayList<>();
        protected final ArrayList<String> activekeys = new ArrayList<>();

        private JSONObject globalConfig;

        protected String root;
        protected String subtype;
        protected int jsonres;
        protected int iconres;
        protected String keyprefix;
        protected String masterenable;

        protected boolean isApps;

        private void loadGlobalConfig(Context context)
        {
            if (globalConfig == null)
            {
                try
                {
                    JSONObject ctemp = StaticUtils.readRawTextResourceJSON(context, jsonres);

                    if (ctemp != null)
                    {
                        String[] parts = root.split("/");

                        for (String part : parts)
                        {
                            ctemp = ctemp.getJSONObject(part);
                        }

                        globalConfig = ctemp;
                    }
                }
                catch (NullPointerException | JSONException ex)
                {
                    Log.e(LOGTAG, "loadGlobalConfig: Cannot read default " + root + " config.");
                }
            }
        }

        public void registerAll(Context context)
        {
            preferences.clear();

            try
            {
                JSONConfigSwitchPreference sw = new JSONConfigSwitchPreference(context);

                sw.setKey(keyprefix + ".enable");
                sw.setTitle(masterenable);
                sw.setIcon(VersionUtils.getDrawableFromResources(context, iconres));
                sw.setDefaultValue(false);

                preferences.add(sw);
                activekeys.add(sw.getKey());

                boolean enabled = sharedPrefs.getBoolean(keyprefix + ".enable",false);

                NicedPreferences.NiceCategoryPreference pc;
                NicedPreferences.NiceScorePreference cb;

                loadGlobalConfig(context);

                if (globalConfig == null) return;

                Iterator<String> keysIterator = globalConfig.keys();

                String key;
                String label;
                String iconurl;
                String summary;
                int iconres;
                Bitmap thumbnail;
                Drawable drawable;
                String apkname;
                int score;

                while (keysIterator.hasNext())
                {
                    String website = keysIterator.next();

                    JSONObject webitem = globalConfig.getJSONObject(website);

                    score = -1;
                    iconres = 0;
                    summary = null;
                    apkname = null;

                    if (isApps)
                    {
                        label = webitem.getString("what");

                        JSONArray sumarray = webitem.getJSONArray("info");

                        summary = "";

                        for (int inx = 0; inx < sumarray.length(); inx++)
                        {
                            summary += sumarray.getString(inx);
                        }

                        drawable = CacheManager.getIconFromAppStore(context, website);
                        apkname = website;
                        score = Integer.parseInt(webitem.getString("score"));
                    }
                    else
                    {
                        label = webitem.getString("label");
                        iconurl = webitem.getString("icon");
                        thumbnail = CacheManager.cacheThumbnail(context, iconurl);
                        drawable = new BitmapDrawable(context.getResources(), thumbnail);
                    }

                    if (webitem.has("enabled") && ! webitem.getBoolean("enabled"))
                    {
                        continue;
                    }

                    if (webitem.has("subtype") && (subtype != null)
                            && ! webitem.getString("subtype").equals(subtype))
                    {
                        continue;
                    }

                    if (! webitem.has("channels"))
                    {
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
                    }
                    else
                    {
                        pc = new NicedPreferences.NiceCategoryPreference(context);

                        pc.setTitle(label);
                        pc.setIcon(drawable);

                        preferences.add(pc);

                        JSONArray channels = webitem.getJSONArray("channels");

                        for (int inx = 0; inx < channels.length(); inx++)
                        {
                            JSONObject channel = channels.getJSONObject(inx);

                            label = channel.getString("label");
                            key = keyprefix + ".channel." + website + ":" + label.replace(" ", "_");
                            iconurl = channel.getString("icon");
                            thumbnail = CacheManager.cacheThumbnail(context, iconurl);
                            drawable = new BitmapDrawable(context.getResources(), thumbnail);

                            cb = new NicedPreferences.NiceScorePreference(context);

                            cb.setKey(key);
                            cb.setTitle(label);
                            cb.setIcon(drawable);
                            cb.setEnabled(enabled);

                            preferences.add(cb);
                            activekeys.add(cb.getKey());

                            boolean def = channel.has("default") && channel.getBoolean("default");

                            if (def && !sharedPrefs.contains(key))
                            {
                                //
                                // Initially commit shared preference.
                                //

                                sharedPrefs.edit().putBoolean(key, true).apply();
                            }
                        }
                    }
                }

                //
                // Remove disabled or obsoleted preferences.
                //

                String websiteprefix = keyprefix + ".website.";

                Map<String, ?> exists = sharedPrefs.getAll();

                for (Map.Entry<String, ?> entry : exists.entrySet())
                {
                    if (! entry.getKey().startsWith(websiteprefix)) continue;

                    if (activekeys.contains(entry.getKey())) continue;

                    sharedPrefs.edit().remove(entry.getKey()).apply();

                    Log.d(LOGTAG, "registerAll: obsolete:" + entry.getKey() + "=" + entry.getValue());
                }
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG,ex);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }

        //region JSONConfigSwitchPreference implementation

        private class JSONConfigSwitchPreference extends SwitchPreference
                implements Preference.OnPreferenceChangeListener
        {
            public JSONConfigSwitchPreference(Context context)
            {
                super(context);

                setOnPreferenceChangeListener(this);
            }

            @Override
            public boolean onPreferenceChange(Preference preference, Object obj)
            {
                Log.d(LOGTAG, "onPreferenceChange:" + obj.toString());

                for (Preference pref : preferences)
                {
                    if (pref == this) continue;

                    if (pref instanceof NicedPreferences.NiceCheckboxPreference)
                    {
                        pref.setEnabled((boolean) obj);
                    }
                }

                return true;
            }
        }

        //endregion JSONConfigSwitchPreference implementation
    }

    //endregion JSONConfigFragment stub

    //region EnablePreferenceFragment stub

    @SuppressWarnings("WeakerAccess")
    public static class EnablePreferenceFragment extends BasePreferenceFragment
    {
        protected String masterenable;
        protected Drawable icondraw;
        protected int iconres;

        public void registerAll(Context context)
        {
            EnableSwitchPreference sw = new EnableSwitchPreference(context);

            sw.setKey(keyprefix + ".enable");
            sw.setTitle(masterenable);
            sw.setDefaultValue(false);
            sw.setIcon((iconres > 0) ? Simple.getDrawable(iconres) : icondraw);

            preferences.add(sw);
            activekeys.add(sw.getKey());

            enabled = Simple.getSharedPrefBoolean(keyprefix + ".enable");
        }

        protected class EnableSwitchPreference extends SwitchPreference
                implements Preference.OnPreferenceChangeListener
        {
            public EnableSwitchPreference(Context context)
            {
                super(context);

                setOnPreferenceChangeListener(this);
            }

            @Override
            public boolean onPreferenceChange(Preference preference, Object obj)
            {
                Log.d(LOGTAG, "onPreferenceChange:" + obj.toString());

                for (Preference pref : preferences)
                {
                    if (pref == this) continue;

                    pref.setEnabled((boolean) obj);
                }

                return true;
            }
        }
    }

    //endregion EnablePreferenceFragment stub

    //region BasePreferenceFragment stub

    @SuppressWarnings("WeakerAccess")
    public static class BasePreferenceFragment extends PreferenceFragment
    {
        protected final ArrayList<Preference> preferences = new ArrayList<>();
        protected final ArrayList<String> activekeys = new ArrayList<>();

        protected String keyprefix;
        protected boolean enabled;

        public void registerAll(Context context)
        {
            preferences.clear();
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(root);

            registerAll(getActivity());

            for (Preference pref : preferences) root.addPreference(pref);
        }
    }

    //endregion BasePreferenceFragment stub
}
