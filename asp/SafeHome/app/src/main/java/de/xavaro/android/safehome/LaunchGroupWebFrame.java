package de.xavaro.android.safehome;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.WebLib;

//
// Web stream receivers base class.
//

public class LaunchGroupWebFrame extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupWebFrame.class.getSimpleName();

    public LaunchGroupWebFrame(Context context)
    {
        super(context);
    }

    public static JSONArray getConfig(String type, String subtype)
    {
        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();
        JSONArray cdir = new JSONArray();
        if (subtype == null) return home;

        JSONObject config = WebLib.getLocaleConfig(type, subtype);
        if (config == null) return home;

        Iterator<String> keysIterator = config.keys();

        while (keysIterator.hasNext())
        {
            String website = keysIterator.next();
            if (website.equals("intent") || website.equals("intents")) continue;

            JSONObject webitem = Json.getObject(config, website);
            if (webitem == null) continue;

            String key = type + "." + subtype + ".website:" + website;

            Json.put(webitem, "type", type);
            Json.put(webitem, "subtype", subtype);
            Json.put(webitem, "name", website);
            Json.put(webitem, "order", 500);

            String mode = Simple.getSharedPrefString(key);

            adir.put(webitem);

            if (Simple.equals(mode, "home")) home.put(webitem);
            if (Simple.equals(mode, "inetdir")) cdir.put(webitem);
        }

        if (adir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", type);
            Json.put(entry, "subtype", subtype);
            Json.put(entry, "label", Simple.getTransVal(R.array.pref_ioc_subtype_keys, subtype));
            Json.put(entry, "order", 550);

            if (config.has("intent")) Json.put(entry, "intent", Json.getObject(config, "intent"));
            if (config.has("intents")) Json.put(entry, "intents", Json.getArray(config, "intents"));

            if (Simple.equals(subtype, "newspaper"))
            {
                Json.put(entry, "iconres", GlobalConfigs.IconResWebConfigNewspaper);
            }

            if (Simple.equals(subtype, "magazine"))
            {
                Json.put(entry, "iconres", GlobalConfigs.IconResWebConfigMagazine);
            }

            if (Simple.equals(subtype, "pictorial"))
            {
                Json.put(entry, "iconres", GlobalConfigs.IconResWebConfigPictorial);
            }

            if (Simple.equals(subtype, "shopping"))
            {
                Json.put(entry, "iconres", GlobalConfigs.IconResWebConfigShopping);
            }

            if (Simple.equals(subtype, "erotics"))
            {
                Json.put(entry, "iconres", GlobalConfigs.IconResWebConfigErotics);
            }

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        if (cdir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", type);
            Json.put(entry, "subtype", "inetdir");
            Json.put(entry, "label", "Internet");
            Json.put(entry, "order", 575);

            Json.put(entry, "iconres", GlobalConfigs.IconResWebConfigInternet);

            Json.put(entry, "launchitems", cdir);
            Json.put(home, entry);
        }

        return home;
    }
}
