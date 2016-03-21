package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import java.io.InputStream;
import java.io.OutputStream;

public class SimpleRequest
{
    private static final String LOGTAG = SimpleRequest.class.getSimpleName();

    public static void testDat()
    {
        doHTTPGet("http://gonzo.ucount.in/stats/update/custom/lstv/pro7maxx/sd&callback=?",
                "http://www.live-stream.tv/online/fernsehen/deutsch/pro7maxx.html");

        doHTTPGet("http://gonzo.ucount.in/stats/update/custom/lstv/pro7/sd&callback=?",
                "http://www.live-stream.tv/online/fernsehen/deutsch/pro7.html");

        //doHTTPGet("http://lstv4.hls2.stream-server.org/live/pro7maxx@sd/index.m3u8");

        /*
        doHTTPGet("http://lstv2.hls2.stream-server.org/live/pro7@sd/index.m3u8");
        */
    }

    @Nullable
    public static String doHTTPGet(String src)
    {
        return doHTTPGet(src, null);
    }

    private static Socket clientSocket;
    private static OutputStream output;
    private static InputStream input;

    private static void logStuff(String string)
    {
        String[] lines = string.split("\r\n");

        for (String line : lines) Log.d(LOGTAG,"logStuff:<" + line + ">");
    }

    @Nullable
    public static String doHTTPGet(String src, String referrer)
    {
        try
        {
            Log.d(LOGTAG, "doHTTPGet: " + src);

            URL url = new URL(src);

            InetAddress ip = InetAddress.getByName(url.getHost());
            int port = url.getPort() > 0 ? url.getPort() : 80;

            //if (clientSocket == null)
            {
                InetSocketAddress sa = new InetSocketAddress(ip, port);
                clientSocket = new Socket();
                clientSocket.connect(sa, 5000);
                output = clientSocket.getOutputStream();
                input = clientSocket.getInputStream();
            }

            String urlpath = url.getPath();
            if (url.getQuery() != null) urlpath += "?" + url.getQuery();

            String header = "";

            header += "GET " + urlpath + " HTTP/1.1\r\n";
            header += "Host: " + url.getHost() + "\r\n";
            header += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
            header += "Accept-Encoding: gzip, deflate\r\n";

            if (referrer != null) header += "Referer: " + referrer + "\r\n";

            header += "Connection: keep-alive\r\n";
            header += "\r\n";

            output.write(header.getBytes("UTF-8"));
            output.flush();

            //
            // Read response headers.
            //

            byte[] headers = new byte[ 4196 ];
            String[] lines = null;

            for (int cnt = 0; cnt < headers.length; cnt++)
            {
                if (new String(headers, 0, cnt).endsWith("\r\n\r\n"))
                {
                    lines = new String(headers, 0, cnt).split("\r\n");

                    break;
                }

                if (input.read(headers, cnt, 1) < 0) break;
            }

            if (lines == null)
            {
                Log.e(LOGTAG, "No response....");

                return null;
            }

            boolean ischunked = false;

            for (String line : lines)
            {
                if (line.equalsIgnoreCase("Transfer-Encoding: chunked")) ischunked = true;
            }

            byte[] response = new byte[ 0 ];

            if (ischunked)
            {
                byte[] csize = new byte[ 256 ];

                while (true)
                {
                    int size = -1;

                    for (int cnt = 0; cnt < csize.length; cnt++)
                    {
                        if (new String(csize, 0, cnt).endsWith("\r\n"))
                        {
                            size = Integer.parseInt(new String(csize, 0, cnt).trim(), 16);
                            break;
                        }

                        if (input.read(csize, cnt, 1) < 0) break;
                    }

                    if (size < 0) break;

                    byte[] chunk = new byte[ size ];
                    int total = 0;

                    while (total < chunk.length)
                    {
                        int xfer = input.read(chunk, total, chunk.length - total);
                        if (xfer < 0) break;

                        total += xfer;
                    }

                    if (total < chunk.length) break;

                    byte[] temp = new byte[ response.length + chunk.length ];
                    System.arraycopy(response, 0, temp, 0, response.length);
                    System.arraycopy(chunk, 0, temp, response.length, chunk.length);
                    response = temp;

                    for (int cnt = 0; cnt < csize.length; cnt++)
                    {
                        if (new String(csize, 0, cnt).endsWith("\r\n")) break;

                        if (input.read(csize, cnt, 1) < 0) break;
                    }

                    if (size == 0)
                    {
                        for (int cnt = 0; cnt < csize.length; cnt++)
                        {
                            if (new String(csize, 0, cnt).endsWith("\r\n")) break;

                            if (input.read(csize, cnt, 1) < 0) break;
                        }

                        break;
                    }
                }

                if (response.length < 80) Log.d(LOGTAG, "doHTTPGet: res=" + new String(response));

                return new String(response);
            }
            else
            {
                Log.e(LOGTAG, "Content-Length: unsupported...");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static String[] readLines(String url)
    {
        try
        {
            HttpURLConnection connection = Simple.openUnderscoreConnection(url);

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

    @Nullable
    public static String readContent(String url)
    {
        try
        {
            HttpURLConnection connection = Simple.openUnderscoreConnection(url);

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
}