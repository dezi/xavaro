package de.xavaro.android.common;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.Locale;

@SuppressWarnings("unused")
public class WebAppUtility
{
    private static final String LOGTAG = WebAppUtility.class.getSimpleName();

    @JavascriptInterface
    public String getLocale()
    {
        return Locale.getDefault().getLanguage() + "-r" + Locale.getDefault().getCountry();
    }

    @JavascriptInterface
    public String getLocaleCountry()
    {
        return Locale.getDefault().getCountry();
    }

    @JavascriptInterface
    public String getLocaleLanguage()
    {
        return Locale.getDefault().getLanguage();
    }

    @JavascriptInterface
    public int getLaunchItemSize()
    {
        return CommonConfigs.LaunchItemSize;
    }
}
