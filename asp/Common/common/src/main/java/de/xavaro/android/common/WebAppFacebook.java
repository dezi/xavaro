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

    private String fbid;
    private String name;
    private String type;
    private String icon;

    public void setTarget(String fbid, String name, String type)
    {
        this.fbid = fbid;
        this.name = name;
        this.type = type;
    }

    @JavascriptInterface
    public String getTarget()
    {
        JSONObject target = new JSONObject();

        String actuser = (fbid != null) ? fbid : SocialFacebook.getUserId();
        File icon = ProfileImages.getFacebookProfileImageFile(actuser);

        Json.put(target, "id", actuser);
        Json.put(target, "name", (name != null) ? name : SocialFacebook.getUserDisplayName());
        Json.put(target, "type", (type != null) ? type : "owner");
        Json.put(target, "icon", (icon != null) ? icon.toString() : null);

        return target.toString();
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
        Bundle bparams = null;
        JSONObject jparams = (params != null) ? Json.fromString(params) : null;

        if (jparams != null)
        {
            bparams = new Bundle();

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

        JSONObject response = SocialFacebook.getGraphRequest(edge, bparams);

        return (response == null) ? "{}" : response.toString();
    }

    @JavascriptInterface
    public void setVerbose(boolean yesno)
    {
        SocialFacebook.setVerbose(yesno);
    }
}
