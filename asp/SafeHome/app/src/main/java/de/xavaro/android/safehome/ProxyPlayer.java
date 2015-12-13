package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
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
    }

    private  ServerSocket proxySocket;
    private  int proxyPort;
    private  String proxyUrl;

    private  Callbacks calling;
    private  Callbacks playing;

    private  MediaPlayer audioPlayer;
    private  boolean audioPrepared;
    private  boolean running;
    private  Context context;
    private  Handler handler;

    public void setAudioUrl(Context ctx, String url, Callbacks caller)
    {
        context = ctx;
        calling = caller;
        proxyUrl = url;

        if (audioPlayer == null)
        {
            audioPlayer = new MediaPlayer();
            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    public boolean isPlaying()
    {
        return (audioPlayer != null) && audioPlayer.isPlaying();
    }

    public void playerPause()
    {
        if (audioPlayer != null)
        {
            //
            // In this state the player still consumes
            // stream data from internet!!!
            //

            audioPlayer.pause();

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

            audioPlayer.reset();
            audioPrepared = false;
        }
    };

    public void playerResume()
    {
        if (audioPlayer != null)
        {
            handler.removeCallbacks(stopAudioPlayer);

            if (audioPrepared)
            {
                //
                // Soft restart.
                //

                audioPlayer.start();
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

                audioPlayer.reset();

                if (mycallback != null) mycallback.onPlaybackPrepare();

                //
                // Set header field and direct mediaplayer to proxy.
                //

                Map<String, String> headers = new HashMap<>();
                headers.put("AudioProxy-Url", proxyUrl);

                try
                {
                    audioPlayer.setDataSource(context, Uri.parse("http://127.0.0.1:" + proxyPort + "/"), headers);
                }
                catch (IOException ex)
                {
                    OopsService.log(LOGTAG, ex);

                    return;
                }

                try
                {
                    audioPlayer.prepare();
                    audioPrepared = true;

                }
                catch (IOException ex)
                {
                    OopsService.log(LOGTAG, ex);

                    return;
                }

                audioPlayer.start();

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
        private InputStream proxy_in;
        private OutputStream proxy_out;
        private HttpURLConnection connection;

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

                proxiurl = readHeaders(proxy_in);
                if (proxiurl == null) return;

                //
                // Open a straight connection with Icy metadata request.
                //

                connection = (HttpURLConnection) new URL(proxiurl).openConnection();
                connection.setRequestProperty("Icy-MetaData", "1");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.connect();

                //
                // Obtain icy metadata interval and copy headers to proxy output.
                //

                Map<String,List<String>> headers = connection.getHeaderFields();

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
                int xfer,max,chunk = 0;

                while (true)
                {
                    max = (icymetaint > 0) ? icymetaint - chunk : buffer.length;
                    if (max > buffer.length) max = buffer.length;

                    xfer = shoutcast.read(buffer,0,max);
                    if (xfer < 0) break;

                    proxy_out.write(buffer,0,xfer);
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
            catch (IOException ignore)
            {
                //
                // Silently ignore and exit thread.
                //

                Log.d(LOGTAG,"ProxyPlayerWorker: terminating thread.");
            }
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

        private String readHeaders(InputStream input) throws IOException
        {
            String proxiurl = null;

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

                if (line.startsWith("AudioProxy-Url:"))
                {
                    proxiurl = line.substring(15).trim();
                }
            }

            return proxiurl;
        }
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