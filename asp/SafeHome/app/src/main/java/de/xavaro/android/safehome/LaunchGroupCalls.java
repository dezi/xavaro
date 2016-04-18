package de.xavaro.android.safehome;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebLib;

//
// Web stream receivers base class.
//

public class LaunchGroupCalls extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupCalls.class.getSimpleName();

    public LaunchGroupCalls(Context context)
    {
        super(context);
    }

    @Nullable
    public static JSONArray getConfig(String type, String subtype)
    {
        if (subtype == null) return null;

        if (! Simple.getSharedPrefBoolean(type + "." + subtype + ".enable")) return null;

        JSONObject config = WebLib.getLocaleConfig(type, subtype);
        if (config == null) return null;

        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();

        Iterator<String> keysIterator = config.keys();

        String keyprefix = type + "." + subtype;

        while (keysIterator.hasNext())
        {
            String subitem = keysIterator.next();
            if (subitem.equals("intent") || subitem.equals("intents")) continue;

            JSONObject webitem = Json.getObject(config, subitem);
            if (webitem == null) continue;

            String key = keyprefix + ".subtype:" + subitem;

            Json.put(webitem, "type", type);
            Json.put(webitem, "subtype", subtype);
            Json.put(webitem, "subitem", subitem);
            Json.put(webitem, "order", 500);

            String nametag = Simple.getSharedPrefString(keyprefix + ".nametag:" + subitem);
            if ((nametag != null) && (nametag.length() > 0)) Json.put(webitem, "label", nametag);

            String phonenumber = Simple.getSharedPrefString(keyprefix + ".phonenumber:" + subitem);
            if (phonenumber != null) Json.put(webitem, "phonenumber", phonenumber);

            String mode = Simple.getSharedPrefString(key);

            if (Simple.equals(mode, "home")) home.put(webitem);
            if (! Simple.equals(mode, "inact")) adir.put(webitem);
        }

        if (adir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", type);
            Json.put(entry, "subtype", subtype);
            Json.put(entry, "label", Simple.getTransVal(R.array.pref_calls_subtype_keys, subtype));
            Json.put(entry, "order", 550);

            if (config.has("intent")) Json.put(entry, "intent", Json.getObject(config, "intent"));
            if (config.has("intents")) Json.put(entry, "intents", Json.getArray(config, "intents"));

            if (Simple.equals(subtype, "important"))
            {
                Json.put(entry, "iconres", GlobalConfigs.IconResCallImportant);
            }

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        return home;
    }
}
