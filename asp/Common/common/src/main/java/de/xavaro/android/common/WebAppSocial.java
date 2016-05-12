package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.webkit.JavascriptInterface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class WebAppSocial
{
    private static final String LOGTAG = WebAppSocial.class.getSimpleName();

    private String plat;
    private String pfid;
    private String name;
    private String type;

    private final ArrayList<Social.SocialInterface> platforms = new ArrayList<>();

    public WebAppSocial()
    {
        platforms.add(SocialTwitter.getInstance());
        platforms.add(SocialFacebook.getInstance());
        platforms.add(SocialInstagram.getInstance());
        platforms.add(SocialGoogleplus.getInstance());
    }

    @Nullable
    public Social.SocialInterface getPlatform(String name)
    {
        for (Social.SocialInterface platform : platforms)
        {
            if (Simple.equals(name, platform.getPlatform()))
            {
                return platform;
            }
        }

        return null;
    }

    public void setPlatform(String platform, String pfid, String name, String type)
    {
        this.plat = platform;
        this.pfid = pfid;
        this.name = name;
        this.type = type;
    }

    @JavascriptInterface
    public String getPlatforms()
    {
        JSONArray targets = new JSONArray();

        if (plat != null)
        {
            File icon = ProfileImages.getSocialUserImageFile(plat, pfid);

            JSONObject target = new JSONObject();

            Json.put(target, "id", pfid);
            Json.put(target, "name", name);
            Json.put(target, "type", type);
            Json.put(target, "plat", plat );
            Json.put(target, "icon", (icon != null) ? icon.toString() : null);

            Json.put(targets, target);
        }
        else
        {
            for (Social.SocialInterface platform : platforms)
            {
                if (platform.isReady())
                {
                    JSONObject target = new JSONObject();

                    String actuser = platform.getUserId();
                    String actname = platform.getUserDisplayName();
                    File icon = ProfileImages.getSocialUserImageFile("facebook", actuser);

                    Json.put(target, "id", actuser);
                    Json.put(target, "name", actname);
                    Json.put(target, "type", "owner");
                    Json.put(target, "plat", platform.getPlatform());
                    Json.put(target, "icon", (icon != null) ? icon.toString() : null);

                    Json.put(targets, target);
                }
            }
        }

        return targets.toString();
    }

    @JavascriptInterface
    public String getUserIcon(String platform, String pfid)
    {
        File iconfile = ProfileImages.getSocialUserImageFile(platform, pfid);
        return (iconfile == null) ? "" : iconfile.toString();
    }

    @JavascriptInterface
    public String getUserFeeds()
    {
        JSONArray allfeeds = new JSONArray();

        for (Social.SocialInterface socialplatform : platforms)
        {
            if (socialplatform.isReady())
            {
                JSONArray feeds = socialplatform.getUserFeeds(true);
                Json.append(allfeeds, feeds);
            }
        }

        return allfeeds.toString();
    }

    @JavascriptInterface
    public String getUserFeeds(String platform)
    {
        JSONArray allfeeds = new JSONArray();

        for (Social.SocialInterface socialplatform : platforms)
        {
            if (socialplatform.isReady() && Simple.equals(platform, socialplatform.getPlatform()))
            {
                allfeeds = socialplatform.getUserFeeds(true);
            }
        }

        return (allfeeds == null) ? "[]" : allfeeds.toString();
    }

    @JavascriptInterface
    public boolean getPostSuitable(String platform, String post)
    {
        JSONObject jpost = Json.fromString(post);
        Social.SocialInterface socialplatform = getPlatform(platform);
        return (socialplatform != null) && socialplatform.getPostSuitable(jpost);
    }

    @JavascriptInterface
    public String getPost(String platform, String postid)
    {
        JSONObject post = null;

        Social.SocialInterface socialplatform = getPlatform(platform);
        if (socialplatform != null) post = socialplatform.getPost(postid);

        return (post == null) ? "{}" : post.toString();
    }

    @JavascriptInterface
    public String getFeed(String platform, String userid)
    {
        Log.d(LOGTAG, "getFeed:" + platform + "=" + userid);

        JSONArray feed = null;

        Social.SocialInterface socialplatform = getPlatform(platform);
        if (socialplatform != null) feed = socialplatform.getFeed(userid);

        return (feed == null) ? "[]" : feed.toString();
    }

    @JavascriptInterface
    public String getGraphSync(String platform, String edge)
    {
        return getGraphSync(platform, edge, null);
    }

    @JavascriptInterface
    public String getGraphSync(String platform, String edge, String params)
    {
        JSONObject jparams = (params != null) ? Json.fromString(params) : null;

        JSONObject response = null;

        Social.SocialInterface socialplatform = getPlatform(platform);
        if (socialplatform != null) response = socialplatform.getGraphRequest(edge, jparams);

        return (response == null) ? "{}" : response.toString();
    }

    @JavascriptInterface
    public void setVerbose(boolean yesno)
    {
        setVerbose(null, yesno);
    }

    @JavascriptInterface
    public void setVerbose(String platform, boolean yesno)
    {
        for (Social.SocialInterface socialplatform : platforms)
        {
            if ((platform == null) || (Simple.equals(platform, socialplatform.getPlatform())))
            {
                socialplatform.setVerbose(yesno);
            }
        }
    }
}
