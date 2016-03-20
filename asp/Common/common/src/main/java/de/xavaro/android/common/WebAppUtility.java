package de.xavaro.android.common;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.Locale;

@SuppressWarnings("unused")
public class WebAppUtility
{
    private static final String LOGTAG = WebAppUtility.class.getSimpleName();

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
        return Json.toPretty(Json.fromString(json));
    }

    @JavascriptInterface
    public void makeClick()
    {
        Simple.makeClick();
    }
}
