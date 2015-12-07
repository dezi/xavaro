package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

//
// Guarded access and display of web client.
//

public class WebGuard extends WebViewClient
{
    //region Internal variables.

    private final static String LOGTAG = "WebGuard";

    private Context context;

    private Uri currentUri;
    private String currentUrl;

    private JSONObject config;

    //endregion

    //region Public setters.

    public void setContext(Context context)
    {
        this.context = context;

        loadGlobalConfig(context);
    }

    public void setCurrent(String url,String website)
    {
        currentUrl = url;
        currentUri = Uri.parse(url);

        config = WebFrame.getConfig(context, website);
    }

    //endregion

    //region Static configuration methods.

    private static JSONObject globalConfig = null;

    // @formatter:off
    private static ArrayList<String> regex_allow   = new ArrayList<>();
    private static ArrayList<String> regex_deny    = new ArrayList<>();
    private static ArrayList<String> domains_allow = new ArrayList<>();
    private static ArrayList<String> domains_deny  = new ArrayList<>();
    // @formatter:on

    private static void loadGlobalConfig(Context context)
    {
        if (globalConfig == null)
        {
            try
            {
                JSONObject ctemp = StaticUtils.readRawTextResourceJSON(context, R.raw.default_webguard);

                if (ctemp != null)
                {
                    globalConfig = ctemp.getJSONObject("webguard");

                    // @formatter:off
                    regex_allow   = getConfigTreeArray(globalConfig, "resource", "regex",   "allow");
                    regex_deny    = getConfigTreeArray(globalConfig, "resource", "regex",   "deny" );
                    domains_allow = getConfigTreeArray(globalConfig, "resource", "domains", "allow");
                    domains_deny  = getConfigTreeArray(globalConfig, "resource", "domains", "deny" );
                    // @formatter:on
                }
            }
            catch (NullPointerException | JSONException ex)
            {
                Log.e(LOGTAG, "loadGlobalConfig: Cannot read default webguard config.");
            }
        }
    }

    private static ArrayList<String> getConfigTreeArray(JSONObject config, String arg1, String arg2, String arg3)
    {
        ArrayList<String> list = new ArrayList<>();

        try
        {
            JSONArray jsonArray = config.getJSONObject(arg1).getJSONObject(arg2).getJSONArray(arg3);

            for (int inx = 0; inx < jsonArray.length(); inx++)
            {
                list.add(jsonArray.getString(inx));
            }
        }
        catch (Exception ex)
        {
            Log.e(LOGTAG, "getConfigTreeArray: Cannot read globalConfig " + arg1 + "/" + arg2 + "/" + arg3);
        }

        return list;
    }

    //endregion

