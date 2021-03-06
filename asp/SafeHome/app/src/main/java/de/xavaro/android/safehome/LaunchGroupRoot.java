package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchGroupRoot extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupRoot.class.getSimpleName();

    public LaunchGroupRoot(Context context)
    {
        super(context);
    }

    private static void insertDuplicateMerge(JSONArray launchitems, JSONObject newlaunchitem)
    {
        if (newlaunchitem.has("launchitems"))
        {
            String newtype = Json.getString(newlaunchitem, "type");
            String newsubtype = Json.getString(newlaunchitem, "subtype");

            for (int inx = 0; inx < launchitems.length(); inx++)
            {
                JSONObject oldlaunchitem = Json.getObject(launchitems, inx);
                if ((oldlaunchitem == null) || !oldlaunchitem.has("launchitems")) continue;

                String oldtype = Json.getString(oldlaunchitem, "type");
                String oldsubtype = Json.getString(oldlaunchitem, "subtype");

                if (!Simple.equals(oldtype, newtype)) continue;
                if (!Simple.equals(oldsubtype, newsubtype)) continue;

                //
                // Merge launchitems.
                //

                JSONArray oldlaunchitems = Json.getArray(oldlaunchitem, "launchitems");
                JSONArray newlaunchitems = Json.getArray(newlaunchitem, "launchitems");

                if ((oldlaunchitems == null) || (newlaunchitems == null)) return;

                for (int cnt = 0; cnt < newlaunchitems.length(); cnt++)
                {
                    Json.put(oldlaunchitems, Json.getObject(newlaunchitems, cnt));
                }

                return;
            }
        }

        Json.put(launchitems, newlaunchitem);
    }

    private static void insertIdentifiers(JSONArray launchitems)
    {
        if (launchitems != null)
        {
            for (int inx = 0; inx < launchitems.length(); inx++)
            {
                JSONObject launchitem = Json.getObject(launchitems, inx);
                if ((launchitem == null) || launchitem.has("identifier")) continue;

                Json.put(launchitem, "identifier", UUID.randomUUID().toString());

                if (launchitem.has("launchitems"))
                {
                    insertIdentifiers(Json.getArray(launchitem, "launchitems"));
                }
            }
        }
    }

    public static JSONObject getConfig()
    {
        //
        // Aquire all currently configured items.
        //

        ArrayList<JSONArray> configs = new ArrayList<>();

        //configs.add(LaunchItemAlertcall.getConfig());
        //configs.add(LaunchItemVoice.getConfig());

        configs.add(LaunchItemBeta.getConfig());
        configs.add(LaunchItemToday.getConfig());
        configs.add(LaunchItemBattery.getConfig());
        configs.add(LaunchItemPrepaid.getConfig());

        configs.add(LaunchGroupCalls.getConfig("calls","important"));

        configs.add(LaunchGroupHealth.getConfig());

        configs.add(LaunchGroupComm.PhoneGroup.getConfig());
        configs.add(LaunchGroupComm.SkypeGroup.getConfig());
        configs.add(LaunchGroupComm.WhatsappGroup.getConfig());
        configs.add(LaunchGroupComm.XavaroGroup.getConfig());

        configs.add(LaunchGroupSocial.getConfig());

        configs.add(LaunchGroupWebStream.getConfig("iptv"));
        configs.add(LaunchGroupWebStream.getConfig("iprd"));

        configs.add(LaunchGroupWebFrame.getConfig("ioc", "newspaper"));
        configs.add(LaunchGroupWebFrame.getConfig("ioc", "pictorial"));
        configs.add(LaunchGroupWebFrame.getConfig("ioc", "magazine"));
        configs.add(LaunchGroupWebFrame.getConfig("ioc", "shopping"));
        configs.add(LaunchGroupWebFrame.getConfig("ioc", "erotics"));

        configs.add(LaunchGroupWebApps.getConfig());

        configs.add(LaunchGroupApps.getConfig("discounter"));

        configs.add(LaunchGroupMediaImage.getConfig());
        configs.add(LaunchGroupMediaVideo.getConfig());

        configs.add(LaunchGroupDeveloper.getConfig());

        //
        // Integrate all configs into one list.
        //

        JSONArray launchitems = new JSONArray();

        for (JSONArray config : configs)
        {
            if (config == null) continue;

            for (int inx = 0; inx < config.length(); inx++)
            {
                JSONObject launchitem = Json.getObject(config, inx);

                insertDuplicateMerge(launchitems, launchitem);
            }
        }

        insertIdentifiers(launchitems);

        JSONArray sorted = Json.sortInteger(launchitems, "order", false);

        JSONObject config = new JSONObject();
        Json.put(config, "launchitems", sorted);

        return config;
    }
}
