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

            this.config = getConfig();
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

        private JSONObject getConfig()
        {
            try
            {
                JSONObject launchgroup = new JSONObject();
                JSONArray launchitems = new JSONArray();

                SharedPreferences sp = Simple.getSharedPrefs();

                String keyprefix = "media.image.directory.";

                Map<String, Object> prefs = Simple.getAllPreferences(keyprefix);

                for (String prefkey : prefs.keySet())
                {
                    String what = sp.getString(prefkey, "");
                    if (what.equals("inact")) continue;

                    String mediadir = prefkey.substring(keyprefix.length());

                    String label = Simple.getTransTrans(
                            R.array.pref_media_image_directories_keys, mediadir);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("label", label);
                    whatsentry.put("type", "media");
                    whatsentry.put("subtype", "image");
                    whatsentry.put("mediadir", mediadir);

                    launchitems.put(whatsentry);
                }

                launchgroup.put("launchitems", launchitems);

                return launchgroup;
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }

            return new JSONObject();
        }
    }
}