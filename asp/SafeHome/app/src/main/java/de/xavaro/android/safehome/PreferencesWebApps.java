package de.xavaro.android.safehome;

import android.webkit.JavascriptInterface;
import android.support.annotation.Nullable;

import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.content.Context;
import android.webkit.WebView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.WebApp;

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

        private final Handler handler = new Handler();
        private String webappkeyprefix;
        private String webappname;
        private WebView webprefs;

        public WebappFragment()
        {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            keyprefix = "webapps";
            webappname = getArguments().getString("webappname");
            webappkeyprefix = keyprefix + ".pref." + webappname + ".";

            super.onCreate(savedInstanceState);
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

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

        @SuppressWarnings("unused")
        private class WebAppPrefBuilder implements
                Preference.OnPreferenceChangeListener,
                NicedPreferences.NiceSearchPreference.SearchCallback
        {
            private final String LOGTAG = WebAppPrefBuilder.class.getSimpleName();

            @Override
            public boolean onPreferenceChange(Preference preference, Object obj)
            {
                Log.d(LOGTAG, "onPreferenceChange:" + preference.getKey());

                if (preference instanceof Preference.OnPreferenceChangeListener)
                {
                    Preference.OnPreferenceChangeListener pcl;
                    pcl = (Preference.OnPreferenceChangeListener) preference;
                    pcl.onPreferenceChange(preference, obj);
                }

                final String key = preference.getKey().substring(webappkeyprefix.length());

                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String script = "WebAppPrefBuilder.onPreferenceChanged"
                                + "(\"" + key + "\");";

                        webprefs.evaluateJavascript(script, null);
                    }
                });

                return true;
            }

            private Preference createPreference(JSONObject pref)
            {
                if (pref == null) return null;

                String key = Json.getString(pref, "key");
                String type = Json.getString(pref, "type");
                String title = Json.getString(pref, "title");
                String summary = Json.getString(pref, "summary");
                boolean enabled = (! Json.has(pref, "enabled")) || Json.getBoolean(pref, "enabled");

                if (key == null) return null;

                key = webappkeyprefix + key;

                NicedPreferences.NiceSearchPreference qp;
                NicedPreferences.NiceCategoryPreference ct;
                NicedPreferences.NiceSwitchPreference sp;
                NicedPreferences.NiceCheckboxPreference cp;
                NicedPreferences.NiceEditTextPreference ep;
                NicedPreferences.NiceListPreference lp;
                NicedPreferences.NiceMultiListPreference mp;
                NicedPreferences.NiceNumberPreference np;
                NicedPreferences.NiceDatePreference dp;

                Preference ap = null;

                if (Simple.equals(type, "date"))
                {
                    ap = dp = new NicedPreferences.NiceDatePreference(Simple.getAppContext());
                }

                if (Simple.equals(type, "search"))
                {
                    ap = qp = new NicedPreferences.NiceSearchPreference(Simple.getAppContext());

                    qp.setSearchCallback(this);
                }

                if (Simple.equals(type, "category"))
                {
                    ap = ct = new NicedPreferences.NiceCategoryPreference(Simple.getAppContext());
                    ct.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                }

                if (Simple.equals(type, "switch"))
                {
                    ap = sp = new NicedPreferences.NiceSwitchPreference(Simple.getAppContext());

                    sp.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                }

                if (Simple.equals(type, "check"))
                {
                    ap = cp = new NicedPreferences.NiceCheckboxPreference(Simple.getAppContext());

                    cp.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                }

                if (Simple.equals(type, "edit"))
                {
                    ap = ep = new NicedPreferences.NiceEditTextPreference(Simple.getAppContext());

                    ep.setDefaultValue(Json.getString(pref, "defvalue"));
                }

                if (Simple.equals(type, "list"))
                {
                    ap = lp = new NicedPreferences.NiceListPreference(Simple.getAppContext());

                    lp.setEntries(Json.getArray(pref, "vals"));
                    lp.setEntryValues(Json.getArray(pref, "keys"));
                    lp.setDefaultValue(Json.getString(pref, "defvalue"));
                }

                if (Simple.equals(type, "multi"))
                {
                    ap = mp = new NicedPreferences.NiceMultiListPreference(Simple.getAppContext());

                    mp.setEntries(Json.getArray(pref, "vals"));
                    mp.setEntryValues(Json.getArray(pref, "keys"));
                }

                if (Simple.equals(type, "number"))
                {
                    ap = np = new NicedPreferences.NiceNumberPreference(Simple.getAppContext());

                    int min = Json.getInt(pref, "min");
                    int max = Json.getInt(pref, "max");
                    int step = Json.getInt(pref, "step");

                    np.setMinMaxValue(min, max, step);
                    np.setUnit(Json.getString(pref, "unit"));
                    np.setDefaultValue(Json.getInt(pref, "defvalue"));
                }

                if (ap != null)
                {
                    ap.setKey(key);
                    ap.setTitle(title);
                    ap.setSummary(summary);
                    ap.setEnabled(enabled);
                    ap.setOnPreferenceChangeListener(this);
                }

                return ap;
            }

            @Nullable
            private Preference preferenceExists(String key)
            {
                for (Preference pref : preferences)
                {
                    if (Simple.equals(key, pref.getKey()))
                    {
                        return pref;
                    }
                }

                return null;
            }

            private void updatePreferences(JSONArray prefs)
            {
                PreferenceScreen prefscreen = getPreferenceScreen();
                ArrayList<Preference> newprefs = new ArrayList<>();

                //
                // Migrate first enable preference.
                //

                newprefs.add(preferences.remove(0));

                //
                // Update or create preferences. Try to keep all
                // unchanged preferences alive until the first
                // change happens. This is done to keep the
                // scolling position of the preference screen
                // intact if possible.
                //

                boolean gameover = false;

                for (int inx = 0; inx < prefs.length(); inx++)
                {
                    JSONObject pref = Json.getObject(prefs, inx);
                    if (pref == null) continue;

                    boolean enabled = (! Json.has(pref, "enabled"))
                            || Json.getBoolean(pref, "enabled");

                    String key = Json.getString(pref, "key");
                    if (key == null) continue;
                    String realkey = webappkeyprefix + key;

                    if ((preferences.size() > 0) && ! gameover)
                    {
                        Preference apref = preferences.get(0);

                        if (Simple.equals(apref.getKey(), realkey))
                        {
                            apref.setEnabled(enabled);
                            preferences.remove(0);
                            newprefs.add(apref);
                            continue;
                        }
                        else
                        {
                            gameover = true;
                        }
                    }

                    Preference npref = createPreference(pref);

                    if (npref != null)
                    {
                        newprefs.add(npref);
                        prefscreen.addPreference(npref);
                    }
                }

                while (preferences.size() > 0)
                {
                    prefscreen.removePreference(preferences.remove(0));
                }

                preferences = newprefs;
            }

            @JavascriptInterface
            public void updatePreferences(String json)
            {
                final JSONArray prefs = Json.fromStringArray(json);

                if (prefs != null)
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            updatePreferences(prefs);
                        }
                    });
                }
            }

            public void onSearchCancel(String prefkey)
            {
                String script = "if (WebAppPrefBuilder.onSearchCancel) "
                        + "WebAppPrefBuilder.onSearchCancel"
                        + "(\"" + prefkey + "\");";

                webprefs.evaluateJavascript(script, null);
            }

            public void onSearchRequest(String prefkey, String query)
            {
                String script = "if (WebAppPrefBuilder.onSearchRequest) "
                        + "WebAppPrefBuilder.onSearchRequest"
                        + "(\"" + prefkey + "\", \"" + query + "\");";

                webprefs.evaluateJavascript(script, null);
            }
        }
    }

    //endregion Webapps preferences
}
