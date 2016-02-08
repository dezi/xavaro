package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

public class LaunchItemApps extends LaunchItem
{
    private final static String LOGTAG = LaunchItemApps.class.getSimpleName();

    public LaunchItemApps(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        ImageView targetIcon = icon;

        if (subtype.equals("discounter"))
        {
            icon.setImageResource(GlobalConfigs.IconResAppsDiscounter);
        }

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);
    }

    @Override
    protected void onMyClick()
    {
        if (subtype.equals("discounter")) launchDiscounter();
    }

    private void launchDiscounter()
    {
        if (config.has("phonenumber"))
        {
            try
            {
                String phonenumber = config.getString("phonenumber");
                String subtype = config.has("subtype") ? config.getString("subtype") : "text";

                if (subtype.equals("text"))
                {
                    Uri uri = Uri.parse("smsto:" + phonenumber);
                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setPackage("com.android.mms");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }

                if (subtype.equals("voip"))
                {
                    Uri uri = Uri.parse("tel:" + phonenumber);
                    Intent sendIntent = new Intent(Intent.ACTION_CALL, uri);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setPackage("com.android.server.telecom");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null)  directory = new LaunchGroupApps.DiscounterGroup(context);

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
