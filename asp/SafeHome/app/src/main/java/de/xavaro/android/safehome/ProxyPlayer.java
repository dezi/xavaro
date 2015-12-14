package de.xavaro.android.safehome;

import android.media.SyncParams;
import android.support.annotation.Nullable;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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

//
// Proxy media player class with icy metadata detection
// in audio streams.
//
public class ProxyPlayer extends Thread
{
    private static final String LOGTAG = ProxyPlayer.class.getSimpleName();

    private static ProxyPlayer proxiPlayer;

    public static ProxyPlayer getInstance()
    {
        if (proxiPlayer == null) proxiPlayer = new ProxyPlayer();

        return proxiPlayer;
    }

    public ProxyPlayer()
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
            OopsService.log(LOGTAG,ex);

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

    private  ServerSocket proxySocket;
    private  int proxyPort;
    private  String proxyUrl;
    private  boolean proxyIsVideo;
    private  boolean proxyIsAudio;

    private  Callbacks calling;
    private  Callbacks playing;

    private  MediaPlayer mediaPlayer;
    private  boolean audioPrepared;
    private  boolean running;
    private  Context context;
    private  Handler handler;

    public void setAudioUrl(Context ctx, String url, Callbacks caller)
    {
        context = ctx;
        calling = caller;
        proxyUrl = url;

        proxyIsVideo = false;
        proxyIsAudio = true;

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    public void setVideoUrl(Context ctx, String url, Callbacks caller)
    {
        context = ctx;
        calling = caller;
        proxyUrl = url;

        proxyIsVideo = true;
        proxyIsAudio = false;

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    public void setDisplay(SurfaceHolder holder)
    {
        mediaPlayer.setDisplay(holder);
    }

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

            handler.postDelayed(stopAudioPlayer,10 * 1000);
        }
    }

