package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class HealthGroup extends LaunchGroup
{
    private static final String LOGTAG = HealthGroup.class.getSimpleName();

    private static BlueTooth bpmBlueTooth;
    private static BlueTooth scaleBlueTooth;

    public static void initialize(Context context)
    {
        SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

        if (sp.getBoolean("health.bpm.enable", false))
        {
            String bpmDevice = sp.getString("health.bpm.device",null);

            if (bpmDevice != null)
            {
                bpmBlueTooth = new BlueToothBPM(context, bpmDevice);

                bpmBlueTooth.connect();
            }
        }

        if (sp.getBoolean("health.scale.enable", false))
        {
            String scaleDevice = sp.getString("health.scale.device",null);

            if (scaleDevice != null)
            {
                scaleBlueTooth = new BlueToothScale(context, scaleDevice);

                scaleBlueTooth.connect();
            }
        }
    }

    public static void subscribeDevice(BlueTooth.BlueToothConnectCallback subscriber, String subtype)
    {
        if (subtype.equals("bpm") && (bpmBlueTooth != null))
        {
            bpmBlueTooth.setConnectCallback(subscriber);
        }

        if (subtype.equals("scale") && (scaleBlueTooth != null))
        {
            scaleBlueTooth.setConnectCallback(subscriber);
        }
    }

    public HealthGroup(Context context)
    {
        super(context);

        this.config = getConfig(context);
    }

    private JSONObject getConfig(Context context)
    {
        try
        {
            JSONObject launchgroup = new JSONObject();
            JSONArray launchitems = new JSONArray();

            SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;
            Map<String, Object> hp = DitUndDat.SharedPrefs.getPrefix("health.");

            for (String prefkey : hp.keySet())
            {
                Object val = hp.get(prefkey);

                Log.d(LOGTAG, "getConfig: " + prefkey + "=" + val);

                if (prefkey.equals("health.bpm.enable"))
                {
                    JSONObject entry = new JSONObject();

                    entry.put("type", "health");
                    entry.put("label", "Blutdruck");
                    entry.put("subtype", "bpm");

                    launchitems.put(entry);
                }

                if (prefkey.equals("health.scale.enable"))
                {
                    JSONObject entry = new JSONObject();

                    entry.put("type", "health");
                    entry.put("label", "Gewicht");
                    entry.put("subtype", "scale");

                    launchitems.put(entry);
                }
            }

            launchgroup.put("launchitems", launchitems);

            return launchgroup;
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }

        return new JSONObject();
    }
}
