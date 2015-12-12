package de.xavaro.android.safehome;

import android.os.Handler;
import android.support.annotation.Nullable;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

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

    private static final Handler handler = new Handler();

    private static ServerSocket proxySocket;
    private static int proxyPort;
    private static String proxyUrl;

    private static CommonCallback callback;
    private static MediaPlayer audioPlayer;
    private static boolean running;
    private static Context context;

    public void setCallback(CommonCallback context)
    {
        callback = context;
    }

    public void setAudioUrl(Context context, String url)
    {
        this.context = context;

        proxyUrl = url;

        //
        // Initialize and start proxy server.
        //

        if (proxySocket == null)
        {
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

            running = true;

            //
            // Should run forever.
            //

            start();
        }

        if (audioPlayer == null)
        {
            audioPlayer = new MediaPlayer();
            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        ProxyPlayerStarter startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    @Override
    public void run()
    {
        try
        {
            while (running)
            {
                Log.d(LOGTAG, "Waiting on port " + proxyPort);

                Socket connect = proxySocket.accept();

                Log.d(LOGTAG, "Accepted connection on port " + proxyPort);

                ProxyPlayerWorker worker = new ProxyPlayerWorker(connect);

                worker.run();
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
        catch (IOException ignore)
        {
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
            CommonCallback mycallback = callback;

            synchronized (audioPlayer)
            {
                if (mycallback != null) mycallback.onStartingActivity(null);

                if (audioPlayer.isPlaying())
                {
                    audioPlayer.reset();
                }

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
                }
                catch (IOException ex)
                {
                    OopsService.log(LOGTAG, ex);

                    return;
                }

                audioPlayer.start();

                if (mycallback != null) mycallback.onFinishedActivity(null);
            }
        }
    };

    //
    // Thread startet on each individual connection.
    //
    private class ProxyPlayerWorker extends Thread
    {
        private final String LOGTAG = ProxyPlayerWorker.class.getSimpleName();

        private int icymetaint;

        private String proxiurl;
        private Socket proxiconn;
        private InputStream proxy_in;
        private OutputStream proxy_out;
        private HttpURLConnection connection;

        public ProxyPlayerWorker(Socket connect) throws IOException
        {
            proxiconn = connect;

            proxy_in =  proxiconn.getInputStream();
            proxy_out = proxiconn.getOutputStream();
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

                        if (icymeta != null) Log.d(LOGTAG, "ICY-Meta-String: " + icymeta);

                        chunk = 0;
                    }
                }
            }
            catch (IOException ignore)
            {
                //
                // Silently ignore and exit thread.
                //
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
}