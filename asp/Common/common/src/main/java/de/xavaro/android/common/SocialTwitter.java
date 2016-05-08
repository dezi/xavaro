package de.xavaro.android.common;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class SocialTwitter extends Social implements Social.SocialInterface
{
    private static final String LOGTAG = SocialTwitter.class.getSimpleName();
    private static SocialTwitter instance;

    public static void initialize()
    {
        if (instance != null) return;
        instance = new SocialTwitter();
    }

    public static SocialTwitter getInstance()
    {
        return instance;
    }

    public SocialTwitter()
    {
        super("twitter");

        appurl = "http://www.xavaro.de/twitter";
        appkey = "ECWy6Av3vqdx9yUzQE6euMuTJ";
        appsecret = "LN4kGTVrmqlw2pyUg11nJme8LoehJSGP2PkiRi6Ae7mPqBjDOw";

        oauthurl = " https://api.twitter.com/oauth/authorize";
        tokenurl = "https://api.twitter.com/oauth/access_token";

        apiurl = "https://api.twitter.com/1.1";

        apioauth10 = true;
        apisigned = true;
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
        return null;
    }

    @Override
    public void getTest()
    {
        JSONObject response = getGraphRequest("/account/verify_credentials.json");
        Log.d(LOGTAG, "====>" + Json.toPretty(response));
    }

    //region User profile

    @Nullable
    public String getUserId()
    {
        JSONObject userdata = getUserProfile();
        return Json.getString(userdata, "id_str");
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

        JSONObject userdata = getUserProfile();

        if ((userdata == null) || ! Simple.equals(pfid, Json.getString(userdata, "id_str")))
        {
            userdata = getGraphUserProfile(pfid);
        }

        String iconurl = Json.getString(userdata, "profile_image_url");
        return SimpleRequest.readData(iconurl);
    }

    //endregion User profile

    //region Graph accessing methods

    @Nullable
    protected JSONObject getGraphUserProfile()
    {
        if (!isReady()) return null;

        return getGraphRequest("/account/verify_credentials.json");
    }

    @Nullable
    protected JSONObject getGraphUserProfile(String pfid)
    {
        Bundle params = new Bundle();

        params.putString("user_id", pfid);

        return getGraphRequest("/users/show.json", params);
    }

    @Override
    protected JSONArray getGraphUserFriendlist()
    {
        Bundle params = new Bundle();

        params.putString("user_id", getUserId());
        params.putString("skip_status", "true");

        JSONObject response = getGraphRequest("/friends/list.json", params);
        JSONArray users = Json.getArray(response, "users");

        if (users != null)
        {
            for (int inx = 0; inx < users.length(); inx++)
            {
                JSONObject user = Json.getObject(users, inx);
                if (user == null) continue;

                int followers = Json.getInt(user, "followers_count");
                if (followers >= 10000) Json.remove(users, inx--);
            }
        }

        return users;
    }

    @Override
    protected JSONArray getGraphUserLikeslist()
    {
        Bundle params = new Bundle();

        params.putString("user_id", getUserId());
        params.putString("skip_status", "true");

        JSONObject response = getGraphRequest("/friends/list.json", params);
        JSONArray users = Json.getArray(response, "users");

        if (users != null)
        {
            for (int inx = 0; inx < users.length(); inx++)
            {
                JSONObject user = Json.getObject(users, inx);
                if (user == null) continue;

                int followers = Json.getInt(user, "followers_count");
                if (followers < 10000) Json.remove(users, inx--);
            }
        }

        return users;
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
