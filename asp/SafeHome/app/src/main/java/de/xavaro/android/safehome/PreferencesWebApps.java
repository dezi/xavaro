package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.json.JSONObject;

import java.util.Iterator;

import de.xavaro.android.common.CacheManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NicedPreferences;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

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

                //
                // http://192.168.2.101/webapps/tvguide/manifest.json
                //

                String httpserver = CommonConfigs.WebappsServerName;
                String httpport = "" + CommonConfigs.WebappsServerPort;

                if (Simple.getSharedPrefBoolean("developer.webapps.httpbypass"))
                {
                    httpserver = Simple.getSharedPrefString("developer.webapps.httpserver");
                    httpport = Simple.getSharedPrefString("developer.webapps.httpport");
                }

                String root = "http://" + httpserver + ":" + httpport
                        + "/webapps/" + webappname + "/";

                String manifestsrc = root + "manifest.json";
                String manifest = WebCache.getContent(manifestsrc);
                JSONObject jmanifest = Json.fromString(manifest);
                jmanifest = Json.getObject(jmanifest, "manifest");
                if (jmanifest == null) continue;

                Log.d(LOGTAG, "manifest:" + jmanifest.toString());

                String label = Json.getString(jmanifest, "label");
                String appiconpng = Json.getString(jmanifest, "appicon");
                String appiconsrc = root + appiconpng;

                Log.d(LOGTAG, "appiconsrc:" + appiconsrc);

                Drawable appicon = WebCache.getImage(appiconsrc);

                String[] keys =  Simple.getTransArray(R.array.pref_webapps_where_keys);
                String[] vals =  Simple.getTransArray(R.array.pref_webapps_where_vals);

                lp = new NicedPreferences.NiceListPreference(context);

                lp.setKey(keyprefix + ".appdef." + webappname + ".mode");
                lp.setIcon(appicon);
                lp.setEntries(vals);
                lp.setEntryValues(keys);
                lp.setDefaultValue("inact");
                lp.setTitle(label);
                lp.setEnabled(enabled);

                preferences.add(lp);
            }
        }
    }

    //endregion Webapps preferences
}
