package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class WebFrame extends FrameLayout
{
    private final static String LOGTAG = "WebFrameLayout";

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
        webview.getSettings().setSupportZoom(true);

        webview.getSettings().setDomStorageEnabled(false);
        webview.getSettings().setAppCacheEnabled(false);
        webview.getSettings().setDatabaseEnabled(false);

        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        addView(webview);
    }

    //endregion

    //region Static methods.

    private static JSONObject config = null;

    public static JSONObject getConfig(Context context)
    {
        if (config == null)
        {
            try
            {
                config = StaticUtils.readRawTextResourceJSON(context, R.raw.default_webframe).getJSONObject("webframes");
            }
            catch (Exception ex)
            {
                Log.e(LOGTAG, "getConfig: Cannot read default webframes.");
            }
        }

        return config;
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

        return null;
    }

    public static Drawable getConfigIconDrawable(Context context,String website)
    {
        try
        {
            String iconurl = getConfig(context,website).getString("icon");
            String iconext = MimeTypeMap.getFileExtensionFromUrl(iconurl);
            String iconfile = website + ".thumbnail." + iconext;
            Bitmap thumbnail = CacheManager.cacheThumbnail(context,iconfile,iconurl);

            Log.d(LOGTAG,"getIconDrawable: " + iconurl + "=>" + iconext);

            return new BitmapDrawable(context.getResources(),thumbnail);
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    public static String getConfigLabel(Context context,String website)
    {
        try
        {
            return getConfig(context,website).getString("label");
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    public static String getConfigUrl(Context context,String website)
    {
        try
        {
            return getConfig(context).getJSONObject(website).getString("url");
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    //endregion

    public void setLoadURL(String url)
    {
        webview.loadUrl(url);
    }

    public boolean doBackPressed()
    {
        Log.d(LOGTAG, "doBackPressed");

        if (webview.canGoBack())
        {
            webview.goBack();

            return false;
        }

        return true;
    }
}