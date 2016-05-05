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

    private final GraphRequest graphrequest;
    private GraphResponse graphresponse;
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

            graphresponse = response;
        }
    };

    public void login()
    {
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

        if (callbackManager == null)
        {
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, callback);
        }

        LoginManager.getInstance().logInWithReadPermissions(Simple.getActContext(), permissions);
    }

    public void logout()
    {
        LoginManager.getInstance().logOut();
    }

    @Override
    public boolean isLoggedIn()
    {
        AccessToken token = AccessToken.getCurrentAccessToken();
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

    @Override
    public JSONArray getUserFriendlist()
    {
        JSONObject json = getGraphRequest("/" + getUserId() + "/friends");
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserFriendlists: data:" + data.toString());

        return data;
    }

    @Override
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

        Log.d(LOGTAG, "getUserIcon: pfid:" + pfid + "=" + iconurl);

        return SimpleRequest.readData(iconurl);
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

        return getGraphRequest(postid, params);
    }

    @Nullable
    protected JSONArray getGraphFeed(String userid)
    {
        if (userid == null) return null;

        JSONObject response = getGraphRequest(userid + "/feed");
        return Json.getArray(response, "data");
    }

    public JSONObject getGraphRequest(String path, Bundle parameters)
    {
        if (path == null) return null;

        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null) return null;

        if (parameters == null) parameters = new Bundle();
        parameters.putString("locale", locale);

        maintainStatistic(path, parameters);

        synchronized (graphrequest)
        {
            graphrequest.setAccessToken(token);
            graphrequest.setGraphPath(path);
            graphrequest.setParameters(parameters);
            graphrequest.executeAndWait();

            return graphresponse.getJSONObject();
        }
    }
}
