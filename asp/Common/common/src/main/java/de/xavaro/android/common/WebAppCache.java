package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class WebAppCache
{
    private static final String LOGTAG = WebAppCache.class.getSimpleName();

    private static JSONObject webappcache;
    private static boolean dirty;

    @Nullable
    public static byte[] getCacheFile(String webappname, String url, int interval)
    {
        Simple.removePost(freeMemory);
        getStorage();

        byte[] content = null;

        if (! webappcache.has(webappname)) Json.put(webappcache, webappname, new JSONObject());
        JSONObject cachefiles = Json.getObject(webappcache, webappname);

        if (cachefiles == null)
        {
            Simple.makePost(freeMemory, 10 * 1000);
            return null;
        }

        File cachedir = new File(Simple.getCacheDir(), "webappcache/" + webappname);

        if (! cachedir.exists())
        {
            cachedir.mkdirs();
        }

        JSONObject cachefile = null;
        String uuid = null;

        if (cachefiles.has(url))
        {
            cachefile = Json.getObject(cachefiles, url);

            uuid = Json.getString(cachefile, "uuid");
            String lget = Json.getString(cachefile, "lget");

            if ((uuid != null) && (lget != null))
            {
                File cfile = new File(cachedir, uuid);

                long age = Simple.getTimeStamp(lget);
                long now = Simple.nowAsTimeStamp();

                if ((now - age) <= (interval * 3600 * 1000L))
                {
                    //
                    // Entry is within age.
                    //

                    content = Simple.readBinaryFile(cfile);

                    Log.d(LOGTAG,"getCacheFile: HIT=" + url);
                }
                else
                {
                    if (! cfile.exists())
                    {
                        //
                        // The file is known in cache but does not
                        // exists. Remove the last modified date
                        // to ensure, it is reloaded.
                        //

                        Json.remove(cachefile, "lmod");
                    }
                }
            }
        }

        if (cachefile == null)
        {
            cachefile = new JSONObject();
            uuid = Simple.getUUID();

            Json.put(cachefile, "uuid", uuid);
            Json.put(cachefile, "ival", interval);

            Json.put(cachefiles, url, cachefile);

            dirty = true;
        }

        if (content == null)
        {
            content = getContentFromServer(url, cachefile);

            if ((content != null) && (uuid != null))
            {
                //
                // We have read something from the server.
                // Store content into cache file.
                //

                File cfile = new File(cachedir, uuid);
                Simple.writeBinaryFile(cfile, content);

                Log.d(LOGTAG, "getCacheFile: GET=" + url);
            }
        }

        if ((content == null) && (uuid != null))
        {
            //
            // The content is either unavailable or has not been modified.
            // Try to read the local cache version or fail.
            //

            File cfile = new File(cachedir, uuid);
            content = Simple.readBinaryFile(cfile);

            if (content != null)
            {
                //
                // The content is revalidated for the next interval
                // since not modified on server.
                //

                Log.d(LOGTAG, "getCacheFile: NOM=" + url);

                Json.put(cachefile, "lget", Simple.nowAsISO());
                dirty = true;
            }
        }

        if (content != null)
        {
            //
            // Set now date as last usage date of this item.
            //

            Json.put(cachefile, "luse", Simple.nowAsISO());
            dirty = true;
        }
        else
        {
            Log.d(LOGTAG, "getCacheFile: ERR=" + url);
        }

        Simple.makePost(freeMemory, 10 * 1000);

        return content;
    }

    @Nullable
    private static byte[] getContentFromServer(String src, JSONObject cachefile)
    {
        try
        {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (cachefile.has("lmod"))
            {
                //
                // Only fetch the file if it was modified.
                //

                String lastmodified = Json.getString(cachefile, "lmod");
                connection.setRequestProperty("If-Modified-Since", lastmodified);
            }

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED)
            {
                //
                // Outdated file is still valid.
                //

                return null;
            }

            //
            // Retrieve last modified date string and content type from headers.
            //

            Map<String, List<String>> headers = connection.getHeaderFields();

            for (String headerKey : headers.keySet())
            {
                if (headerKey == null) continue;

                for (String headerValue : headers.get(headerKey))
                {
                    if (headerKey.equalsIgnoreCase("Last-Modified"))
                    {
                        Json.put(cachefile, "lmod", headerValue);
                        dirty = true;
                    }

                    if (headerKey.equalsIgnoreCase("Content-Type"))
                    {
                        Json.put(cachefile, "mime", headerValue);
                        dirty = true;
                    }
                }
            }

            //
            // Fetch file.
            //

            int length = connection.getContentLength();
            InputStream input = connection.getInputStream();
            byte[] buffer;
            int total = 0;

            if (length > 0)
            {
                buffer = new byte[ length ];

                for (int xfer = 0; total < length; total += xfer)
                {
                    xfer = input.read(buffer, total, length - total);
                }
            }
            else
            {
                byte[] chunk = new byte[ 32 * 1024 ];

                buffer = new byte[ 0 ];

                for (int xfer = 0; ; total += xfer)
                {
                    xfer = input.read(chunk, 0, chunk.length);
                    if (xfer <= 0) break;

                    byte[] temp = new byte[ buffer.length + xfer ];
                    System.arraycopy(buffer, 0, temp, 0, buffer.length);
                    System.arraycopy(chunk, 0, temp, buffer.length, xfer);
                    buffer = temp;
                }
            }

            input.close();

            Json.put(cachefile, "lget", Simple.nowAsISO());
            dirty = true;

            return buffer;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    private static final Runnable freeMemory = new Runnable()
    {
        @Override
        public void run()
        {
            if (webappcache != null)
            {
                if (dirty) putStorage();

                webappcache = null;
            }
        }
    };

    private static void getStorage()
    {
        if (webappcache != null) return;

        File file = new File(Simple.getFilesDir(), "webappcache.act.json");
        if (! file.exists()) file = new File(Simple.getFilesDir(), "webappcache.bak.json");

        try
        {
            if (! file.exists())
            {
                webappcache = new JSONObject();
            }
            else
            {
                String json = Simple.getFileContent(file);
                webappcache = (json != null) ? new JSONObject(json) : new JSONObject();
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }

    private static void putStorage()
    {
        if (webappcache == null) return;

        File tmp = new File(Simple.getFilesDir(), "webappcache.tmp.json");
        File bak = new File(Simple.getFilesDir(), "webappcache.bak.json");
        File act = new File(Simple.getFilesDir(), "webappcache.act.json");

        try
        {
            if (Simple.putFileContent(tmp, Json.defuck(Json.toPretty(webappcache))))
            {
                boolean ok = true;

                if (bak.exists()) ok = bak.delete();
                if (act.exists()) ok &= act.renameTo(bak);
                if (tmp.exists()) ok &= tmp.renameTo(act);

                Log.d(LOGTAG, "putStorage: ok=" + ok);
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }

}
