package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

public class WebAppWeather
{
    private static final String LOGTAG = WebAppWeather.class.getSimpleName();

    private static final String baseurl = "http://api.openweathermap.org/data/2.5/forecast";

    private final String webappname;
    private String webappextra;

    public WebAppWeather(String webappname)
    {
        this.webappname = webappname;
    }

    @JavascriptInterface
    public String getForecast(String id)
    {
        if (webappextra == null)
        {
            JSONObject manifest = WebApp.getManifest(webappname);
            webappextra = Simple.dezify(Json.getString(manifest, "appextra"));
        }

        String url = baseurl + "?id=" + id + "&APPID=" + webappextra + "&units=metric";

        Log.d(LOGTAG, "getForecast url=" + url);

        String json = SimpleRequest.doHTTPGet(url);
        return (json == null) ? "{}" : json;
    }

    @JavascriptInterface
    public String getForecast16(String id)
    {
        if (webappextra == null)
        {
            JSONObject manifest = WebApp.getManifest(webappname);
            webappextra = Simple.dezify(Json.getString(manifest, "appextra"));
        }

        String url = baseurl + "/daily" + "?id=" + id + "&APPID=" + webappextra + "&units=metric";

        Log.d(LOGTAG, "getForecast16 url=" + url);

        String json = SimpleRequest.doHTTPGet(url);
        return (json == null) ? "{}" : json;
    }
}
