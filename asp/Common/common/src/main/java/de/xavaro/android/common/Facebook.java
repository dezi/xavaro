package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
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
    private static String locale;

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

        cachedir = new File(Simple.getExternalCacheDir(), "facebook");
        locale = Simple.getLocaleLanguage() + "_" + Simple.getLocaleCountry();
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
            String edge = response.getRequest().getGraphPath();
            String mess = (response.getJSONObject() == null) ? "failed" : "success";

            if (verbose)
            {
                Log.d(LOGTAG, "graphcallback: onCompleted:" + mess + "=" + edge);
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

    public static JSONArray getUserFeeds(boolean feedonly)
    {
        JSONArray data = new JSONArray();

        //
        // Add facebook account owner as a owner feed.
        //

        JSONObject owner = new JSONObject();

        String fbid = Simple.getSharedPrefString("social.facebook.fbid");
        String name = Simple.getSharedPrefString("social.facebook.name");

        if ((fbid != null) && (name != null))
        {
            Json.put(owner, "id", fbid);
            Json.put(owner, "name", name);
            Json.put(owner, "type", "owner");

            File icon = ProfileImages.getFacebookProfileImageFile(fbid);
            if (icon != null) Json.put(owner, "icon", icon.toString());

            Json.put(data, owner);
        }

        getUserFeeds(data, "friend", feedonly);
        getUserFeeds(data, "like", feedonly);

        return data;
    }

    private static JSONArray getUserFeeds(JSONArray data, String type, boolean feedonly)
    {
        String modeprefix = "social.facebook." + type + ".mode.";
        String nameprefix = "social.facebook." + type + ".name.";

        Map<String, Object> friends = Simple.getAllPreferences(modeprefix);

        for (Map.Entry<String, Object> entry : friends.entrySet())
        {
            Object fmode = entry.getValue();
            if (! (fmode instanceof String)) continue;

            String mode = (String) fmode;
            if (feedonly && ! mode.contains("feed")) continue;
            if (mode.equals("inactive")) continue;

            String fbid = entry.getKey().substring(modeprefix.length());
            String name = Simple.getSharedPrefString(nameprefix + fbid);
            if (name == null) continue;

            JSONObject item = new JSONObject();

            Json.put(item, "id", fbid);
            Json.put(item, "name", name);
            Json.put(item, "type", type);

            File icon = ProfileImages.getFacebookProfileImageFile(fbid);
            if (icon != null) Json.put(item, "icon", icon.toString());

            Json.put(data, item);
        }

        return data;
    }

    @Nullable
    public static JSONArray getUserFriendlist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/friends");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserFriendlists: data:" + data.toString());

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

    @Nullable
    public static JSONObject getPost(String postid)
    {
        if (postid == null) return null;

        File postfile = new File(cachedir, postid + ".post.json");

        if (postfile.exists())
        {
            return Json.fromString(Simple.getFileContent(postfile));
        }

        return getGraphPost(postid);
    }

    @Nullable
    public static JSONArray getFeed(String userid)
    {
        if (userid == null) return null;

        File feedfile = new File(cachedir, userid + ".feed.json");

        if (feedfile.exists())
        {
            return Json.fromStringArray(Simple.getFileContent(feedfile));
        }

        JSONObject response = getGraphRequest(userid + "/feed");
        return Json.getArray(response, "data");
    }

    private static JSONObject getGraphPost(String postid)
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

        return getGraphRequest(postid, params);
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
        parameters.putString("locale", locale);

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

    //region Cache maintenance

    private static long totalInterval = 60;
    private static long lastReconfigure;
    private static long nextInterval;
    private static long nextAction;
    private static File cachedir;

    private static JSONArray feedList;

    public static void commTick()
    {
        long now = Simple.nowAsTimeStamp();

        if ((now - lastReconfigure) > 24 * 3600 * 1000)
        {
            cachedir = new File(Simple.getExternalCacheDir(), "facebook");

            if (! cachedir.exists())
            {
                if (cachedir.mkdirs()) Log.d(LOGTAG, "commTick: created cache:" + cachedir);
            }

            Log.d(LOGTAG, "commTick: reconfigureFriendsAndLikes");

            reconfigureFriendsAndLikes();
            lastReconfigure = now;
            nextAction = now;

            return;
        }

        if (now < nextAction) return;

        if ((feedList == null) || feedList.length() == 0)
        {
            feedList = getUserFeeds(false);

            if (feedList.length() == 0)
            {
                nextAction = now + (totalInterval * 1000);
            }
            else
            {
                nextInterval = (totalInterval * 1000) / feedList.length();
                nextAction = now;
            }

            return;
        }

        nextAction += nextInterval;

        //
        // Load one feed.
        //

        JSONObject feed = Json.getObject(feedList, 0);
        Json.remove(feedList, 0);
        if (feed == null) return;

        final String feedfbid = Json.getString(feed, "id");
        final String feedname = Json.getString(feed, "name");

        Log.d(LOGTAG, "commTick: feed:" + feedfbid + " => " + feedname);

        JSONObject response = getGraphRequest(feedfbid + "/feed");
        JSONArray feeddata = Json.getArray(response, "data");
        if (feeddata == null) return;

        File feedfile = new File(cachedir, feedfbid + ".feed.json");
        Simple.putFileContent(feedfile, Json.toPretty(feeddata));

        //
        // Check feed stories.
        //

        FilenameFilter postsfilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                return filename.startsWith(feedfbid + "_") && filename.endsWith(".post.json");
            }
        };

        if ((! cachedir.exists()) && cachedir.mkdirs()) Log.d(LOGTAG, "commtick: created cache");
        ArrayList<String> postfiles = Simple.getDirectoryAsList(cachedir, postsfilter);

        for (int inx = 0; inx < feeddata.length(); inx++)
        {
            JSONObject post = Json.getObject(feeddata, inx);
            String postid = Json.getString(post, "id");
            if (postid == null) continue;

            String postname = postid + ".post.json";
            File postfile = new File(cachedir, postname);

            if (postfiles.contains(postname))
            {
                postfiles.remove(postname);
                continue;
            }

            JSONObject postdata = getGraphPost(postid);
            if (postdata == null) continue;

            Simple.putFileContent(postfile, Json.toPretty(postdata));
        }

        //
        // Remove outdated posts.
        //

        while (postfiles.size() > 0)
        {
            File obsolete = new File(cachedir, postfiles.remove(0));
            if (obsolete.delete()) Log.d(LOGTAG, "commTick: deleted:" + obsolete);
        }
    }

    //endregion Cache maintenance
}
