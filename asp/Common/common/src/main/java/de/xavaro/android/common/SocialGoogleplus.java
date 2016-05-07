package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.app.Application;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class SocialGoogleplus extends Social implements Social.SocialInterface
{
    private static final String LOGTAG = SocialGoogleplus.class.getSimpleName();
    private static SocialGoogleplus instance;

    public static void initialize()
    {
        if (instance != null) return;
        instance = new SocialGoogleplus();
    }

    public static SocialGoogleplus getInstance()
    {
        return instance;
    }

    public SocialGoogleplus()
    {
        super("googleplus");

        appurl = "http://www.xavaro.de/googleplus";
        appkey = "404416935707-b7o1okeho0c3s2oj9qipksprnn8bshoi.apps.googleusercontent.com";
        appsecret = "D65banXZZN55o3Cn1qNc3bSy";

        oauthurl = "https://accounts.google.com/o/oauth2/v2/auth";
        tokenurl = "https://www.googleapis.com/oauth2/v4/token";

        scopes = new String[]{ "userinfo.profile", "plus.login", "plus.me", "plus.circles.read" };

        apiurl = "https://www.googleapis.com/plus/v1";
        apiextraparam = "&prettyPrint=true";
    }

    @Override
    public boolean isLoggedIn()
    {
        return (Simple.getSharedPrefString(accesstokenpref) != null);
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
        String scopeval = "";

        for (String scope : scopes)
        {
            if (scopeval.length() > 0) scopeval += "%20";

            scopeval += "https://www.googleapis.com/auth/" + scope;
        }

        return scopeval;
    }

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
        return Json.getString(userdata, "displayName");
    }

    @Nullable
    public byte[] getUserIconData(String pfid)
    {
        if (pfid == null) return null;

        JSONObject userdata = getUserProfile();

        if ((userdata == null) || !Simple.equals(pfid, Json.getString(userdata, "id")))
        {
            userdata = getGraphUser(pfid);
        }

        JSONObject image = Json.getObject(userdata, "image");
        String iconurl = Json.getString(image, "url");
        if (iconurl != null) iconurl = iconurl.replace("?sz=50", "?sz=400");
        return SimpleRequest.readData(iconurl);
    }

    @Override
    public String getAccessToken()
    {
        String validdate = Simple.getSharedPrefString(expirationpref);
        if (validdate == null) return null;

        if (validdate.compareTo(Simple.nowAsISO()) < 0)
        {
            //
            // Token is expired.
            //

            String refreshToken = Simple.getSharedPrefString(refreshtokenpref);

            Log.d(LOGTAG, "getAccessToken: expired=" + validdate);
            Log.d(LOGTAG, "getAccessToken: refresh=" + refreshToken);

            if (refreshToken == null)
            {
                //
                // Recover from oauth file.
                //

                File socialdir = Simple.getMediaPath("social");
                File oauthfile = new File(socialdir, platform + ".oauth.json");

                JSONObject joauth = Simple.getFileJSONObject(oauthfile);
                Log.d(LOGTAG, "========================>" + Json.toPretty(joauth));

                refreshToken = Json.getString(joauth, "refresh_token");
                Simple.setSharedPrefString(refreshtokenpref, refreshToken);
            }

            JSONObject postdata = new JSONObject();

            Json.put(postdata, "client_id", appkey);
            Json.put(postdata, "client_secret", appsecret);
            Json.put(postdata, "refresh_token", refreshToken);
            Json.put(postdata, "grant_type", "refresh_token");

            String content = SimpleRequest.readContent(tokenurl, postdata);

            Log.d(LOGTAG, "=====>" + content);

            if (content != null)
            {
                JSONObject jcontent = Json.fromString(content);

                String accessToken = Json.getString(jcontent, "access_token");
                int expiseconds = Json.getInt(jcontent, "expires_in");
                long expiration = Simple.nowAsTimeStamp() + (expiseconds - 10) * 1000;

                Simple.setSharedPrefString(expirationpref, Simple.timeStampAsISO(expiration));
                Simple.setSharedPrefString(accesstokenpref, accessToken);
            }
        }

        return Simple.getSharedPrefString(accesstokenpref);
    }

    @Nullable
    public JSONObject getGraphUserProfile()
    {
        return isLoggedIn() ? getGraphRequest("/people/me") : null;
    }

    @Nullable
    public JSONObject getGraphUser(String pfid)
    {
        return isLoggedIn() ? getGraphRequest("/people/" + pfid) : null;
    }

    @Nullable
    protected JSONArray getGraphFeed(String userid)
    {
        JSONObject response = getGraphRequest("/people/" +  userid + "/activities/public");
        return Json.getArray(response, "items");
    }

    @Nullable
    protected JSONObject getGraphPost(String postid)
    {
        return getGraphRequest("/activities/" + postid);
    }

    @Override
    public void getTest()
    {
        JSONObject response = getGraphRequest("/people/" +  getUserId() + "/activities/public");

        Log.d(LOGTAG,"getTestDat:" + Json.toPretty(response));
    }

    @Override
    public JSONArray getGraphUserFriendlist()
    {
        return getGraphUserList("person");
    }

    @Override
    public JSONArray getGraphUserLikeslist()
    {
        return getGraphUserList("page");
    }

    @Nullable
    private JSONArray getGraphUserList(String type)
    {
        JSONObject response = getGraphRequest("/people/" +  getUserId() + "/people/visible");
        JSONArray items = Json.getArray(response, "items");
        if (items == null) return null;

        for (int inx = 0; inx < items.length(); inx++)
        {
            JSONObject item = Json.getObject(items, inx);
            String objectType = Json.getString(item, "objectType");
            if (Simple.equals(objectType, type)) continue;

            Json.remove(items, inx--);
        }

        return items;
    }
}
