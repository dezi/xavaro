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

    public LaunchGroupWebFrame(Context context, LaunchItem parent)
    {
        super(context);

        this.parent = parent;
    }

    public LaunchGroupWebFrame(Context context, LaunchItem parent, String webtype, String subtype)
    {
        super(context);

        this.parent = parent;
        this.config = getConfig(context, webtype, subtype);
    }

    //region Static methods.

    private static JSONObject globalConfig = new JSONObject();

    private static JSONObject loadConfig(Context context, String type)
    {
        JSONObject typeroot = new JSONObject();

        try
        {
            if (! globalConfig.has(type))
            {
                int resourceId = context.getResources().getIdentifier("default_" + type, "raw", context.getPackageName());

                JSONObject jot = StaticUtils.readRawTextResourceJSON(context, resourceId);

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

    public static JSONArray getConfig(String type)
    {
        return getConfig(type, null);
    }

    public static JSONArray getConfig(String type, String subtype)
    {
        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();

        String ptype = getPrefPrefix(type, subtype);

        JSONObject config = loadConfig(Simple.getAnyContext(), type);
        Iterator<String> keysIterator = config.keys();

        while (keysIterator.hasNext())
        {
            String website = keysIterator.next();

            JSONObject webitem = Json.getObject(config, website);
            if (webitem == null) continue;

            if (! Json.equals(webitem, "subtype", subtype))
            {
                continue;
            }

            if (! webitem.has("channels"))
            {
                String key = ptype + ".website." + website;

                if (! Simple.getSharedPrefBoolean(key)) continue;

                Json.put(webitem, "type", "webframe");
                Json.put(webitem, "name", website);
                adir.put(webitem);
            }
            else
            {
                JSONArray channels = Json.getArray(webitem, "channels");
                if (channels == null) continue;

                for (int inx = 0; inx < channels.length(); inx++)
                {
                    JSONObject channel = Json.clone(Json.getObject(channels, inx));

                    if (channel.has("audiourl")) Json.put(channel, "type", "audioplayer");
                    if (channel.has("videourl")) Json.put(channel, "type", "videoplayer");

                    String label = Json.getString(channel, "label");
                    if (label == null) continue;

                    String key = ptype + "." + website + ":" + label.replace(" ", "_");

                    if (! Simple.getSharedPrefBoolean(key)) continue;

                    Log.d(LOGTAG, "===========iptv:!!!" + key);

                    adir.put(channel);
                }
            }
        }

        if (adir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", type);
            Json.put(entry, "label", type);
            Json.put(entry, "order", 1050);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        return home;
    }

    @Nullable
    private static String getPrefPrefix(String type, String subtype)
    {
        if (type.equals("webiptv")) return "iptelevision.channel";

        if (type.equals("webradio")) return "ipradio.channel";

        if (type.equals("webconfig") && (subtype != null))
        {
            return type + "." + subtype;
        }

        return null;
    }

    //endregion Static methods.

    private JSONObject getConfig(Context context, String type, String subtype)
    {
        try
        {
            JSONObject config = loadConfig(context, type);

            JSONObject launchgroup = new JSONObject();
            JSONArray launchitems = new JSONArray();

            Iterator<String> keysIterator = config.keys();

            String key;
            String label;
            String ptype = getPrefPrefix(type,subtype);

            while (keysIterator.hasNext())
            {
                String website = keysIterator.next();

                JSONObject webitem = config.getJSONObject(website);

                if (webitem.has("subtype") && (subtype != null)
                        && ! webitem.getString("subtype").equals(subtype))
                {
                    continue;
                }

                if (! webitem.has("channels"))
                {
                    key = ptype + ".website." + website;

                    if (StaticUtils.getSharedPrefsBoolean(context, key))
                    {
                        webitem.put("type","webframe");
                        webitem.put("name",website);
                        launchitems.put(webitem);
                    }
                }
                else
                {
                    JSONArray channels = webitem.getJSONArray("channels");

                    for (int inx = 0; inx < channels.length(); inx++)
                    {
                        JSONObject channel = Json.clone(channels.getJSONObject(inx));

                        if (channel.has("audiourl")) Json.put(channel, "type", "audioplayer");
                        if (channel.has("videourl")) Json.put(channel, "type", "videoplayer");

                        label = Json.getString(channel, "label");
                        if (label == null) continue;

                        key = ptype + "." + website + ":" + label.replace(" ", "_");

                        if (StaticUtils.getSharedPrefsBoolean(context, key))
                        {
                            launchitems.put(channel);
                        }
                    }
                }
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

    //endregion
}
