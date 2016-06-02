package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class SocialTinder extends Social implements Social.SocialInterface
{
    private static final String LOGTAG = SocialTinder.class.getSimpleName();
    private static SocialTinder instance;

    public static SocialTinder getInstance()
    {
        if (instance == null) instance = new SocialTinder();

        return instance;
    }

    private String agent = "Tinder/4.8.2 (iPhone; iOS 9.2; Scale/2.00)";

    public SocialTinder()
    {
        super("tinder", "Tinder");

        appurl = "https://www.facebook.com/connect/login_success.html";
        appfix = "464891386855067";

        //
        // https://www.facebook.com/dialog/oauth
        //      ?client_id=464891386855067
        //      &redirect_uri=https://www.facebook.com/connect/login_success.html
        //      &scope=basic_info,email,public_profile,user_about_me,user_activities,
        //             user_birthday,user_education_history,user_friends,user_interests,
        //             user_likes,user_location,user_photos,user_relationship_details,
        //             user_work_history
        //      &response_type=token
        //

        oauthurl = "https://www.facebook.com/dialog/oauth";
        tokenurl = "https://api.gotinder.com/auth";

        scopes = new String[]{ "basic_info", "email,public_profile", "user_about_me",
                "user_activities", "user_birthday", "user_education_history", "user_friends",
                "user_interests", "user_likes", "user_location", "user_photos",
                "user_relationship_details", "user_work_history" };

        apiurl = "https://api.gotinder.com";

        apifeedhasposts = true;

        windowSecs = 60 * 60;
    }

    @Override
    protected void exchangeCodeforToken(String url)
    {
        //
        // Final oauth step. The user has accepted access or not
        // and now we try to exchange our code from authorization
        // against a valid access token for Tinder.
        //

        Log.d(LOGTAG, "exchangeCodeforToken: url=" + url);

        url = url.replace("#", "?");
        Uri uri = Uri.parse(url);

        String access_token = uri.getQueryParameter("access_token");

        if (access_token != null)
        {
            Log.d(LOGTAG, "exchangeCodeforToken: access_token=" + access_token);

            JSONObject postdata = new JSONObject();

            Json.put(postdata, "facebook_token", access_token);

            String content = SimpleRequest.readContent(tokenurl, postdata, true);

            Log.d(LOGTAG, "=====>" + content);

            if (content != null)
            {
                JSONObject jcontent = Json.fromString(content);

                String accessToken = Json.getString(jcontent, "token");
                Simple.setSharedPrefString(accesstokenpref, accessToken);

                File socialdir = Simple.getMediaPath("social");
                File userfile = new File(socialdir, platform + ".user.json");
                Simple.putFileContent(userfile, Json.toPretty(jcontent));
            }
        }
    }

    @Override
    public boolean hasFriends()
    {
        return false;
    }

    @Override
    public boolean hasLikes()
    {
        return false;
    }

    @Override
    protected String getScopeParameter()
    {
        return TextUtils.join(",", scopes);
    }

    @Override
    public void getTest()
    {
    }

    public boolean getPostSuitable(JSONObject post)
    {
        return false;
    }

    //region User profile

    @Nullable
    public String getUserId()
    {
        JSONObject userdata = getUserProfile();
        userdata = Json.getObject(userdata, "user");
        return Json.getString(userdata, "_id");
    }

    @Nullable
    public String getUserDisplayName()
    {
        JSONObject userdata = getUserProfile();
        userdata = Json.getObject(userdata, "user");
        return Json.getString(userdata, "full_name");
    }

    @Nullable
    public byte[] getUserIconData(String pfid)
    {
        JSONObject userdata = getUserProfile();
        userdata = Json.getObject(userdata, "user");
        JSONArray photos = Json.getArray(userdata, "photos");
        JSONObject image = Json.getObject(photos, 0);
        String iconurl = Json.getString(image, "url");

        return SimpleRequest.readData(iconurl);
    }

    //endregion User profile

    //region Graph accessing methods

    @Nullable
    protected JSONObject getGraphUserProfile()
    {
        return null;
    }

    @Nullable
    protected JSONObject getGraphUserProfile(String pfid)
    {
        return null;
    }

    @Override
    protected JSONArray getGraphUserFriendlist()
    {
        return null;
    }

    @Override
    protected JSONArray getGraphUserLikeslist()
    {
        return null;
    }

    @Override
    protected JSONObject getGraphPost(String postid)
    {
        return null;
    }

    @Override
    protected JSONArray getGraphFeed(String userid)
    {
        return null;
    }

    @Nullable
    public JSONObject getRecommendations()
    {
        //
        // curl https://api.gotinder.com/user/recs
        //  -H 'X-Auth-Token: a9c59454-546b-47b5-854b-ad022b755f14'
        //  -H 'User-Agent: Tinder/4.8.2 (iPhone; iOS 9.2; Scale/2.00)'
        //  -H 'Content-Type: application/json'
        //

        String url = apiurl + "/user/recs";
        String response = SimpleRequest.readContent(url, getAccessToken(), agent, null, false);
        JSONObject jresponse = Json.fromStringObject(response);

        if ((jresponse == null) || ! jresponse.has("results"))
        {
            Log.d(LOGTAG, "getRecommendations:" + response);
        }

        return jresponse;
    }

    @Nullable
    public boolean getLikePass(String what, String userid)
    {
        //
        // curl https://api.gotinder.com/user/recs
        //  -H 'X-Auth-Token: a9c59454-546b-47b5-854b-ad022b755f14'
        //  -H 'User-Agent: Tinder/4.8.2 (iPhone; iOS 9.2; Scale/2.00)'
        //  -H 'Content-Type: application/json'
        //

        String url = apiurl + "/" + what + "/" + userid;
        String response = SimpleRequest.readContent(url, getAccessToken(), agent, null, false);
        JSONObject jresponse = Json.fromStringObject(response);

        if ((jresponse == null) || ! jresponse.has("match"))
        {
            Log.d(LOGTAG, "getLikePass:" + response);
        }

        return Json.getBoolean(jresponse, "match");
    }

    @Nullable
    public JSONObject getUpdates(String date)
    {
        //
        // curl https://api.gotinder.com/updates
        //  -H 'X-Auth-Token: a9c59454-546b-47b5-854b-ad022b755f14'
        //  -H 'User-Agent: Tinder/4.8.2 (iPhone; iOS 9.2; Scale/2.00)'
        //  -H 'Content-Type: application/json'
        //  --data '{"last_activity_date": "2014-04-10T10:17:54.379Z"}'
        //

        if (date == null) date = Simple.timeStampAsISO(0);

        JSONObject data = new JSONObject();
        Json.put(data, "last_activity_date", date);

        String url = apiurl + "/updates";
        String response = SimpleRequest.readContent(url, getAccessToken(), agent, data, true);
        JSONObject jresponse = Json.fromStringObject(response);

        if ((jresponse == null) || ! jresponse.has("matches"))
        {
            Log.d(LOGTAG, "getUpdates:" + response);
        }

        return jresponse;
    }

    //endregion Graph accessing methods
}
