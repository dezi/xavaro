package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

//
// Web stream receivers base class.
//

public class WebStream extends LaunchGroup
{
    private static final String LOGTAG = WebStream.class.getSimpleName();

    public WebStream(Context context)
    {
        super(context);
    }

    public WebStream(Context context, String webtype)
    {
        super(context);

        this.config = getConfig(context, webtype, null);
    }

    public WebStream(Context context, String webtype, String subtype)
    {
        super(context);

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

    private String getPrefPrefix(String type, String subtype)
    {
        if (type.equals("webiptv")) return "iptelevision.channel";

        if (type.equals("webradio")) return "ipradio.channel";

        if (type.equals("webconfig") && (subtype != null))
        {
            return type + "." + subtype;
        }

        return "unknown";
    }

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
                        JSONObject channel = channels.getJSONObject(inx);

                        label = channel.getString("label");
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
