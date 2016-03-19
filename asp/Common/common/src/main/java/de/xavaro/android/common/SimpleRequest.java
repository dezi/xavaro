package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.util.Log;

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

        //doHTTPGet("http://lstv4.hls2.stream-server.org/live/pro7maxx@sd/index.m3u8");

        /*
        doHTTPGet("http://gonzo.ucount.in/stats/update/custom/lstv/pro7/sd&callback=?",
                "http://www.live-stream.tv/online/fernsehen/deutsch/pro7.html");
        doHTTPGet("http://lstv2.hls2.stream-server.org/live/pro7@sd/index.m3u8");
        */
    }

    @Nullable
    public static String doHTTPGet(String src)
    {
        return doHTTPGet(src, null);
    }

    @Nullable
    public static String doHTTPGet(String src, String referrer)
    {
        try
        {
            URL url = new URL(src);

            InetAddress ip = InetAddress.getByName(url.getHost());

            Log.d(LOGTAG, "===================host=" + url.getHost() + " port=" + url.getPort());
            Log.d(LOGTAG, "===================ip=" + ip.getHostAddress());
            Log.d(LOGTAG, "===================ip=" + url.getPath());
            Log.d(LOGTAG, "===================query=" + url.getQuery());

            int port = url.getPort() > 0 ? url.getPort() : 80;
            InetSocketAddress sa = new InetSocketAddress(ip, port);
            Socket clientSocket = new Socket();
            clientSocket.connect(sa, 5000);

            OutputStream output = clientSocket.getOutputStream();

            String header = "";

            header += "GET " + url.getPath() + "?" + url.getQuery() + " HTTP/1.1\r\n";

            Log.d(LOGTAG,"===================" + header);

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

            InputStream input = clientSocket.getInputStream();

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

            if (lines == null) return null;

            boolean ischunked = false;

            for (String line : lines)
            {
                Log.d(LOGTAG, "==========================Headers: " + line);

                if (line.equals("Transfer-Encoding: chunked")) ischunked = true;
            }

            byte[] buffer = new byte[ 8192 ];

            if (ischunked)
            {
                while (true)
                {
                    long size = -1;

                    for (int cnt = 0; cnt < buffer.length; cnt++)
                    {
                        if (new String(buffer, 0, cnt).endsWith("\r\n"))
                        {
                            size = Long.parseLong(new String(headers, 0, cnt).trim(), 16);
                            break;
                        }

                        if (input.read(buffer, cnt, 1) < 0) break;
                    }
                    
                    if (size == 0) break;
                }



            }

            byte[] buffer = new byte[ 8192 ];

            while (true)
            {
                int xfer = input.read(buffer);
                if (xfer < 0) break;

                String str = new String(buffer, 0 ,xfer);
                str = str.replace("\r", "+r");
                str = str.replace("\n", "+n");

                Log.d(LOGTAG,"===<" + str + ">");
            }

            output.close();
            input.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }
}
