package de.xavaro.android.safehome;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.BatteryManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.Simple;

public class LaunchItemBattery extends LaunchItem implements NotifyIntent.NotifyService
{
    private final static String LOGTAG = LaunchItemBattery.class.getSimpleName();

    public static JSONArray getConfig()
    {
        JSONArray launchitems = new JSONArray();

        if (Simple.getSharedPrefBoolean("monitors.enable"))
        {
            String mode = Simple.getSharedPrefString("monitors.battery.mode");

            JSONObject launchitem = new JSONObject();

            Json.put(launchitem, "type", "battery");
            Json.put(launchitem, "label", "Batterie");
            Json.put(launchitem, "order", 100);

            if (Simple.equals(mode, "home"))
            {
                Json.put(launchitems, launchitem);
            }
            else
            {
                String rem = Simple.getSharedPrefString("monitors.battery.remind");
                String wrn = Simple.getSharedPrefString("monitors.battery.warn");
                String ass = Simple.getSharedPrefString("monitors.battery.assistance");

                if (((rem != null) && !rem.equals("never")) ||
                        ((wrn != null) && !wrn.equals("never")) ||
                        ((ass != null) && !ass.equals("never")))
                {
                    Json.put(launchitem, "notify", "only");
                    Json.put(launchitems, launchitem);
                }
            }
        }

        return launchitems;
    }

    public LaunchItemBattery(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        icon.setImageResource(R.drawable.battery_discon_0_600x600);

        postDelayed(updateBatteryPost, 1000);
    }

    @Override
    public NotifyIntent onGetNotifiyIntent()
    {
        return BatteryManager.getNotifyEvent();
    }

    @Override
    protected void onMyClick()
    {
    }

    private String lastresname;

    private void updateBatteryNow()
    {
        JSONObject status = BatteryManager.getBatteryStatus();

        String statustag = Json.getString(status, "statustag");
        int percent = Json.getInt(status, "percent");

        String chargetab = "discon";

        if (Simple.equals(statustag, "charging")) chargetab = "charge";
        if (Simple.equals(statustag, "discharging")) chargetab = "discharge";

        String loadtag = "100";

        if (percent <= 75) loadtag = "75";
        if (percent <= 50) loadtag = "50";
        if (percent <= 25) loadtag = "25";
        if (percent <=  5) loadtag =  "0";

        String resname = "battery" + "_" + chargetab + "_" + loadtag + "_600x600";

        if (! Simple.equals(lastresname, resname))
        {
            Log.d(LOGTAG, "updateBatteryNow: status=" + statustag + " percent=" + percent);

            Resources res = Simple.getAnyContext().getResources();
            int resid = res.getIdentifier(resname, "drawable", Simple.getPackageName());
            icon.setImageResource(resid);
            lastresname = resname;
        }

        setLabelText(Json.getString(config, "label") + " " + percent + "%");
    }

    private final Runnable updateBatteryPost = new Runnable()
    {
        @Override
        public void run()
        {
            updateBatteryNow();

            postDelayed(updateBatteryPost, 3 * 1000);
        }
    };
}
