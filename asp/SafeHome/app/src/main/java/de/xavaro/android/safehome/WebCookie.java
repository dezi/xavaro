package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//
// Cookie manager exchanges cookies between android.wekbit
// and java.net cookie store to allow crosswise loading
// of resources with a webview or pure http fetch.
//

public class WebCookie extends CookieManager
{
    private final static String LOGTAG = "WebCookie";

    private android.webkit.CookieManager webkitCookieManager;

    public WebCookie(CookieStore store, CookiePolicy cookiePolicy)
    {
        super(null, cookiePolicy);

        this.webkitCookieManager = android.webkit.CookieManager.getInstance();
    }

    public static void initCookies()
    {
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);

        WebCookie coreCookieManager = new WebCookie(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(coreCookieManager);
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException
    {
        if ((uri == null) || (responseHeaders == null)) return;

        String url = uri.toString();

        Log.d(LOGTAG, "put=" + url);

        for (String headerKey : responseHeaders.keySet())
        {
            if (headerKey == null) continue;

            if (! (headerKey.equalsIgnoreCase("Set-Cookie") ||
                   headerKey.equalsIgnoreCase("Set-Cookie2"))) continue;

            for (String headerValue : responseHeaders.get(headerKey))
            {
                this.webkitCookieManager.setCookie(url, headerValue);
            }
        }
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException
    {
        if ((uri == null) || (requestHeaders == null)) throw new IllegalArgumentException("Argument is null");

        String url = uri.toString();

        Log.d(LOGTAG, "get=" + url);

        Map<String, List<String>> res = new java.util.HashMap<String, List<String>>();

        String cookie = this.webkitCookieManager.getCookie(url);

        if (cookie != null)
        {
            Log.d(LOGTAG, "get=" + cookie);

            res.put("Cookie", Arrays.asList(cookie));
        }

        return res;
    }

    @Override
    public CookieStore getCookieStore()
    {
        throw new UnsupportedOperationException();
    }
}
