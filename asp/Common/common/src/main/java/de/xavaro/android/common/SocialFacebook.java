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

public class SocialFacebook extends Social
{
    private static final String LOGTAG = SocialFacebook.class.getSimpleName();

    private static SocialFacebook instance;
    private static AppEventsLogger logger;

    private static final Collection<String> permissions = Arrays.asList
            ( "public_profile", "user_friends", "user_likes", "user_posts" );

    private GraphRequest graphrequest;
    private GraphResponse response;
    private CallbackManager callbackManager;

    public SocialFacebook()
    {
        super("facebook");

        graphrequest = new GraphRequest();
        graphrequest.setCallback(graphcallback);
        graphrequest.setAccessToken(AccessToken.getCurrentAccessToken());
        graphrequest.setHttpMethod(HttpMethod.GET);
    }

    public static void initialize(Application app)
    {
        if (instance != null) return;

        FacebookSdk.sdkInitialize(app.getApplicationContext());
        AppEventsLogger.setFlushBehavior(AppEventsLogger.FlushBehavior.EXPLICIT_ONLY);

        logger = AppEventsLogger.newLogger(app);
        instance = new SocialFacebook();

        logEvent(Simple.getAppName());
    }

    public static SocialFacebook getInstance()
    {
        return instance;
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
        if (instance.callbackManager != null)
        {
            SocialFacebook.instance.callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private final GraphRequest.Callback graphcallback = new GraphRequest.Callback()
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

            SocialFacebook.this.response = response;
        }
    };

    public void login()
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

    public void logout()
    {
        LoginManager.getInstance().logOut();
    }

    public boolean isEnabled()
    {
        return Simple.getSharedPrefBoolean("social.facebook.enable");
    }

    public boolean isLoggedIn()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null)
        {
            Log.d(LOGTAG, "isLoggedIn: permissions=" + token.getPermissions());
        }

        return (token != null) && (Profile.getCurrentProfile() != null);
    }

    @Nullable
    public String getUserId()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return (token == null) ? null : token.getUserId();
    }

    @Nullable
    public Set<String> getUserPermissions()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return token.getPermissions();
    }

    @Nullable
    public String getUserTokenExpiration()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return (token == null) ? null : Simple.timeStampAsISO(token.getExpires().getTime());
    }

    @Nullable
    public String getUserDisplayName()
    {
        Profile profile = Profile.getCurrentProfile();
        return (profile == null) ? null : profile.getName();
    }

    public JSONArray getUserFeeds(boolean feedonly)
    {
        JSONArray data = new JSONArray();

        getOwnerFeed(data, "facebook");

        getUserFeeds(data, "facebook", "friend", feedonly);
        getUserFeeds(data, "facebook", "like", feedonly);

        return data;
    }

    @Nullable
    public JSONArray getUserFriendlist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/friends");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserFriendlists: data:" + data.toString());

        return data;
    }

    @Nullable
    public JSONArray getUserLikeslist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/likes");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserLikeslist: data:" + data.toString());

        return data;
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

        Log.d(LOGTAG, "getUserIcon: facebookid:" + pfid + "=" + iconurl);

        return SimpleRequest.readData(iconurl);
    }

    @Nullable
    public JSONObject getPost(String postid)
    {
        if (postid == null) return null;

        File postfile = new File(cachedir, postid + ".post.json");

        if (postfile.exists())
        {
            return Json.fromString(Simple.getFileContent(postfile));
        }

        return getGraphPost(postid);
    }

    private JSONObject getGraphPost(String postid)
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

    @Nullable
    public JSONArray getFeed(String userid)
    {
        if (userid == null) return null;

        File feedfile = new File(cachedir, userid + ".feed.json");

        if (feedfile.exists())
        {
            return Json.fromStringArray(Simple.getFileContent(feedfile));
        }

        return getGraphFeed(userid);
    }

    @Nullable
    private JSONArray getGraphFeed(String userid)
    {
        if (userid == null) return null;

        JSONObject response = getGraphRequest(userid + "/feed");
        return Json.getArray(response, "data");
    }

    private JSONObject getGraphRequest(String path)
    {
        return getGraphRequest(path, new Bundle());
    }

    @Nullable
    public JSONObject getGraphRequest(String path, JSONObject parameters)
    {
        return getGraphRequest(path, getParameters(parameters));
    }

    public JSONObject getGraphRequest(String path, Bundle parameters)
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

    public void reconfigureFriendsAndLikes()
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

                String pfid = Json.getString(friend, "id");
                String name = Json.getString(friend, "name");
                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social.facebook.friend.name." + pfid;
                String fmodepref = "social.facebook.friend.mode." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.getFacebookLoadProfileImage(pfid);

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

                String pfid = Json.getString(like, "id");
                String name = Json.getString(like, "name");
                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social.facebook.like.name." + pfid;
                String fmodepref = "social.facebook.like.mode." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                ProfileImages.getFacebookLoadProfileImage(pfid);

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

    private long totalInterval = 3600;
    private long lastReconfigure;
    private long nextInterval;
    private long nextAction;

    private JSONArray feedList;

    public void commTick()
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

        final String feedpfid = Json.getString(feed, "id");
        final String feedname = Json.getString(feed, "name");

        Log.d(LOGTAG, "commTick: feed:" + feedpfid + " => " + feedname);

        JSONArray feeddata = getGraphFeed(feedpfid);
        if (feeddata == null) return;

        File feedfile = new File(cachedir, feedpfid + ".feed.json");
        Simple.putFileContent(feedfile, Json.toPretty(feeddata));

        //
        // Check feed stories.
        //

        FilenameFilter postsfilter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                return filename.startsWith(feedpfid + "_") && filename.endsWith(".post.json");
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