    //region Overridden methods.

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon)
    {
        currentUrl = url;
        currentUri = Uri.parse(url);

        Log.d(LOGTAG,"onPageStarted=" + url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "shouldOverrideUrlLoading=" + url);

        Uri follow = Uri.parse(url);

        if (currentUri.getHost().equals(follow.getHost()))
        {
            currentUrl = url;
            currentUri = Uri.parse(url);

            return false;
        }

        //
        // Do more cherry picking here.
        //

        if (follow.getScheme().equals("mailto") && GlobalConfigs.likeEmail)
        {
            //
            // We like Email.
            //

            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(follow);
                context.startActivity(intent);
            }
            catch (Exception ignore)
            {
            }

            return true;
        }

        if (follow.getScheme().equals("whatsapp") && GlobalConfigs.likeWhatsApp)
        {
            //
            // We love WhatsApp.
            //

            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(follow);
                intent.setPackage(GlobalConfigs.packageWhatsApp);
                context.startActivity(intent);
            }
            catch (Exception ignore)
            {
            }

            return true;
        }

        if (follow.getHost().endsWith("twitter.com") && GlobalConfigs.likeTwitter)
        {
            //
            // We like Twitter.
            //

            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(follow);
                intent.setPackage(GlobalConfigs.packageTwitter);
                context.startActivity(intent);

                return true;
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }

        if (follow.getHost().equals("plus.google.com") && GlobalConfigs.likeGooglePlus)
        {
            //
            // We hate GooglePlus.
            //

            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(follow);
                intent.setPackage(GlobalConfigs.packageGooglePlus);
                context.startActivity(intent);

                return true;
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }

        if (follow.getHost().endsWith(".facebook.com") && GlobalConfigs.likeFacebook)
        {
            //
            // We hate Facebook.
            //

            try
            {
                if (follow.getQueryParameter("u") != null)
                {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", follow.getQueryParameter("u"));
                    intent.setPackage(GlobalConfigs.packageFacebook);
                    context.startActivity(intent);

                    return true;
                }
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }

        //
        // Finally block stuff.
        //

        Toast.makeText(context, "Blocking: " + url, Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    @SuppressLint("NewApi")
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request)
    {
        return checkUrlResource(request.getUrl().toString());
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url)
    {
        return checkUrlResource(url);
    }

    //endregion

    //region Access checking methods.

    //
    // Validate host name against wildcard domain specification.
    //

    private boolean validateHost(String host, String domain)
    {
        if (domain.startsWith("*."))
        {
            String rest = domain.substring(2);

            String[] restparts = rest.split("\\.");
            String[] hostparts = host.split("\\.");

            int restinx = restparts.length - 1;
            int hostinx = hostparts.length - 1;

            while ((restinx >= 0) && (hostinx >= 0))
            {
                if (!restparts[ restinx ].equals(hostparts[ hostinx ]))
                {
                    return false;
                }

                restinx--;
                hostinx--;
            }

            return true;
        }

        return host.equals(domain);
    }

    @Nullable
    private WebResourceResponse checkUrlResource(String url)
    {
        if (currentUrl.equals(url))
        {
            //
            // We are in root page load.
            //

            Log.d(LOGTAG, "=====> " + url);

            if ((config != null) && config.has("webguard"))
            {
                //
                // Check for any static cookies to be added.
                //

                try
                {
                    if (config.getJSONObject("webguard").has("cookies"))
                    {
                        JSONArray cookies = config.getJSONObject("webguard").getJSONArray("cookies");
                        android.webkit.CookieManager wkCookieManager = android.webkit.CookieManager.getInstance();

                        for (int inx = 0; inx < cookies.length(); inx++)
                        {
                            String cookie = cookies.getString(inx);
                            wkCookieManager.setCookie(url, cookie);
                            Log.d(LOGTAG, "checkUrlResource set cookie=" + cookie);
                        }
                    }
                }
                catch (JSONException ignore)
                {
                }

                //
                // Check for for replacement rules.
                //

                try
                {
                    JSONObject mingle = config.getJSONObject("webguard").getJSONObject("mingle");
                    JSONArray replace = mingle.has("replace") ? mingle.getJSONArray("replace") : null;

                    if (replace != null)
                    {
                        //
                        // Open a connection using java.net environment.
                        //

                        android.webkit.CookieManager wkCookieManager = android.webkit.CookieManager.getInstance();
                        String wkCookies = wkCookieManager.getCookie(url);
                        if (wkCookies != null) Log.d(LOGTAG,"######" + wkCookies);

                        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setDoInput(true);
                        connection.connect();

                        String content = StaticUtils.getContentFromStream(connection.getInputStream());

                        if (content != null)
                        {
                            for (int inx = 0; inx < replace.length(); inx++)
                            {
                                JSONObject item = replace.getJSONObject(inx);

                                if (!item.has("regex")) continue;
                                if (!item.has("replace")) continue;

                                content = content.replaceAll(
                                        item.getString("regex"),
                                        item.getString("replace"));
                            }

                            byte[] bytes = content.getBytes("utf-8");
                            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

                            return new WebResourceResponse("text/html", "utf-8", bais);
                        }
                    }
                }
                catch (JSONException | IOException ignore)
                {
                }
            }
        }

        //
        // Check positive url regex list.
        //

        for (int inx = 0; inx < regex_allow.size(); inx++)
        {
            if (url.matches(regex_allow.get(inx)))
            {
                Log.d(LOGTAG, "checkUrlResource: allow " + url);

                return null;
            }
        }

        //
        // Check negative url regex list.
        //

        for (int inx = 0; inx < regex_deny.size(); inx++)
        {
            if (url.matches(regex_deny.get(inx)))
            {
                Log.d(LOGTAG, "checkUrlResource: deny " + url);

                return new WebResourceResponse("text/plain", "utf-8", null);
            }
        }

        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        //
        // Check positive domain list.
        //

        for (int inx = 0; inx < domains_allow.size(); inx++)
        {
            if (validateHost(host, domains_allow.get(inx)))
            {
                Log.d(LOGTAG, "checkUrlResource: allow " + host);

                return null;
            }
        }

        //
        // Check negative domain list.
        //

        for (int inx = 0; inx < domains_deny.size(); inx++)
        {
            if (validateHost(host, domains_deny.get(inx)))
            {
                Log.d(LOGTAG, "checkUrlResource: deny " + host);

                return new WebResourceResponse("text/plain", "utf-8", null);
            }
        }

        if ((! url.endsWith(".png")) && (! url.endsWith(".jpg")) && (! url.endsWith(".svg")) &&
                (! url.endsWith(".gif")) && (! url.endsWith(".ico")))
        {
            Log.d(LOGTAG, "checkUrlResource: load " + url);
        }

        return null;
    }

    //endregion
}
