package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

@SuppressWarnings("unused")
public class WebAppPrefs
{
    private static final String LOGTAG = WebAppPrefs.class.getSimpleName();

    private final String webappname;

    public WebAppPrefs(String webappname)
    {
        this.webappname = webappname;
    }

    @JavascriptInterface
    public String getPrefString(String key)
    {
        String pkey = "webapp.pref." + webappname + "." + key;
        return Simple.getSharedPrefString(pkey);
    }

    @JavascriptInterface
    public int getPrefInt(String key)
    {
        String pkey = "webapp.pref." + webappname + "." + key;
        return Simple.getSharedPrefInt(pkey);
    }

    @JavascriptInterface
    public boolean getPrefBoolean(String key)
    {
        String pkey = "webapp.pref." + webappname + "." + key;
        return Simple.getSharedPrefBoolean(pkey);
    }
}
