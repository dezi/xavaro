package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WebAppCache
{
    private static final String LOGTAG = WebAppCache.class.getSimpleName();

    private static final JSONObject webappsync = new JSONObject();
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
        public final boolean notmodified;

        public WebAppCacheResponse(String mimetype, String encoding, byte[] content, boolean notmodified)
        {
            this.mimetype = mimetype;
            this.encoding = encoding;
            this.content = content;
            this.notmodified = notmodified;
        }
    }

    private static void removeDirectories(File dir)
    {
        try
        {
            if (dir.exists())
            {
                File[] list = dir.listFiles();

                for (File item : list)
                {
                    if (item.isDirectory()) removeDirectories(item);

                    if (!item.delete())
                    {
                        Log.d(LOGTAG, "removeDirectories: failed:" + item);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static void checkWebAppCache()
    {
        String version = Simple.getBetaVersion();
        String lastversion = Simple.getSharedPrefString("system.version");

        if (! Simple.equals(version, lastversion))
        {
            nukeWebAppCache();

            Simple.setSharedPrefString("system.version", version);
        }
    }

    public static void nukeWebAppCache()
    {
        File file = new File(Simple.getExternalCacheDir(), "webappcache");
        removeDirectories(file);

        freeMemory.run();

        File act = new File(Simple.getFilesDir(), "webappcache.act.json");
        File bak = new File(Simple.getFilesDir(), "webappcache.bak.json");

        if (act.delete() && bak.delete())
        {
            Log.d(LOGTAG, "nukeWebAppCache: deleted caches.");
        }
    }

    //
    // Get content from cache. If interval is greater zero reflect age of cached
    // item and get a fresh copy if required. If interval equals zero, always get
    // a fresh copy from server and store for offline usage.
    //

    public static WebAppCacheResponse getCacheFile
            (String webappname, String url, int interval)
    {
        return getCacheFile(webappname, url, interval, null);
    }

    public static WebAppCacheResponse getCacheFile(
            String webappname, String url, int interval, boolean nolastuse)
    {
        return getCacheFile(webappname, url, interval, null, nolastuse);
    }

    public static WebAppCacheResponse getCacheFile(
            String webappname, String url, int interval, String agent)
    {
        return getCacheFile(webappname, url, interval, agent, false);
    }

    private static WebAppCacheResponse getCacheFile(
            String webappname, String url, int interval, String agent, boolean nolastuse)
    {
        Simple.removePost(freeMemory);
        getStorage();

        //
        // Modify webapp server urls to be w/o web server root
        // to allow cache writing independendly of developer.
        //

        String cacheurl = url;

        if (url.startsWith(WebApp.getHTTPRoot()))
        {
            cacheurl = url.substring(WebApp.getHTTPRoot().length());
        }

        JSONObject cachefiles;

        synchronized (webappsync)
        {
            if (! webappcache.has(webappname)) Json.put(webappcache, webappname, new JSONObject());
            cachefiles = Json.getObject(webappcache, webappname);
        }

        if (cachefiles == null)
        {
            Simple.makePost(freeMemory, 10 * 1000);
            return new WebAppCacheResponse(null, null, null, false);
        }

        File cachedir = new File(Simple.getExternalCacheDir(), "webappcache/" + webappname);

        Log.d(LOGTAG, "getCacheFile:======================" + cachedir);

        if (! cachedir.exists())
        {
            if (cachedir.mkdirs())
            {
                Log.d(LOGTAG,"getCacheFile: created=" + cachedir);
            }
            else
            {
                Log.d(LOGTAG,"getCacheFile: failed=" + cachedir);
            }
        }

        byte[] content = null;
        String mimetype = null;
        JSONObject cachefile;
        String uuid;

        if (cachefiles.has(cacheurl))
        {
            cachefile = Json.getObject(cachefiles, cacheurl);

            uuid = Json.getString(cachefile, "uuid");
            mimetype = Json.getString(cachefile, "mime");
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
            //
            // Check if file is present on disk before going into
            // content fetch. If not present, remove the last modified
            // date from entry to avoid failure if the file was not
            // modified since.
            //

            if ((cachefile != null) && (interval >= 0) && (uuid != null))
            {
                File cfile = new File(cachedir, uuid);
                if (! cfile.exists()) Json.remove(cachefile, "lmod");
            }

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

        boolean notmodified = false;

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

                notmodified = true;
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

            if (interval > 1) Json.put(cachefile, "ival", interval);

            if (interval >= 0)
            {
                if (! nolastuse) Json.put(cachefile, "luse", Simple.nowAsISO());
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

        return new WebAppCacheResponse(mimetype, encoding, content, notmodified);
    }

    @Nullable
    private static byte[] getContentFromServer(
            String webappname, String src, JSONObject cachefile, String agent)
    {
        String wifiname = Simple.getWifiName();

        if (Simple.getSharedPrefBoolean("developer.enable") &&
            Simple.getSharedPrefBoolean("developer.webapps.httpbypass." + wifiname))
        {
            String server = WebApp.getHTTPRoot();

            if (src.startsWith(server))
            {
                String httpserver = Simple.getSharedPrefString("developer.webapps.httpserver." + wifiname);
                String httpport = Simple.getSharedPrefString("developer.webapps.httpport." + wifiname);

                String devserver;

                if ((httpport == null) || httpport.equals("80"))
                {
                    devserver = "http://" + httpserver;
                }
                else
                {
                    devserver = "http://" + httpserver + ":" + httpport;
                }

                src = devserver + src.substring(server.length());
                Log.d(LOGTAG, "getContentFromServer: real=" + src);
            }
        }

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

            if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP)
            {
                //
                // A cross protocoll redirect happened from
                // HTTP to HTTPS or vice versa.
                //

                String location = connection.getHeaderField("Location");

                Log.d(LOGTAG, "getContentFromServer: 302=" + location);
                Log.d(LOGTAG, "getContentFromServer: old=" + src);

                url = new URL(location);

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(4000);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.connect();
            }

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

                Log.d(LOGTAG, "getContentFromServer: ERR=" + connection.getResponseCode());

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
            synchronized (webappsync)
            {
                if (webappcache != null)
                {
                    if (dirty) putStorage();

                    webappcache = null;
                }
            }
        }
    };

    private static void getStorage()
    {
        synchronized (webappsync)
        {
            if (webappcache != null) return;

            File file = new File(Simple.getFilesDir(), "webappcache.act.json");
            if (! file.exists()) file = new File(Simple.getFilesDir(), "webappcache.bak.json");

            try
            {
                if (!file.exists())
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
                OopsService.log(LOGTAG, ex);
            }
        }
    }

    private static void putStorage()
    {
        synchronized (webappsync)
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

                    dirty = false;

                    Log.d(LOGTAG, "putStorage: ok=" + ok);
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }
    }

    private static long nextLoadTime;
    private static JSONObject nextLoadItem;

    @Nullable
    private static JSONObject getNextLoadItem()
    {
        Simple.removePost(freeMemory);
        getStorage();

        JSONObject nextItem = null;

        synchronized (webappsync)
        {
            long nowsecs = Simple.nowAsTimeStamp() / 1000;
            long todaysecs = (nowsecs / 86400) * 86400;
            long nextDue = Long.MAX_VALUE;

            Iterator<String> webappsIter = webappcache.keys();

            while (webappsIter.hasNext())
            {
                String webappname = webappsIter.next();

                JSONObject cachefiles = Json.getObject(webappcache, webappname);
                if (cachefiles == null) continue;

                //
                // Process webapp cached urls list.
                //

                ArrayList<String> unusedUrls = new ArrayList<>();
                Iterator<String> cachefilesIter = cachefiles.keys();

                while (cachefilesIter.hasNext())
                {
                    String url = cachefilesIter.next();
                    JSONObject cachefile = Json.getObject(cachefiles, url);
                    if (cachefile == null) continue;

                    String luse = Json.getString(cachefile, "luse");
                    String lget = Json.getString(cachefile, "lget");

                    if ((luse == null) && (lget == null))
                    {
                        //
                        // Entry is somewhat junky.
                        //

                        unusedUrls.add(url);
                        continue;
                    }

                    //
                    // Check if entry is from webapps or weblibs directory.
                    // This is an all or nothing update. Only trigger on
                    // the modification of the manifest.json entry.
                    //

                    if (url.startsWith("/") && !url.endsWith("manifest.json"))
                    {
                        continue;
                    }

                    int ival = Json.getInt(cachefile, "ival");
                    int ivalsecs = ival * 3600;

                    //
                    // Check last usage of item.
                    //

                    if (luse != null)
                    {
                        long lastusage = nowsecs - (Simple.getTimeStamp(luse) / 1000);

                        if (((ivalsecs > 0) && (lastusage >= ivalsecs))
                                || ((ivalsecs == 0) && (lastusage > (3600 * 48))))
                        {
                            //
                            // File was not used within cache interval
                            // or is developer bypass and not used within
                            // 48 hours.
                            //

                            unusedUrls.add(url);
                            continue;
                        }
                    }

                    //
                    // Check last fetch of item.
                    //

                    if (ival == 0) continue;
                    int time = Json.getInt(cachefile, "time") % ivalsecs;

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

                //
                // Process unused list.
                //

                File cachedir = new File(Simple.getExternalCacheDir(), "webappcache/" + webappname);

                for (String url : unusedUrls)
                {
                    JSONObject cachefile = Json.getObject(cachefiles, url);
                    String uuid = Json.getString(cachefile, "uuid");
                    if (uuid == null) continue;

                    File cfile = new File(cachedir, uuid);

                    if (cfile.delete()) Log.d(LOGTAG, "getNextLoadItem: unused/deleted:" + url);

                    Json.remove(cachefiles, url);
                    dirty = true;
                }
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);

        return nextItem;
    }

    public static void revalidateWebapp(String webappname)
    {
        Simple.removePost(freeMemory);
        getStorage();

        synchronized (webappsync)
        {
            JSONObject cachefiles = Json.getObject(webappcache, webappname);
            if (cachefiles == null) return;

            //
            // Process webapp cached urls list.
            //

            Iterator<String> cachefilesIter = cachefiles.keys();

            while (cachefilesIter.hasNext())
            {
                String url = cachefilesIter.next();
                JSONObject cachefile = Json.getObject(cachefiles, url);

                if (cachefile == null) continue;
                if (!url.startsWith("/")) continue;

                url = WebApp.getHTTPRoot() + url;
                getCacheFile(webappname, url, 1, true);
            }
        }

        Simple.makePost(freeMemory, 10 * 1000);
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

        if (url == null)
        {
            nextLoadItem = null;
            nextLoadTime = 0;
        }
        else
        {
            String logline = webappname + "=" + ival + "=" + secondsdue + "=" + url;

            if (secondsdue > 0)
            {
                Log.d(LOGTAG, "commTick: wait=" + logline);

                nextLoadTime = (Simple.nowAsTimeStamp() / 1000) + secondsdue;
            }
            else
            {
                Log.d(LOGTAG, "commTick: load=" + logline);

                if (url.startsWith("/"))
                {
                    url = WebApp.getHTTPRoot() + url;

                    if (url.endsWith("manifest.json"))
                    {
                        //
                        // Manifest check.
                        //

                        WebAppCacheResponse res = getCacheFile(webappname, url, ival, true);

                        if (! res.notmodified)
                        {
                            //
                            // Re-validate / reload all local
                            // cache files for this webapp.
                            //

                            revalidateWebapp(webappname);
                        }
                    }
                    else
                    {
                        //
                        // Should not happen any more.
                        //

                        OopsService.log(LOGTAG, "Webapp local file re-cached on schedule...");
                    }
                }
                else
                {
                    //
                    // File from app data servers or other.
                    //

                    getCacheFile(webappname, url, ival, true);
                }

                nextLoadItem = null;
                nextLoadTime = 0;
            }
        }
    }
}