    private final Runnable stopAudioPlayer = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "stopAudioPlayer");

            mediaPlayer.reset();
            audioPrepared = false;
        }
    };

    public void playerResume()
    {
        if (mediaPlayer != null)
        {
            handler.removeCallbacks(stopAudioPlayer);

            if (audioPrepared)
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

    @Override
    public void run()
    {
        try
        {
            //
            // Proxy HTTP server loop.
            //

            running = true;

            while (running)
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
        finally
        {
            running = false;
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

    private class ProxyPlayerStarter extends Thread
    {
        @Override
        public void run()
        {
            Callbacks mycallback = calling;

            synchronized (LOGTAG)
            {
                if (playing != null) playing.onPlaybackFinished();

                mediaPlayer.reset();

                if (mycallback != null) mycallback.onPlaybackPrepare();

                //
                // Set header field and direct mediaplayer to proxy.
                //

                Map<String, String> headers = new HashMap<>();

                if (proxyIsAudio) headers.put("AudioProxy-Url", proxyUrl);
                if (proxyIsVideo) headers.put("VideoProxy-Url", proxyUrl);

                try
                {
                    mediaPlayer.setDataSource(context, Uri.parse("http://127.0.0.1:" + proxyPort + "/"), headers);
                }
                catch (IOException ex)
                {
                    OopsService.log(LOGTAG, ex);

                    return;
                }

                try
                {
                    mediaPlayer.prepare();
                    audioPrepared = true;

                }
                catch (IOException ex)
                {
                    OopsService.log(LOGTAG, ex);

                    return;
                }

                mediaPlayer.start();

                if (mycallback != null) mycallback.onPlaybackStartet();

                playing = mycallback;
            }
        }
    }

    //
    // Thread startet on each individual connection.
    //
    private class ProxyPlayerWorker extends Thread
    {
        private final String LOGTAG = ProxyPlayerWorker.class.getSimpleName();

        private int icymetaint;

        private String proxiurl;
        private boolean proxyIsAudio;
        private boolean proxyIsVideo;
        private InputStream proxy_in;
        private OutputStream proxy_out;

        private boolean isPartial;
        private HttpURLConnection connection;

        private ArrayList<StreamOptions> streamOptions;
        private int currentOption;
        private String lastFragment;
        private String nextFragment;

        public ProxyPlayerWorker(Socket connect) throws IOException
        {
            proxy_in = connect.getInputStream();
            proxy_out = connect.getOutputStream();
        }

        @Override
        public void run()
        {
            try
            {
                //
                // Read headers to obtain real url.
                //

                readHeaders(proxy_in);

                Log.d(LOGTAG,"#########################" + audioPrepared);

                /*
                if (isPartial)
                {
                    //
                    // MediaPLayer fucks around with partial requests
                    // to test capability on seeking. We are not
                    // on a file so we behave a little bit stupid
                    // to speed things up.
                    //

                    String goodbye = "HTTP/1.0 200 Ok\r\nContent-Type: text/plain\r\n\r\n";

                    proxy_out.write(goodbye.getBytes());

                    Log.d(LOGTAG, "workOnVideo: partial request exit.");
                }
                else
                */
                {
                    if (proxiurl != null)
                    {
                        if (proxyIsAudio) workOnAudio(proxiurl);
                        if (proxyIsVideo) workOnVideo(proxiurl);
                    }
                }

                proxy_in.close();
                proxy_out.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        private void workOnVideo(String url)
        {
            try
            {
                //
                // Try to read in stream configuration.
                //

                readMaster(url);

                boolean first = true;

                while (true)
                {
                    while (true)
                    {
                        readFragments();

                        if (nextFragment != null) break;

                        StaticUtils.sleep(10000);
                    }

                    Log.d(LOGTAG, "workOnVideo: fragment: " + nextFragment);

                    openUnderscoreConnection(nextFragment);

                    if (first)
                    {
                        Map<String, List<String>> headers = connection.getHeaderFields();

                        for (String headerKey : headers.keySet())
                        {
                            for (String headerValue : headers.get(headerKey))
                            {
                                if ((headerKey != null) && headerKey.equals("Content-Length"))
                                {
                                    headerValue = "" + Long.MAX_VALUE;
                                }

                                if ((headerKey == null)
                                        || headerKey.equalsIgnoreCase("content-length")
                                        || headerKey.equalsIgnoreCase("content-type"))
                                {
                                    String copy = ((headerKey != null) ? headerKey + ": " : "") + headerValue + "\r\n";

                                    proxy_out.write(copy.getBytes());

                                    Log.d(LOGTAG, "First:" + copy.trim());
                                }
                            }
                        }

                        proxy_out.write("\r\n".getBytes());
                        proxy_out.flush();

                        first = false;
                    }

                    InputStream fraginput = connection.getInputStream();

                    byte[] buffer = new byte[ 32 * 1024 ];
                    int xfer,total = 0;

                    while (true)
                    {
                        xfer = fraginput.read(buffer, 0, buffer.length);

                        if (xfer < 0) break;

                        proxy_out.write(buffer, 0, xfer);
                        proxy_out.flush();

                        total += xfer;
                    }

                    fraginput.close();

                    lastFragment = nextFragment;
                    nextFragment = null;

                    Log.d(LOGTAG, "workOnVideo: fragment close: " + total);
                }
            }
            catch (Exception ignore)
            {
                //
                // Silently ignore and exit thread.
                //

                Log.d(LOGTAG, "ProxyPlayerWorker: =========> " + ignore.getMessage());
            }

            Log.d(LOGTAG, "ProxyPlayerWorker: workOnVideo terminating thread.");
        }

        private void workOnAudio(String url)
        {
            try
            {
                openUnderscoreConnection(url);

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

                        proxy_out.write(copy.getBytes());
                    }
                }

                proxy_out.write("\r\n".getBytes());
                proxy_out.flush();

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

                    proxy_out.write(buffer, 0, xfer);
                    proxy_out.flush();
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

            Log.d(LOGTAG, "ProxyPlayerWorker: workOnAudio terminating thread.");
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
            if (proxyIsAudio) connection.setRequestProperty("Icy-MetaData", "1");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();
        }

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

            return string.toString().split("\n");
        }

        private void readMaster(String url) throws Exception
        {
            streamOptions = new ArrayList<>();

            String[] lines = readLines(url);

            for (int inx = 0; (inx + 1) < lines.length; inx++)
            {
                if (! lines[ inx ].startsWith("#EXT-X-STREAM-INF:")) continue;

                String line = lines[ inx ].substring(18);

                String width     = StaticUtils.findDat("RESOLUTION=([0-9]*)x", line);
                String height    = StaticUtils.findDat("RESOLUTION=[0-9]*x([0-9]*),", line);
                String bandwith  = StaticUtils.findDat("BANDWIDTH=([0-9]*),", line);
                String streamurl = lines[ ++inx ];

                if ((width == null) || (height == null) || (bandwith == null) || (streamurl == null))
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

                StreamOptions so = new StreamOptions();

                so.width = Integer.parseInt(width);
                so.height = Integer.parseInt(height);
                so.bandWidth = Integer.parseInt(bandwith);
                so.streamUrl = streamurl;

                streamOptions.add(so);

                Log.d(LOGTAG,"readMaster: Live-Stream: " + width + "x" + height + " bw=" + bandwith);
            }

            if (streamOptions.size() == 0)
            {
                //
                // Nothing found, so add original url as stream.
                //

                StreamOptions so = new StreamOptions();
                so.streamUrl = url;

                streamOptions.add(so);
            }

            currentOption = streamOptions.size() >> 2;
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

            String[] lines = readLines(url);

            ArrayList<String> frags = new ArrayList<>();
            Boolean next = false;

            for (String line : lines)
            {
                if (line.startsWith("#")) continue;

                frags.add(line);

                if (next)
                {
                    nextFragment = line;

                    return;
                }

                next = ((lastFragment != null) && lastFragment.equals(line));
            }

            if (audioPrepared) while (frags.size() > 3) frags.remove(0);

            nextFragment = (lastFragment == null) ? frags.get(0) : null;
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

        private void readHeaders(InputStream input) throws IOException
        {
            proxiurl = null;
            isPartial = false;
            proxyIsAudio = false;
            proxyIsVideo = false;

            byte[] buffer = new byte[ 32000 ];

            int cnt = 0;

            while (! new String(buffer,0,cnt).endsWith("\r\n\r\n"))
            {
                if (input.read(buffer, cnt++, 1) < 0) break;
            }

            String[] lines = new String(buffer,0,cnt).split("\r\n");

            for (String line : lines)
            {
                Log.d(LOGTAG,"Header: " + line);

                if (line.startsWith("Range:"))
                {
                    isPartial = true;
                }

                if (line.startsWith("AudioProxy-Url:"))
                {
                    proxiurl = line.substring(15).trim();
                    proxyIsAudio = true;
                }

                if (line.startsWith("VideoProxy-Url:"))
                {
                    proxiurl = line.substring(15).trim();
                    proxyIsVideo = true;
                }
            }
        }
    }

    private class StreamOptions
    {
        public String streamUrl;

        public int bandWidth;

        public int width;
        public int height;
    }

    public interface Callbacks
    {
        void onPlaybackPrepare();
        void onPlaybackStartet();
        void onPlaybackPaused();
        void onPlaybackResumed();
        void onPlaybackFinished();

        void onPlaybackMeta(String meta);
    }
}