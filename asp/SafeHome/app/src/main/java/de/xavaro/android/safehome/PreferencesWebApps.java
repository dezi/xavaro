package de.xavaro.android.safehome;

import android.webkit.JavascriptInterface;
import android.support.annotation.Nullable;

import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.PreferenceFragments;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.WebApp;
import de.xavaro.android.common.WebAppView;

public class PreferencesWebApps
{
    //region Webapps preferences

    public static class WebappFragment extends PreferenceFragments.EnableFragmentStub
    {
        private static final String LOGTAG = WebappFragment.class.getSimpleName();

        public static void getHeaders(List<PreferenceActivity.Header> target, String category)
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
                String appcat = WebApp.getCategory(webappname);

                if (! (((category == null) && (appcat == null)) || Simple.equals(category, appcat)))
                {
                    continue;
                }

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
        private String webappname;
        private WebAppView webprefs;

        public WebappFragment()
        {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            webappname = getArguments().getString("webappname");
            keyprefix = "webapps.pref." + webappname + ".";
            icondraw = WebApp.getAppIcon(webappname);
            masterenable = WebApp.getLabel(webappname) + " " + "freischalten";

            super.onCreate(savedInstanceState);
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceListPreference lp;

            String category = WebApp.getCategory(webappname);

            String[] keys =  Simple.getTransArray(R.array.pref_webapps_where_keys);
            String[] vals =  Simple.getTransArray(R.array.pref_webapps_where_vals);

            if (Simple.equals(category, "games"))
            {
                vals =  Simple.getTransArray(R.array.pref_webapps_where_games_vals);
            }

            lp = new NicedPreferences.NiceListPreference(context);

            lp.setKey("webapps.mode." + webappname);
            lp.setEntries(vals);
            lp.setEntryValues(keys);
            lp.setDefaultValue("none");
            lp.setTitle("Anzeige");
            lp.setEnabled(enabled);

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
            activekeys.add(lp.getKey());

            if ((webprefs == null) && WebApp.hasPreferences(webappname))
            {
                webprefs = new WebAppView(Simple.getActContext());
                webprefs.loadWebView(webappname, "pref");

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
                NicedPreferences.NiceSearchPreference.SearchCallback,
                NicedPreferences.NiceDeletePreference.DeleteCallback
        {
            private final String LOGTAG = WebAppPrefBuilder.class.getSimpleName();

            private final ArrayList<IconPrefData> iconmap = new ArrayList<>();
            private final Drawable nixpix;
            private Thread worker;

            public WebAppPrefBuilder()
            {
                nixpix = Simple.getDrawable(Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888));
            }

            private class IconPrefData
            {
                public final String icon;
                public final Preference ap;

                IconPrefData(String icon, Preference ap)
                {
                    this.icon = icon;
                    this.ap = ap;
                }
            }

            private Runnable iconload = new Runnable()
            {
                @Override
                public void run()
                {
                    if (iconmap.size() > 0)
                    {
                        IconPrefData ip = iconmap.remove(0);

                        byte[] idata = webprefs.webapploader.getRequestData(ip.icon);

                        if (idata != null)
                        {
                            Bitmap bm = BitmapFactory.decodeByteArray(idata, 0, idata.length);
                            ip.ap.setIcon(Simple.getDrawable(bm));
                        }
                    }

                    if (iconmap.size() > 0)
                    {
                        handler.postDelayed(iconload, 0);
                    }
                }
            };

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

                final String key = preference.getKey().substring(keyprefix.length());

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
                String icon = Json.getString(pref, "icon");
                String type = Json.getString(pref, "type");
                String title = Json.getString(pref, "title");
                String summary = Json.getString(pref, "summary");
                boolean enabled = (! Json.has(pref, "enabled")) || Json.getBoolean(pref, "enabled");

                if (key == null) return null;

                key = keyprefix + key;

                NicedPreferences.NiceSearchPreference qp;
                NicedPreferences.NiceCategoryPreference ct;
                NicedPreferences.NiceSwitchPreference sp;
                NicedPreferences.NiceCheckboxPreference cp;
                NicedPreferences.NiceEditTextPreference ep;
                NicedPreferences.NiceListPreference lp;
                NicedPreferences.NiceMultiListPreference mp;
                NicedPreferences.NiceNumberPreference np;
                NicedPreferences.NiceDatePreference dp;
                NicedPreferences.NiceDeletePreference xp;

                Preference ap = null;

                if (Simple.equals(type, "search"))
                {
                    ap = qp = new NicedPreferences.NiceSearchPreference(Simple.getActContext());

                    qp.setSearchCallback(this);
                }

                if (Simple.equals(type, "delete"))
                {
                    ap = xp = new NicedPreferences.NiceDeletePreference(Simple.getActContext());

                    xp.setDeleteCallback(this);

                    xp.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                }

                if (Simple.equals(type, "category"))
                {
                    ap = ct = new NicedPreferences.NiceCategoryPreference(Simple.getActContext());

                    ct.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                }

                if (Simple.equals(type, "date"))
                {
                    ap = dp = new NicedPreferences.NiceDatePreference(Simple.getActContext());

                    dp.setDefaultValue(Json.getString(pref, "defvalue"));
                }

                if (Simple.equals(type, "switch"))
                {
                    ap = sp = new NicedPreferences.NiceSwitchPreference(Simple.getActContext());

                    sp.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                }

                if (Simple.equals(type, "check"))
                {
                    ap = cp = new NicedPreferences.NiceCheckboxPreference(Simple.getActContext());

                    cp.setDefaultValue(Json.getBoolean(pref, "defvalue"));
                }

                if (Simple.equals(type, "edit"))
                {
                    ap = ep = new NicedPreferences.NiceEditTextPreference(Simple.getActContext());

                    ep.setDefaultValue(Json.getString(pref, "defvalue"));
                }

                if (Simple.equals(type, "list"))
                {
                    ap = lp = new NicedPreferences.NiceListPreference(Simple.getActContext());

                    lp.setEntries(Json.getArray(pref, "vals"));
                    lp.setEntryValues(Json.getArray(pref, "keys"));
                    lp.setDefaultValue(Json.getString(pref, "defvalue"));
                }

                if (Simple.equals(type, "multi"))
                {
                    ap = mp = new NicedPreferences.NiceMultiListPreference(Simple.getActContext());

                    mp.setEntries(Json.getArray(pref, "vals"));
                    mp.setEntryValues(Json.getArray(pref, "keys"));
                    mp.setDefaultValue(Json.toSet(Json.getArray(pref, "defvalue")));
                }

                if (Simple.equals(type, "number"))
                {
                    ap = np = new NicedPreferences.NiceNumberPreference(Simple.getActContext());

                    int min = Json.getInt(pref, "min");
                    int max = Json.getInt(pref, "max");
                    int step = Json.getInt(pref, "step");

                    np.setMinMaxValue(min, max, step);
                    np.setUnit(Json.getString(pref, "unit"));
                    np.setDefaultValue(Json.getInt(pref, "defvalue"));
                }

                if (ap != null)
                {
                    if (icon != null)
                    {
                        iconmap.add(new IconPrefData(icon, ap));
                        ap.setIcon(nixpix);
                    }

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
                // Migrate first enable and display preference.
                //

                newprefs.add(preferences.remove(0));
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
                    String realkey = keyprefix + key;

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

                if (iconmap.size() > 0) handler.post(iconload);
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
                prefkey = prefkey.substring(keyprefix.length());

                String script = "if (WebAppPrefBuilder.onSearchCancel) "
                        + "WebAppPrefBuilder.onSearchCancel"
                        + "(\"" + prefkey + "\");";

                webprefs.evaluateJavascript(script, null);
            }

            public void onSearchRequest(String prefkey, String query)
            {
                prefkey = prefkey.substring(keyprefix.length());

                String script = "if (WebAppPrefBuilder.onSearchRequest) "
                        + "WebAppPrefBuilder.onSearchRequest"
                        + "(\"" + prefkey + "\", \"" + query + "\");";

                webprefs.evaluateJavascript(script, null);
            }

            public void onDeleteRequest(String prefkey)
            {
                prefkey = prefkey.substring(keyprefix.length());

                String script = "if (WebAppPrefBuilder.onDeleteRequest) "
                        + "WebAppPrefBuilder.onDeleteRequest"
                        + "(\"" + prefkey + "\");";

                webprefs.evaluateJavascript(script, null);
            }
        }
    }

    //endregion Webapps preferences
}
