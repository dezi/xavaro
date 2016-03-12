package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

//
// Proxy media player class with icy metadata detection
// in audio streams.
//
@SuppressWarnings("InfiniteLoopStatement")
public class ProxyPlayer extends Thread
{
    private static final String LOGTAG = ProxyPlayer.class.getSimpleName();

    //region Contructor and static instance startup.

    private static ProxyPlayer proxiPlayer;

    public static ProxyPlayer getInstance()
    {
        if (proxiPlayer == null) proxiPlayer = new ProxyPlayer();

        return proxiPlayer;
    }

    private ProxyPlayer()
    {
        //
        // Initialize and start proxy server.
        //

        try
        {
            proxySocket = new ServerSocket(0);
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);

            return;
        }

        proxyPort = proxySocket.getLocalPort();

        //
        // Should run forever.
        //

        start();

        handler = new Handler();
        mediaPlayer = new MediaPlayer();
    }

    //endregion

    private ServerSocket proxySocket;
    private int proxyPort;

    private String proxyUrl;
    private boolean proxyIsVideo;
    private boolean proxyIsAudio;

    private String mediaFile;
    private boolean mediaIsVideo;
    private boolean mediaIsAudio;

    private Callback calling;
    private Callback playing;

    private MediaPlayer mediaPlayer;
    private Context context;
    private Handler handler;

    //region Setter methods.

    public void setAudioUrl(Context ctx, String url, Callback caller)
    {
        context = ctx;
        calling = caller;
        proxyUrl = url;

        proxyIsVideo = false;
        proxyIsAudio = true;
        mediaIsAudio = false;
        mediaIsVideo = false;

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    public void setVideoUrl(Context ctx, String url, Callback caller)
    {
        context = ctx;
        calling = caller;
        proxyUrl = url;

        proxyIsVideo = true;
        proxyIsAudio = false;
        mediaIsAudio = false;
        mediaIsVideo = false;

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    public void setVideoFile(Context ctx, String file, Callback caller)
    {
        context = ctx;
        calling = caller;
        mediaFile = file;

        proxyIsVideo = false;
        proxyIsAudio = false;
        mediaIsAudio = false;
        mediaIsVideo = true;

        ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    public void setDisplay(SurfaceHolder holder)
    {
        mediaPlayer.setDisplay(holder);
    }

    //endregion

    //region Control methods.

    public boolean isPlaying()
    {
        return (mediaPlayer != null) && mediaPlayer.isPlaying();
    }

    public void playerPause()
    {
        if (mediaPlayer != null)
        {
            //
            // In this state the player still consumes
            // stream data from internet!!!
            //

            mediaPlayer.pause();

            playing.onPlaybackPaused();

            //
            // Schedule a full stop within limited time.
            //

            handler.postDelayed(stopMediaPlayer,10 * 1000);
        }
    }

    private final Runnable stopMediaPlayer = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "stopMediaPlayer");

            mediaPlayer.reset();
            mediaPrepared = false;
        }
    };

    public void playerResume()
    {
        if (mediaPlayer != null)
        {
            handler.removeCallbacks(stopMediaPlayer);

            if (mediaPrepared)
            {
                //
                // Soft restart.
                //

                mediaPlayer.start();
            }
            else
            {
                //
                // Full restart on media player.
                //

                ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
                startPlayer.start();
            }

            playing.onPlaybackResumed();
        }
    }

    public void playerRestart()
    {
        if (mediaPlayer != null)
        {
            handler.removeCallbacks(stopMediaPlayer);

            if (mediaPrepared)
            {
                mediaPlayer.reset();
                mediaPrepared = false;
            }

            //
            // Full restart on media player.
            //

            ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
            startPlayer.start();
        }
    }

    public void playerReset()
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.reset();
            mediaPrepared = false;
        }

        if (playing != null) playing.onPlaybackFinished();
    }

    public void setCurrentQuality(int quality)
    {
        desiredQuality = quality;
    }

    public int getCurrentQuality()
    {
        return (streamOptions == null) ? 0 : streamOptions.get(currentOption).quality;
    }

    public int getAvailableQualities()
    {
        int mask = 0;

        if (streamOptions != null)
        {
            for (DitUndDat.StreamOptions so : streamOptions)
            {
                mask |= so.quality;
            }
        }

        return mask;
    }

    //endregion Control methods.

    //region Proxy server thread.

    @Override
    public void run()
    {
        try
        {
            //
            // Proxy HTTP server loop.
            //

            while (true)
            {
                Log.d(LOGTAG, "Waiting on port " + proxyPort);

                Socket connect = proxySocket.accept();

                Log.d(LOGTAG, "Accepted connection on port " + proxyPort);

                ProxyPlayerWorker worker = new ProxyPlayerWorker(connect);

                worker.start();
            }
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        try
        {
            proxySocket.close();
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
        finally
        {
            proxySocket = null;
        }
    }

    //endregion Proxy server thread.

    //region ProxyPlayerStarter Class.

    //
    // Start mediaplayer asynchronously.
    //

    private class ProxyPlayerStarter extends Thread
    {
        @Override
        public void run()
        {
            Callback current = calling;

            synchronized (LOGTAG)
            {
                //
                // Kill mediaplayer and inform callback for
                // current controller.
                //

                mediaPlayer.reset();
                mediaPrepared = false;

                if (playing != null)
                {
                    playing.onPlaybackFinished();
                    playing = null;
                }

                //
                // Inform current controller about preparing.
                //

                if (current != null) current.onPlaybackPrepare();

                //
                // Set header field and direct mediaplayer to proxy.
                //

                Map<String, String> headers = new HashMap<>();

                if (proxyIsAudio) headers.put("AudioProxy-Url", proxyUrl);
                if (proxyIsVideo) headers.put("VideoProxy-Url", proxyUrl);

                if (proxyIsAudio || proxyIsVideo)
                {
                    try
                    {
                        int rnd = new Random().nextInt();
                        Uri uri = Uri.parse("http://127.0.0.1:" + proxyPort + "/?rnd=" + rnd);

                        mediaPlayer.setDataSource(context, uri, headers);
                    }
                    catch (IOException ex)
                    {
                        OopsService.log(LOGTAG, ex);

                        return;
                    }

                    //
                    // Inform all upcomming thread workers of what
                    // we want to play. Used for video only.
                    //

                    desiredUrl = proxyUrl;
                    desiredNextFragment = null;
                }

                if (mediaIsAudio || mediaIsVideo)
                {
                    try
                    {
                        mediaPlayer.setDataSource(mediaFile);
                    }
                    catch (IOException ex)
                    {
                        OopsService.log(LOGTAG, ex);

                        return;
                    }
                }

                //
                // Try to prepare stream.
                //

                try
                {
                    mediaPlayer.prepare();
                    mediaPrepared = true;
                }
                catch (IOException ex)
                {
                    OopsService.log(LOGTAG, ex);

                    return;
                }

                mediaPlayer.start();

                if (current != null) current.onPlaybackStartet();

                playing = current;
            }
        }
    }

    //endregion

    //region ProxyPlayerWorker class.

    //
    // Service variables from outer instance. Used
    // for continuity in partial requests from
    // m.f. Android MediaPlayer to make sure the
    // partial stream exactly fits the previous
    // request.
    //

    private String desiredUrl;
    private String desiredNextFragment;
    private int desiredQuality;

    private ArrayList<DitUndDat.StreamOptions> streamOptions;
    private int currentOption;

    private boolean mediaPrepared;

    //
    // Thread startet on each individual connection.
    //
    private class ProxyPlayerWorker extends Thread
    {
        private final String LOGTAG = ProxyPlayerWorker.class.getSimpleName();

        //
        // Fake content length > 24h for broadcasts.
        //
        private final long fakeContentLength = 99999999L;

        //
        // Request properties.
        //

        private InputStream requestInput;
        private OutputStream requestOutput;

        private String requestUrl;

        private boolean requestIsAudio;
        private boolean requestIsVideo;

        private boolean requestIsPartial;
        private long partialFrom;
        private long partialToto;

        //
        // Our proxy connection to real stream server.
        //

        private HttpURLConnection connection;

        //
        // Audio stuff.
        //

        private int icymetaint;

        //
        // Video stuff.
        //

        private String lastFragment;
        private String nextFragment;

        public ProxyPlayerWorker(Socket connect) throws IOException
        {
            requestInput = connect.getInputStream();
            requestOutput = connect.getOutputStream();
        }

        @Override
        public void run()
        {
            try
            {
                //
                // Read headers to obtain real url and request mode.
                //

                readHeaders();

                if (requestUrl != null)
                {
                    Log.d(LOGTAG, "#########################=" + mediaPrepared + " " + partialFrom + "=>" + partialToto);

                    if (requestIsPartial && ! mediaPrepared)
                    {
                        //
                        // MediaPLayer fucks around with partial requests
                        // to test capability on seeking. We are not
                        // on a file so we behave a little bit stupid
                        // to speed things up.
                        //

                        String goodbye = "HTTP/1.1 200 Ok\r\n"
                                + "Content-Type: text/plain\r\n"
                                + "Content-Length: 0\r\n"
                                + "\r\n";

                        requestOutput.write(goodbye.getBytes());

                        Log.d(LOGTAG, "partial request at prepare early exit");
                        Log.d(LOGTAG, "-------------------------------------");
                    }
                    else
                    {
                        if (requestIsAudio) workOnAudio();
                        if (requestIsVideo) workOnVideo();
                    }
                }

                requestInput.close();
                requestOutput.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        private void workOnVideo()
        {
            long total = 0;

            try
            {
                //
                // Try to read in stream configuration.
                //

                readMaster();

                boolean first = true;

                if (desiredUrl.equals(requestUrl))
                {
                    //
                    // Re-entrance check. MediaPlayer opens
                    // stream serveral times. Prefer to continue
                    // with the last partially streamed fragment.
                    //

                    nextFragment = desiredNextFragment;
                }

                while (true)
                {
                    while (nextFragment == null)
                    {
                        readFragments();

                        if (nextFragment != null) break;

                        StaticUtils.sleep(10000);
                    }

                    Log.d(LOGTAG, "workOnVideo: fragment: " + nextFragment);

                    openUnderscoreConnection(nextFragment);

                    InputStream fraginput = connection.getInputStream();

                    byte[] buffer = new byte[ 32 * 1024 ];
                    int xfer, fragt = 0;

                    if (first)
                    {
                        //
                        // First fragment in request. Prepare header and
                        // eventually skip partial content.
                        //

                        String response = "HTTP/1.1 200 Ok";
                        String contentType = "Content-Type: " + readContentType();
                        String contentLength = "Content-Length: " + fakeContentLength;
                        String contentRange = null;

                        if (requestIsPartial)
                        {
                            response = "HTTP/1.1 206 Partial Content";

                            contentLength = "Content-Length: "
                                    + (fakeContentLength - partialFrom);

                            contentRange = "Content-Range: bytes "
                                    + partialFrom + "-"
                                    + fakeContentLength + "/"
                                    + fakeContentLength;
                        }

                        Log.d(LOGTAG, "workOnVideo First: " + response);
                        Log.d(LOGTAG, "workOnVideo First: " + contentType);
                        Log.d(LOGTAG, "workOnVideo First: " + contentLength);

                        requestOutput.write((response + "\r\n").getBytes());
                        requestOutput.write((contentType + "\r\n").getBytes());
                        requestOutput.write((contentLength + "\r\n").getBytes());

                        if (requestIsPartial)
                        {
                            Log.d(LOGTAG, "workOnVideo First: " + contentRange);
                            requestOutput.write((contentRange + "\r\n").getBytes());
                        }

                        requestOutput.write("\r\n".getBytes());
                        requestOutput.flush();

                        if (requestIsPartial)
                        {
                            //
                            // Skip first bytes.
                            //

                            long max = partialFrom;
                            long want;

                            while (max > 0)
                            {
                                want = (max > buffer.length) ? buffer.length : max;
                                xfer = fraginput.read(buffer, 0, (int) want);
                                if (xfer < 0) break;

                                max -= xfer;
                            }

                            Log.d(LOGTAG, "workOnVideo: skipped partial: " + partialFrom);
                        }

                        first = false;
                    }

                    while (true)
                    {
                        xfer = fraginput.read(buffer, 0, buffer.length);
                        if (xfer < 0) break;

                        requestOutput.write(buffer, 0, xfer);
                        requestOutput.flush();

                        total += xfer;
                        fragt += xfer;
                    }

                    fraginput.close();

                    lastFragment = nextFragment;
                    nextFragment = null;

                    Log.d(LOGTAG, "workOnVideo: fragment close: " + fragt);
                }
            }
            catch (Exception ex)
            {
                Log.d(LOGTAG, "workOnVideo =========> " + ex.getMessage());
            }

            Log.d(LOGTAG, "workOnVideo wrote total:" + total);
            Log.d(LOGTAG, "workOnVideo terminating thread");
        }

        private void workOnAudio()
        {
            try
            {
                openUnderscoreConnection(requestUrl);

                //
                // Obtain icy metadata interval and copy headers to proxy output.
                //

                Map<String, List<String>> headers = connection.getHeaderFields();

                for (String headerKey : headers.keySet())
                {
                    for (String headerValue : headers.get(headerKey))
                    {
                        if ((headerKey != null) && (headerKey.equalsIgnoreCase("icy-metaint")))
                        {
                            icymetaint = Integer.parseInt(headerValue);

                            continue;
                        }

                        String copy = ((headerKey != null) ? headerKey + ": " : "") + headerValue + "\r\n";

                        requestOutput.write(copy.getBytes());
                    }
                }

                requestOutput.write("\r\n".getBytes());
                requestOutput.flush();

                Log.d(LOGTAG, "Icy-Metaint: " + icymetaint);

                //
                // Read stream and copy until any stream closes.
                //

                InputStream shoutcast = connection.getInputStream();

                byte[] buffer = new byte[ 4096 ];
                int xfer, max, chunk = 0;

                while (true)
                {
                    max = (icymetaint > 0) ? icymetaint - chunk : buffer.length;
                    if (max > buffer.length) max = buffer.length;

                    xfer = shoutcast.read(buffer, 0, max);
                    if (xfer < 0) break;

                    requestOutput.write(buffer, 0, xfer);
                    requestOutput.flush();
                    chunk += xfer;

                    if ((icymetaint > 0) && (chunk == icymetaint))
                    {
                        String icymeta = readIcyMeta(shoutcast);

                        if (icymeta != null)
                        {
                            Log.d(LOGTAG, "ICY-Meta-String: " + icymeta);

                            if (playing != null) playing.onPlaybackMeta(icymeta);
                        }

                        chunk = 0;
                    }
                }
            }
            catch (Exception ignore)
            {
                //
                // Silently ignore and exit thread.
                //
            }

            Log.d(LOGTAG, "workOnAudio terminating thread.");
        }

        //
        // Bug workaround:
        //
        // Check for underscore in hostname bug. HttpURLConnection
        // desperately refuses to resolve hosts with underscores.
        // Unfortunately, Akamai fuckers love underscores. RTFM.
        //

        private void openUnderscoreConnection(String src) throws Exception
        {
            URL url = new URL(src);

            String host = null;

            if (url.getHost().contains("_"))
            {
                host = url.getHost();

                InetAddress ip = InetAddress.getByName(host);
                src = src.replace(host, ip.getHostAddress());
                url = new URL(src);
            }

            connection = (HttpURLConnection) url.openConnection();

            if (host != null) connection.setRequestProperty("Host", host);
            if (requestIsAudio) connection.setRequestProperty("Icy-MetaData", "1");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();
        }

        @Nullable
        private String[] readLines(String url) throws Exception
        {
            openUnderscoreConnection(url);

            InputStream input = connection.getInputStream();
            StringBuilder string = new StringBuilder();
            byte[] buffer = new byte[ 4096 ];
            int xfer;

            while ((xfer = input.read(buffer)) > 0)
            {
                string.append(new String(buffer, 0, xfer));
            }

            input.close();

            String temp = string.toString();

            if (temp.contains("\r\n")) return temp.split("\r\n");

            return temp.split("\n");
        }

        private String resolveRelativeUrl(String baseurl, String streamurl)
        {
            if (! (streamurl.startsWith("http:") || streamurl.startsWith("https:")))
            {
                //
                // Releative fragment urls.
                //

                String prefix = baseurl;

                if (prefix.lastIndexOf("/") > 0)
                {
                    prefix = prefix.substring(0,prefix.lastIndexOf("/"));
                }

                while (streamurl.startsWith("../"))
                {
                    streamurl = streamurl.substring(3);
                    prefix = prefix.substring(0,prefix.lastIndexOf("/"));
                }

                streamurl = prefix + "/" + streamurl;
            }

            return streamurl;
        }

        private void readMaster() throws Exception
        {
            Log.d(LOGTAG,"readMaster: " + requestUrl);

            streamOptions = new ArrayList<>();

            String[] lines = readLines(requestUrl);

            if (lines != null)
            {
                Log.d(LOGTAG, "readMaster: " + lines.length);

                for (int inx = 0; (inx + 1) < lines.length; inx++)
                {
                    if (!lines[ inx ].startsWith("#EXT-X-STREAM-INF:")) continue;

                    String line = lines[ inx ].substring(18);

                    String width = Simple.getMatch("RESOLUTION=([0-9]*)x", line);
                    String height = Simple.getMatch("RESOLUTION=[0-9]*x([0-9]*)", line);
                    String bandwith = Simple.getMatch("BANDWIDTH=([0-9]*)", line);
                    String streamurl = lines[ ++inx ];

                    if ((bandwith == null) || (streamurl == null))
                    {
                        continue;
                    }

                    if (streamurl.contains("akamaihd.net/") && streamurl.contains("av-b.m3u8"))
                    {
                        //
                        // My guess: these are interlaced variants we do not need.
                        //

                        continue;
                    }

                    streamurl = resolveRelativeUrl(requestUrl, streamurl);

                    DitUndDat.StreamOptions so = new DitUndDat.StreamOptions();

                    so.width = (width == null) ? 0 : Integer.parseInt(width);
                    so.height = (height == null) ? 0 : Integer.parseInt(height);

                    so.bandWidth = Integer.parseInt(bandwith);
                    so.streamUrl = streamurl;

                    so.quality = DitUndDat.VideoQuality.deriveQuality(so.height);

                    streamOptions.add(so);

                    Log.d(LOGTAG, "readMaster: Live-Stream: " + so.width + "x" + so.height + " bw=" + so.bandWidth);
                }
            }

            if (streamOptions.size() == 0)
            {
                //
                // Nothing found, so add original url as stream.
                //

                DitUndDat.StreamOptions so = new DitUndDat.StreamOptions();
                so.streamUrl = requestUrl;
                so.quality = DitUndDat.VideoQuality.LQ;

                streamOptions.add(so);
            }

            //
            // Preset a medium quality and choose the best
            // fitting quality the user desired.
            //

            currentOption = streamOptions.size() >> 2;

            if (desiredQuality > 0)
            {
                int currentQuality   = 0;
                int currentBandwidth = 0;

                for (int inx = 0; inx < streamOptions.size(); inx++)
                {
                    DitUndDat.StreamOptions so = streamOptions.get( inx );

                    if ((so.quality <= desiredQuality)
                            && (so.quality >= currentQuality)
                            && (so.bandWidth >= currentBandwidth))
                    {
                        currentOption = inx;
                        currentQuality = so.quality;
                        currentBandwidth = so.bandWidth;
                    }
                }
            }
        }

        private void readFragments() throws Exception
        {
            //
            // If no fragments have been processed yet,
            // return the n-th last fragment from list.
            //
            // If fragments have been processed, return
            // next fragment after last, if it already
            // available otherwise return null.
            //

            String url = streamOptions.get(currentOption).streamUrl;

            ArrayList<String> frags = new ArrayList<>();
            Boolean next = false;

            String[] lines = readLines(url);

            if (lines != null)
            {
                for (String line : lines)
                {
                    if (line.startsWith("#")) continue;

                    line = resolveRelativeUrl(url, line);

                    frags.add(line);

                    if (next)
                    {
                        nextFragment = line;

                        return;
                    }

                    next = ((lastFragment != null) && lastFragment.equals(line));
                }
            }

            //
            // We go back at most 5 fragments for buffering.
            //

            while (frags.size() > 5) frags.remove(0);

            nextFragment = ((lastFragment == null) && (frags.size() > 0)) ? frags.get(0) : null;

            if (desiredNextFragment == null) desiredNextFragment = nextFragment;
        }

        @Nullable
        private String readIcyMeta(InputStream input) throws IOException
        {
            int bytesToRead = input.read() * 16;

            if (bytesToRead > 0)
            {
                byte[] line = new byte[ bytesToRead ];
                int read = 0;
                while (read != bytesToRead) line[ read++ ] = (byte) input.read();

                return new String(line).trim();
            }

            return null;
        }

        private String readContentType()
        {
            Map<String, List<String>> headers = connection.getHeaderFields();

            for (String headerKey : headers.keySet())
            {
                for (String headerValue : headers.get(headerKey))
                {
                    if ((headerKey != null) && headerKey.equalsIgnoreCase("content-type"))
                    {
                        return headerValue;
                    }
                }
            }

            return "application/unknown";
        }

        private void readHeaders() throws IOException
        {
            byte[] buffer = new byte[ 32000 ];

            int cnt = 0;

            while (! new String(buffer,0,cnt).endsWith("\r\n\r\n"))
            {
                if (requestInput.read(buffer, cnt++, 1) < 0) break;
            }

            String[] lines = new String(buffer,0,cnt).split("\r\n");

            for (String line : lines)
            {
                Log.d(LOGTAG,"Header: " + line);

                if (line.startsWith("Range:"))
                {
                    requestIsPartial = true;

                    String from = Simple.getMatch("bytes=([0-9]*)-", line);
                    String toto = Simple.getMatch("bytes=[0-9]*-([0-9]*)", line);

                    if ((from != null) && (from.length() > 0)) partialFrom = Long.parseLong(from);
                    if ((toto != null) && (toto.length() > 0)) partialToto = Long.parseLong(toto);
                }

                if (line.startsWith("AudioProxy-Url:"))
                {
                    requestUrl = line.substring(15).trim();
                    requestIsAudio = true;
                }

                if (line.startsWith("VideoProxy-Url:"))
                {
                    requestUrl = line.substring(15).trim();
                    requestIsVideo = true;
                }
            }
        }
    }

    public interface Callback
    {
        void onPlaybackPrepare();
        void onPlaybackStartet();
        void onPlaybackPaused();
        void onPlaybackResumed();
        void onPlaybackFinished();

        void onPlaybackMeta(String meta);
    }

    //endregion
}