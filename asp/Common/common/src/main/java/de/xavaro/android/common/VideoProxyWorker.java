package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoProxyWorker extends Thread
{
    /*
    private final String LOGTAG = VideoProxyWorker.class.getSimpleName();

    //
    // Fake content length > 24h for broadcasts.
    //
    private final long fakeContentLength = 99999999L;

    //
    // Request properties.
    //

    private boolean running;

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
        running = true;
    }

    public void terminate()
    {
        running = false;
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

                String response = "HTTP/1.1 404 Not found";

                requestOutput.write("HTTP/1.1 404 Not found\r\n".getBytes());
                requestOutput.write("\r\n".getBytes());
                requestOutput.flush();

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

                while (running)
                {
                    while (nextFragment == null)
                    {
                        readFragments();

                        if (nextFragment != null) break;

                        StaticUtils.sleep(5000);

                        if (! running) break;
                    }

                    if (!running) break;

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

                    try
                    {
                        while (running)
                        {
                            xfer = fraginput.read(buffer, 0, buffer.length);
                            if (xfer < 0) break;

                            requestOutput.write(buffer, 0, xfer);
                            requestOutput.flush();

                            total += xfer;
                            fragt += xfer;
                        }
                    }
                    catch (Exception ignore)
                    {
                        Log.d(LOGTAG, "workOnVideo: player closed connect...");

                        fraginput.close();
                        break;
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

            while (running)
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

    private String elmostring;
    private String elmoreferrer;

    private boolean readWebpage()
    {
        String lines = readContent(requestUrl);
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

        elmostring = null;
        elmoreferrer = null;

        if (requestUrl.endsWith(".html") && ! readWebpage()) return false;

        ArrayList<VideoStream> streamOptions = new ArrayList<>();

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

            VideoStream so = new VideoStream();

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

            VideoStream so = new VideoStream();
            so.streamUrl = requestUrl;
            so.quality = VideoQuality.LQ;

            streamOptions.add(so);
        }

        //
        // Preset a medium quality and choose the best
        // fitting quality the user desired.
        //

        int currentOption = streamOptions.size() >> 2;
        int desiredQuality = VideoProxy.getInstance().getDesiredQuality();

        if (desiredQuality > 0)
        {
            int currentQuality   = 0;
            int currentBandwidth = 0;

            for (int inx = 0; inx < streamOptions.size(); inx++)
            {
                VideoStream so = streamOptions.get( inx );

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

        if (elmostring != null) SimpleRequest.doHTTPGet(elmostring, elmoreferrer);

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

            Log.d(LOGTAG, "readFragments: at end");

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
    */
}