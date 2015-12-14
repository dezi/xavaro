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
// Web stream receivers base class.
//

public class WebStream extends LaunchGroup
{
    private static final String LOGTAG = WebStream.class.getSimpleName();

    private String webtype;
    private String webstream;

    public WebStream(Context context)
    {
        super(context);
    }

    public void setName(LaunchItem parent, String type, String website)
    {
        this.parent = parent;
        this.webtype = type;
        this.webstream = website;

        config = getConfig(context, webtype, webstream);
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

    private static JSONObject globalConfig = new JSONObject();

    private static JSONObject getConfig(Context context,String type)
    {
        JSONObject typeroot = new JSONObject();

        try
        {
            if (! globalConfig.has(type))
            {
                int resourceId = context.getResources().getIdentifier("default_" + type, "raw", context.getPackageName());

                JSONObject jot = StaticUtils.readRawTextResourceJSON(context, resourceId);

                if ((jot == null) || !jot.has(type))
                {
                    Log.e(LOGTAG, "getConfig: Cannot read default " + type);
                }
                else
                {
                    globalConfig.put(type, jot.getJSONObject(type));
                }
            }

            typeroot = globalConfig.getJSONObject(type);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return typeroot;
    }

    public static JSONObject getConfig(Context context, String type, String website)
    {
        try
        {
            return getConfig(context,type).getJSONObject(website);
        }
        catch (JSONException ignore)
        {
        }

        return new JSONObject();
    }

    @Nullable
    public static Drawable getConfigIconDrawable(Context context, String type, String website)
    {
        try
        {
            String iconurl = getConfig(context, type, website).getString("icon");
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
    public static String getConfigLabel(Context context,String type, String website)
    {
        try
        {
            return getConfig(context, type, website).getString("label");
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    //endregion
}
