package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

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

    //region Cache maintenance

    private long lastReconfigure;
    private long nextAction;

    public void commTick()
    {
        long now = Simple.nowAsTimeStamp();

        if (nextAction == 0)
        {
            //
            // Do not immediately start actions.
            //

            nextAction = now; // + 2 * 60 * 1000;

            return;
        }

        if ((now - lastReconfigure) > 24 * 3600 * 1000)
        {
            cachedir = new File(Simple.getExternalCacheDir(), platform);

            if (! cachedir.exists())
            {
                if (cachedir.mkdirs()) Log.d(LOGTAG, "commTick: created cache:" + cachedir);
            }

            lastReconfigure = now;
            nextAction = now;
        }

        if (now < nextAction) return;
        nextAction += 60 * 1000;

        final String feedpfid = getUserId();
        String feedname = getUserDisplayName();
        if (feedpfid == null) return;

        Log.d(LOGTAG, "commTick: feed:" + feedpfid + " => " + feedname);

        File feedfile = new File(cachedir, feedpfid + ".feed.json");
        JSONObject updates = Simple.getFileJSONObject(feedfile);
        String lastdate = Json.getString(updates, "last_activity_date");

        updates = getUpdates(lastdate);

        if (updates != null)
        {
            cacheFeedMatches(feedpfid, updates);

            Simple.putFileContent(feedfile, Json.toPretty(updates));
        }
    }

    protected void cacheFeedMatches(String feedpfid, JSONObject updates)
    {
        JSONArray matches = Json.getArray(updates, "matches");
        if (matches == null) return;

        for (int inx = 0; inx < matches.length(); inx++)
        {
            JSONObject umatch = Json.getObject(matches, inx);
            String matchid = Json.getString(umatch, "_id");
            if (matchid == null) continue;

            File matchfile = new File(cachedir, matchid + ".match.json");
            JSONObject omatch = Simple.getFileJSONObject(matchfile);
            if (omatch == null) omatch = umatch;

            Json.copy(omatch, "dead", umatch);
            Json.copy(omatch, "muted", umatch);
            Json.copy(omatch, "closed", umatch);
            Json.copy(omatch, "pending", umatch);

            Json.copy(omatch, "message_count", umatch);
            Json.copy(omatch, "last_activity_date", umatch);

            JSONArray umessages = Json.getArray(umatch, "messages");
            JSONArray omessages = Json.getArray(omatch, "messages");

            int newmessages = 0;

            if ((umessages != null) && (omessages != null))
            {
                for (int uinx = 0; uinx < umessages.length(); uinx++)
                {
                    JSONObject umessage = Json.getObject(umessages, uinx);
                    String umid = Json.getString(umessage, "_id");
                    if (umid == null) continue;

                    boolean duplicate = false;

                    for (int oinx = 0; oinx < omessages.length(); oinx++)
                    {
                        JSONObject omessage = Json.getObject(omessages, oinx);
                        String omid = Json.getString(omessage, "_id");
                        if (omid == null) continue;

                        if (Simple.equals(umid, omid))
                        {
                            duplicate = true;
                            break;
                        }
                    }

                    if (duplicate) continue;

                    String to = Json.getString(umessage, "to");
                    if (Simple.equals(to, feedpfid)) newmessages++;

                    omessages.put(umessage);
                }
            }

            Simple.putFileContent(matchfile, Json.toPretty(omatch));

            if (newmessages > 0)
            {
                SimpleStorage.addInt("socialfeednews", platform + ".count." + matchid, newmessages);
                SimpleStorage.put("socialfeednews", platform + ".stamp." + matchid, Simple.nowAsISO());

                NotificationService.doCallbacks(platform, matchid);
            }
        }
    }

    //endregion Cache maintenance
}
