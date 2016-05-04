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
            JSONArray home = new JSONArray();
            JSONArray cdir = new JSONArray();
            JSONArray fbdir = new JSONArray();
            JSONArray igdir = new JSONArray();

            if (Simple.getSharedPrefBoolean("social.facebook.enable"))
            {
                configPrefs("facebook", "friend", home, fbdir, cdir);
                configPrefs("facebook", "like", home, fbdir, cdir);
            }

            if (Simple.getSharedPrefBoolean("social.instagram.enable"))
            {
                configPrefs("instagram", "friend", home, igdir, cdir);
            }

            if (fbdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "facebook");
                Json.put(entry, "label", "Facebook");
                Json.put(entry, "order", 550);

                Json.put(entry, "launchitems", fbdir);
                Json.put(home, entry);
            }

            if (igdir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "type", "instagram");
                Json.put(entry, "label", "Instagram");
                Json.put(entry, "order", 550);

                Json.put(entry, "launchitems", igdir);
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

        private static void configPrefs(String platform, String type,
                                        JSONArray home, JSONArray adir, JSONArray cdir)
        {
            String modeprefix = "social." + platform + "." + type + ".mode.";
            String nameprefix = "social." + platform + "." + type + ".name.";

            Map<String, Object> friends = Simple.getAllPreferences(modeprefix);

            for (Map.Entry<String, Object> item : friends.entrySet())
            {
                Object fmode = item.getValue();

                if ((fmode == null) || !(fmode instanceof String)) continue;

                String mode = (String) fmode;
                String pfid = item.getKey().substring(modeprefix.length());
                String name = Simple.getSharedPrefString(nameprefix + pfid);
                if (name == null) continue;

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", name);
                Json.put(entry, "type", platform);
                Json.put(entry, "subtype", type);
                Json.put(entry, "pfid", pfid);
                Json.put(entry, "order", 500);

                if (mode.contains("home")) Json.put(home, entry);
                if (mode.contains("home")) Json.put(adir, entry);
                if (mode.contains("folder")) Json.put(adir, entry);
                if (mode.contains("contacts")) Json.put(cdir, entry);

                Log.d(LOGTAG, "Prefe:" + item.getKey() + "=id=" + pfid + "=" + name);
            }
        }
    }
}