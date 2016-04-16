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
        JSONObject entry;

        //
        // "webapps.mode." + webappname
        // "webapps.mode.tvguide"
        //

        String prefix = "webapps.mode.";

        Map<String, Object> webapps = Simple.getAllPreferences(prefix);

        for (String prefkey : webapps.keySet())
        {
            String webappname = prefkey.substring(prefix.length());

            entry = new JSONObject();

            Json.put(entry, "type", "webapp");
            Json.put(entry, "subtype", webappname);
            Json.put(entry, "order", 400);

            JSONArray intents = WebApp.getVoiceIntents(webappname);
            if (intents != null) Json.put(entry, "intents", intents);

            String mode = (String) webapps.get(prefkey);

            if (Simple.equals(mode, "home")) home.put(entry);
            if (Simple.equals(mode, "folder")) adir.put(entry);
        }

        if (adir.length() > 0)
        {
            entry = new JSONObject();

            Json.put(entry, "type", "webapp");
            Json.put(entry, "label", "Anwendungen");
            Json.put(entry, "order", 1050);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        return home;
    }
}
