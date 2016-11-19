package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebApp;

public class LaunchGroupWebApps extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupWebApps.class.getSimpleName();

    public LaunchGroupWebApps(Context context)
    {
        super(context);
    }

    public static JSONArray getConfig()
    {
        //
        // Legacy remove.
        //

        Simple.removeSharedPref("webapps.mode.testing");

        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();
        JSONArray gdir = new JSONArray();
        JSONObject entry;

        //
        // "webapps.mode." + webappname
        // "webapps.mode.tvguide"
        //

        String prefix = "webapps.mode.";

        //
        // Legacy remove.
        //

        Simple.removeSharedPref(prefix + "instaface");

        Map<String, Object> webapps = Simple.getAllPreferences(prefix);

        for (String prefkey : webapps.keySet())
        {
            String webappname = prefkey.substring(prefix.length());
            String category = WebApp.getCategory(webappname);
            String iconurl = WebApp.getAppIconName(webappname);

            entry = new JSONObject();

            Json.put(entry, "type", "webapp");
            Json.put(entry, "subtype", webappname);
            Json.put(entry, "order", 400);
            Json.put(entry, "icon", "webapp|" + webappname);

            JSONArray intents = WebApp.getVoiceIntents(webappname);
            if (intents != null) Json.put(entry, "intents", intents);

            String mode = (String) webapps.get(prefkey);

            if (Simple.equals(mode, "home")) home.put(entry);

            if (Simple.equals(category, "games"))
            {
                if (Simple.equals(mode, "folder")) gdir.put(entry);
            }
            else
            {
                if (Simple.equals(mode, "folder")) adir.put(entry);
            }
        }

        if (adir.length() > 0)
        {
            entry = new JSONObject();

            Json.put(entry, "type", "webapp");
            Json.put(entry, "label", "Anwendungen");
            Json.put(entry, "iconres", GlobalConfigs.IconResWebApps);
            Json.put(entry, "order", 1050);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        if (gdir.length() > 0)
        {
            entry = new JSONObject();

            Json.put(entry, "type", "webapp");
            Json.put(entry, "label", "Spiele");
            Json.put(entry, "iconres", GlobalConfigs.IconResGames);
            Json.put(entry, "order", 1050);

            Json.put(entry, "launchitems", gdir);
            Json.put(home, entry);
        }

        return home;
    }
}
