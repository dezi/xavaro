package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//
// Proxy media player class with icy metadata detection
// in audio streams.
//
@SuppressWarnings("InfiniteLoopStatement")
public class MediaProxy extends Thread implements MediaPlayer.OnSeekCompleteListener
{
    private static final String LOGTAG = MediaProxy.class.getSimpleName();

    //region Contructor and static instance startup.

    private static MediaProxy proxiPlayer;

    public static MediaProxy getInstance()
    {
        if (proxiPlayer == null) proxiPlayer = new MediaProxy();

        return proxiPlayer;
    }

    private MediaProxy()
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

        MediaSurface.getInstance();
    }

    //endregion

    private ProxyPlayerStarter startPlayer;
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

    private String desiredUrl;
    private String desiredNextFragment;
    private int desiredQuality;

    private ArrayList<MediaStream> streamOptions;
    private int currentOption;

    private boolean mediaPrepared;

    //region Setter methods.

    public void setAudioUrl(String url)
    {
        setAudioUrl(url, null);
    }

    public void setAudioUrl(String url, Callback caller)
    {
        calling = caller;
        proxyUrl = url;

        proxyIsVideo = false;
        proxyIsAudio = true;
        mediaIsAudio = false;
        mediaIsVideo = false;

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        startPlayer = new ProxyPlayerStarter();
        startPlayer.start();
    }

    public void setVideoUrl(String url)
    {
        setVideoUrl(url, null);
    }

    public void setVideoUrl(String url, Callback caller)
    {
        calling = caller;
        proxyUrl = url;

        proxyIsVideo = true;
        proxyIsAudio = false;
        mediaIsAudio = false;
        mediaIsVideo = false;

        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        startPlayer = new ProxyPlayerStarter();
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

        startPlayer = new ProxyPlayerStarter();
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

    public boolean isPrepared()
    {
        return mediaPrepared;
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
            if (isVideo()) MediaSurface.getInstance().onPlaybackPaused();

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
            if (isVideo()) MediaSurface.getInstance().onPlaybackResumed();
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

            startPlayer = new ProxyPlayerStarter();
            startPlayer.start();
        }
    }

    public void playerReset()
    {
        if (startPlayer != null)
        {
            if (startPlayer.isAlive())
            {
                startPlayer.interrupt();
                startPlayer = null;
            }
        }

        if (mediaPlayer != null)
        {
            mediaPlayer.reset();
            mediaPrepared = false;
        }

        if (calling != null) calling.onPlaybackFinished();
        if (playing != null) playing.onPlaybackFinished();

        if (isVideo()) MediaSurface.getInstance().onPlaybackFinished();

        calling = null;
        playing = null;
    }

    public void setDesiredQuality(int quality)
    {
        desiredQuality = quality;
    }

    public int getDesiredQuality()
    {
        return desiredQuality;
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
            for (MediaStream so : streamOptions)
            {
                mask |= so.quality;
            }
        }

        return mask;
    }

    public void setStreamOptions(ArrayList<MediaStream> streamOptions, int currentOption)
    {
        this.streamOptions = streamOptions;
        this.currentOption = currentOption;
    }

    @Nullable
    public MediaStream getCurrentStreamOption()
    {
        if ((streamOptions != null) && (currentOption < streamOptions.size()))
        {
            return streamOptions.get(currentOption);
        }

        return null;
    }

    //endregion Control methods.

    //region Proxy server thread.

    private MediaProxyWorker worker;

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

                if (worker != null)
                {
                    if (worker.isAlive())
                    {
                        worker.terminate();
                        worker = null;
                    }
                }

                worker = new MediaProxyWorker(connect);
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
                MediaSurface.getInstance().onPlaybackFinished();

                //
                // Inform current controller about preparing.
                //

                if (current != null) current.onPlaybackPrepare();
                if (isVideo()) MediaSurface.getInstance().onPlaybackPrepare();

                //
                // Check if stream is readable.
                //

                if (proxyIsVideo)
                {
                    MediaStreamMaster sm = new MediaStreamMaster(proxyUrl, desiredQuality);

                    if (! sm.readMaster())
                    {
                        if (current != null) current.onPlaybackFinished();
                        if (isVideo()) MediaSurface.getInstance().onPlaybackFinished();

                        return;
                    }

                    streamOptions = sm.getStreamOptions();
                    currentOption = sm.getCurrentOption();
                }

                if (proxyIsAudio || proxyIsVideo)
                {
                    //
                    // Set header field and direct mediaplayer to proxy.
                    //

                    Map<String, String> headers = new HashMap<>();

                    if (proxyIsAudio) headers.put("AudioProxy-Url", proxyUrl);
                    if (proxyIsVideo) headers.put("VideoProxy-Url", proxyUrl);

                    try
                    {
                        int rnd = new Random().nextInt();
                        Uri uri = Uri.parse("http://127.0.0.1:" + proxyPort + "/?rnd=" + rnd);

                        mediaPlayer.setDataSource(Simple.getAnyContext(), uri, headers);
                    }
                    catch (IOException ex)
                    {
                        OopsService.log(LOGTAG, ex);

                        if (current != null) current.onPlaybackFinished();
                        if (isVideo()) MediaSurface.getInstance().onPlaybackFinished();

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

                        if (current != null) current.onPlaybackFinished();
                        if (isVideo()) MediaSurface.getInstance().onPlaybackFinished();

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

                    if (current != null) current.onPlaybackFinished();
                    if (isVideo()) MediaSurface.getInstance().onPlaybackFinished();

                    return;
                }

                mediaPlayer.start();
                playing = current;

                if (playing != null) playing.onPlaybackStartet();
                if (isVideo()) MediaSurface.getInstance().onPlaybackStartet();
            }

            Log.d(LOGTAG, "ProxyPlayerStarter: done.");
        }
    }

    //endregion

    public interface Callback
    {
        void onPlaybackPrepare();
        void onPlaybackStartet();
        void onPlaybackPaused();
        void onPlaybackResumed();
        void onPlaybackFinished();

        void onPlaybackMeta(String meta);
    }
}