package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONObject;

import java.util.Iterator;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.WebApp;

public class PreferencesWebApps
{
    //region Webapps preferences

    public static class WebappFragment extends SettingsFragments.EnablePreferenceFragment
    {
        private static final String LOGTAG = WebappFragment.class.getSimpleName();

        public static PreferenceActivity.Header getHeader()
        {
            PreferenceActivity.Header header;

            header = new PreferenceActivity.Header();
            header.title = "Webapps";
            header.iconRes = GlobalConfigs.IconResWebApps;
            header.fragment = WebappFragment.class.getName();

            return header;
        }

        public WebappFragment()
        {
            super();

            iconres = GlobalConfigs.IconResWebApps;
            keyprefix = "webapps";
            masterenable = "Webapps freischalten";
        }

        @Override
        public void registerAll(Context context)
        {
            super.registerAll(context);

            NicedPreferences.NiceCategoryPreference pc;
            NicedPreferences.NiceListPreference lp;

            pc = new NicedPreferences.NiceCategoryPreference(context);
            pc.setTitle("Webapps");

            preferences.add(pc);

            JSONObject webapps = StaticUtils.readRawTextResourceJSON(context, R.raw.default_webapps);
            if ((webapps == null) || ! webapps.has("webapps")) return;
            webapps = Json.getObject(webapps, "webapps");
            if (webapps == null) return;

            Iterator<String> keysIterator = webapps.keys();

            while (keysIterator.hasNext())
            {
                String webappname = keysIterator.next();

                String[] keys =  Simple.getTransArray(R.array.pref_webapps_where_keys);
                String[] vals =  Simple.getTransArray(R.array.pref_webapps_where_vals);

                lp = new NicedPreferences.NiceListPreference(context);

                lp.setKey(keyprefix + ".appdef.mode." + webappname);
                lp.setEntries(vals);
                lp.setEntryValues(keys);
                lp.setDefaultValue("inact");
                lp.setIcon(WebApp.getAppIcon(webappname));
                lp.setTitle(WebApp.getLabel(webappname));
                lp.setEnabled(enabled);

                preferences.add(lp);
            }
        }
    }

    //endregion Webapps preferences
}
