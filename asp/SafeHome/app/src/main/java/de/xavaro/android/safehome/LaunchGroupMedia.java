package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        public ImageGroup(Context context)
        {
            super(context);

            this.config = getConfig(context);
        }

        private JSONObject getConfig(Context context)
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
                    String directory = prefkey.substring(keyprefix.length());

                    if ((what == null) || what.equals("inact")) continue;

                    String label = Simple.getTransTrans(
                            R.array.pref_media_image_directories_keys, directory);

                    JSONObject whatsentry = new JSONObject();

                    whatsentry.put("type", "media");
                    whatsentry.put("subtype", "image");
                    whatsentry.put("label", label);

                    launchitems.put(whatsentry);

                    Log.d(LOGTAG, "Prefe:" + prefkey + "=" + "=" + directory);
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