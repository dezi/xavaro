package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import de.xavaro.android.common.StaticUtils;

//
// User agent: Mozilla/5.0 (Linux; Android 5.0.2; SM-T555 Build/LRX22G; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/46.0.2490.76 Safari/537.36
//

public class WebFrame extends FrameLayout
{
    private static final String LOGTAG = WebFrame.class.getSimpleName();

    private Context context;

    private WebView webview;
    private WebGuard webguard;

    //region Region: Constructor logic.

    public WebFrame(Context context)
    {
        super(context);

        myInit(context);
    }

    public WebFrame(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public WebFrame(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void myInit(Context context)
    {
        this.context = context;

        setBackgroundColor(0xffffffee);

        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        setLayoutParams(layout);

        webguard = new WebGuard();
        webguard.setContext(context);

        webview = new WebView(context);
        webview.setWebViewClient(webguard);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(false);

        webview.getSettings().setSupportZoom(true);

        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        addView(webview);
    }

    //endregion

    //region Static methods.

    private static JSONObject globalConfig = null;

    public static JSONObject getConfig(Context context)
    {
        if (globalConfig == null)
        {
            JSONObject jot = StaticUtils.readRawTextResourceJSON(context, R.raw.default_webconfig);

            if (jot == null)
            {
                Log.e(LOGTAG, "getConfig: Cannot read default webconfig.");
            }
            else
            {
                try
                {
                    globalConfig = jot.getJSONObject("webconfig");

                    return globalConfig;
                }
                catch (JSONException ignore)
                {
                    Log.e(LOGTAG, "getConfig: Tag <webconfig> missing in config.");
                }
            }

            globalConfig = new JSONObject();
        }

        return globalConfig;
    }

    public static JSONObject getConfig(Context context,String website)
    {
        try
        {
            return getConfig(context).getJSONObject(website);
        }
        catch (JSONException ignore)
        {
        }

        return new JSONObject();
    }

    @Nullable
    public static Drawable getConfigIconDrawable(Context context,String website)
    {
        try
        {
            String iconurl = getConfig(context,website).getString("icon");
            String iconext = MimeTypeMap.getFileExtensionFromUrl(iconurl);
            String iconfile = website + ".thumbnail." + iconext;
            Bitmap thumbnail = CacheManager.cacheThumbnail(context, iconurl, iconfile);

            return new BitmapDrawable(context.getResources(), thumbnail);
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String getConfigLabel(Context context,String website)
    {
        try
        {
            return getConfig(context,website).getString("label");
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String getConfigUrl(Context context,String website)
    {
        try
        {
            return getConfig(context).getJSONObject(website).getString("url");
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    //endregion

    public void setLoadURL(String website,String url)
    {
        webguard.setCurrent(url, website);
        webguard.setFeatures(webview);

        webview.loadUrl(url);
    }

    public boolean doBackPressed()
    {
        Log.d(LOGTAG, "doBackPressed");

        if (webview.canGoBack())
        {
            webview.goBack();

            webguard.setWasBackAction();

            return false;
        }

        return true;
    }
}