package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class MediaStreamMaster
{
    private static final String LOGTAG = MediaStreamMaster.class.getSimpleName();

    private String requestUrl;

    private String elmostring;
    private String elmoreferrer;

    private ArrayList<MediaStream> streamOptions;
    private int currentOption;
    private int desiredQuality;

    public MediaStreamMaster(String requestUrl, int desiredQuality)
    {
        this.requestUrl = requestUrl;
        this.desiredQuality = desiredQuality;
    }

    @Nullable
    public ArrayList<MediaStream> getStreamOptions()
    {
        return streamOptions;
    }

    public int getCurrentOption()
    {
        return currentOption;
    }

    @Nullable
    public MediaStream getCurrentStream()
    {
        return (streamOptions != null) ? streamOptions.get(currentOption) : null;
    }

    public boolean readMaster()
    {
        Log.d(LOGTAG, "readMaster: " + requestUrl);

        if (requestUrl.endsWith(".html") && ! readWebpage()) return false;

        streamOptions = new ArrayList<>();

        String[] lines = SimpleRequest.readLines(requestUrl);
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

            MediaStream so = new MediaStream();

            so.streamUrl = streamurl;

            so.width = (width == null) ? 0 : Integer.parseInt(width);
            so.height = (height == null) ? 0 : Integer.parseInt(height);

            so.quality = MediaQuality.deriveQuality(so.height);
            so.bandWidth = Integer.parseInt(bandwith);

            so.elmostring = elmostring;
            so.elmoreferrer = elmoreferrer;

            streamOptions.add(so);

            havewidhei = havewidhei || ((width != null) && (height != null));

            Log.d(LOGTAG, "readMaster: url=" + so.streamUrl);
            Log.d(LOGTAG, "readMaster: dim=" + so.width + "x" + so.height + " bw=" + so.bandWidth);
        }

        if (streamOptions.size() == 0)
        {
            //
            // Nothing found, so add original url as stream.
            //

            MediaStream so = new MediaStream();

            so.streamUrl = requestUrl;

            so.quality = MediaQuality.LQ;

            so.elmostring = elmostring;
            so.elmoreferrer = elmoreferrer;

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
                MediaStream so = streamOptions.get( inx );

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

    //
    // Parse master index url from specific website.
    //

    private boolean readWebpage()
    {
        String lines = SimpleRequest.readContent(requestUrl);
        if (lines == null) return false;

        //
        // updateStreamStatistics ('rtl','sd', 'zeus');
        // updateStreamStatistics ('dmax','sd', 'elmo');
        //

        String pm = "updateStreamStatistics[^']*'";

        String sender = Simple.getMatch(pm + "([^']*)'", lines);
        String sdtype = Simple.getMatch(pm + "[^']*','([^']*)'", lines);
        String server = Simple.getMatch(pm + "[^']*','[^']*', '([^']*)'", lines);

        if ((sender != null) && (sdtype != null) && (server != null))
        {
            //
            // Very special server update to get correct streams.
            //

            Log.d(LOGTAG, "===============" + sender);
            Log.d(LOGTAG, "===============" + sdtype);
            Log.d(LOGTAG, "===============" + server);

            //
            // http://elmo.ucount.in/stats/update/custom/lstv/kabel1/sd&callback=?
            // Referer: http://www.live-stream.tv/online/fernsehen/deutsch/kabel1.html
            //

            if (! server.equals("zeus"))
            {
                elmoreferrer = requestUrl;
                elmostring = "http://" + server + ".ucount.in"
                        + "/stats/update/custom/lstv/"
                        + sender + "/" + sdtype
                        + "&callback=?";

                SimpleRequest.doHTTPGet(elmostring, elmoreferrer);
            }
        }

        //
        // type:'html5', config: { file:'http://lstv3.hls1.stream-server.org/live/orf1@sd/index.m3u8' }
        //

        String stream = Simple.getMatch("type:'html5'.*?file:'([^']*)'", lines);
        Log.d(LOGTAG,"=========================================>stream=" + stream);

        if (stream != null) requestUrl = stream;

        return (stream != null);
    }

    public static String resolveRelativeUrl(String baseurl, String streamurl)
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
}
