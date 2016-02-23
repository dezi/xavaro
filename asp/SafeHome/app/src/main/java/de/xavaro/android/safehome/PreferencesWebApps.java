package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Simple;
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

        private String webappname;

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

            preferences.add(lp);
        }
    }

    //endregion Webapps preferences
}
