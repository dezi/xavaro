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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaProxyWorker extends Thread
{
    private final static String LOGTAG = MediaProxyWorker.class.getSimpleName();

    //
    // Fake content length > 24h for broadcasts.
    //

    private final static long fakeContentLength = 9999999999L;

    //
    // Request properties.
    //

    private boolean running;

    private MediaStream currentOption;
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

    public MediaProxyWorker(Socket connect) throws IOException
    {
        requestInput = connect.getInputStream();
        requestOutput = connect.getOutputStream();

        running = true;
    }

    public void terminate()
    {
        Log.d(LOGTAG, "terminate: ...");

        running = false;
    }

    //region Partial request fragment evaluation.

    private final static Map<String, ArrayList<Fragment>> streamFragments = new HashMap<>();

    private class Fragment
    {
        public String fragment;
        public long offset;

        public Fragment(String fragment, long offset)
        {
            this.fragment = fragment;
            this.offset = offset;
        }
    }

    private void registerFragment(String fragment, long offset)
    {
        synchronized (streamFragments)
        {
            Log.d(LOGTAG, "registerFragment: requestUrl=" + requestUrl);
            Log.d(LOGTAG, "registerFragment: fragment=" + fragment);
            Log.d(LOGTAG, "registerFragment: offset=" + offset);

            if (! streamFragments.containsKey(requestUrl))
            {
                streamFragments.put(requestUrl, new ArrayList<Fragment>());
            }

            ArrayList<Fragment> fragments = streamFragments.get(requestUrl);
            if (fragments.size() > 10) fragments.remove(0);
            fragments.add(new Fragment(fragment, offset));
        }
    }

    private long retrieveFragment()
    {
        synchronized (streamFragments)
        {
            Log.d(LOGTAG, "retrieveFragment: requestUrl=" + requestUrl);
            Log.d(LOGTAG, "retrieveFragment: partialFrom=" + partialFrom);

            if (streamFragments.containsKey(requestUrl))
            {
                ArrayList<Fragment> fragments = streamFragments.get(requestUrl);

                for (int inx = fragments.size() - 1; inx >= 0; inx--)
                {
                    Fragment fragment = fragments.get(inx);

                    if (fragment.offset <= partialFrom)
                    {
                        nextFragment = fragment.fragment;

                        Log.d(LOGTAG, "retrieveFragment: nextFragment=" + nextFragment);
                        Log.d(LOGTAG, "retrieveFragment: partialFrom=" + partialFrom);
                        Log.d(LOGTAG, "retrieveFragment: offset=" + fragment.offset);

                        return fragment.offset;
                    }
                }
            }
        }

        return 0;
    }

    private void clearFragments()
    {
        synchronized (streamFragments)
        {
            Log.d(LOGTAG, "clearFragments: requestUrl=" + requestUrl);

            if (streamFragments.containsKey(requestUrl))
            {
                streamFragments.remove(requestUrl);
            }
        }
    }

    //endregion Partial request fragment evaluation.

    @Override
    public void run()
    {
        try
        {
            //
            // Read headers to obtain real url and request mode.
            //

            if (readHeaders() && (requestUrl != null))
            {
                if (requestIsPartial && ! MediaProxy.getInstance().isPrepared())
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
            currentOption = MediaProxy.getInstance().getCurrentStreamOption();

            if (requestIsPartial)
            {
                //
                // Adjust last fragment to last fully
                // delivered fragment.
                //

                total = retrieveFragment();
            }
            else
            {
                //
                // Reset list of fragments for new request.
                //

                clearFragments();
            }

            boolean first = true;

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

                registerFragment(nextFragment, total);
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

                        long skip = partialFrom - total;
                        long have = 0;
                        long want;

                        while (have < skip)
                        {
                            want = ((skip - have) > buffer.length) ? buffer.length : skip - have;
                            xfer = fraginput.read(buffer, 0, (int) want);
                            if (xfer < 0) break;

                            total += xfer;
                            have += xfer;
                        }

                        Log.d(LOGTAG, "workOnVideo: skipped partial: " + skip);
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

                    running = false;
                }

                Log.d(LOGTAG, "workOnVideo: fragment close: " + fragt);
                Log.d(LOGTAG, "workOnVideo: total so far: " + total);
                fraginput.close();

                lastFragment = nextFragment;
                nextFragment = null;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            Log.d(LOGTAG, "workOnVideo exception: " + ex.getMessage());
        }

        Log.d(LOGTAG, "workOnVideo: wrote total:" + total);
        Log.d(LOGTAG, "workOnVideo: terminating thread");
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

                        //if (playing != null) playing.onPlaybackMeta(icymeta);
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

    private void readFragments()
    {
        if (currentOption.elmostring != null)
        {
            //
            // Update specific website if required.
            //

            SimpleRequest.doHTTPGet(currentOption.elmostring, currentOption.elmoreferrer);
        }

        String url = currentOption.streamUrl;

        //
        // If no fragments have been processed yet,
        // return the n-th last fragment from list.
        //
        // If fragments have been processed, return
        // next fragment after last, if it already
        // available otherwise return null.
        //

        Log.d(LOGTAG, "readFragments: " + url);

        String[] lines = SimpleRequest.readLines(url);
        if (lines == null) return;

        Log.d(LOGTAG, "readFragments: " + url + "=" + lines.length);
        Log.d(LOGTAG, "readFragments: last=" + lastFragment);

        ArrayList<String> frags = new ArrayList<>();
        Boolean next = false;

        for (String line : lines)
        {
            if (line.startsWith("#")) continue;

            line = MediaStreamMaster.resolveRelativeUrl(url, line);

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

        Log.d(LOGTAG, "readFragments: first=" + nextFragment);
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

    private boolean readHeaders()
    {
        try
        {
            byte[] buffer = new byte[ 32000 ];

            int cnt = 0;

            while (! new String(buffer, 0, cnt).endsWith("\r\n\r\n"))
            {
                if (requestInput.read(buffer, cnt++, 1) < 0) break;
            }

            String[] lines = new String(buffer, 0, cnt).split("\r\n");

            for (String line : lines)
            {
                Log.d(LOGTAG, "Header: " + line);

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

            return true;
        }
        catch (Exception ignore)
        {

        }

        return false;
    }
}