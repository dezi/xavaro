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
import java.util.Set;

public class Facebook
{
    private static final String LOGTAG = Facebook.class.getSimpleName();

    private static final Collection<String> permissions = Arrays.asList
    (
        "public_profile",
        "user_friends",
        "user_posts"
    );

    private static final GraphRequest graphrequest = new GraphRequest();
    private static GraphResponse response;
    private static CallbackManager callbackManager;
    private static AppEventsLogger logger;

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
            Log.d(LOGTAG, "graphcallback: onCompleted..." + response);

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
        if (AccessToken.getCurrentAccessToken() != null)
        {
            Log.d(LOGTAG, "isLoggedIn: permissions=" + AccessToken.getCurrentAccessToken().getPermissions());
        }

        return (AccessToken.getCurrentAccessToken() != null) &&
                (Profile.getCurrentProfile() != null);
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
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null) return null;

        Bundle parameters = new Bundle();

        //graphrequest.setAccessToken(AccessToken.getCurrentAccessToken());
        graphrequest.setGraphPath("/" + getUserId() + "/friends");
        graphrequest.setParameters(parameters);
        graphrequest.executeAndWait();

        JSONObject json = response.getJSONObject();
        JSONArray data = Json.getArray(json, "data");

        if (data != null) Log.d(LOGTAG, "getUserFriendlists: data:" + data.toString());

        return data;
    }

    @Nullable
    public static byte[] getUserIcon(String fbid)
    {
        if (fbid == null) return null;

        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null) return null;

        Bundle parameters = new Bundle();
        parameters.putBoolean("redirect", false);
        parameters.putString("type", "square");
        parameters.putInt("width", 400);
        parameters.putInt("height", 400);

        graphrequest.setGraphPath("/" + fbid + "/picture");
        graphrequest.setParameters(parameters);
        graphrequest.executeAndWait();

        JSONObject json = response.getJSONObject();
        JSONObject data = Json.getObject(json, "data");
        String iconurl = Json.getString(data, "url");

        Log.d(LOGTAG, "getUserIcon: facebookid:" + fbid + "=" + iconurl);

        return SimpleRequest.readData(iconurl);
    }
}
