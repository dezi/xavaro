package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class Social
{
    private static final String LOGTAG = Social.class.getSimpleName();

    protected static boolean verbose = true;

    public static void setVerbose(boolean yesno)
    {
        verbose = yesno;
    }

    protected static Bundle getParameters(JSONObject jparams)
    {
        Bundle bparams = new Bundle();

        if (jparams != null)
        {
            Iterator<String> keysIterator = jparams.keys();

            while (keysIterator.hasNext())
            {
                String key = keysIterator.next();
                Object val = Json.get(jparams, key);

                if (val instanceof Boolean)
                {
                    bparams.putBoolean(key, (Boolean) val);
                    continue;
                }

                if (val instanceof Integer)
                {
                    bparams.putInt(key, (Integer) val);
                    continue;
                }

                if (val instanceof Long)
                {
                    bparams.putLong(key, (Long) val);
                    continue;
                }

                if (val instanceof String)
                {
                    bparams.putString(key, (String) val);
                    continue;
                }

                if (val instanceof JSONArray)
                {
                    JSONArray array = (JSONArray) val;
                    String imploded = "";

                    for (int inx = 0; inx < array.length(); inx++)
                    {
                        Object jobj = Json.get(array, inx);
                        if (! (jobj instanceof String)) continue;

                        if (imploded.length() > 0) imploded += ",";
                        imploded  += (String) jobj;
                    }

                    bparams.putString(key, imploded);
                }
            }
        }

        return bparams;
    }

    protected static JSONArray getOwnerFeed(JSONArray data, String platform)
    {
        //
        // Add facebook account owner as a owner feed.
        //

        JSONObject owner = new JSONObject();

        String pfid = Simple.getSharedPrefString("social." + platform + ".pfid");
        String name = Simple.getSharedPrefString("social." + platform + ".name");

        if ((pfid != null) && (name != null))
        {
            Json.put(owner, "id", pfid);
            Json.put(owner, "name", name);
            Json.put(owner, "type", "owner");
            Json.put(owner, "plat", platform);

            File icon = ProfileImages.getFacebookProfileImageFile(pfid);
            if (icon != null) Json.put(owner, "icon", icon.toString());

            Json.put(data, owner);
        }

        return data;
    }

    protected static JSONArray getUserFeeds(
            JSONArray data, String platform, String type, boolean feedonly)
    {
        String modeprefix = "social." + platform + "." + type + ".mode.";
        String nameprefix = "social." + platform + "." + type + ".name.";

        Map<String, Object> friends = Simple.getAllPreferences(modeprefix);

        for (Map.Entry<String, Object> entry : friends.entrySet())
        {
            Object fmode = entry.getValue();
            if (! (fmode instanceof String)) continue;

            String mode = (String) fmode;
            if (feedonly && ! mode.contains("feed")) continue;
            if (mode.equals("inactive")) continue;

            String pfid = entry.getKey().substring(modeprefix.length());
            String name = Simple.getSharedPrefString(nameprefix + pfid);
            if (name == null) continue;

            JSONObject item = new JSONObject();

            Json.put(item, "id", pfid);
            Json.put(item, "name", name);
            Json.put(item, "type", type);
            Json.put(item, "plat", platform);

            File icon = null;

            if (Simple.equals(platform, "facebook"))
            {
                icon = ProfileImages.getFacebookProfileImageFile(pfid);
            }

            if (Simple.equals(platform, "instagram"))
            {
                icon = ProfileImages.getInstagramProfileImageFile(pfid);
            }

            if (icon != null) Json.put(item, "icon", icon.toString());

            Json.put(data, item);
        }

        return data;
    }
}
