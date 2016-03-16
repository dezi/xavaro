package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.media.MediaMetadataRetriever;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.io.File;

public class MediaRecorder
{
    private static final String LOGTAG = MediaRecorder.class.getSimpleName();

    private static final ArrayList<JSONObject> recordingsList = new ArrayList<>();
    private static final String eventgroupkey = "media.recorder";
    private static Thread recorder;

    public static void testDat()
    {
        JSONArray events = new JSONArray();
        JSONObject event = new JSONObject();
        events.put(event);

        Json.put(event, "date", "2016-03-12T16:30:00Z");
        Json.put(event, "dateend", "2016-03-12T18:30:00Z");
        Json.put(event, "channel", "Das Erste HD");
        Json.put(event, "country", "de");
        Json.put(event, "type", "tv");

        //handleEvent(events);
    }

    public static void handleEvent(JSONArray events)
    {
        Log.d(LOGTAG, "handleEvent:" + events.length());

        for (int inx = 0; inx < events.length(); inx++)
        {
            JSONObject event = Json.getObject(events, inx);
            if (event == null) continue;

            int oldinx = getIndexFromList(event, false);

            if (oldinx < 0)
            {
                JSONObject recording = Json.clone(event);

                if (! setupIPTVStream(recording))
                {
                    completeEvent(event, false);
                    continue;
                }

                synchronized (recordingsList)
                {
                    recordingsList.add(recording);
                }
            }
        }

        if ((recordingsList.size() > 0) && (recorder == null))
        {
            recorder = new Thread(recorderThread);
            recorder.start();
        }
    }

    private static int getIndexFromList(JSONObject event, boolean remove)
    {
        synchronized (recordingsList)
        {
            for (int inx = 0; inx < recordingsList.size(); inx++)
            {
                JSONObject oldevent = recordingsList.get(inx);

                if (Json.equals(event, "date", oldevent) &&
                        Json.equals(event, "type", oldevent) &&
                        Json.equals(event, "channel", oldevent) &&
                        Json.equals(event, "country", oldevent))
                {
                    if (remove) recordingsList.remove(inx);

                    return inx;
                }
            }
        }

        return -1;
    }

    private static void completeEvent(JSONObject event, boolean success)
    {
        Json.put(event, "completed", true);
        Json.put(event, "success", success);

        EventManager.updateComingEvent(eventgroupkey, event);
    }

    private final static Runnable recorderThread = new Runnable()
    {
        @Override
        public void run()
        {
            while (recordingsList.size() > 0)
            {
                String stopnow = Simple.nowAsISO();

                JSONObject recording;

                synchronized (recordingsList)
                {
                    recording = recordingsList.remove(0);
                }

                String stoptime = Json.getString(recording, "stop");

                if ((stoptime != null) && (stopnow.compareTo(stoptime) > 0))
                {
                    Log.d(LOGTAG, "recorderThread: einer fettig....");

                    completeEvent(recording, true);
                    continue;
                }

                if (! dosomeRecording(recording))
                {
                    Log.d(LOGTAG, "recorderThread: einer kaputt....");

                    completeEvent(recording, false);
                    continue;
                }

                synchronized (recordingsList)
                {
                    recordingsList.add(recording);
                }

                Simple.sleep(4000);
            }

            recorder = null;
        }
    };

