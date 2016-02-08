package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.webkit.MimeTypeMap;
import android.util.Log;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;
import java.net.URL;

public class CacheManager
{
    private final static String LOGTAG = "CacheManager";

    @Nullable
    public static Bitmap getThumbnail(Context context, String filename)
    {
        File file = new File(context.getCacheDir(), filename);

        if (file.exists())
        {
            Log.d(LOGTAG,"getThumbnail: load " + file.toString());

            try
            {
                FileInputStream input = new FileInputStream(file);

                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                input.close();

                Log.d(LOGTAG, "getThumbnail: load done ");

                return myBitmap;

            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return null;
    }

    @Nullable
    public static Bitmap cacheThumbnail(Context context, String src)
    {
        Uri uri = Uri.parse(src);
        String name = uri.getLastPathSegment();
        String ext = MimeTypeMap.getFileExtensionFromUrl(name);
        name = name.substring(0,name.length() - ext.length());

        String filename = uri.getHost() + "." + name + "thumbnail." + ext;

        return cacheThumbnail(context, src, filename);
    }

    @Nullable
    public static Bitmap cacheThumbnail(Context context, String src, String filename)
    {
        File file = new File(context.getCacheDir(), filename);

        if (! file.exists())
        {
            Log.d(LOGTAG,"cacheThumbnail: fetch " + filename);

            try
            {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(file);

                byte[] bytes = new byte[ 4096 ];
                int xfer;

                while ((xfer = input.read(bytes)) > 0)
                {
                    output.write(bytes, 0, xfer);
                }

                output.close();
                input.close();
            }
            catch (Exception ex)
            {
                if (file.exists()) file.delete();

                return null;
            }

            Log.d(LOGTAG,"cacheThumbnail: fetch done");
        }

        return getThumbnail(context, filename);
    }

    @Nullable
    public static BitmapDrawable getIconFromAppStore(Context context, String packageName)
    {
        String iconfile = "appstore." + packageName + ".thumbnail.png";

        Bitmap icon = CacheManager.getThumbnail(context,iconfile);
        if (icon != null) return new BitmapDrawable(context.getResources(), icon);

        try
        {
            String url = "https://play.google.com/store/apps/details?id=" + packageName;

            Log.d(LOGTAG,"getIconFromAppStore:" + url);

            String content = StaticUtils.getContentFromUrl(url);
            if (content == null) return null;

            Pattern pattern = Pattern.compile("class=\"cover-image\" src=\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(content);
            if (! matcher.find()) return null;

            String iconurl = matcher.group(1);

            if (iconurl.startsWith("//")) iconurl = "http:" + iconurl;

            Log.d(LOGTAG, "getIconFromAppStore:" + iconurl);

            icon = CacheManager.cacheThumbnail(context, iconurl, iconfile);

            if (icon != null) return new BitmapDrawable(context.getResources(), icon);
        }
        catch (Exception oops)
        {
            OopsService.log(LOGTAG, oops);
        }

        return null;
    }
}
