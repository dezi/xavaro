package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
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
import java.util.ArrayList;

//
// Guarded access and display of web client.
//

public class WebGuard extends WebViewClient
{
    private final static String LOGTAG = "WebGuard";

    private Context context;

    private Uri currentUri;
    private String currentUrl;

    private JSONObject config;

    public void setContext(Context context)
    {
        this.context = context;

        getGlobalConfig(context);
    }

    public void setCurrent(String website, String url)
    {
        currentUrl = url;
        currentUri = Uri.parse(url);

        config = WebFrame.getConfig(context, website);
    }

    //region Static configuration methods.

    private static JSONObject globalConfig = null;

    // @formatter:off
    private static ArrayList<String> regex_allow   = new ArrayList<>();
    private static ArrayList<String> regex_deny    = new ArrayList<>();
    private static ArrayList<String> domains_allow = new ArrayList<>();
    private static ArrayList<String> domains_deny  = new ArrayList<>();
    // @formatter:on

    @Nullable
    private static JSONObject getGlobalConfig(Context context)
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
                Log.e(LOGTAG, "getGlobalConfig: Cannot read default webguard config.");
            }
        }

        return globalConfig;
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
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "URL=" + url);

        Uri follow = Uri.parse(url);

        if (currentUri.getHost().equals(follow.getHost()))
        {
            currentUrl = url;
            currentUri = follow;

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
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.SEND");
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

    @Override
    public void onPageFinished(WebView view, String url)
    {
        super.onPageFinished(view, url);
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

            //Log.d(LOGTAG,"tubu <" + domain + ">" + host);

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

    private WebResourceResponse checkUrlResource(String url)
    {
        if (currentUrl.equals(url))
        {
            Log.d(LOGTAG, "=====> " + url);

            if ((config != null) && config.has("webguard"))
            {
                try
                {
                    JSONObject mingle = config.getJSONObject("webguard").getJSONObject("mingle");
                    JSONArray replace = mingle.has("replace") ? mingle.getJSONArray("replace") : null;

                    if (replace != null)
                    {
                        String content = StaticUtils.getContentFromUrl(url);

                        if (content != null)
                        {
                            for (int inx = 0; inx < replace.length(); inx++)
                            {
                                JSONObject item = replace.getJSONObject(inx);

                                if (! item.has("regex")) continue;
                                if (! item.has("replace")) continue;

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

        if ((!url.endsWith(".png")) && (!url.endsWith(".jpg")) && (!url.endsWith(".gif")) && (!url.endsWith(".ico")))
        {
            Log.d(LOGTAG, "checkUrlResource: load " + url);
        }

        return null;
    }

    //endregion
}
