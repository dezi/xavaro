package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebGuard extends WebViewClient
{
    private final static String LOGTAG = "WebGuard";

    private Context context;

    //region Static methods.

    private static JSONObject config = null;

    public static JSONObject getConfig(Context context)
    {
        if (config == null)
        {
            try
            {
                config = StaticUtils.readRawTextResourceJSON(context, R.raw.default_webguard).getJSONObject("webguard");
            }
            catch (Exception ex)
            {
                Log.e(LOGTAG, "getConfig: Cannot read default webguard.");
            }
        }

        return config;
    }

    public static JSONArray getDomainsAllow(Context context)
    {
        try
        {
            return getConfig(context).getJSONObject("domains").getJSONArray("allow");
        }
        catch (Exception ex)
        {
            Log.e(LOGTAG, "getDomainsAllow: Cannot read config.");
        }

        return null;
    }

    public static JSONArray getDomainsDeny(Context context)
    {
        try
        {
            return getConfig(context).getJSONObject("domains").getJSONArray("deny");
        }
        catch (Exception ex)
        {
            Log.e(LOGTAG, "getDomainsAllow: Cannot read config.");
        }

        return null;
    }

    //endregion

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        Log.d(LOGTAG, "URL=" + url);

        Toast.makeText(context,url,Toast.LENGTH_LONG).show();

        view.loadUrl(url);

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

    //
    // Validate host name against wildcard domain specification.
    //
    private boolean validateHost(String host,String domain)
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
                if (! restparts[ restinx ].equals(hostparts[ hostinx ]))
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
        if (url.contains("smartadserver.com") && url.contains("diff/251"))
        {
            Log.d(LOGTAG, "Allow URL=" + url);

            return null;
        }

        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        JSONArray domain_allow = getDomainsAllow(context);
        JSONArray domain_deny  = getDomainsDeny(context);

        boolean allow;
        String domain;
        int inx;

        //
        // Check positive domain list.
        //

        allow = false;

        if (domain_allow != null)
        {
            try
            {
                for (inx = 0; inx < domain_allow.length(); inx++)
                {
                    domain = domain_allow.getString(inx);

                    if (validateHost(host,domain))
                    {
                        allow = true;
                        break;
                    }
                }
            }
            catch (JSONException ignore)
            {
            }
        }

        if (allow)
        {
            Log.d(LOGTAG,"checkUrlResource: allow " + host);

            return null;
        }

        //
        // Check negative domain list.
        //

        allow = true;

        if (domain_deny != null)
        {
            try
            {
                for (inx = 0; inx < domain_deny.length(); inx++)
                {
                    domain = domain_deny.getString(inx);

                    if (validateHost(host,domain))
                    {
                        allow = false;
                        break;
                    }
                }
            }
            catch (JSONException ignore)
            {
            }
        }

        if (! allow)
        {
            Log.d(LOGTAG, "checkUrlResource: deny " + host);

            return new WebResourceResponse("text/plain", "utf-8", null);
        }

        if ((! url.endsWith(".png")) && (! url.endsWith(".jpg")) && (! url.endsWith(".gif")) && (! url.endsWith(".ico")))
        {
            Log.d(LOGTAG, "checkUrlResource: load " + host);
        }

        return null;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }
}
