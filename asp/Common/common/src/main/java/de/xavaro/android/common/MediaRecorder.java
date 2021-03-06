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
                Log.d(LOGTAG, "handleEvent: is new");

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
                        Json.equals(event, "country", oldevent) &&
                        Json.equals(event, "channel", oldevent))
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
                    recordingsList.add(recording);
                }

                String stoptime = Json.getString(recording, "datestop");

                if ((stoptime != null) && (stopnow.compareTo(stoptime) > 0))
                {
                    Log.d(LOGTAG, "recorderThread: einer fettig....");

                    synchronized (recordingsList)
                    {
                        recordingsList.remove(recording);
                    }

                    completeEvent(recording, true);
                    continue;
                }

                if (! dosomeRecording(recording))
                {
                    Log.d(LOGTAG, "recorderThread: einer kaputt....");

                    synchronized (recordingsList)
                    {
                        recordingsList.remove(recording);
                    }

                    completeEvent(recording, false);
                    continue;
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

        if ((starttime == null) || (channel == null) || (country == null) || (type == null))
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

        Log.d(LOGTAG, "dosomeRecording: " + mediafile);
        Log.d(LOGTAG, "dosomeRecording: " + iptvplaylist);

        while (true)
        {
            String elmostring = Json.getString(recording, "elmostring");
            String elmoreferrer = Json.getString(recording, "elmoreferrer");

            if (elmostring != null)
            {
                //
                // Update specific website if required.
                //

                SimpleRequest.doHTTPGet(elmostring, elmoreferrer);
            }

            String[] playlist = SimpleRequest.readLines(iptvplaylist);
            if ((playlist == null) || (playlist.length == 0)) return false;

            Log.d(LOGTAG, "dosomeRecording: loaded playlist:" + playlist.length);

            String lastChunk = Json.getString(recordstatus, "lastchunk");
            String lastline;

            int startindex = -1;
            float lastlength = 0;

            //
            // Build chunk array and identify the last chunk
            // index.
            //

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

                lastline = MediaStreamMaster.resolveRelativeUrl(masterurl, line);

                chunks.add(lastline);
                length.add(lastlength);

                if ((lastChunk != null) && lastChunk.equals(lastline))
                {
                    startindex = chunks.size();
                }
            }

            if (startindex < 0)
            {
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

                    startindex = chunks.size() - 1;

                    long startsecs = Simple.getTimeStamp(starttime) / 1000;
                    long nowsecs = Simple.nowAsTimeStamp() / 1000;
                    float secondslost = nowsecs - startsecs;

                    while ((secondslost > 0) && (startindex > 0))
                    {
                        secondslost -= length.get(startindex--);
                    }

                    Log.d(LOGTAG, "dosomeRecording: initial=" + startindex + "/" + chunks.size());
                }
            }

            int loaded = 0;

            for (int loadinx = startindex; loadinx < chunks.size(); loadinx++)
            {
                if (! appendIPTVStream(recordstatus, mediafile, chunks.get(loadinx)))
                {
                    return false;
                }

                Json.putFileContent(metafile, metadata);

                loaded++;
            }

            //
            // We leave the loop when all required
            // chunks have been loaded. If there were
            // more than one chunks loaded, we have a
            // backlog and need to fetch an updated
            // playlist.
            //

            if (loaded <= 1) break;
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
            Json.put(recordstatus, "lastsize", Long.toString(lastSizeLong));
            Json.put(recordstatus, "chunkcount", chunkcount);
            Json.put(recordstatus, "nextimage", nextimage);

            Log.d(LOGTAG, "appendIPTVStream: mediafile=" + mediafile.toString());
            Log.d(LOGTAG, "appendIPTVStream: lastchunk=" + lastchunk);
            Log.d(LOGTAG, "appendIPTVStream: size=" + lastSizeLong + "/" + mediafile.length());
            Log.d(LOGTAG, "appendIPTVStream: cc=" + chunkcount + "/" + nextimage);

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
        String stoptime = Json.getString(recording, "datestop");

        if ((type == null) || (country == null) || (channel == null)
                || (starttime == null) || (stoptime == null))
        {
            return false;
        }

        String channeltag = type + "/" + country + "/" + channel;
        JSONObject iptvchannel = identifyIPTVStream(channeltag);
        if (iptvchannel == null) return false;

        String masterurl = Json.getString(iptvchannel, "videourl");
        MediaStream iptvstream = identifyPlaylist(masterurl);
        if (iptvstream == null) return false;

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
        Json.put(recording, "iptvplaylist", iptvstream.streamUrl);
        Json.put(recording, "elmostring", iptvstream.elmostring);
        Json.put(recording, "elmoreferrer", iptvstream.elmoreferrer);

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
    private static MediaStream identifyPlaylist(String requestUrl)
    {
        Log.d(LOGTAG,"identifyPlaylist: " + requestUrl);

        MediaStreamMaster sm = new MediaStreamMaster(requestUrl, MediaQuality.HD);

        if (! sm.readMaster())
        {
            Log.d(LOGTAG, "identifyPlaylist: No streams found: " + requestUrl);

            return null;
        }

        return sm.getCurrentStream();
    }
}