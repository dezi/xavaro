package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Social
{
    private static final String LOGTAG = Social.class.getSimpleName();

    protected String platform;
    protected boolean verbose;
    protected String locale;
    protected File cachedir;

    public Social(String platform)
    {
        this.platform = platform;

        locale = Simple.getLocaleLanguage() + "_" + Simple.getLocaleCountry();
        cachedir = new File(Simple.getExternalCacheDir(), platform);
    }

    public boolean isEnabled()
    {
        return Simple.getSharedPrefBoolean("social." + platform + ".enable");
    }

    public boolean isLoggedIn()
    {
        Log.d(LOGTAG, "isLoggedIn: not overridden.");
        return false;
    }

    public boolean isReady()
    {
        return isEnabled() && isLoggedIn();
    }

    public void setVerbose(boolean yesno)
    {
        verbose = yesno;
    }

    protected Bundle getParameters(JSONObject jparams)
    {
        Bundle bparams = new Bundle();

        if (jparams != null)
        {
            Iterator<String> keysIterator = jparams.keys();

            while (keysIterator.hasNext())
            {
                String key = keysIterator.next();
                Object val = Json.get(jparams, key);

                if (val instanceof Boolean)
                {
                    bparams.putBoolean(key, (Boolean) val);
                    continue;
                }

                if (val instanceof Integer)
                {
                    bparams.putInt(key, (Integer) val);
                    continue;
                }

                if (val instanceof Long)
                {
                    bparams.putLong(key, (Long) val);
                    continue;
                }

                if (val instanceof String)
                {
                    bparams.putString(key, (String) val);
                    continue;
                }

                if (val instanceof JSONArray)
                {
                    JSONArray array = (JSONArray) val;
                    String imploded = "";

                    for (int inx = 0; inx < array.length(); inx++)
                    {
                        Object jobj = Json.get(array, inx);
                        if (!(jobj instanceof String)) continue;

                        if (imploded.length() > 0) imploded += ",";
                        imploded += (String) jobj;
                    }

                    bparams.putString(key, imploded);
                }
            }
        }

        return bparams;
    }

    protected File getUserImageFile(String pfid)
    {
        File icon = null;

        if (Simple.equals(platform, "facebook"))
        {
            icon = ProfileImages.getFacebookProfileImageFile(pfid);
        }

        if (Simple.equals(platform, "instagram"))
        {
            icon = ProfileImages.getInstagramProfileImageFile(pfid);
        }

        return icon;
    }

    protected void loadUserImageFile(String pfid)
    {
        if (Simple.equals(platform, "facebook"))
        {

            ProfileImages.getFacebookLoadProfileImage(pfid);
        }

        if (Simple.equals(platform, "instagram"))
        {
            ProfileImages.getInstagramLoadProfileImage(pfid);
        }
    }

    protected JSONArray getOwnerFeed(JSONArray data)
    {
        //
        // Add account owner as an owner feed.
        //

        JSONObject owner = new JSONObject();

        String pfid = Simple.getSharedPrefString("social." + platform + ".pfid");
        String name = Simple.getSharedPrefString("social." + platform + ".name");

        if ((pfid != null) && (name != null))
        {
            Json.put(owner, "id", pfid);
            Json.put(owner, "name", name);
            Json.put(owner, "type", "owner");
            Json.put(owner, "plat", platform);

            File icon = getUserImageFile(pfid);
            if (icon != null) Json.put(owner, "icon", icon.toString());

            Json.put(data, owner);
        }

        return data;
    }

    public JSONArray getUserFeeds(boolean feedonly)
    {
        JSONArray data = new JSONArray();

        getOwnerFeed(data);

        getUserFeeds(data, "friend", feedonly);
        getUserFeeds(data, "like", feedonly);

        return data;
    }

    protected JSONArray getUserFeeds(JSONArray data, String type, boolean feedonly)
    {
        String modeprefix = "social." + platform + "." + type + ".mode.";
        String nameprefix = "social." + platform + "." + type + ".name.";

        Map<String, Object> friends = Simple.getAllPreferences(modeprefix);

        for (Map.Entry<String, Object> entry : friends.entrySet())
        {
            Object fmode = entry.getValue();
            if (!(fmode instanceof String)) continue;

            String mode = (String) fmode;
            if (feedonly && !mode.contains("feed")) continue;
            if (mode.equals("inactive")) continue;

            String pfid = entry.getKey().substring(modeprefix.length());
            String name = Simple.getSharedPrefString(nameprefix + pfid);
            if (name == null) continue;

            JSONObject item = new JSONObject();

            Json.put(item, "id", pfid);
            Json.put(item, "name", name);
            Json.put(item, "type", type);
            Json.put(item, "plat", platform);

            File icon = getUserImageFile(pfid);
            if (icon != null) Json.put(item, "icon", icon.toString());

            Json.put(data, item);
        }

        return data;
    }

    @Nullable
    public JSONArray getUserFriendlist()
    {
        Log.d(LOGTAG, "getUserFriendlist: not overridden.");
        return null;
    }

    @Nullable
    public JSONArray getUserLikeslist()
    {
        Log.d(LOGTAG, "getUserLikelist: not overridden.");
        return null;
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

    @Nullable
    protected JSONObject getGraphPost(String postid)
    {
        Log.d(LOGTAG, "getGraphPost: not overridden.");
        return null;
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

    protected JSONArray getGraphFeed(String userid)
    {
        Log.d(LOGTAG, "getGraphFeed: not overridden.");
        return null;
    }

    public void reconfigureFriendsAndLikes()
    {
        if (!isLoggedIn()) return;

        JSONArray friends = getUserFriendlist();

        if (friends != null)
        {
            Map<String, Object> oldfriends = Simple.getAllPreferences("social." + platform + ".friend.");

            String dfmode = Simple.getSharedPrefString("social." + platform + ".newfriends.default");
            if (dfmode == null) dfmode = "feed+folder";

            for (int inx = 0; inx < friends.length(); inx++)
            {
                JSONObject friend = Json.getObject(friends, inx);
                if (friend == null) continue;

                String pfid = Json.getString(friend, "id");
                String name = Json.getString(friend, "full_name");
                if (name == null) name = Json.getString(friend, "name");
                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social." + platform + ".friend.name." + pfid;
                String fmodepref = "social." + platform + ".friend.mode." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                loadUserImageFile(pfid);

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
            Map<String, Object> oldlikes = Simple.getAllPreferences("social." + platform + ".like.");

            String dfmode = Simple.getSharedPrefString("social." + platform + ".newlikes.default");
            if (dfmode == null) dfmode = "folder";

            for (int inx = 0; inx < likes.length(); inx++)
            {
                JSONObject like = Json.getObject(likes, inx);
                if (like == null) continue;

                String pfid = Json.getString(like, "id");
                String name = Json.getString(like, "full_name");
                if (name == null) name = Json.getString(like, "name");
                if ((pfid == null) || (name == null)) continue;

                String fnamepref = "social." + platform + ".like.name." + pfid;
                String fmodepref = "social." + platform + ".like.mode." + pfid;

                Simple.setSharedPrefString(fnamepref, name);

                if (Simple.getSharedPrefString(fmodepref) == null)
                {
                    Simple.setSharedPrefString(fmodepref, dfmode);
                }

                loadUserImageFile(pfid);

                if (oldlikes.containsKey(fnamepref)) oldlikes.remove(fnamepref);
                if (oldlikes.containsKey(fmodepref)) oldlikes.remove(fmodepref);
            }

            for (Map.Entry<String, ?> entry : oldlikes.entrySet())
            {
                Simple.removeSharedPref(entry.getKey());
            }
        }
    }

    @Nullable
    public JSONObject getGraphRequest(String path)
    {
        return getGraphRequest(path, new Bundle());
    }

    @Nullable
    public JSONObject getGraphRequest(String path, JSONObject parameters)
    {
        return getGraphRequest(path, getParameters(parameters));
    }

    @Nullable
    public JSONObject getGraphRequest(String path, Bundle parameters)
    {
        Log.d(LOGTAG, "getGraphRequest: not overridden.");
        return null;
    }

    //region Graph call statistic

    public int getHourStatistic()
    {
        getStorage();

        synchronized (LOGTAG)
        {
            JSONArray hour = Json.getArray(statData, "hour");
            return (hour == null) ? 0 : hour.length();
        }
    }

    public int getTodayStatistic()
    {
        getStorage();

        synchronized (LOGTAG)
        {
            int today = Json.getInt(statData, Simple.getLocalDate(Simple.nowAsTimeStamp()));
            JSONArray hour = Json.getArray(statData, "hour");
            return (hour == null) ? today : (today  + hour.length());
        }
    }

    protected JSONObject statData;
    protected boolean statDirty;

    protected void maintainStatistic(String path)
    {
        getStorage();

        JSONArray hour = Json.getArray(statData, "hour");
        JSONObject stat = new JSONObject();

        Json.put(stat, "dst", Simple.nowAsISO());
        Json.put(stat, "api", path);

        Json.put(hour, stat);

        statDirty = true;
    }

    private final Runnable freeMemory = new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (LOGTAG)
            {
                if (statData != null)
                {
                    if (statDirty) putStorage();

                    statData = null;
                }
            }
        }
    };

    protected void removeOutdated()
    {
        long now = Simple.nowAsTimeStamp();
        String out = Simple.timeStampAsISO(now - (3600 * 1000));

        if (! Json.has(statData, "hour")) Json.put(statData, "hour", new JSONArray());

        JSONArray hour = Json.getArray(statData, "hour");

        String today = Simple.getLocalDate(Simple.nowAsTimeStamp());

        while ((hour != null) && (hour.length() > 0))
        {
            JSONObject old = Json.getObject(hour, 0);
            String dst = Json.getString(old, "dst");

            if ((dst == null) || (dst.compareTo(out) < 0))
            {
                hour.remove(0);

                Json.put(statData, today, Json.getInt(statData, today) + 1);
            }
            else
            {
                break;
            }
        }
    }

    private void getStorage()
    {
        Simple.removePost(freeMemory);
        Simple.makePost(freeMemory, 10 * 1000);

        if (statData != null) return;

        synchronized (LOGTAG)
        {
            try
            {
                File statfile = new File(cachedir, platform + ".statistic.json");
                statData = Json.fromStringObject(Simple.getFileContent(statfile));
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            if (statData == null) statData = new JSONObject();

            removeOutdated();
        }
    }

    private void putStorage()
    {
        if (statData == null) return;

        synchronized (LOGTAG)
        {
            try
            {
                File statfile = new File(cachedir, platform + ".statistic.json");
                Simple.putFileContent(statfile, Json.defuck(Json.toPretty(statData)));
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }
    }

    //endregion Graph call statistic

    //region Cache maintenance

    private final long totalInterval = 3600;
    private long lastReconfigure;
    private long nextInterval;
    private long nextAction;

    private JSONArray feedList;

    public void commTick()
    {
        long now = Simple.nowAsTimeStamp();

        if ((now - lastReconfigure) > 24 * 3600 * 1000)
        {
            cachedir = new File(Simple.getExternalCacheDir(), platform);

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

                //
                // Impose a rate limit on too many feeds.
                //

                if (now < (60 * 1000)) now = 60 * 1000;

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
        String feedname = Json.getString(feed, "full_name");
        if (feedname == null) feedname = Json.getString(feed, "name");

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
