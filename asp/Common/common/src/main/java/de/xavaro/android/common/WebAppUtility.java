package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.content.Context;

@SuppressWarnings("unused")
public class WebAppUtility
{
    private static final String LOGTAG = WebAppUtility.class.getSimpleName();

    @JavascriptInterface
    public String getAppName()
    {
        return Simple.getAppName();
    }

    @JavascriptInterface
    public String getBetaVersion()
    {
        Context appcontext = Simple.getAppContext();

        if (appcontext instanceof AppInfoHandler)
        {
            return ((AppInfoHandler) appcontext).getBetaVersion();
        }

        return "";
    }

    @JavascriptInterface
    public String getBetaVersionDate()
    {
        Context appcontext = Simple.getAppContext();

        if (appcontext instanceof AppInfoHandler)
        {
            return ((AppInfoHandler) appcontext).getBetaVersionDate();
        }

        return "";
    }

    @JavascriptInterface
    public String getLocale()
    {
        return Simple.getLocale();
    }

    @JavascriptInterface
    public String getLocaleCountry()
    {
        return Simple.getLocaleCountry();
    }

    @JavascriptInterface
    public String getLocaleLanguage()
    {
        return Simple.getLocaleLanguage();
    }

    @JavascriptInterface
    public boolean getDeveloperMode()
    {
        return Simple.getSharedPrefBoolean("developer.enable");
    }

    @JavascriptInterface
    public int getLaunchItemSize()
    {
        return CommonConfigs.LaunchItemSize;
    }

    @JavascriptInterface
    public String getOwnerSiezen()
    {
        return Simple.getSharedPrefString("owner.siezen");
    }

    @JavascriptInterface
    public String getOwnerName()
    {
        return Simple.getOwnerName();
    }

    @JavascriptInterface
    public String getPrettyJson(String json)
    {
        if (json != null)
        {
            if (json.startsWith("[")) return Json.toPretty(Json.fromStringArray(json));
            if (json.startsWith("{")) return Json.toPretty(Json.fromStringObject(json));
        }

        return "";
    }

    @JavascriptInterface
    public void makePrettyLog(String json)
    {
        Log.d(LOGTAG, "makePrettyLog:" + getPrettyJson(json));
    }

    @JavascriptInterface
    public String getDezify(String input)
    {
        return Simple.dezify(input);
    }

    @JavascriptInterface
    public void makeClick()
    {
        Simple.makeClick();
    }
}
