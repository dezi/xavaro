package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.webkit.JavascriptInterface;

import android.webkit.WebView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

@SuppressWarnings("unused")
public class WebAppWeather
{
    private static final String LOGTAG = WebAppWeather.class.getSimpleName();

    private static final String baseurl = "http://api.openweathermap.org/data/2.5/forecast";

    private final String webappname;
    private final WebAppLoader webapploader;

    private String webappserver;
    private String webappextra;

    public WebAppWeather(String webappname, WebView webview, WebAppLoader webapploader)
    {
        this.webappname = webappname;
        this.webapploader = webapploader;
    }

    private boolean makeServer()
    {
        if (webappserver == null)
        {
            JSONObject manifest = WebApp.getManifest(webappname);

            if (manifest != null)
            {
                webappserver = Json.getString(manifest, "appserver");
            }
        }

        return (webappserver != null);
    }

    private boolean makeExtra()
    {
        if (webappextra == null)
        {
            JSONObject manifest = WebApp.getManifest(webappname);

            if (manifest != null)
            {
                String guid = Json.getString(manifest, "appguid");

                if (guid != null)
                {
                    UUID uuid = Simple.dezify(UUID.fromString(guid));

                    if (uuid != null)
                    {
                        webappextra = uuid.toString().replace("-", "");
                    }
                }
            }
        }

        return (webappextra != null);
    }

    @Nullable
    private String validateByAge(byte[] bytes)
    {
        if (bytes != null)
        {
            String json = new String(bytes);

            //
            // Check age of what we have got.
            //

            JSONObject owm = Json.fromStringObject(json);
            JSONArray list = Json.getArray(owm, "list");
            JSONObject entry = Json.getObject(list, 0);
            long dt = Json.getLong(entry, "dt");

            if (dt > 0)
            {
                long diff = dt - (Simple.nowAsTimeStamp() / 1000L);
                String utc = Simple.timeStampAsISO(dt * 1000L);

                Log.d(LOGTAG,"validateByAge: diff=" + utc + "=" + diff);

                if (diff > 0) return json;
            }
        }

        return null;
    }

    @JavascriptInterface
    public String getForecast(String id)
    {
        String json = null;

        if (makeServer())
        {
            String url = "http://" + webappserver + "/owmdata/forecast/" + id + ".05.json.gz";

            json = validateByAge(webapploader.getRequestData(url));
        }

        if ((json == null) && makeExtra())
        {
            //
            // Fetch new data from weather server.
            //

            String url = baseurl + "?id=" + id + "&APPID=" + webappextra + "&units=metric";

            Log.d(LOGTAG, "getForecast url=" + url);

            json = SimpleRequest.doHTTPGet(url);

            if (json != null)
            {
                String puturl = "http://" + webappserver + "/owmuploader/" + id + ".05.json.gz";

                if (! SimpleRequest.doHTTPPut(puturl, json))
                {
                    Log.e(LOGTAG, "getForecast put failed=" + puturl);
                }
            }
        }

        return (json == null) ? "{}" : json;
    }

    @JavascriptInterface
    public String getForecast16(String id)
    {
        String json = null;

        if (makeServer())
        {
            String url = "http://" + webappserver + "/owmdata/forecast/" + id + ".16.json.gz";

            json = validateByAge(webapploader.getRequestData(url));
        }

        if ((json == null) && makeExtra())
        {
            String url = baseurl + "/daily" + "?id=" + id + "&APPID=" + webappextra + "&units=metric";

            Log.d(LOGTAG, "getForecast16 url=" + url);

            json = SimpleRequest.doHTTPGet(url);

            if (json != null)
            {
                String puturl = "http://" + webappserver + "/owmuploader/" + id + ".16.json.gz";

                if (! SimpleRequest.doHTTPPut(puturl, json))
                {
                    Log.e(LOGTAG, "getForecast16 put failed=" + puturl);
                }
            }
        }

        return (json == null) ? "{}" : json;
    }

    @JavascriptInterface
    public String getQuery(String city)
    {
        JSONArray json = new JSONArray();

        Pattern pattern = Pattern.compile(city);

        if (makeServer())
        {
            String url = "http://" + webappserver + "/owmdata/city.csv.gzbin";

            byte[] gzbin = webapploader.getRequestData(url);

            if (gzbin != null)
            {
                try
                {
                    byte[] data = new byte[ 128 * 1024 ];

                    ByteArrayInputStream bais = new ByteArrayInputStream(gzbin);
                    GZIPInputStream gzis = new GZIPInputStream(bais, data.length);

                    int xfer;
                    int rest = 0;

                    while ((xfer = gzis.read(data, rest, data.length - rest)) >= 0)
                    {
                        int inbuf = rest + xfer;

                        //
                        // Find last newline.
                        //

                        int lastp = inbuf;
                        while (lastp > 0) if (data[ --lastp ] == '\n') break;

                        //
                        // Execute match on this.
                        //

                        String chunk = new String(data, 0, lastp);

                        Matcher matcher = pattern.matcher(chunk);

                        while (matcher.find())
                        {
                            int von = matcher.start();
                            int bis = matcher.end();

                            while ((von > 0) && (chunk.charAt(von - 1) != '\n')) von--;
                            while ((bis < (inbuf - 1)) && (chunk.charAt(bis + 1) != '\n')) bis++;

                            json.put(chunk.substring(von, bis));
                        }

                        //
                        // Copy rest to start of buffer.
                        //

                        rest = inbuf - (lastp + 1);
                        System.arraycopy(data, lastp + 1, data, 0, rest);
                    }

                    gzis.close();
                    bais.close();
                }
                catch (Exception ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }
        }

        return json.toString();
    }
}
