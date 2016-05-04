package de.xavaro.android.common;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

@SuppressWarnings("unused")
public class WebAppFacebook
{
    private static final String LOGTAG = WebAppFacebook.class.getSimpleName();

    private String plat;
    private String pfid;
    private String name;
    private String type;

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
            if (SocialFacebook.isLoggedIn() && SocialFacebook.isLoggedIn())
            {
                JSONObject target = new JSONObject();

                String actuser = SocialFacebook.getUserId();
                String actname = SocialFacebook.getUserDisplayName();
                icon = ProfileImages.getFacebookProfileImageFile(actuser);

                Json.put(target, "id", actuser);
                Json.put(target, "name", actname);
                Json.put(target, "type", "owner");
                Json.put(target, "plat", "facebook" );
                Json.put(target, "icon", (icon != null) ? icon.toString() : null);

                Json.put(targets, target);
            }

            if (SocialInstagram.isLoggedIn() && SocialInstagram.isLoggedIn())
            {
                JSONObject target = new JSONObject();

                String actuser = SocialInstagram.getUserId();
                String actname = SocialInstagram.getUserDisplayName();
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
        return SocialFacebook.getUserFeeds(true).toString();
    }

    @JavascriptInterface
    public String getPost(String postid)
    {
        Log.d(LOGTAG, "getPost:" + postid);

        JSONObject post = SocialFacebook.getPost(postid);
        return (post == null) ? "{}" : post.toString();
    }

    @JavascriptInterface
    public String getFeed(String userid)
    {
        Log.d(LOGTAG, "getFeed:" + userid);

        JSONArray feed = SocialFacebook.getFeed(userid);
        return (feed == null) ? "[]" : feed.toString();
    }

    @JavascriptInterface
    public String getGraphSync(String edge)
    {
        return getGraphSync(edge, null);
    }

    @JavascriptInterface
    public String getGraphSync(String edge, String params)
    {
        JSONObject jparams = (params != null) ? Json.fromString(params) : null;
        JSONObject response = SocialFacebook.getGraphRequest(edge, jparams);

        return (response == null) ? "{}" : response.toString();
    }

    @JavascriptInterface
    public void setVerbose(boolean yesno)
    {
        SocialFacebook.setVerbose(yesno);
        SocialInstagram.setVerbose(yesno);
    }
}
