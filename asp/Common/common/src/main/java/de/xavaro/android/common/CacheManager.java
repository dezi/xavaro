package de.xavaro.android.common;

import android.graphics.drawable.Drawable;
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
    private static final String LOGTAG = CacheManager.class.getSimpleName();

    private final static File cachedir =  Simple.getExternalCacheDir("iconcache");

    @Nullable
    public static Bitmap getIcon(String filename)
    {
        File file = new File(cachedir, filename);

        if (file.exists())
        {
            try
            {
                FileInputStream input = new FileInputStream(file);
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                input.close();

                return myBitmap;

            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return null;
    }

    private static String loadWebIcon(String filename, String src)
    {
        File file = new File(cachedir, filename);

        if (! file.exists())
        {
            Log.d(LOGTAG, "getIcon: fetch " + file.toString());
            Log.d(LOGTAG, "getIcon:   url " + src);

            try
            {
                Simple.makeDirectory(cachedir);

                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(file);

                byte[] bytes = new byte[ 4096 ];

                int xfer;
                while ((xfer = input.read(bytes)) > 0) output.write(bytes, 0, xfer);

                output.close();
                input.close();
            }
            catch (Exception ex)
            {
                if (file.exists()) file.delete();

                return null;
            }

            Log.d(LOGTAG, "cacheThumbnail: fetch done");
        }

        return file.toString();
    }

    private static String loadAppIcon(String filename, String packagename)
    {
        File file = new File(cachedir, filename);

        if (! file.exists())
        {
            try
            {
                String url = "https://play.google.com/store/apps/details?id=" + packagename;

                Log.d(LOGTAG, "getIconFromAppStore:" + url);

                String content = StaticUtils.getContentFromUrl(url);

                if (content == null)
                {
                    //
                    // For some reasons specific apps result in
                    // a 403 forbidden result. Fallback and try
                    // to extract icon from installed app.
                    //
                    // For some reason means: The app has been discontinued.
                    //

                    Drawable drawable = VersionUtils.getIconFromApplication(packagename);

                    if (drawable != null)
                    {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                        if (bitmap != null)
                        {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.close();
                        }
                    }
                }
                else
                {
                    Log.d(LOGTAG, "getIconFromAppStore(1):" + url);

                    Pattern pattern = Pattern.compile("class=\"cover-image\" src=\"([^\"]*)\"");
                    Matcher matcher = pattern.matcher(content);
                    if (!matcher.find()) return null;

                    String iconurl = matcher.group(1);

                    if (iconurl.startsWith("//")) iconurl = "http:" + iconurl;

                    Log.d(LOGTAG, "getIconFromAppStore:" + iconurl);

                    loadWebIcon(filename, iconurl);
                }
            }
            catch (Exception oops)
            {
                OopsService.log(LOGTAG, oops);

                return null;
            }
        }

        return file.toString();
    }

    @Nullable
    public static Drawable getAppIcon(String packagename)
    {
        String iconfile = "app." + packagename + ".icon";

        loadAppIcon(iconfile, packagename);

        return Simple.getDrawable(getIcon(iconfile));
    }

    @Nullable
    public static String getAppIconPath(String packagename)
    {
        String iconfile = "app." + packagename + ".icon";

        return loadAppIcon(iconfile, packagename);
    }

    @Nullable
    public static Drawable getWebIcon(String website, String src)
    {
        String iconfile = "web." + website + ".icon";

        loadWebIcon(iconfile, src);

        return Simple.getDrawable(getIcon(iconfile));
    }

    @Nullable
    public static String getWebIconPath(String website, String src)
    {
        String iconfile = "web." + website + ".icon";

        return loadWebIcon(iconfile, src);
    }
}
