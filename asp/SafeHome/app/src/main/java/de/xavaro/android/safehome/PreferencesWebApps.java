package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.WebApp;
import de.xavaro.android.common.WebAppPrefs;

public class PreferencesWebApps
{
    //region Webapps preferences

    public static class WebappFragment extends SettingsFragments.BasePreferenceFragment
    {
        private static final String LOGTAG = WebappFragment.class.getSimpleName();

        public static void getHeaders(List<PreferenceActivity.Header> target)
        {
            JSONObject webapps = StaticUtils.readRawTextResourceJSON(
                    Simple.getAnyContext(), R.raw.default_webapps);

            if ((webapps == null) || ! webapps.has("webapps")) return;
            webapps = Json.getObject(webapps, "webapps");
            if (webapps == null) return;

            Iterator<String> keysIterator = webapps.keys();

            while (keysIterator.hasNext())
            {
                String webappname = keysIterator.next();

                PreferenceActivity.Header header;

                header = new PreferenceActivity.Header();
                header.title = WebApp.getLabel(webappname);
                header.fragment = WebappFragment.class.getName();
                header.fragmentArguments = new Bundle();
                header.fragmentArguments.putString("webappname", webappname);

                target.add(header);
            }
        }

        private String webappname;
        private WebView webprefs;

        public WebappFragment()
        {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            webappname = getArguments().getString("webappname");
            keyprefix = "webapps";

            super.onCreate(savedInstanceState);
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;

            String[] keys =  Simple.getTransArray(R.array.pref_webapps_where_keys);
            String[] vals =  Simple.getTransArray(R.array.pref_webapps_where_vals);

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey(keyprefix + ".mode." + webappname);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("inact");
            lp.setIcon(WebApp.getAppIcon(webappname));
            lp.setTitle("Aktiviert");

            lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    ((NicedPreferences.NiceListPreference) preference).onPreferenceChange(
                            preference, newValue);

                    onEnableStateChanged((String) newValue);

                    return true;
                }
            });

            onEnableStateChanged(Simple.getSharedPrefString(lp.getKey()));

            preferences.add(lp);

            if ((webprefs == null) && WebApp.hasPreferences(webappname))
            {
                webprefs = new WebView(Simple.getAppContext());
                WebApp.loadWebView(webprefs, webappname, "pref");

                Object builder = new WebAppPrefBuilder();
                webprefs.addJavascriptInterface(builder, "WebAppPrefBuilder");
            }
        }

        public void onEnableStateChanged(String mode)
        {
            Log.d(LOGTAG, "==========================>" + mode);
        }

        private class WebAppPrefBuilder
        {
            private final String LOGTAG = WebAppPrefBuilder.class.getSimpleName();

            private void addPreference(JSONObject pref)
            {
                if (pref == null) return;

                String key = Json.getString(pref, "key");
                String type = Json.getString(pref, "type");
                String title = Json.getString(pref, "title");

                if (key == null) return;

                key = keyprefix + ".pref." + webappname + "." + key;

                NicedPreferences.NiceCategoryPreference ct;
                NicedPreferences.NiceSwitchPreference sp;
                NicedPreferences.NiceCheckboxPreference cp;
                NicedPreferences.NiceEditTextPreference ep;
                NicedPreferences.NiceListPreference lp;
                NicedPreferences.NiceMultiListPreference mp;

                if (Simple.equals(type, "category"))
                {
                    ct = new NicedPreferences.NiceCategoryPreference(Simple.getAppContext());

                    ct.setKey(key);
                    ct.setTitle(title);
                    preferences.add(ct);
                    getPreferenceScreen().addPreference(ct);
                }

                if (Simple.equals(type, "switch"))
                {
                     sp = new NicedPreferences.NiceSwitchPreference(Simple.getAppContext());

                    sp.setKey(key);
                    sp.setTitle(title);
                    sp.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                    preferences.add(sp);
                    getPreferenceScreen().addPreference(sp);
                }

                if (Simple.equals(type, "check"))
                {
                    cp = new NicedPreferences.NiceCheckboxPreference(Simple.getAppContext());

                    cp.setKey(key);
                    cp.setTitle(title);
                    cp.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                    preferences.add(cp);
                    getPreferenceScreen().addPreference(cp);
                }

                if (Simple.equals(type, "edit"))
                {
                    ep = new NicedPreferences.NiceEditTextPreference(Simple.getAppContext());

                    ep.setKey(key);
                    ep.setTitle(title);
                    ep.setDefaultValue(Json.getString(pref, "defvalue"));
                    preferences.add(ep);
                    getPreferenceScreen().addPreference(ep);
                }

                if (Simple.equals(type, "list"))
                {
                    lp = new NicedPreferences.NiceListPreference(Simple.getAppContext());

                    lp.setKey(key);
                    lp.setTitle(title);
                    lp.setEntries(Json.getArray(pref, "vals"));
                    lp.setEntryValues(Json.getArray(pref, "keys"));
                    lp.setDefaultValue(Json.getString(pref, "defvalue"));
                    preferences.add(lp);
                    getPreferenceScreen().addPreference(lp);
                }

                if (Simple.equals(type, "multi"))
                {
                    mp = new NicedPreferences.NiceMultiListPreference(Simple.getAppContext());

                    mp.setKey(key);
                    mp.setTitle(title);
                    mp.setEntries(Json.getArray(pref, "vals"));
                    mp.setEntryValues(Json.getArray(pref, "keys"));
                    preferences.add(mp);
                    getPreferenceScreen().addPreference(mp);
                }
            }

            private void addPreferences(JSONArray prefs)
            {
                if (prefs == null) return;

                for (int inx = 0; inx < prefs.length(); inx++)
                {
                    addPreference(Json.getObject(prefs, inx));
                }
            }

            @JavascriptInterface
            public void addPreference(String json)
            {
                Log.d(LOGTAG, "addPreference: " + json);
                addPreference(Json.fromString(json));
            }

            @JavascriptInterface
            public void addPreferences(String json)
            {
                Log.d(LOGTAG, "addPreferences: " + json);
                addPreferences(Json.fromStringArray(json));
            }
        }
    }

    //endregion Webapps preferences
}
