package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;

import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class WebAppPrices
{
    private static final String LOGTAG = WebAppPrices.class.getSimpleName();

    private final String webappname;
    private final WebAppLoader webapploader;

    private String webappserver;

    public WebAppPrices(String webappname, WebView webview, WebAppLoader webapploader)
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

    @JavascriptInterface
    public String getProducts(String categories)
    {
        JSONArray catlist = Json.fromStringArray(categories);
        if (catlist == null) return "[]";

        String query = "";

        for (int inx = 0; inx < catlist.length(); inx++)
        {
            if (query.length() > 0) query += "|";
            query += "\\|" + Json.getString(catlist, inx) + "\\|";
        }

        return getQuery(3, query);
    }

    @JavascriptInterface
    public String getQuery(int recordtype, String query)
    {
        JSONArray json = new JSONArray();

        Pattern pattern = Pattern.compile(query);
        String recordstart = recordtype + "|";

        if (makeServer())
        {
            String url = "http://" + webappserver + "/prodata/proprices.de-rDE.csv.gzbin";

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
                            while ((bis < inbuf) && (chunk.charAt(bis) != '\n')) bis++;

                            String line = chunk.substring(von, bis);
                            if (line.startsWith(recordstart)) json.put(line);
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
