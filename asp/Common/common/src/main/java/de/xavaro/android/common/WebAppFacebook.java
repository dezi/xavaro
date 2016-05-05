package de.xavaro.android.common;

import android.webkit.JavascriptInterface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

@SuppressWarnings("unused")
public class WebAppFacebook
{
    private static final String LOGTAG = WebAppFacebook.class.getSimpleName();

    private String plat;
    private String pfid;
    private String name;
    private String type;

    private final SocialFacebook facebook;
    private final SocialInstagram instagram;

    public WebAppFacebook()
    {
        facebook = SocialFacebook.getInstance();
        instagram = SocialInstagram.getInstance();
    }

    public void setTarget(String platform, String pfid, String name, String type)
    {
        this.plat = platform;
        this.pfid = pfid;
        this.name = name;
        this.type = type;
    }

    @JavascriptInterface
    public String getTargets()
    {
        JSONArray targets = new JSONArray();

        File icon = null;

        if (plat != null)
        {
            if (Simple.equals(plat, "facebook"))
            {
                icon = ProfileImages.getFacebookProfileImageFile(pfid);
            }

            if (Simple.equals(plat, "instagram"))
            {
                icon = ProfileImages.getFacebookProfileImageFile(pfid);
            }

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
            if (facebook.isReady())
            {
                JSONObject target = new JSONObject();

                String actuser = facebook.getUserId();
                String actname = facebook.getUserDisplayName();
                icon = ProfileImages.getFacebookProfileImageFile(actuser);

                Json.put(target, "id", actuser);
                Json.put(target, "name", actname);
                Json.put(target, "type", "owner");
                Json.put(target, "plat", "facebook" );
                Json.put(target, "icon", (icon != null) ? icon.toString() : null);

                Json.put(targets, target);
            }

            if (instagram.isReady())
            {
                JSONObject target = new JSONObject();

                String actuser = instagram.getUserId();
                String actname = instagram.getUserDisplayName();
                icon = ProfileImages.getInstagramProfileImageFile(actuser);

                Json.put(target, "id", actuser);
                Json.put(target, "name", actname);
                Json.put(target, "type", "owner");
                Json.put(target, "plat", "instagram" );
                Json.put(target, "icon", (icon != null) ? icon.toString() : null);

                Json.put(targets, target);
            }
        }

        return targets.toString();
    }

    @JavascriptInterface
    public String getUserFeeds()
    {
        JSONArray allfeeds = new JSONArray();

        if (facebook.isReady())
        {
            JSONArray feeds = facebook.getUserFeeds(true);
            Json.append(allfeeds, feeds);
        }

        if (instagram.isReady())
        {
            JSONArray feeds = instagram.getUserFeeds(true);
            Json.append(allfeeds, feeds);
        }

        return allfeeds.toString();
    }

    @JavascriptInterface
    public String getPost(String platform, String postid)
    {
        Log.d(LOGTAG, "getPost:" + platform + "=" + postid);

        JSONObject post = null;

        if (isFacebook(platform)) post = facebook.getPost(postid);
        if (isInstagram(platform)) post = instagram.getPost(postid);

        return (post == null) ? "{}" : post.toString();
    }

    @JavascriptInterface
    public String getFeed(String platform, String userid)
    {
        Log.d(LOGTAG, "getFeed:" + platform + "=" + userid);

        JSONArray feed = null;

        if (isFacebook(platform)) feed = facebook.getFeed(userid);
        if (isInstagram(platform)) feed = instagram.getFeed(userid);

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

        if (isFacebook(platform)) response = facebook.getGraphRequest(edge, jparams);
        if (isInstagram(platform)) response = instagram.getGraphRequest(edge, jparams);

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
        if ((platform == null) || isFacebook(platform)) facebook.setVerbose(yesno);
        if ((platform == null) || isInstagram(platform)) instagram.setVerbose(yesno);
    }

    private boolean isFacebook(String platform)
    {
        return Simple.equals(platform, "facebook");
    }

    private boolean isInstagram(String platform)
    {
        return Simple.equals(platform, "instagram");
    }
}
