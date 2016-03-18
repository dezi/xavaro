package de.xavaro.android.common;

import android.support.annotation.Nullable;

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

//
// Proxy media player class with icy metadata detection
// in audio streams.
//
@SuppressWarnings("InfiniteLoopStatement")
public class VideoProxy extends Thread implements MediaPlayer.OnSeekCompleteListener
{
    private static final String LOGTAG = VideoProxy.class.getSimpleName();

    //region Contructor and static instance startup.

    private static VideoProxy proxiPlayer;

    public static VideoProxy getInstance()
    {
        if (proxiPlayer == null) proxiPlayer = new VideoProxy();

        return proxiPlayer;
    }

    private VideoProxy()
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
        mediaPlayer.setOnSeekCompleteListener(this);

        //
        // Initialize video surface.
        //

        VideoSurface.getInstance();
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
    private Handler handler;

    //region Setter methods.

    public void setAudioUrl(String url, Callback caller)
    {
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

    public void setVideoUrl(String url, Callback caller)
    {
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

    public void setVideoFile(String file)
    {
        setVideoFile(file, null);
    }

    public void setVideoFile(String file, Callback caller)
    {
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

    public int getDuration()
    {
        if ((mediaPlayer != null) && mediaPrepared && isLocalFile())
        {
            return mediaPlayer.getDuration();
        }

        return -1;
    }

    public int getCurrentPosition()
    {
        if ((mediaPlayer != null) && mediaPrepared && isLocalFile())
        {
            return mediaPlayer.getCurrentPosition();
        }

        return -1;
    }

    public void setCurrentPosition(int position)
    {
        if ((mediaPlayer != null) && mediaPrepared && isLocalFile())
        {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isLocalFile()
    {
        return mediaIsVideo || mediaIsAudio;
    }

    public boolean isVideo()
    {
        return mediaIsVideo || proxyIsVideo;
    }

    public boolean isAudio()
    {
        return mediaIsAudio || proxyIsAudio;
    }

    //endregion

    public void onSeekComplete (MediaPlayer mp)
    {
        Log.d(LOGTAG,"onSeekComplete:");
    }

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

            if (playing != null) playing.onPlaybackPaused();
            if (isVideo()) VideoSurface.getInstance().onPlaybackPaused();

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

            if (playing != null) playing.onPlaybackResumed();
            if (isVideo()) VideoSurface.getInstance().onPlaybackResumed();
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

        if (calling != null) calling.onPlaybackFinished();
        if (playing != null) playing.onPlaybackFinished();

        if (isVideo()) VideoSurface.getInstance().onPlaybackFinished();

        calling = null;
        playing = null;
    }

    public void setCurrentQuality(int quality)
    {
        desiredQuality = quality;
    }

    public int getCurrentQuality()
    {
        try
        {
            if ((streamOptions == null) || (currentOption < 0)
                    || (currentOption >= streamOptions.size()))
            {
                return 0;
            }

            return streamOptions.get(currentOption).quality;
        }
        catch (Exception ignore)
        {
            //
            // Race condition on reload.
            //
        }

        return 0;
    }

    public int getAvailableQualities()
    {
        int mask = 0;

        if (streamOptions != null)
        {
            for (VideoStreams so : streamOptions)
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

                VideoProxyWorker worker = new VideoProxyWorker(connect);

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

                if (playing != null) playing.onPlaybackFinished();
                VideoSurface.getInstance().onPlaybackFinished();

                //
                // Inform current controller about preparing.
                //

                if (current != null) current.onPlaybackPrepare();
                if (isVideo()) VideoSurface.getInstance().onPlaybackPrepare();

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

                        mediaPlayer.setDataSource(Simple.getAnyContext(), uri, headers);
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
                    if (current != null) current.onPlaybackFinished();
                    VideoSurface.getInstance().onPlaybackFinished();

                    OopsService.log(LOGTAG, ex);

                    return;
                }

                mediaPlayer.start();

                playing = current;

                if (playing != null) playing.onPlaybackStartet();
                if (isVideo()) VideoSurface.getInstance().onPlaybackStartet();
            }
        }
    }

    //endregion

    //region VideoProxyWorker class.

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

    private ArrayList<VideoStreams> streamOptions;
    private int currentOption;

    private boolean mediaPrepared;

    //
    // Thread startet on each individual connection.
    //
    private class VideoProxyWorker extends Thread
    {
        private final String LOGTAG = VideoProxyWorker.class.getSimpleName();

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

        public VideoProxyWorker(Socket connect) throws IOException
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

                if (! readMaster())
                {
                    Log.d(LOGTAG, "=============================== nix drin....");

                    playerReset();
                }
                else
                {
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
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
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
        private String readContent(String url)
        {
            try
            {
                openUnderscoreConnection(url);

                InputStream input = connection.getInputStream();
                StringBuilder string = new StringBuilder();
                byte[] buffer = new byte[ 4096 ];
                int xfer;

                while ((xfer = input.read(buffer)) >= 0)
                {
                    string.append(new String(buffer, 0, xfer));
                }

                input.close();

                return string.toString();
            }
            catch (Exception ignore)
            {
            }

            return null;
        }

        @Nullable
        private String[] readLines(String url)
        {
            try
            {
                openUnderscoreConnection(url);

                InputStream input = connection.getInputStream();
                StringBuilder string = new StringBuilder();
                byte[] buffer = new byte[ 4096 ];
                int xfer;

                while (true)
                {
                    xfer = input.read(buffer);
                    if (xfer < 0) break;
                    string.append(new String(buffer, 0, xfer));
                }

                input.close();

                String temp = string.toString();

                temp = temp.replace("\r\n", "\n");
                temp = temp.replace("\n\n", "\n");
                temp = temp.replace("\r", "\n");

                return temp.split("\n");
            }
            catch (Exception ignore)
            {
            }

            return null;
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

        private boolean readWebpage()
        {
            String lines = readContent(requestUrl);
            if (lines == null) return false;

            //
            // type:'html5', config: { file:'http://lstv3.hls1.stream-server.org/live/orf1@sd/index.m3u8' }
            //

            String stream = Simple.getMatch("type:'html5'.*?file:'([^']*)'", lines);
            Log.d(LOGTAG,"=========================================>stream=" + stream);

            if (stream != null)
            {
                if ((desiredUrl != null) && desiredUrl.equals(requestUrl)) desiredUrl = stream;

                requestUrl = stream;
            }

            return (stream != null);
        }

        private boolean readMaster()
        {
            Log.d(LOGTAG,"readMaster: " + requestUrl);

            if (requestUrl.endsWith(".html") && ! readWebpage()) return false;

            currentOption = -1;
            streamOptions = new ArrayList<>();

            String[] lines = readLines(requestUrl);
            if (lines == null) return false;

            Log.d(LOGTAG, "readMaster: " + lines.length);

            boolean havewidhei = false;

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

                if (havewidhei && ((width == null) || (height == null)))
                {
                    //
                    // Master has at least one stream with width and height
                    // specification. Do not accept streams w/o this because
                    // they are audio versions.
                    //

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

                VideoStreams so = new VideoStreams();

                so.width = (width == null) ? 0 : Integer.parseInt(width);
                so.height = (height == null) ? 0 : Integer.parseInt(height);

                so.bandWidth = Integer.parseInt(bandwith);
                so.streamUrl = streamurl;

                so.quality = VideoQuality.deriveQuality(so.height);

                streamOptions.add(so);

                havewidhei = havewidhei || ((width != null) && (height != null));

                Log.d(LOGTAG, "readMaster: Live-Stream: " + so.width + "x" + so.height + " bw=" + so.bandWidth);
            }

            if (streamOptions.size() == 0)
            {
                //
                // Nothing found, so add original url as stream.
                //

                VideoStreams so = new VideoStreams();
                so.streamUrl = requestUrl;
                so.quality = VideoQuality.LQ;

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
                    VideoStreams so = streamOptions.get( inx );

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

            return true;
        }

        private boolean equalsFragment(String last, String line)
        {
            //
            // Some stream providers put random numbers
            // into playlist url. So only compare the last
            // path element.
            //

            int pos = last.lastIndexOf("/") + 1;
            return line.endsWith(last.substring(pos));
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

            Log.d(LOGTAG, "readFragments: " + url);

            String[] lines = readLines(url);
            if (lines == null) return;

            Log.d(LOGTAG, "readFragments: " + url + "=" + lines.length);
            Log.d(LOGTAG, "readFragments: last=" + lastFragment);

            ArrayList<String> frags = new ArrayList<>();
            Boolean next = false;

            for (String line : lines)
            {
                if (line.startsWith("#")) continue;

                line = resolveRelativeUrl(url, line);

                frags.add(line);

                if (next)
                {
                    nextFragment = line;

                    Log.d(LOGTAG, "readFragments: next=" + nextFragment);

                    return;
                }

                next = ((lastFragment != null) && equalsFragment(lastFragment, line));
            }

            if (next)
            {
                //
                // Playlist is at end. Simply do nothing and wait.
                //

                return;
            }

            if (frags.size() == 0)
            {
                //
                // Playlist is empty.
                //

                return;
            }

            //
            // We go back at most 10 fragments for buffering.
            //

            while (frags.size() > 10) frags.remove(0);
            nextFragment = frags.get(0);

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