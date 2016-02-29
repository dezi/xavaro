package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
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
        return getCacheFile(webappname, url, interval, null);
    }

    public static WebAppCacheResponse getCacheFile(
            String webappname, String url, int interval, String agent)
    {
        Simple.removePost(freeMemory);
        getStorage();

        //
        // Modify webapp server urls to be w/o web server root
        // to allow cache writing independendly of developer.
        //

        String cacheurl = url;

        if (url.startsWith(WebApp.getHTTPAppRoot(webappname)))
        {
            cacheurl = url.substring(WebApp.getHTTPAppRoot(webappname).length());
        }

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

        byte[] content = null;
        String mimetype = null;
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

                long get = Simple.getTimeStamp(lget);
                long now = Simple.nowAsTimeStamp();
                long age = (now - get) / 1000;

                if (age < (interval * 3600))
                {
                    //
                    // Entry is within age.
                    //

                    content = Simple.readBinaryFile(cfile);
                    mimetype = Json.getString(cachefile, "mime");

                    Log.d(LOGTAG,"getCacheFile: HIT=" + age + "=" + mimetype + "=" + url);
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

            if (interval >= 0)
            {
                //
                // Register offline copy with cache.
                //

                Json.put(cachefiles, cacheurl, cachefile);
                dirty = true;
            }
        }

        if (content == null)
        {
            content = getContentFromServer(webappname, url, cachefile, interval < 0 ? agent : null);

            if ((content != null) && (uuid != null))
            {
                //
                // We have read something from the server.
                // Store content into cache file.
                //

                File cfile = new File(cachedir, uuid);
                Simple.writeBinaryFile(cfile, content);
                mimetype = Json.getString(cachefile, "mime");

                Log.d(LOGTAG, "getCacheFile: GET=" + mimetype + "=" + url);
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

                Log.d(LOGTAG, "getCacheFile: NOM=" + mimetype + "=" + url);
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

            if (interval >= 0)
            {
                Json.put(cachefile, "ival", interval);
                Json.put(cachefile, "luse", Simple.nowAsISO());
                dirty = true;
            }
        }
        else
        {
            Log.d(LOGTAG, "getCacheFile: ERR=" + url);
        }

        Simple.makePost(freeMemory, 10 * 1000);

        //
        // Adjust mimetype and encoding.
        //

        String encoding = "UTF-8";

        if ((mimetype != null) && mimetype.contains("; charset="))
        {
            String[] mparts = mimetype.split("; charset=");

            if (mparts.length == 2)
            {
                mimetype = mparts[ 0 ];
                encoding = mparts[ 1 ];
            }
        }

        return new WebAppCacheResponse(mimetype, encoding, content);
    }

    @Nullable
    private static byte[] getContentFromServer(
            String webappname, String src, JSONObject cachefile, String agent)
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

            if (agent == null)
            {
                //
                // Set simple agent and referer for our own server logs.
                //

                connection.setRequestProperty("Referer", webappname);
                connection.setRequestProperty("User-Agent", "Xavaro-" + Simple.getAppName());
            }
            else
            {
                connection.setRequestProperty("User-Agent", agent);
            }

            connection.setConnectTimeout(4000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED)
            {
                //
                // Outdated file is still valid. Mark with recent fetch date.
                //

                Json.put(cachefile, "lget", Simple.nowAsISO());
                dirty = true;

                return null;
            }

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                //
                // File cannot be loaded.
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
    private static JSONObject nextLoadItem;

    @Nullable
    private static JSONObject getNextLoadItem()
    {
        Simple.removePost(freeMemory);
        getStorage();

        long nowsecs = Simple.nowAsTimeStamp() / 1000;
        long todaysecs = (nowsecs / 86400) * 86400;

        JSONObject nextItem = null;
        long nextDue = Long.MAX_VALUE;

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
                int ivalsecs = ival * 3600;
                int time = Json.getInt(cachefile, "time") % ivalsecs;

                String lget = Json.getString(cachefile, "lget");
                long lastLoad = (lget == null) ? todaysecs : (Simple.getTimeStamp(lget) / 1000);
                long nextLoad = ((lastLoad / 86400) * 86400) + time;

                while (nextLoad < nowsecs) nextLoad += ivalsecs;
                if ((nextLoad - lastLoad) <= ivalsecs) nextLoad += ivalsecs;

                long secstoload = nextLoad - nowsecs;

                if (secstoload < nextDue)
                {
                    nextItem = new JSONObject();

                    Json.put(nextItem, "webappname", webappname);
                    Json.put(nextItem, "nextload", Simple.timeStampAsISO(nextLoad * 1000L));
                    Json.put(nextItem, "ival", ival);
                    Json.put(nextItem, "url", url);

                    nextDue = secstoload;
                }
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);

        return nextItem;
    }

    public static void commTick()
    {
        if (nextLoadTime > (Simple.nowAsTimeStamp() / 1000)) return;

        if (nextLoadItem == null)
        {
            nextLoadItem = getNextLoadItem();

            if (nextLoadItem == null)
            {
                nextLoadTime = (Simple.nowAsTimeStamp() / 1000) + 20;

                return;
            }
        }

        long nextload = Simple.getTimeStamp(Json.getString(nextLoadItem, "nextload"));
        String webappname = Json.getString(nextLoadItem, "webappname");
        String url = Json.getString(nextLoadItem, "url");
        int ival = Json.getInt(nextLoadItem, "ival");

        long secondsdue = (nextload - Simple.nowAsTimeStamp()) / 1000;

        if (secondsdue > 0)
        {
            Log.d(LOGTAG, "commTick: wait=" + webappname + "=" + ival + "=" + secondsdue + "=" + url);

            nextLoadTime = (Simple.nowAsTimeStamp() / 1000) + secondsdue;
        }
        else
        {
            if ((url != null) && ! url.startsWith("http:"))
            {
                url = WebApp.getHTTPAppRoot(webappname) + url;
            }

            Log.d(LOGTAG, "commTick: load=" + webappname + "=" + ival + "=" + secondsdue + "=" + url);

            getCacheFile(webappname, url, ival);

            nextLoadItem = null;
            nextLoadTime = 0;
        }
    }
}
