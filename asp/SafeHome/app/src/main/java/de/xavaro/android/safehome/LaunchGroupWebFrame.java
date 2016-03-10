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

    //region Static methods.

    public static JSONArray getConfig(String type, String subtype)
    {
        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();
        JSONArray cdir = new JSONArray();
        if (subtype == null) return home;

        JSONObject config = WebLib.getLocaleConfig(type);
        if (config == null) return home;

        Iterator<String> keysIterator = config.keys();

        while (keysIterator.hasNext())
        {
            String website = keysIterator.next();

            JSONObject webitem = Json.getObject(config, website);
            if (webitem == null) continue;

            if (! Json.equals(webitem, "subtype", subtype)) continue;

            String key = type + "." + subtype + ".website:" + website;

            Json.put(webitem, "type", type);
            Json.put(webitem, "name", website);

            String mode = Simple.getSharedPrefString(key);

            if (Simple.equals(mode, "home")) home.put(webitem);
            if (Simple.equals(mode, "folder")) adir.put(webitem);
            if (Simple.equals(mode, "inetdir")) cdir.put(webitem);
        }

        if (adir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", type);
            Json.put(entry, "subtype", subtype);
            Json.put(entry, "label", Simple.getTransVal(R.array.pref_ioc_subtype_keys, subtype));
            Json.put(entry, "order", 1050);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        if (cdir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "type");
            Json.put(entry, "label", "Internet");
            Json.put(entry, "order", 1075);

            Json.put(entry, "launchitems", cdir);
            Json.put(home, entry);
        }

        return home;
    }

    //endregion
}
