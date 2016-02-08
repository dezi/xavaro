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

            this.config = getConfig(context);
        }

        private JSONObject getConfig(Context context)
        {
            try
            {
                JSONObject launchgroup = new JSONObject();
                JSONArray launchitems = new JSONArray();

                JSONObject config = loadConfig(context, "discounter");
                SharedPreferences sp = Simple.getSharedPrefs();
                Iterator<String> keysIterator = config.keys();

                while (keysIterator.hasNext())
                {
                    String apkname = keysIterator.next();
                    JSONObject apkjson = Json.getObject(config, apkname);
                    if (apkjson == null) continue;

                    String prefkey = "apps.discounter.apk." + apkname;

                    Boolean enable = sp.getBoolean(prefkey, false);

                    if (! enable) continue;

                    String label = Json.getString(apkjson, "what");

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("type", "apps");
                    whatsentry.put("subtype", "discounter");
                    whatsentry.put("label", label);
                    whatsentry.put("apk", apkname);

                    launchitems.put(whatsentry);

                    Log.d(LOGTAG, "Prefe:" + prefkey + "=" + apkname + "=" + label);
                }

                launchgroup.put("launchitems", launchitems);

                return launchgroup;
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }

            return new JSONObject();
        }
    }
}