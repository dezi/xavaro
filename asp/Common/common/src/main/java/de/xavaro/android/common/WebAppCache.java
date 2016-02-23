package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebAppCache
{
    private static final String LOGTAG = WebAppCache.class.getSimpleName();

    private static JSONObject webappcache;
    private static boolean dirty;

    //
    // Small response class for transporting content, mimetype and encoding.
    // Encoding is by default UTF-8 and we do not want anything else.
    //

    public static class WebAppCacheResponse
    {
        public final String mimetype;
        public final String encoding;
        public final byte[] content;

        public WebAppCacheResponse(String mimetype, String encoding, byte[] content)
        {
            this.mimetype = mimetype;
            this.encoding = encoding;
            this.content = content;
        }
    }

    //
    // Get content from cache. If intervall is greater zero reflect age of cached
    // item and get a fresh copy if required. If intervall equals zero, always get
    // a fresh copy from server and store for offline usage.
    //

    public static WebAppCacheResponse getCacheFile(String webappname, String url, int interval)
    {
        Simple.removePost(freeMemory);
        getStorage();

        //
        // Modify webapp server urls to be w/o web server root
        // to allow cache writing independendly of developer.
        //

        String cacheurl = url;

        if (url.startsWith(WebApp.getHTTPRoot(webappname)))
        {
            cacheurl = url.substring(WebApp.getHTTPRoot(webappname).length());
        }

        byte[] content = null;
        String mimetype = null;

        if (! webappcache.has(webappname)) Json.put(webappcache, webappname, new JSONObject());
        JSONObject cachefiles = Json.getObject(webappcache, webappname);

        if (cachefiles == null)
        {
            Simple.makePost(freeMemory, 10 * 1000);
            return new WebAppCacheResponse(null, null, null);
        }

        File cachedir = new File(Simple.getCacheDir(), "webappcache/" + webappname);

        if (! cachedir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            cachedir.mkdirs();
        }

        JSONObject cachefile;
        String uuid;

        if (cachefiles.has(cacheurl))
        {
            cachefile = Json.getObject(cachefiles, cacheurl);

            uuid = Json.getString(cachefile, "uuid");
            String lget = Json.getString(cachefile, "lget");

            if ((interval > 0) && (uuid != null) && (lget != null))
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
                    mimetype = Json.getString(cachefile, "mime");

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
        else
        {
            cachefile = new JSONObject();
            uuid = Simple.getUUID();

            //
            // Create a new cache entry with final uuid, current
            // intervall and a random update time to ensure even
            // distributed cache updates on application servers.
            //

            Json.put(cachefile, "uuid", uuid);
            Json.put(cachefile, "ival", interval);
            Json.put(cachefile, "time", Simple.getRandom(0, 86400));

            Json.put(cachefiles, cacheurl, cachefile);

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
                // since not modified on server or unavailable.
                //

                Log.d(LOGTAG, "getCacheFile: NOM=" + url);

                Json.put(cachefile, "lget", Simple.nowAsISO());
                dirty = true;
            }
        }

        if (content != null)
        {
            //
            // Set now date as last usage date of this item. Also
            // write current intervall to reflect developer and
            // non developer settings.
            //

            mimetype = Json.getString(cachefile, "mime");
            Json.put(cachefile, "ival", interval);
            Json.put(cachefile, "luse", Simple.nowAsISO());
            dirty = true;
        }
        else
        {
            Log.d(LOGTAG, "getCacheFile: ERR=" + url);
        }

        Simple.makePost(freeMemory, 10 * 1000);

        return new WebAppCacheResponse(mimetype, "UTF-8", content);
    }

    @Nullable
    private static byte[] getContentFromServer(String src, JSONObject cachefile)
    {
        try
        {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if ((cachefile != null) && cachefile.has("lmod"))
            {
                //
                // Only fetch the file if it was modified.
                //

                String lastmodified = Json.getString(cachefile, "lmod");
                connection.setRequestProperty("If-Modified-Since", lastmodified);
            }

            connection.setConnectTimeout(4000);
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

            if (cachefile != null)
            {
                //
                // Retrieve last modified date string and content type
                // from headers and put into cache descriptor.
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

                for (int xfer; total < length; total += xfer)
                {
                    xfer = input.read(buffer, total, length - total);
                }
            }
            else
            {
                byte[] chunk = new byte[ 32 * 1024 ];

                buffer = new byte[ 0 ];

                for (int xfer; ; total += xfer)
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

            if (cachefile != null)
            {
                //
                // Put fetch date into cache descriptor.
                //

                Json.put(cachefile, "lget", Simple.nowAsISO());
                dirty = true;
            }

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

    private static long nextLoadTime;

    @Nullable
    private static JSONObject getNextLoadItem()
    {
        Simple.removePost(freeMemory);
        getStorage();

        long nowsecs = Simple.nowAsTimeStamp() / 1000;
        long todaysecs = (nowsecs / 86400) * 86400;

        JSONObject nextItem = null;

        Iterator<String> webappsIter = webappcache.keys();

        while (webappsIter.hasNext())
        {
            String webappname = webappsIter.next();

            JSONObject cachefiles = Json.getObject(webappcache, webappname);
            if (cachefiles == null) continue;

            Iterator<String> cachefilesIter = cachefiles.keys();

            while (cachefilesIter.hasNext())
            {
                String url = cachefilesIter.next();
                JSONObject cachefile = Json.getObject(cachefiles, url);
                if (cachefile == null) continue;

                int ival = Json.getInt(cachefile, "ival");
                if (ival == 0) continue;

                //
                // Last moad must be older than half of the intervall.
                //

                String lget = Json.getString(cachefile, "lget");
                long lastLoad = (lget == null) ? todaysecs : (Simple.getTimeStamp(lget) / 1000);

                int time = Json.getInt(cachefile, "time");
                long nextLoad = ((lastLoad / 86400) * 86400) + time;

                long overdue = nowsecs - nextLoad;
            }
        }

        return nextItem;
    }

    public static void autoUpdate()
    {
        if (nextLoadTime == 0)
        {
        }


        long now = Simple.nowAsTimeStamp();

    }
}
