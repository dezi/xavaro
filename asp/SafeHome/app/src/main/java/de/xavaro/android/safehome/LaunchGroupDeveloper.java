package de.xavaro.android.safehome;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchGroupDeveloper extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupDeveloper.class.getSimpleName();

    public LaunchGroupDeveloper(Context context)
    {
        super(context);
    }

    public static JSONArray getConfig()
    {
        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();
        JSONObject entry;

        entry = new JSONObject();

        Json.put(entry, "type", "developer");
        Json.put(entry, "subtype", "preferences");
        Json.put(entry, "label", "Preferences");
        Json.put(entry, "order", 4000);
        Json.put(adir, entry);

        entry = new JSONObject();

        Json.put(entry, "type", "developer");
        Json.put(entry, "subtype", "settings");
        Json.put(entry, "label", "Settings");
        Json.put(entry, "order", 4000);
        Json.put(adir, entry);

        entry = new JSONObject();

        Json.put(entry, "type", "developer");
        Json.put(entry, "subtype", "contacts");
        Json.put(entry, "label", "Contacts");
        Json.put(entry, "order", 4000);
        Json.put(adir, entry);

        if (adir.length() > 0)
        {
            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "label", "Developer");
            Json.put(entry, "order", 4000);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        return home;
    }
}
