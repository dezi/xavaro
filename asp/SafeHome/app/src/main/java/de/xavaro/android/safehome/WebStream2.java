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

public class WebStream2 extends LaunchGroup
{
    private static final String LOGTAG = WebStream2.class.getSimpleName();

    public WebStream2(Context context)
    {
        super(context);
    }

    public WebStream2(Context context, String webtype)
    {
        super(context);

        this.config = getConfig(context, webtype);
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

    private String getPrefPrefix(String type)
    {
        if (type.equals("webiptv")) return "iptelevision.channel";

        if (type.equals("webradio")) return "ipradio.channel";

        return "unknown";
    }

    private JSONObject getConfig(Context context, String type)
    {
        try
        {
            JSONObject config = loadConfig(context, type);

            JSONObject launchgroup = new JSONObject();
            JSONArray launchitems = new JSONArray();

            Iterator<String> keysIterator = config.keys();

            while (keysIterator.hasNext())
            {
                String website = keysIterator.next();

                JSONObject sender = config.getJSONObject(website);

                JSONArray channels = sender.getJSONArray("channels");

                for (int inx = 0; inx < channels.length(); inx++)
                {
                    JSONObject channel = channels.getJSONObject(inx);

                    String label = channel.getString("label");
                    String ptype = getPrefPrefix(type);
                    String key = ptype + "." + website + ":" + label.replace(" ", "_");

                    if (StaticUtils.getSharedPrefsBoolean(context, key))
                    {
                        launchitems.put(channel);
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
