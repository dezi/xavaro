package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.WebLib;

//
// Utility namespace for app launch groups.
//

public class LaunchGroupApps
{
    private static final String LOGTAG = LaunchGroupApps.class.getSimpleName();

    //region Static methods.

    private static JSONObject globalConfig = new JSONObject();

    private static JSONObject loadConfig(Context context, String type)
    {
        JSONObject typeroot = new JSONObject();

        try
        {
            if (! globalConfig.has(type))
            {
                int resourceId = context.getResources().getIdentifier("default_apps", "raw", context.getPackageName());

                JSONObject jot = StaticUtils.readRawTextResourceJSON(context, resourceId);

                if ((jot == null) || !jot.has("apps"))
                {
                    Log.e(LOGTAG, "getConfig: Cannot read default apps");
                }
                else
                {
                    jot = Json.getObject(jot, "apps");
                }

                if ((jot == null) || !jot.has(type))
                {
                    Log.e(LOGTAG, "getConfig: Cannot read default " + type);
                }
                else
                {
                    globalConfig.put(type, jot.getJSONObject(type));
                }
            }

            typeroot = globalConfig.getJSONObject(type);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return typeroot;
    }

    //endregion Static methods.

    public static class DiscounterGroup extends LaunchGroup
    {
        private static final String LOGTAG = DiscounterGroup.class.getSimpleName();

        public DiscounterGroup(Context context)
        {
            super(context);
        }

        public static JSONArray getConfig()
        {
            JSONArray home = new JSONArray();
            JSONArray adir = new JSONArray();
            JSONObject entry;

            //
            // Get available items.
            //

            JSONObject config = WebLib.getLocaleConfig("appstore");
            config = Json.getObject(config, "discounter");
            if (config == null) return home;

            //
            // "apps.discounter.package:" + apkname = mode
            //

            String prefix = "apps.discounter.package:";

            Map<String, Object> natapps = Simple.getAllPreferences(prefix);

            for (String prefkey : natapps.keySet())
            {
                String apkname = prefkey.substring(prefix.length());

                JSONObject item = Json.getObject(config, apkname);
                String label = Json.getString(item, "label");
                if (label == null) continue;

                entry = new JSONObject();

                Json.put(entry, "type", "apps");
                Json.put(entry, "subtype", "discounter");
                Json.put(entry, "label", label);
                Json.put(entry, "apkname", apkname);
                Json.put(entry, "order", 400);

                String mode = (String) natapps.get(prefkey);

                if (Simple.equals(mode, "home")) home.put(entry);
                if (Simple.equals(mode, "folder")) adir.put(entry);
            }

            if (adir.length() > 0)
            {
                entry = new JSONObject();

                Json.put(entry, "type", "apps");
                Json.put(entry, "subtype", "discounter");
                Json.put(entry, "label", "Discounter");
                Json.put(entry, "order", 1050);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            return home;
        }
    }
}