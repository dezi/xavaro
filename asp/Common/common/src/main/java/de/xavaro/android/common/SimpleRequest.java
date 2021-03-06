package de.xavaro.android.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

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

    @Nullable
    public static String doHTTPGet(String src, String referrer)
    {
        byte[] data = doHTTPGetData(src, referrer);
        return (data == null) ? null : new String(data);
    }

    @Nullable
    public static byte[] doHTTPGetData(String src, String referrer)
    {
        try
        {
            Log.d(LOGTAG, "doHTTPGet: " + src);

            URL url = new URL(src);

            InetAddress ip = InetAddress.getByName(url.getHost());
            int port = url.getPort() > 0 ? url.getPort() : 80;

            InetSocketAddress sa = new InetSocketAddress(ip, port);
            Socket clientSocket = new Socket();
            clientSocket.connect(sa, 5000);
            OutputStream output = clientSocket.getOutputStream();
            InputStream input = clientSocket.getInputStream();

            String urlpath = url.getPath();
            if (url.getQuery() != null) urlpath += "?" + url.getQuery();

            String header = "";

            header += "GET " + urlpath + " HTTP/1.1\r\n";
            header += "Host: " + url.getHost() + "\r\n";
            header += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
            header += "Accept-Encoding: gzip, deflate\r\n";

            if (referrer != null) header += "Referer: " + referrer + "\r\n";

            header += "Connection: close\r\n";
            header += "\r\n";

            output.write(header.getBytes("UTF-8"));
            output.flush();

            Log.d(LOGTAG, "doHTTPGet: headers...");

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

                input.close();
                output.close();

                return null;
            }

            boolean isok = false;
            boolean ischunked = false;
            int contentlength = 0;

            for (String line : lines)
            {
                if (line.equalsIgnoreCase("HTTP/1.0 200 OK") ||
                        line.equalsIgnoreCase("HTTP/1.1 200 OK"))
                {
                    isok = true;
                }

                if (line.equalsIgnoreCase("Transfer-Encoding: chunked"))
                {
                    ischunked = true;
                }

                if (line.toLowerCase().startsWith("content-length: "))
                {
                    contentlength = Integer.parseInt(line.substring(16));
                }
            }

            Log.d(LOGTAG, "doHTTPGet: response:" + isok + "=" + ischunked + "=" + contentlength);

            if (! isok)
            {
                input.close();
                output.close();

                return null;
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

                Log.d(LOGTAG, "doHTTPGet: done length:" + response.length);

                if (response.length < 80) Log.d(LOGTAG, "doHTTPGet: res=" + new String(response));

                input.close();
                output.close();

                return response;
            }
            else
            {
                response = new byte[ contentlength ];
                int total = 0;

                while (total < response.length)
                {
                    int xfer = input.read(response, total, response.length - total);
                    if (xfer < 0) break;

                    total += xfer;
                }

                if (total < response.length)
                {
                    Log.d(LOGTAG, "doHTTPGet: incomplete:" + response.length + "=" + total);

                    input.close();
                    output.close();

                    return null;
                }

                input.close();
                output.close();

                return response;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    public static boolean doHTTPPut(String src, String string)
    {
        Log.d(LOGTAG, "doHTTPPut: " + src);
        if (string == null) return false;

        try
        {
            URL url = new URL(src);

            InetAddress ip = InetAddress.getByName(url.getHost());
            int port = url.getPort() > 0 ? url.getPort() : 80;

            InetSocketAddress sa = new InetSocketAddress(ip, port);
            Socket clientSocket = new Socket();
            clientSocket.connect(sa, 5000);

            OutputStream output = clientSocket.getOutputStream();

            String urlpath = url.getPath();
            if (url.getQuery() != null) urlpath += "?" + url.getQuery();

            byte[] data = string.getBytes("UTF-8");

            String header = "";

            header += "PUT " + urlpath + " HTTP/1.1\r\n";
            header += "Host: " + url.getHost() + "\r\n";
            header += "Content-Length: " + data.length + "\r\n";
            header += "Upload-File: " + urlpath + "\r\n";
            header += "Connection: close\r\n";
            header += "\r\n";

            output.write(header.getBytes("UTF-8"));
            output.flush();

            Log.d(LOGTAG, "doHTTPPut: headers...");

            output.write(data);
            output.close();

            return true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    @Nullable
    public static String[] readLines(String url)
    {
        if (url == null) return null;

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
        return readContent(url, null, null, null, false);
    }

    @Nullable
    public static String readContent(String url, String oauth)
    {
        return readContent(url, oauth, null, null, false);
    }

    @Nullable
    public static String readContent(String url, JSONObject post)
    {
        return readContent(url, null, null, post, false);
    }

    @Nullable
    public static String readContent(String url, JSONObject post, boolean asjson)
    {
        return readContent(url, null, null, post, asjson);
    }

    @Nullable
    public static String readContent(String url, String oauth, String agent, JSONObject post, boolean asjson)
    {
        if (url == null) return null;

        if (agent == null) agent = "Mozilla/5.0 Gecko/20100101 Firefox/40.1";

        try
        {
            HttpURLConnection connection = Simple.openUnderscoreConnection(url, false);

            connection.setUseCaches(false);
            connection.setDoInput(true);

            connection.setRequestMethod((post == null) ? "GET" : "POST");

            connection.setRequestProperty("User-Agent", agent);

            if (oauth != null)
            {
                connection.setRequestProperty("Authorization", oauth);
                connection.setRequestProperty("X-Auth-Token", oauth);
            }

            if (post != null)
            {
                String payload = "";

                if (asjson)
                {
                    connection.setRequestProperty("Content-Type", "application/json");

                    payload = post.toString();
                }
                else
                {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    Iterator<String> keysIterator = post.keys();

                    while (keysIterator.hasNext())
                    {
                        String key = keysIterator.next();
                        String val = Json.getString(post, key);
                        if (val == null) continue;

                        payload += ((payload.length() > 0) ? "&" : "")
                                + Simple.getUrlEncoded(key)
                                + "="
                                + Simple.getUrlEncoded(val);
                    }
                }

                connection.setRequestProperty("Content-Length", "" + payload.getBytes().length);
                connection.setDoOutput(true);

                connection.connect();

                OutputStream output = connection.getOutputStream();
                output.write(payload.getBytes());
                output.close();
            }
            else
            {
                connection.connect();
            }

            int httpres = connection.getResponseCode();
            String httpmsg = connection.getResponseMessage();

            Log.d(LOGTAG, "readContent:" + httpres + "=" + httpmsg);
            if ((httpres >= 400) && (httpres <= 499)) return null;

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
            ignore.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static byte[] readData(String url)
    {
        if (url == null) return null;

        try
        {
            HttpURLConnection connection = Simple.openUnderscoreConnection(url);

            InputStream input = connection.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[ 4096 ];
            int xfer;

            while ((xfer = input.read(buffer)) >= 0)
            {
                output.write(buffer, 0, xfer);
            }

            input.close();

            return output.toByteArray();
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    @Nullable
    public static Bitmap readBitmap(String url)
    {
        byte[] data = readData(url);
        if (data == null) return null;

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    @Nullable
    public static Drawable readDrawable(String url)
    {
        Bitmap bitmap = readBitmap(url);
        if (bitmap == null) return null;

        return new BitmapDrawable(Simple.getResources(), bitmap);
    }
}
