package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

//
// Utility namespace for app launch groups.
//

public class LaunchGroupMedia
{
    private static final String LOGTAG = LaunchGroupMedia.class.getSimpleName();

    public static class ImageGroup extends LaunchGroup
    {
        private static final String LOGTAG = ImageGroup.class.getSimpleName();

        private String mediadir;

        public ImageGroup(Context context)
        {
            super(context);
        }

        public ImageGroup(Context context, String mediadir)
        {
            super(context);

            this.mediadir = mediadir;
            this.config = getImages();
        }

        private JSONObject getImages()
        {
            JSONObject launchgroup = new JSONObject();
            JSONArray launchitems = new JSONArray();

            File mediapath = Simple.getMediaPath(mediadir);

            JSONArray images = Simple.getDirectorySortedByAge(
                    mediapath, new Simple.ImageFileFilter(), true);

            if (images != null)
            {
                for (int inx = 0; inx < images.length(); inx++)
                {
                    JSONObject imageitem = Json.getObject(images, inx);
                    String imagefile = Json.getString(imageitem, "file");
                    File file = new File(imagefile);

                    JSONObject launchitem = new JSONObject();

                    Json.put(launchitem, "label", file.getName());
                    Json.put(launchitem, "type", "media");
                    Json.put(launchitem, "subtype", "image");
                    Json.put(launchitem, "mediaitem", imagefile);

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

            if (Simple.getSharedPrefBoolean("media.image.enable"))
            {
                String keyprefix = "media.image.directory.";

                SharedPreferences sp = Simple.getSharedPrefs();
                Map<String, Object> prefs = Simple.getAllPreferences(keyprefix);

                for (String prefkey : prefs.keySet())
                {
                    String what = sp.getString(prefkey, null);
                    if ((what == null) || what.equals("inact")) continue;

                    String mediadir = prefkey.substring(keyprefix.length());

                    String label = Simple.getTransVal(
                            R.array.pref_media_image_directories_keys, mediadir);

                    JSONObject entry = new JSONObject();

                    Json.put(entry, "label", label);
                    Json.put(entry, "type", "media");
                    Json.put(entry, "subtype", "image");
                    Json.put(entry, "mediadir", mediadir);
                    Json.put(entry, "order", 800);

                    if (Simple.equals(what, "home")) home.put(entry);
                    if (Simple.equals(what, "images")) adir.put(entry);
                }
            }

            if (adir.length() > 0)
            {
                JSONObject entry = new JSONObject();

                Json.put(entry, "label", "Fotoalben");
                Json.put(entry, "type", "media");
                Json.put(entry, "subtype", "image");
                Json.put(entry, "order", 850);

                Json.put(entry, "launchitems", adir);
                Json.put(home, entry);
            }

            return home;
        }
    }
}