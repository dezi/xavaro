package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Facebook
{
    private static final String LOGTAG = Facebook.class.getSimpleName();

    private static final Collection<String> permissions = Arrays.asList
    (
        "public_profile",

        "user_friends",
        "user_likes",
        "user_posts"
    );

    private static final GraphRequest graphrequest = new GraphRequest();
    private static GraphResponse response;
    private static CallbackManager callbackManager;
    private static AppEventsLogger logger;
    private static boolean verbose = true;

    public static void initialize(Application app)
    {
        FacebookSdk.sdkInitialize(app.getApplicationContext());

        //AppEventsLogger.activateApp(app);

        AppEventsLogger.setFlushBehavior(AppEventsLogger.FlushBehavior.EXPLICIT_ONLY);

        logger = AppEventsLogger.newLogger(app);

        logEvent(Simple.getAppName());

        //
        // Initialize API calls.
        //

        graphrequest.setCallback(graphcallback);
        graphrequest.setAccessToken(AccessToken.getCurrentAccessToken());
        graphrequest.setHttpMethod(HttpMethod.GET);
    }

    public static void logEvent(String eventName)
    {
        if (logger != null)
        {
            logger.logEvent(eventName);
            logger.flush();
        }
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (Facebook.callbackManager != null)
        {
            Facebook.callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static final GraphRequest.Callback graphcallback = new GraphRequest.Callback()
    {
        @Override
        public void onCompleted(GraphResponse response)
        {
            if (verbose)
            {
                Log.d(LOGTAG, "graphcallback: onCompleted..." + response);
            }
            else
            {
                Log.d(LOGTAG, "graphcallback: onCompleted...");
            }

            Facebook.response = response;
        }
    };

    public static void login()
    {
        callbackManager = CallbackManager.Factory.create();

        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Log.d(LOGTAG, "LoginManager: FacebookCallback onSuccess");

                if (loginResult.getAccessToken() != null)
                {
                    Log.d(LOGTAG, "LoginManager: Access Token:" + loginResult.getAccessToken());
                }
            }

            @Override
            public void onCancel()
            {
                Log.d(LOGTAG, "LoginManager: FacebookCallback onCancel");
            }

            @Override
            public void onError(FacebookException ex)
            {
                Log.i(LOGTAG, "LoginManager: FacebookCallback onError");

                OopsService.log(LOGTAG, ex);
            }
        };

        LoginManager.getInstance().registerCallback(callbackManager, callback);
        LoginManager.getInstance().logInWithReadPermissions(Simple.getActContext(), permissions);
    }

    public static void logout()
    {
        LoginManager.getInstance().logOut();
    }

    public static boolean isLoggedIn()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null)
        {
            Log.d(LOGTAG, "isLoggedIn: permissions=" + token.getPermissions());
        }

        return (token != null) && (Profile.getCurrentProfile() != null);
    }

    public static void setVerbose(boolean yesno)
    {
        verbose = yesno;
    }

    @Nullable
    public static String getUserId()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return (token == null) ? null : token.getUserId();
    }

    @Nullable
    public static Set<String> getUserPermissions()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return token.getPermissions();
    }

    @Nullable
    public static String getUserTokenExpiration()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return (token == null) ? null : Simple.timeStampAsISO(token.getExpires().getTime());
    }

    @Nullable
    public static String getUserDisplayName()
    {
        Profile profile = Profile.getCurrentProfile();
        return (profile == null) ? null : profile.getName();
    }

    @Nullable
    public static JSONArray getUserFriendlist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/friends");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserFriendlists: data:" + data.toString());

        return data;
    }

    public static JSONArray getUserFeedFriends()
    {
        return getUserFeed("friend");
    }

    public static JSONArray getUserFeedLikes()
    {
        return getUserFeed("like");
    }

    public static JSONArray getUserFeed(String what)
    {
        JSONArray data = new JSONArray();

        String modeprefix = "social.facebook." + what + ".mode.";
        String nameprefix = "social.facebook." + what + ".name.";

        Map<String, Object> friends = Simple.getAllPreferences(modeprefix);

        for (Map.Entry<String, Object> entry : friends.entrySet())
        {
            Object fmode = entry.getValue();

            if ((! (fmode instanceof String)) || ! ((String) fmode).contains("feed")) continue;

            String fbid = entry.getKey().substring(modeprefix.length());
            String name = Simple.getSharedPrefString(nameprefix + fbid);
            if (name == null) continue;

            JSONObject item = new JSONObject();

            Json.put(item, "id", fbid);
            Json.put(item, "name", name);

            Json.put(data, item);
        }

        return data;
    }

    @Nullable
    public static JSONArray getUserLikeslist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/likes");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserLikeslist: data:" + data.toString());

        return data;
    }

    @Nullable
    public static byte[] getUserIconData(String fbid)
    {
        if (fbid == null) return null;

        Bundle parameters = new Bundle();
        parameters.putBoolean("redirect", false);
        parameters.putString("type", "square");
        parameters.putInt("width", 400);
        parameters.putInt("height", 400);

        JSONObject json = getGraphRequest("/" + fbid + "/picture", parameters);

        JSONObject data = Json.getObject(json, "data");
        String iconurl = Json.getString(data, "url");

        Log.d(LOGTAG, "getUserIcon: facebookid:" + fbid + "=" + iconurl);

        return SimpleRequest.readData(iconurl);
    }

    private static JSONObject getGraphRequest(String path)
    {
        return getGraphRequest(path, null);
    }

    public static JSONObject getGraphRequest(String path, Bundle parameters)
    {
        if (path == null) return null;

        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null) return null;

        if (parameters == null) parameters = new Bundle();

        synchronized (graphrequest)
        {
            graphrequest.setAccessToken(token);
            graphrequest.setGraphPath(path);
            graphrequest.setParameters(parameters);
            graphrequest.executeAndWait();

            return response.getJSONObject();
        }
    }

    public static void reconfigureFriendsAndLikes()
    {
        if (! isLoggedIn()) return;

        JSONArray friends = getUserFriendlist();

        if (friends != null)
        {
            Map<String, Object> oldfriends = Simple.getAllPreferences("social.facebook.friend.");

            String dfmode = Simple.getSharedPrefString("social.facebook.newfriends.default");
            if (dfmode == null) dfmode = "feed+folder";

            for (int inx = 0; inx < friends.length(); inx++)
            {
                JSONObject friend = Json.getObject(friends, inx);
                if (friend == null) continue;

                String fbid = Json.getString(friend, "id");
                String name = Json.getString(friend, "name");
                if ((fbid == null) || (name == null)) continue;

                String fnamepref = "social.facebook.friend.name." + fbid;
                String fmodepref = "social.facebook.friend.mode." + fbid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.getFacebookLoadProfileImage(fbid);

                if (oldfriends.containsKey(fnamepref)) oldfriends.remove(fnamepref);
                if (oldfriends.containsKey(fmodepref)) oldfriends.remove(fmodepref);
            }

            for (Map.Entry<String, ?> entry : oldfriends.entrySet())
            {
                Simple.removeSharedPref(entry.getKey());
            }
        }

        JSONArray likes = getUserLikeslist();

        if (likes != null)
        {
            Map<String, Object> oldlikes = Simple.getAllPreferences("social.facebook.like.");

            String dfmode = Simple.getSharedPrefString("social.facebook.newlikes.default");
            if (dfmode == null) dfmode = "folder";

            for (int inx = 0; inx < likes.length(); inx++)
            {
                JSONObject like = Json.getObject(likes, inx);
                if (like == null) continue;

                String fbid = Json.getString(like, "id");
                String name = Json.getString(like, "name");
                if ((fbid == null) || (name == null)) continue;

                String fnamepref = "social.facebook.like.name." + fbid;
                String fmodepref = "social.facebook.like.mode." + fbid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.getFacebookLoadProfileImage(fbid);

                if (oldlikes.containsKey(fnamepref)) oldlikes.remove(fnamepref);
                if (oldlikes.containsKey(fmodepref)) oldlikes.remove(fmodepref);
            }

            for (Map.Entry<String, ?> entry : oldlikes.entrySet())
            {
                Simple.removeSharedPref(entry.getKey());
            }
        }
    }
}