    private static boolean dosomeRecording(JSONObject recording)
    {
        String starttime = Json.getString(recording, "date");
        String channel = Json.getString(recording, "channel");
        String country = Json.getString(recording, "country");
        String type = Json.getString(recording, "type");

        if ((channel == null) || (starttime == null) || (country == null) || (type == null))
        {
            //
            // Should not happen, only satisfy compiler.
            //

            return false;
        }

        String metafilepath = Json.getString(recording, "metafile");
        String mediafilepath = Json.getString(recording, "mediafile");
        String iptvplaylist = Json.getString(recording, "iptvplaylist");
        String masterurl = Json.getString(recording, "masterurl");

        if ((metafilepath == null) || (mediafilepath == null)
                || (iptvplaylist == null) || (masterurl == null))
        {
            //
            // Should not happen, only satisfy compiler.
            //

            return false;
        }

        File metafile = new File(metafilepath);
        File mediafile = new File(mediafilepath);

        JSONObject metadata = metafile.exists() ? Json.getFileContent(metafile) : null;
        if (metadata == null) metadata = Json.clone(recording);

        if (! metadata.has("recordstatus")) Json.put(metadata, "recordstatus", new JSONObject());
        JSONObject recordstatus = Json.getObject(metadata, "recordstatus");
        if (recordstatus == null) return false;

        Log.d(LOGTAG, "dosomeRecording:" + mediafile);
        Log.d(LOGTAG, "dosomeRecording:" + iptvplaylist);

        String[] playlist = readLines(iptvplaylist);
        if ((playlist == null) || (playlist.length == 0)) return false;

        Log.d(LOGTAG, "dosomeRecording:" + playlist.length);

        String lastChunk = Json.getString(recordstatus, "lastchunk");
        String lastline;
        float lastlength = 0;

        boolean foundlastchunk = false;

        ArrayList<String> chunks = new ArrayList<>();
        ArrayList<Float> length = new ArrayList<>();

        for (String line : playlist)
        {
            if (line.length() == 0) continue;

            if (line.startsWith("#EXTINF:"))
            {
                int endpos = line.contains(",") ? line.indexOf(",") : line.length();
                lastlength = Float.parseFloat(line.substring(8, endpos));
            }

            if (line.startsWith("#")) continue;

            lastline = resolveRelativeUrl(masterurl, line);

            chunks.add(lastline);
            length.add(lastlength);

            if (foundlastchunk)
            {
                //
                // The recording is old. Append all
                // new chunks to media file.
                //

                if (! appendIPTVStream(recordstatus, mediafile, lastline))
                {
                    return false;
                }

                Json.putFileContent(metafile, metadata);

                continue;
            }

            if ((lastChunk != null) && lastChunk.equals(lastline)) foundlastchunk = true;
        }

        if (! foundlastchunk)
        {
            //
            // The recording is new. Check when the recording started
            // and identify the matching start chunk. This allows to
            // make one touch recording into the past.
            //

            int startindex = chunks.size() - 1;

            if (lastChunk != null)
            {
                //
                // The recording is old, but we did not find
                // the last chunk. We have possibly been restartet,
                // so start with the first available fragment to
                // keep the loss minimal.
                //

                startindex = 0;

                Log.d(LOGTAG, "dosomeRecording: continued=" + startindex + "/" + chunks.size());
            }
            else
            {
                //
                // This is the beginning of a new recording. The User
                // possibly clicked on an allready sending item, so
                // try to go backwards in time as much as possible and
                // required to make the recording complete.
                //

                long startsecs = (Simple.getTimeStamp(starttime) / 1000) - (3 * 60);
                long nowsecs = Simple.nowAsTimeStamp() / 1000;
                float secondslost = nowsecs - startsecs;

                while ((secondslost > 0) && (startindex > 0))
                {
                    secondslost -= length.get(startindex--);
                }

                Log.d(LOGTAG, "dosomeRecording: initial=" + startindex + "/" + chunks.size());
            }

            if (! appendIPTVStream(recordstatus, mediafile, chunks.get(startindex)))
            {
                return false;
            }

            Json.putFileContent(metafile, metadata);
        }

        return true;
    }

