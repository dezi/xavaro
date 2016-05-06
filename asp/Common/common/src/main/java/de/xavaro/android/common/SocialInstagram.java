package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.app.Application;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class SocialInstagram extends Social implements Social.SocialInterface
{
    private static final String LOGTAG = SocialInstagram.class.getSimpleName();
    private static SocialInstagram instance;

    public static void initialize(Application app)
    {
        if (instance != null) return;
        instance = new SocialInstagram();
    }

    public static SocialInstagram getInstance()
    {
        return instance;
    }

    public SocialInstagram()
    {
        super("instagram");

        appurl = "http://www.xavaro.de/instagram";
        appkey = "63afb3307ec24f4886230f44d2fda884";
        appsecret = "ab19aa46d88f4fb9a433f6d6cc1d4df3";

        oauthurl = "https://www.instagram.com/oauth/authorize/";
        tokenurl = "https://api.instagram.com/oauth/access_token";

        scopes = new String[]{ "basic", "public_content", "follower_list" };

        apiurl = "https://api.instagram.com/v1";
    }

    @Override
    public boolean hasFriends()
    {
        return true;
    }

    @Override
    public boolean hasLikes()
    {
        return false;
    }

    @Override
    protected String getScopeParameter()
    {
        return TextUtils.join("+", scopes);
    }

    @Override
    public void getTest()
    {
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
        return Json.getString(userdata, "full_name");
    }

    @Nullable
    public byte[] getUserIconData(String pfid)
    {
        if (pfid == null) return null;

        JSONObject userdata = getUserProfile();

        if ((userdata == null) || ! Simple.equals(pfid, Json.getString(userdata, "username")))
        {
            userdata = getGraphUserProfile(pfid);
        }

        String iconurl = Json.getString(userdata, "profile_picture");
        return SimpleRequest.readData(iconurl);
    }

    //endregion User profile

    //region Graph accessing methods

    @Nullable
    protected JSONObject getGraphUserProfile()
    {
        if (! isReady()) return null;

        JSONObject response = getGraphRequest("/users/self");
        return Json.getObject(response, "data");
    }

    @Nullable
    protected JSONObject getGraphUserProfile(String pfid)
    {
        JSONObject response = getGraphRequest("/users/" + pfid);
        return Json.getObject(response, "data");
    }

    @Override
    protected JSONArray getGraphUserFriendlist()
    {
        JSONObject response = getGraphRequest("/users/self/follows");
        return Json.getArray(response, "data");
    }

    @Override
    protected JSONArray getGraphUserLikeslist()
    {
        return null;
    }

    @Override
    protected JSONObject getGraphPost(String postid)
    {
        JSONObject response = getGraphRequest("/media/" + postid);
        return Json.getObject(response, "data");
    }

    @Override
    protected JSONArray getGraphFeed(String userid)
    {
        if (userid == null) return null;

        JSONObject response = getGraphRequest("/users/" + userid  + "/media/recent");
        return Json.getArray(response, "data");
    }

    //endregion Graph accessing methods
}
