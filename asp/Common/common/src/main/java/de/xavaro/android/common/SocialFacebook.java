package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class SocialFacebook extends Social implements Social.SocialInterface
{
    private static final String LOGTAG = SocialFacebook.class.getSimpleName();

    private static SocialFacebook instance;

    public static void initialize()
    {
        if (instance != null) return;
        instance = new SocialFacebook();
    }

    public static SocialFacebook getInstance()
    {
        return instance;
    }

    public SocialFacebook()
    {
        super("facebook");

        appurl = "http://www.xavaro.de/facebook";
        appkey = "610331582448824";
        appsecret = "9db8d13a21307cd99c5ba0d8dff0b6e2";

        oauthurl = "https://www.facebook.com/dialog/oauth";
        tokenurl = "https://graph.facebook.com/v2.3/oauth/access_token";

        scopes = new String[]{ "public_profile", "user_friends", "user_likes", "user_posts" };

        apiurl = "https://graph.facebook.com/v2.6";
    }

    @Override
    public boolean hasFriends()
    {
        return true;
    }

    @Override
    public boolean hasLikes()
    {
        return true;
    }

    @Override
    protected String getScopeParameter()
    {
        return TextUtils.join(",", scopes);
    }

    @Override
    public void getTest()
    {
        JSONObject response = getGraphRequest("/me");
        Log.d(LOGTAG, "==============>" + Json.toPretty(response));
    }

    //region User profile

    @Nullable
    public String getUserId()
    {
        JSONObject userdata = getUserProfile();
        return Json.getString(userdata, "id");
    }

    @Nullable
    public String getUserDisplayName()
    {
        JSONObject userdata = getUserProfile();
        return Json.getString(userdata, "name");
    }

    @Nullable
    public byte[] getUserIconData(String pfid)
    {
        if (pfid == null) return null;

        Bundle parameters = new Bundle();
        parameters.putBoolean("redirect", false);
        parameters.putString("type", "square");
        parameters.putInt("width", 400);
        parameters.putInt("height", 400);

        JSONObject json = getGraphRequest("/" + pfid + "/picture", parameters);

        JSONObject data = Json.getObject(json, "data");
        String iconurl = Json.getString(data, "url");

        Log.d(LOGTAG, "getUserIcon: pfid:" + pfid + "=" + iconurl);

        return SimpleRequest.readData(iconurl);
    }

    //endregion User profile

    @Override
    protected JSONObject getGraphUserProfile()
    {
        return getGraphRequest("/me");
    }

    @Override
    protected JSONArray getGraphUserFriendlist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/friends");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserFriendlists: data:" + data.toString());

        return data;
    }

    @Override
    protected JSONArray getGraphUserLikeslist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/likes");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserLikeslist: data:" + data.toString());

        return data;
    }

    @Override
    protected JSONObject getGraphPost(String postid)
    {
        String[] fields =
                {
                        "caption",
                        "created_time",
                        "description",
                        "event",
                        "expanded_height",
                        "expanded_width",
                        "from",
                        "full_picture",
                        "height",
                        "icon",
                        "link",
                        "message",
                        "message_tags",
                        "name",
                        "object_id",
                        "picture",
                        "place",
                        "properties",
                        "shares",
                        "source",
                        "status_type",
                        "story",
                        "story_tags",
                        "width",
                        "likes",
                        "comments",
                        "reactions",
                        "sharedposts",
                        "attachments"
                };

        Bundle params = new Bundle();
        params.putString("fields", TextUtils.join(",", fields));

        return getGraphRequest("/" + postid, params);
    }

    @Nullable
    protected JSONArray getGraphFeed(String userid)
    {
        if (userid == null) return null;

        String[] fields = { "object_attachment" };

        Bundle params = new Bundle();
        params.putString("fields", TextUtils.join(",", fields));

        JSONObject response = getGraphRequest("/" + userid + "/feed", params);
        return Json.getArray(response, "data");
    }
}
