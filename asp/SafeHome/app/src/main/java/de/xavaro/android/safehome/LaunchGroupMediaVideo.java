package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchGroupMediaVideo extends LaunchGroupMedia
{
    private static final String LOGTAG = LaunchGroupMediaVideo.class.getSimpleName();

    public LaunchGroupMediaVideo(Context context)
    {
        super(context);
    }

    public LaunchGroupMediaVideo(Context context, String mediadir)
    {
        super(context, mediadir);
        this.config = getVideos();
    }

    private JSONObject getVideos()
    {
        JSONObject launchgroup = new JSONObject();
        JSONArray launchitems = new JSONArray();

        File mediapath = Simple.getMediaPath(mediadir);

        JSONArray videos = Simple.getDirectorySortedByAge(
                mediapath, new Simple.VideoFileFilter(), true);

        if (videos != null)
        {
            for (int inx = 0; inx < videos.length(); inx++)
            {
                JSONObject videoitem = Json.getObject(videos, inx);
                String videofile = Json.getString(videoitem, "file");
                if (videofile == null) continue;
                File file = new File(videofile);

                JSONObject launchitem = new JSONObject();

                Json.put(launchitem, "label", file.getName());
                Json.put(launchitem, "type", "media");
                Json.put(launchitem, "subtype", "video");
                Json.put(launchitem, "mediaitem", videofile);

                launchitems.put(launchitem);
            }
        }

        Json.put(launchgroup, "launchitems", launchitems);

        return launchgroup;
    }

    public static JSONArray getConfig()
    {
        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();

        if (Simple.getSharedPrefBoolean("media.video.enable"))
        {
            String keyprefix = "media.video.directory.";

            SharedPreferences sp = Simple.getSharedPrefs();
            Map<String, Object> prefs = Simple.getAllPreferences(keyprefix);

            for (String prefkey : prefs.keySet())
            {
                String what = sp.getString(prefkey, null);
                if ((what == null) || what.equals("inact")) continue;

                String mediadir = prefkey.substring(keyprefix.length());

                String label = Simple.getTransVal(
                        R.array.pref_media_video_directories_keys, mediadir);

                JSONObject entry = new JSONObject();

                Json.put(entry, "label", label);
                Json.put(entry, "type", "media");
                Json.put(entry, "subtype", "video");
                Json.put(entry, "mediadir", mediadir);
                Json.put(entry, "order", 800);

                if (Simple.equals(what, "home")) home.put(entry);
                if (Simple.equals(what, "videos")) adir.put(entry);
            }
        }

        if (adir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "label", "Filme");
            Json.put(entry, "type", "media");
            Json.put(entry, "subtype", "video");
            Json.put(entry, "order", 850);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        return home;
    }
}
