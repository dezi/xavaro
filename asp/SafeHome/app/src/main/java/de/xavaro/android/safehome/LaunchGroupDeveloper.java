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

        if (Simple.getSharedPrefBoolean("developer.enable"))
        {
            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "preferences");
            Json.put(entry, "label", "Preferences");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.preferences", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.preferences", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "settings");
            Json.put(entry, "label", "Settings");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.settings", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.settings", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "identities");
            Json.put(entry, "label", "Identities");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.identities", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.identities", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "contacts");
            Json.put(entry, "label", "Contacts");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.contacts", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.contacts", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "rcontacts");
            Json.put(entry, "label", "Remote Contacts");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.rcontacts", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.rcontacts", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "rgroups");
            Json.put(entry, "label", "Remote Groups");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.rgroups", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.rgroups", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "sdcard");
            Json.put(entry, "label", "SD-Card");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.sdcard", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.sdcard", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "cache");
            Json.put(entry, "label", "Cache");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.cache", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.cache", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "known");
            Json.put(entry, "label", "Known");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.known", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.known", "folder")) adir.put(entry);

            entry = new JSONObject();

            Json.put(entry, "type", "developer");
            Json.put(entry, "subtype", "webappcache");
            Json.put(entry, "label", "Webapp Cache");
            Json.put(entry, "order", 4000);

            if (Simple.sharedPrefEquals("developer.browser.webappcache", "home")) home.put(entry);
            if (Simple.sharedPrefEquals("developer.browser.webappcache", "folder")) adir.put(entry);
        }

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
