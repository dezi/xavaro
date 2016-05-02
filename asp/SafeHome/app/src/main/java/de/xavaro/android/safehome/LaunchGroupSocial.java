package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VoiceIntent;

public class LaunchGroupSocial
{
    public static class FacebookGroup extends LaunchGroup
    {
        private static final String LOGTAG = FacebookGroup.class.getSimpleName();

        public FacebookGroup(Context context)
        {
            super(context);
        }

        @Nullable
        public static JSONArray getConfig()
        {
            if (! Simple.getSharedPrefBoolean("social.facebook.enable")) return null;

            JSONArray home = new JSONArray();
            JSONArray adir = new JSONArray();
            JSONArray cdir = new JSONArray();

            configPrefs("friend", home, adir, cdir);
            configPrefs("like", home, adir, cdir);

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "facebook");
                Json.put(entry, "label", "Facebook");
                Json.put(entry, "order", 550);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            if (cdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "contacts");
                Json.put(entry, "label", "Kontakte");
                Json.put(entry, "order", 950);

                Json.put(entry, "launchitems", cdir);
                Json.put(home, entry);
            }

            return home;
        }

        private static void configPrefs(String type, JSONArray home, JSONArray adir, JSONArray cdir)
        {
            String modeprefix = "social.facebook." + type + ".mode.";
            String nameprefix = "social.facebook." + type + ".name.";

            Map<String, Object> friends = Simple.getAllPreferences(modeprefix);

            for (Map.Entry<String, Object> item : friends.entrySet())
            {
                Object fmode = item.getValue();

                if ((fmode == null) || !(fmode instanceof String)) continue;

                String mode = (String) fmode;
                String fbid = item.getKey().substring(modeprefix.length());
                String name = Simple.getSharedPrefString(nameprefix + fbid);
                if (name == null) continue;

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", name);
                Json.put(entry, "type", "facebook");
                Json.put(entry, "subtype", type);
                Json.put(entry, "fbid", fbid);
                Json.put(entry, "order", 500);

                if (mode.contains("home")) Json.put(home, entry);
                if (mode.contains("home")) Json.put(adir, entry);
                if (mode.contains("folder")) Json.put(adir, entry);
                if (mode.contains("contacts")) Json.put(cdir, entry);

                Log.d(LOGTAG, "Prefe:" + item.getKey() + "=fbid=" + fbid + "=" + name);
            }
        }
    }
}