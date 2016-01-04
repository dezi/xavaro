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

    public static void initialize(Context context)
    {
        Log.d(LOGTAG,"initialize");

        SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

        if (sp.getBoolean("health.bpm.enable", false))
        {
            String bpmDevice = sp.getString("health.bpm.device",null);

            if ((bpmDevice != null) && ! bpmDevice.equals("unknown"))
            {
                HealthBPM.getInstance().setBlueTooth(new BlueToothBPM(context, bpmDevice));
            }
        }

        if (sp.getBoolean("health.scale.enable", false))
        {
            String scaleDevice = sp.getString("health.scale.device",null);

            if ((scaleDevice != null) && ! scaleDevice.equals("unknown"))
            {
                HealthScale.getInstance().setBlueTooth(new BlueToothScale(context, scaleDevice));
            }
        }

        if (sp.getBoolean("health.sensor.enable", false))
        {
            String sensorDevice = sp.getString("health.sensor.device",null);

            if ((sensorDevice != null) && ! sensorDevice.equals("unknown"))
            {
                HealthSensor.getInstance().setBlueTooth(new BlueToothSensor(context, sensorDevice));
            }
        }
    }

    public static void subscribeDevice(BlueTooth.BlueToothConnectCallback subscriber, String subtype)
    {
        if (subtype.equals("bpm") && (HealthBPM.getInstance() != null))
        {
            HealthBPM.subscribe(subscriber);
        }

        if (subtype.equals("scale") && (HealthScale.getInstance() != null))
        {
            HealthScale.subscribe(subscriber);
        }

        if (subtype.equals("sensor") && (HealthSensor.getInstance() != null))
        {
            HealthSensor.subscribe(subscriber);
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
                if (prefkey.equals("health.bpm.enable") && sp.getBoolean("health.bpm.enable",false))
                {
                    JSONObject entry = new JSONObject();

                    entry.put("type", "health");
                    entry.put("label", "Blutdruck");
                    entry.put("subtype", "bpm");

                    launchitems.put(entry);
                }

                if (prefkey.equals("health.scale.enable") && sp.getBoolean("health.scale.enable",false))
                {
                    JSONObject entry = new JSONObject();

                    entry.put("type", "health");
                    entry.put("label", "Gewicht");
                    entry.put("subtype", "scale");

                    launchitems.put(entry);
                }

                if (prefkey.equals("health.sensor.enable") && sp.getBoolean("health.sensor.enable",false))
                {
                    JSONObject entry = new JSONObject();

                    entry.put("type", "health");
                    entry.put("label", "Aktivität");
                    entry.put("subtype", "sensor");

                    launchitems.put(entry);
                }

                if (prefkey.equals("health.glucose.enable") && sp.getBoolean("health.glucose.enable",false))
                {
                    JSONObject entry = new JSONObject();

                    entry.put("type", "health");
                    entry.put("label", "Blutzucker");
                    entry.put("subtype", "glucose");

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
