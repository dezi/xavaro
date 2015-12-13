package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//
// Webradio base class.
//
public class WebRadio extends LaunchGroup
{
    private static final String LOGTAG = WebRadio.class.getSimpleName();

    protected String webradio;

    public WebRadio(Context context)
    {
        super(context);
    }

    public void setName(LaunchItem parent, String webradio)
    {
        this.parent = parent;
        this.webradio = webradio;

        config = getConfig(context,webradio);
    }

    @Override
    protected void createLaunchItems()
    {
        launchItems = new ArrayList<>();

        if ((config == null) || ! config.has("channels"))
        {
            Toast.makeText(context, "Keine <channels> gefunden.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            JSONArray lis = config.getJSONArray("channels");

            int numItems = horzItems * vertItems;
            int maxSlots = lis.length();

            for (int inx = 0; inx < numItems; inx++)
            {
                LaunchItem li = new LaunchItem(context);
                li.setSize(horzSize, vertSize);

                if (inx < maxSlots) li.setConfig(this,lis.getJSONObject(inx));

                launchItems.add(li);
                addView(li);
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    //region Static methods.

    private static JSONObject globalConfig = null;

    public static JSONObject getConfig(Context context)
    {
        if (globalConfig == null)
        {
            JSONObject jot = StaticUtils.readRawTextResourceJSON(context, R.raw.default_webradio);

            if (jot == null)
            {
                Log.e(LOGTAG, "getConfig: Cannot read default webradios.");
            }
            else
            {
                try
                {
                    globalConfig = jot.getJSONObject("webradio");

                    return globalConfig;
                }
                catch (JSONException ignore)
                {
                    Log.e(LOGTAG, "getConfig: Tag <webradio> missing in config.");
                }
            }

            globalConfig = new JSONObject();
        }

        return globalConfig;
    }

    public static JSONObject getConfig(Context context,String website)
    {
        try
        {
            return getConfig(context).getJSONObject(website);
        }
        catch (JSONException ignore)
        {
        }

        return new JSONObject();
    }

    @Nullable
    public static Drawable getConfigIconDrawable(Context context,String website)
    {
        try
        {
            String iconurl = getConfig(context,website).getString("icon");
            String iconext = MimeTypeMap.getFileExtensionFromUrl(iconurl);
            String iconfile = website + ".thumbnail." + iconext;
            Bitmap thumbnail = CacheManager.cacheThumbnail(context, iconurl, iconfile);

            return new BitmapDrawable(context.getResources(), thumbnail);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String getConfigLabel(Context context,String website)
    {
        try
        {
            return getConfig(context, website).getString("label");
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    //endregion
}
