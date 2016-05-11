package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class SocialInstagram extends Social implements Social.SocialInterface
{
    private static final String LOGTAG = SocialInstagram.class.getSimpleName();
    private static SocialInstagram instance;

    public static SocialInstagram getInstance()
    {
        if (instance == null) instance = new SocialInstagram();

        return instance;
    }

    public SocialInstagram()
    {
        super("instagram");

        appurl = "http://www.xavaro.de/instagram";
        appfix = "63afb3307ec24f4886230f44d2fda884";
        appfox = "aGc4O2hkPTRtPTFkPWNrO2gxOjFvM200amY4Zj1hbzE=";

        oauthurl = "https://www.instagram.com/oauth/authorize/";
        tokenurl = "https://api.instagram.com/oauth/access_token";

        scopes = new String[]{ "basic", "public_content", "follower_list" };

        apiurl = "https://api.instagram.com/v1";

        apifeedhasposts = true;
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
            //
            // Check for manually added friends and likes.
            //

            String ficonpreflike = "social.instagram.like.icon." + pfid;
            String iconlikeurl = Simple.getSharedPrefString(ficonpreflike);
            if (iconlikeurl != null) return SimpleRequest.readData(iconlikeurl);

            String ficonpreffriend = "social.instagram.friend.icon." + pfid;
            String iconfriendurl = Simple.getSharedPrefString(ficonpreffriend);
            if (iconfriendurl != null) return SimpleRequest.readData(iconfriendurl);

            userdata = getGraphUserProfile(pfid);
        }

        String iconurl = Json.getString(userdata, "profile_picture");
        return SimpleRequest.readData(iconurl);
    }

    //endregion User profile

    //region Searching

    public boolean canSearchUsers()
    {
        return true;
    }

    public JSONArray getSearchUsers(String query, int max)
    {
        //
        // Fuck da Instagram rate limitz.
        //

        String url = "https://www.instagram.com/web/search/topsearch/"
                + "?query=" + Simple.getUrlEncoded(query);

        String content = SimpleRequest.readContent(url);
        JSONObject response = Json.fromString(content);
        JSONArray users = Json.getArray(response, "users");
        if (users == null) return null;

        JSONArray result = new JSONArray();

        for (int inx = 0; inx < users.length(); inx++)
        {
            JSONObject user = Json.getObject(users, inx);
            user = Json.getObject(user, "user");
            if (user == null) continue;

            String pfid = Json.getString(user, "username");
            String name = Json.getString(user, "full_name");
            String icon = Json.getString(user, "profile_pic_url");
            String type = (Json.getInt(user, "follower_count") >= 10000) ? "like" : "friend";
            Boolean very = Json.getBoolean(user, "is_verified");

            //Log.d(LOGTAG, "getSearchUsers: " + Json.toPretty(user));

            if ((pfid == null) || (name == null) || (icon == null)) continue;

            JSONObject resultuser = new JSONObject();

            Json.put(resultuser, "pfid", pfid);
            Json.put(resultuser, "name", name);
            Json.put(resultuser, "icon", icon);
            Json.put(resultuser, "type", type);
            Json.put(resultuser, "very", very);

            Json.put(result, resultuser);

            if ((max > 0) && (result.length() >= max)) break;
        }

        return result;
    }

    //endregion Searching

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
        if (pfid == null) return null;

        if (! pfid.matches("[0-9]*"))
        {
            //
            // Access via public json url.
            //

            String url = "https://www.instagram.com/" + pfid + "/media";
            JSONObject response = Json.fromString(SimpleRequest.readContent(url));
            JSONArray items = Json.getArray(response, "items");
            JSONObject item = Json.getObject(items, 0);
            return Json.getObject(item, "user");
        }

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

        JSONObject response;

        if (! userid.matches("[0-9]*"))
        {
            //
            // Access via public json url.
            //

            String url = "https://www.instagram.com/" + userid + "/media";
            response = Json.fromString(SimpleRequest.readContent(url));
            JSONArray feeddata = Json.getArray(response, "items");

            //
            // Make sure all items are cached because they
            // cannot be accessed via api.
            //

            cacheFeedStories(userid, feeddata);

            return feeddata;
        }

        //
        // Access via api.
        //

        response = getGraphRequest("/users/" + userid + "/media/recent");
        return Json.getArray(response, "data");
    }

    //endregion Graph accessing methods
}
