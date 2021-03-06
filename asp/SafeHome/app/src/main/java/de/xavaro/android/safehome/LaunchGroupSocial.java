package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.SocialGoogleplus;
import de.xavaro.android.common.SocialInstagram;
import de.xavaro.android.common.SocialTwitter;
import de.xavaro.android.common.WebApp;

public class LaunchGroupSocial extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupSocial.class.getSimpleName();

    public LaunchGroupSocial(Context context)
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
        JSONArray gpdir = new JSONArray();
        JSONArray twdir = new JSONArray();

        if (Simple.getSharedPrefBoolean("social.facebook.enable"))
        {
            configPrefs("facebook", "owner", home, fbdir, cdir);
            configPrefs("facebook", "friend", home, fbdir, cdir);
            configPrefs("facebook", "like", home, fbdir, null);
        }

        if (Simple.getSharedPrefBoolean("social.instagram.enable"))
        {
            configPrefs("instagram", "owner", home, igdir, cdir);
            configPrefs("instagram", "friend", home, igdir, cdir);
            configPrefs("instagram", "like", home, igdir, null);
        }

        if (Simple.getSharedPrefBoolean("social.googleplus.enable"))
        {
            configPrefs("googleplus", "owner", home, gpdir, cdir);
            configPrefs("googleplus", "friend", home, gpdir, cdir);
            configPrefs("googleplus", "like", home, gpdir, null);
        }

        if (Simple.getSharedPrefBoolean("social.twitter.enable"))
        {
            configPrefs("twitter", "owner", home, twdir, cdir);
            configPrefs("twitter", "friend", home, twdir, cdir);
            configPrefs("twitter", "like", home, twdir, null);
        }

        if (Simple.getSharedPrefBoolean("social.tinder.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "tinder");
            Json.put(entry, "label", "Tinder");
            Json.put(entry, "order", 550);

            Json.put(home, entry);
        }

        if (twdir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "twitter");
            Json.put(entry, "label", "Twitter");
            Json.put(entry, "order", 550);

            Json.put(entry, "launchitems", twdir);
            Json.put(home, entry);
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

        if (gpdir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "googleplus");
            Json.put(entry, "label", "Google+");
            Json.put(entry, "order", 550);

            Json.put(entry, "launchitems", gpdir);
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
        if (type.equals("owner"))
        {
            String mode = Simple.getSharedPrefString("social." + platform + ".owner.mode");
            String name = null;
            String pfid = null;

            if (Simple.equals(platform, "twitter"))
            {
                pfid = SocialTwitter.getInstance().getUserId();
                name = SocialTwitter.getInstance().getUserDisplayName();
            }

            if (Simple.equals(platform, "facebook"))
            {
                pfid = SocialFacebook.getInstance().getUserId();
                name = SocialFacebook.getInstance().getUserDisplayName();
            }

            if (Simple.equals(platform, "instagram"))
            {
                pfid = SocialInstagram.getInstance().getUserId();
                name = SocialInstagram.getInstance().getUserDisplayName();
            }

            if (Simple.equals(platform, "googleplus"))
            {
                pfid = SocialGoogleplus.getInstance().getUserId();
                name = SocialGoogleplus.getInstance().getUserDisplayName();
            }

            if ((mode != null) && (pfid != null) && (name != null))
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "label", name);
                Json.put(entry, "type", platform);
                Json.put(entry, "subtype", type);
                Json.put(entry, "pfid", pfid);
                Json.put(entry, "order", 500);

                if (mode.contains("home")) Json.put(home, entry);
                if (mode.contains("home")) Json.put(adir, entry);
                if (mode.contains("folder")) Json.put(adir, entry);

                if ((cdir != null) && mode.contains("contacts")) Json.put(cdir, entry);
            }
        }

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
        }
    }
}