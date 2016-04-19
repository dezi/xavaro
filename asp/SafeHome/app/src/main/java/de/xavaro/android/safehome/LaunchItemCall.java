package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.WebApp;
import de.xavaro.android.common.WebAppView;
import de.xavaro.android.common.WebLib;

public class LaunchItemCall extends LaunchItem
{
    private final static String LOGTAG = LaunchItemCall.class.getSimpleName();

    public LaunchItemCall(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (config.has("subitem"))
        {
            String iconurl = Json.getString(config, "icon");
            icon.setImageDrawable(WebLib.getIconDrawable("calls", iconurl));
        }
        else
        {
            if (Simple.equals(subtype, "important"))
            {
                icon.setImageResource(GlobalConfigs.IconResCallImportant);
            }

            if (directory == null)
            {
                directory = new LaunchGroupCalls(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }
        }
    }

    @Override
    protected void onMyClick()
    {
        launchCall();
    }

    private void launchCall()
    {
        if (config.has("subitem"))
        {
            if (config.has("phonenumber"))
            {
                try
                {
                    String phonenumber = config.getString("phonenumber");
                    phonenumber = phonenumber.replace("#", "%23");

                    Uri uri = Uri.parse("tel:" + phonenumber);
                    Intent sendIntent = new Intent(Intent.ACTION_CALL, uri);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setPackage("com.android.server.telecom");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }
                catch (Exception ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }
        }
        else
        {
            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }
}
