package de.xavaro.android.safehome;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebLib;

//
// Utility namespace for app launch groups.
//

public class LaunchGroupApps extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupApps.class.getSimpleName();

    public LaunchGroupApps(Context context)
    {
        super(context);
    }

    public static JSONArray getConfig(String subtype)
    {
        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();
        JSONObject entry;

        //
        // Get available items.
        //

        JSONObject config = WebLib.getLocaleConfig("appstore");
        config = Json.getObject(config, subtype);
        if (config == null) return home;

        //
        // "apps.discounter.package:" + apkname = mode
        //

        String prefix = "apps." + subtype + ".package:";

        Map<String, Object> natapps = Simple.getAllPreferences(prefix);

        for (String prefkey : natapps.keySet())
        {
            String apkname = prefkey.substring(prefix.length());

            JSONObject item = Json.getObject(config, apkname);
            if (item == null) continue;

            entry = Json.clone(item);

            Json.put(entry, "type", "apps");
            Json.put(entry, "subtype", subtype);
            Json.put(entry, "apkname", apkname);
            Json.put(entry, "order", 400);

            String mode = (String) natapps.get(prefkey);

            if (Simple.equals(mode, "home")) home.put(entry);

            adir.put(entry);
        }

        if (adir.length() > 0)
        {
            entry = new JSONObject();

            Json.put(entry, "type", "apps");
            Json.put(entry, "subtype", subtype);
            Json.put(entry, "label", Simple.getTransVal(R.array.pref_apps_subtype_keys, subtype));
            Json.put(entry, "order", 1050);

            if (config.has("intent")) Json.put(entry, "intent", Json.getObject(config, "intent"));
            if (config.has("intents")) Json.put(entry, "intents", Json.getArray(config, "intents"));

            if (Simple.equals(subtype, "discounter"))
            {
                Json.put(entry, "iconres", GlobalConfigs.IconResAppsDiscounter);
            }

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        return home;
    }
}