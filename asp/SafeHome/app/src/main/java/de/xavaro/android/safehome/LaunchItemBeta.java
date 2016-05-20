package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebLib;

public class LaunchItemBeta extends LaunchItem implements NotifyIntent.NotifiyService
{
    private final static String LOGTAG = LaunchItemBeta.class.getSimpleName();

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

        Simple.makePost(checkBeta);
    }

    private String latestVersion;
    private boolean actionDue;

    private final Runnable checkBeta = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "checkBeta...");

            JSONObject beta = WebLib.getConfig("beta");

            if (beta != null)
            {
                JSONObject betaapp = Json.getObject(beta, Simple.getAppName());

                if (betaapp != null)
                {
                    latestVersion = Json.getString(betaapp, "latest");

                    if ((latestVersion != null) && (latestVersion.compareTo(GlobalConfigs.BetaVersion) > 0))
                    {
                        overicon.setImageResource(R.drawable.circle_green_256x256);
                        overtext.setText(R.string.simple_new);
                        overlay.setVisibility(VISIBLE);

                        actionDue = true;
                    }
                }
            }

            Simple.makePost(checkBeta, 3600 * 1000);
        }
    };

    @Override
    public NotifyIntent onGetNotifiyIntent()
    {
        NotifyIntent intent = new NotifyIntent();

        intent.actionDue = actionDue;

        if (actionDue)
        {
            intent.title = "Es ist eine neue Version verfügbar.";
            intent.declineText = "Morgen erinnern";
            intent.declineRunner = declineRunner;
            intent.followText = "Version installieren";
            intent.followRunner = launchBetaRunner;
            intent.importance = NotifyIntent.REMINDER;
        }
        else
        {
            intent.title = "Sie haben bereits die aktuelle Version.";
            intent.declineText = null;
            intent.followText = "Infos anzeigen";
            intent.followRunner = launchBetaRunner;
            intent.importance = NotifyIntent.INFOONLY;
        }

        return intent;
    }

    private final Runnable launchBetaRunner = new Runnable()
    {
        @Override
        public void run()
        {
            launchBeta();
        }
    };

    private final Runnable declineRunner = new Runnable()
    {
        @Override
        public void run()
        {
        }
    };

    @Override
    protected void onMyClick()
    {
        if (type.equals("beta")) launchBeta();
    }

    private void launchBeta()
    {
        LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context);
        webappFrame.setWebAppName("betaversion");
        webappFrame.setParent(this);

        ((HomeActivity) context).addWorkerToBackStack("Beta Version", webappFrame);
    }
}
