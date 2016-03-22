package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebLib;

public class LaunchItemBeta extends LaunchItem
{
    private final static String LOGTAG = LaunchItemBeta.class.getSimpleName();

    private LaunchFrameWebApp webappFrame;

    public LaunchItemBeta(Context context)
    {
        super(context);
    }

    public static JSONArray getConfig()
    {
        JSONArray launchitems = new JSONArray();

        if (GlobalConfigs.BetaFlag)
        {
            JSONObject launchitem = new JSONObject();

            Json.put(launchitem, "type", "beta");
            Json.put(launchitem, "label", "Version");
            Json.put(launchitem, "order", 175);

            Json.put(launchitems, launchitem);
        }

        return launchitems;
    }

    @Override
    protected void setConfig()
    {
        icon.setImageResource(CommonConfigs.IconResBetaVersion);

        JSONObject beta = WebLib.getConfig("beta");

        if (beta != null)
        {
            Log.d(LOGTAG, "Beta=" + beta.toString());

            JSONObject betaapp = Json.getObject(beta, Simple.getAppName());

            if (betaapp != null)
            {
                String latest = Json.getString(betaapp, "latest");

                if ((latest != null) && (latest.compareTo(GlobalConfigs.BetaVersion) > 0))
                {
                    overicon.setImageResource(R.drawable.circle_green_256x256);
                    overtext.setText(R.string.simple_new);
                    overlay.setVisibility(VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("beta")) launchBeta();
    }

    @Override
    public void onBackKeyExecuted()
    {
        webappFrame = null;
    }

    private void launchBeta()
    {
        if (webappFrame == null)
        {
            webappFrame = new LaunchFrameWebApp(context);
            webappFrame.setWebAppName("betaversion");
            webappFrame.setParent(this);
        }

        ((HomeActivity) context).addViewToBackStack(webappFrame);
    }
}
