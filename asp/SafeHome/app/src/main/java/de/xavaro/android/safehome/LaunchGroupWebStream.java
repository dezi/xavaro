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
// Web stream players base class.
//

public class LaunchGroupWebStream extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupWebStream.class.getSimpleName();

    public LaunchGroupWebStream(Context context)
    {
        super(context);
    }

    public LaunchGroupWebStream(Context context, LaunchItem parent)
    {
        super(context);

        this.parent = parent;
    }

    @Nullable
    public static JSONArray getConfig(String type)
    {
        if (! Simple.getSharedPrefBoolean(type + ".enable")) return null;

        JSONObject config = WebLib.getLocaleConfig(type);
        if (config == null) return null;

        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();

        Iterator<String> keysIterator = config.keys();

        while (keysIterator.hasNext())
        {
            String website = keysIterator.next();

            JSONObject webitem = Json.getObject(config, website);
            if (webitem == null) continue;

            JSONArray channels = Json.getArray(webitem, "channels");
            if (channels == null) continue;

            for (int inx = 0; inx < channels.length(); inx++)
            {
                JSONObject channel = Json.clone(Json.getObject(channels, inx));

                boolean developer = Json.getBoolean(channel, "develop");

                if (developer && ! Simple.getSharedPrefBoolean("developer.enable"))
                {
                    //
                    // Developer only enabled content.
                    //

                    continue;
                }

                if (channel.has("audiourl")) Json.put(channel, "type", "audioplayer");
                if (channel.has("videourl")) Json.put(channel, "type", "videoplayer");

                String label = Json.getString(channel, "label");
                if (label == null) continue;
                Json.put(channel, "name", website + "." + label);

                String key = type + ".channel:" + website + ":" + label;

                String mode = Simple.getSharedPrefString(key);

                if (Simple.equals(mode, "home")) home.put(channel);
                if (Simple.equals(mode, "folder")) adir.put(channel);
            }
        }

        if (adir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", type);
            Json.put(entry, "label", type.equals("iptv") ? "Fernsehen" : "Radio");
            Json.put(entry, "order", 1050);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        return home;
    }
}
