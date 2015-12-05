package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

//
// Guarded access and display of web client.
//

public class WebGuard extends WebViewClient
{
    private final static String LOGTAG = "WebGuard";

    private Context context;

    private Uri initialUri;

    public void setContext(Context context)
    {
        this.context = context;

        getConfig(context);
    }

    public void setInitialUrl(String url)
    {
        initialUri = Uri.parse(url);
    }

    //region Static methods.

    private static JSONObject config = null;

    private static ArrayList<String> regex_allow;
    private static ArrayList<String> regex_deny;
    private static ArrayList<String> domains_allow;
    private static ArrayList<String> domains_deny;

    public static JSONObject getConfig(Context context)
    {
        if (config == null)
        {
            regex_allow   = new ArrayList<String>();
            regex_deny    = new ArrayList<String>();
            domains_allow = new ArrayList<String>();
            domains_deny  = new ArrayList<String>();

            try
            {
                config = StaticUtils.readRawTextResourceJSON(context, R.raw.default_webguard).getJSONObject("webguard");

                // @formatter:off
                regex_allow   = getConfigTreeArray(context, "resource", "regex",   "allow");
                regex_deny    = getConfigTreeArray(context, "resource", "regex",   "deny" );
                domains_allow = getConfigTreeArray(context, "resource", "domains", "allow");
                domains_deny  = getConfigTreeArray(context, "resource", "domains", "deny" );
                // @formatter:on
            }
            catch (Exception ex)
            {
                Log.e(LOGTAG, "getConfig: Cannot read default webguard.");
            }
        }

        return config;
    }

    public static ArrayList<String> getConfigTreeArray(Context context, String arg1, String arg2, String arg3)
    {
        ArrayList<String> list = new ArrayList<String>();

        try
        {
            JSONArray jsonArray = getConfig(context).getJSONObject(arg1).getJSONObject(arg2).getJSONArray(arg3);

            for (int inx = 0; inx < jsonArray.length(); inx++)
            {
                list.add(jsonArray.getString(inx));
            }
        }
        catch (Exception ex)
        {
            Log.e(LOGTAG, "getConfigTreeArray: Cannot read config " + arg1 + "/" + arg2 + "/" + arg3);
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

        if (initialUri.getHost().equals(follow.getHost()))
        {
            view.loadUrl(url);

            return true;
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

        if (url.startsWith("https://twitter.com/") && GlobalConfigs.likeTwitter)
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

        if (url.startsWith("https://plus.google.com/") && GlobalConfigs.likeGooglePlus)
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
            // We like Facebook.
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
                if (!restparts[restinx].equals(hostparts[hostinx]))
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
        int inx;

        //
        // Check positive url regex list.
        //

        for (inx = 0; inx < regex_allow.size(); inx++)
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

        for (inx = 0; inx < regex_deny.size(); inx++)
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

        for (inx = 0; inx < domains_allow.size(); inx++)
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

        for (inx = 0; inx < domains_deny.size(); inx++)
        {
            if (validateHost(host, domains_deny.get(inx)))
            {
                Log.d(LOGTAG, "checkUrlResource: deny " + host);

                return new WebResourceResponse("text/plain", "utf-8", null);
            }
        }

        if ((! url.endsWith(".png")) && (! url.endsWith(".jpg")) && (! url.endsWith(".gif")) && (! url.endsWith(".ico")))
        {
            Log.d(LOGTAG, "checkUrlResource: load " + host);
        }

        return null;
    }

    //endregion
}