    private static void getStillImage(FileDescriptor fd, long offset, long size, File mediafile)
    {
        try
        {
            File jpegfile = Simple.changeExtension(mediafile, ".jpg");

            Log.d(LOGTAG, "getStillImage: start=" + jpegfile.toString());

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(fd, offset, size);
            Bitmap still = retriever.getFrameAtTime();

            FileOutputStream jpeg = new FileOutputStream(jpegfile);
            still.compress(Bitmap.CompressFormat.JPEG, 85, jpeg);
            jpeg.close();

            Log.d(LOGTAG, "getStillImage: finis=" + jpegfile.toString());
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private static boolean appendIPTVStream(JSONObject recordstatus, File mediafile, String lastchunk)
    {
        String lastSize = Json.getString(recordstatus, "lastsize");
        if (lastSize == null) lastSize = "0";
        long lastSizeLong = Long.parseLong(lastSize);

        if (! recordstatus.has("nextimage")) Json.put(recordstatus, "nextimage", 1);
        int nextimage = Json.getInt(recordstatus, "nextimage");
        if (! recordstatus.has("chunkcount")) Json.put(recordstatus, "chunkcount", 0);
        int chunkcount = Json.getInt(recordstatus, "chunkcount");

        try
        {
            HttpURLConnection connection = Simple.openUnderscoreConnection(lastchunk);
            InputStream input = connection.getInputStream();

            RandomAccessFile output = new RandomAccessFile(mediafile, "rw");
            output.seek(lastSizeLong);

            long stillPos = lastSizeLong;

            byte[] buffer = new byte[ 32 * 1024 ];
            int xfer;

            while ((xfer = input.read(buffer)) > 0)
            {
                output.write(buffer, 0, xfer);
                lastSizeLong += xfer;
            }

            chunkcount += 1;

            if (chunkcount == nextimage)
            {
                getStillImage(output.getFD(), stillPos, lastSizeLong - stillPos, mediafile);

                nextimage = nextimage * 2;
            }

            output.close();
            input.close();

            Json.put(recordstatus, "lastchunk", lastchunk);
            Json.put(recordstatus, "lastsize", "" + lastSizeLong);
            Json.put(recordstatus, "chunkcount", chunkcount);
            Json.put(recordstatus, "nextimage", nextimage);

            Log.d(LOGTAG, "appendIPTVStream: mediafile=" + mediafile.toString());
            Log.d(LOGTAG, "appendIPTVStream: lastchunk=" + lastchunk);
            Log.d(LOGTAG, "appendIPTVStream: size=" + lastSizeLong + "/" + mediafile.length());

            return true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    private static boolean setupIPTVStream(JSONObject recording)
    {
        String type  = Json.getString(recording, "type");
        String country = Json.getString(recording, "country");
        String channel = Json.getString(recording, "channel");
        String starttime = Json.getString(recording, "date");
        String stoptime = Json.getString(recording, "dateend");

        if ((type == null) || (country == null) || (channel == null)
                || (starttime == null) || (stoptime == null))
        {
            return false;
        }

        String channeltag = type + "/" + country + "/" + channel;
        JSONObject iptvchannel = identifyIPTVStream(channeltag);
        if (iptvchannel == null) return false;

        String masterurl = Json.getString(iptvchannel, "videourl");
        String iptvplaylist = identifyPlaylist(masterurl);
        if (iptvplaylist == null) return false;

        File mediadir = Simple.getMediaPath("recordings");
        Simple.makeDirectory(mediadir);

        String basename = starttime + "." + type + "." + country + "." + channel;
        basename = basename.replace(":", "-");

        File metafile = new File(mediadir, basename + ".json");
        File mediafile = new File(mediadir, basename + (type.equals("tv") ? ".mp4" : ".mp3"));
        File previewfile = new File(mediadir, basename + ".jpg");

        Json.put(recording, "metafile", metafile.toString());
        Json.put(recording, "mediafile", mediafile.toString());

        if (type.equals("tv")) Json.put(recording, "previewfile", previewfile.toString());

        Json.put(recording, "masterurl", masterurl);
        Json.put(recording, "iptvchannel", iptvchannel);
        Json.put(recording, "iptvplaylist", iptvplaylist);

        Log.d(LOGTAG, "setupIPTVStream: " + Json.defuck(recording.toString()));

        return true;
    }

    @Nullable
    private static JSONObject identifyIPTVStream(String channeltag)
    {
        Log.d(LOGTAG, "identifyIPTVStream:" + channeltag);

        JSONObject iptv = WebLib.getLocaleConfig("iptv");
        if (iptv == null) return null;

        Iterator<String> keysIterator = iptv.keys();

        while (keysIterator.hasNext())
        {
            String website = keysIterator.next();
            JSONObject webitem = Json.getObject(iptv, website);

            if (webitem == null) continue;
            if (! webitem.has("channels")) continue;

            JSONArray channels = Json.getArray(webitem, "channels");
            if (channels == null) continue;

            for (int inx = 0; inx < channels.length(); inx++)
            {
                JSONObject channel = Json.getObject(channels, inx);
                if (channel == null) continue;
                if (! channel.has("aliase")) continue;
                if (! (channel.has("videourl") || channel.has("audiourl"))) continue;

                JSONArray aliase = Json.getArray(channel, "aliase");
                if (aliase == null) continue;

                for (int cnt = 0; cnt < aliase.length(); cnt++)
                {
                    String alias = Json.getString(aliase, cnt);

                    if (Simple.equals(alias, channeltag))
                    {
                        Log.d(LOGTAG, "identifyIPTVStream:" + channeltag + "=" + Json.defuck(channel.toString()));

                        return channel;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private static String identifyPlaylist(String requestUrl)
    {
        Log.d(LOGTAG,"identifyPlaylist: " + requestUrl);

        JSONArray streamOptions = new JSONArray();
        String[] lines = readLines(requestUrl);

        if (lines != null)
        {
            Log.d(LOGTAG, "identifyPlaylist: " + lines.length);

            for (int inx = 0; (inx + 1) < lines.length; inx++)
            {
                if (! lines[ inx ].startsWith("#EXT-X-STREAM-INF:")) continue;

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

                JSONObject so = new JSONObject();

                Json.put(so, "width", (width == null) ? 0 : Integer.parseInt(width));
                Json.put(so, "height", (height == null) ? 0 : Integer.parseInt(height));
                Json.put(so, "bandwith", Integer.parseInt(bandwith));
                Json.put(so, "streamurl", streamurl);

                streamOptions.put(so);

                Log.d(LOGTAG, "identifyPlaylist: Live-Stream: " + width + "x" + height + " bw=" + bandwith);
            }
        }

        if (streamOptions.length() == 0)
        {
            Log.d(LOGTAG, "identifyPlaylist: No streams found: " + requestUrl);

            return null;
        }

        streamOptions = Json.sortInteger(streamOptions, "bandwith", true);
        JSONObject streamOption = Json.getObject(streamOptions, 0);

        return (streamOption != null) ? Json.getString(streamOption, "streamurl") : null;
    }

    private static String resolveRelativeUrl(String baseurl, String streamurl)
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

    @Nullable
    private static String[] readLines(String url)
    {
        try
        {
            HttpURLConnection connection = Simple.openUnderscoreConnection(url);
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
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }
}