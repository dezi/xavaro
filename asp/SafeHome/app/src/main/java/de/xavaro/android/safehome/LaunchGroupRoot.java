package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchGroupRoot extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupRoot.class.getSimpleName();

    public LaunchGroupRoot(Context context)
    {
        super(context);
    }

    public static JSONObject getConfig()
    {
        //
        // Aquire all current available items.
        //

        ArrayList<JSONArray> configs = new ArrayList<>();

        configs.add(LaunchItemToday.getConfig());
        configs.add(LaunchItemAlertcall.getConfig());

        //
        // Integrate all configs into one list.
        //

        JSONArray launchitems = new JSONArray();

        for (JSONArray config : configs)
        {
            for (int inx = 0; inx < config.length(); inx++)
            {
                JSONObject launchitem = Json.getObject(config, inx);

                Json.put(launchitems, launchitem);
            }
        }

        JSONArray sorted = Json.sortInteger(launchitems, "order", false);

        JSONObject config = new JSONObject();
        Json.put(config, "launchitems", sorted);

        Log.d(LOGTAG, "getConfig: " + config.toString());

        return config;
    }
}
