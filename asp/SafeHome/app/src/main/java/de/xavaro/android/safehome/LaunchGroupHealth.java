package de.xavaro.android.safehome;

import android.support.annotation.Nullable;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchGroupHealth extends LaunchGroup
{
    private static final String LOGTAG = LaunchGroupHealth.class.getSimpleName();

    public LaunchGroupHealth(Context context)
    {
        super(context);
    }

    public static JSONArray getConfig()
    {
        JSONArray home = new JSONArray();
        JSONArray adir = new JSONArray();

        if (Simple.getSharedPrefBoolean("health.bpm.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "bpm");
            Json.put(entry, "label", "Blutdruck");
            Json.put(entry, "order", 1000);

            Json.put(Simple.sharedPrefEquals("health.bpm.icon", "home") ? home : adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.oxy.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "oxy");
            Json.put(entry, "label", "Blutsauerstoff");
            Json.put(entry, "order", 1000);

            Json.put(Simple.sharedPrefEquals("health.oxy.icon", "home") ? home : adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.scale.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "scale");
            Json.put(entry, "label", "Gewicht");
            Json.put(entry, "order", 1010);

            Json.put(Simple.sharedPrefEquals("health.scale.icon", "home") ? home : adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.sensor.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "sensor");
            Json.put(entry, "label", "Aktivität");
            Json.put(entry, "order", 1020);

            Json.put(Simple.sharedPrefEquals("health.sensor.icon", "home") ? home : adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.glucose.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "glucose");
            Json.put(entry, "label", "Blutzucker");
            Json.put(entry, "order", 1030);

            Json.put(Simple.sharedPrefEquals("health.glucose.icon", "home") ? home : adir, entry);
        }

        if (adir.length() > 0)
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "label", "Vitaldaten");
            Json.put(entry, "order", 1100);

            Json.put(entry, "launchitems", adir);
            Json.put(home, entry);
        }

        reConfigure();

        return home;
    }

    @Nullable
    private static String getDevice(String subtype)
    {
        if (Simple.getSharedPrefBoolean("health." + subtype + ".enable"))
        {
            String bpmDevice = Simple.getSharedPrefString("health." + subtype + ".device");

            if ((bpmDevice != null) && ! bpmDevice.equals("unknown"))
            {
                return bpmDevice;
            }
        }

        return null;
    }

    private static void reConfigure()
    {
        Context context = Simple.getAppContext();

        if (getDevice("bpm") != null)
        {
            if (! HealthBPM.getInstance().isConfigured())
            {
                HealthBPM.getInstance().setBlueTooth(
                        new BlueToothBPM(context, getDevice("bpm")));
            }
        }
        else
        {
            if (HealthBPM.getInstance().isConfigured())
            {
                HealthBPM.getInstance().setBlueTooth(null);
            }
        }

        if (getDevice("oxy") != null)
        {
            if (! HealthOxy.getInstance().isConfigured())
            {
                HealthOxy.getInstance().setBlueTooth(
                        new BlueToothOxy(context, getDevice("oxy")));
            }
        }
        else
        {
            if (HealthOxy.getInstance().isConfigured())
            {
                HealthOxy.getInstance().setBlueTooth(null);
            }
        }

        if (getDevice("scale") != null)
        {
            if (! HealthScale.getInstance().isConfigured())
            {
                HealthScale.getInstance().setBlueTooth(
                        new BlueToothScale(context, getDevice("scale")));
            }
        }
        else
        {
            if (HealthScale.getInstance().isConfigured())
            {
                HealthScale.getInstance().setBlueTooth(null);
            }
        }

        if (getDevice("sensor") != null)
        {
            if (! HealthSensor.getInstance().isConfigured())
            {
                HealthSensor.getInstance().setBlueTooth(
                        new BlueToothSensor(context, getDevice("sensor")));
            }
        }
        else
        {
            if (HealthSensor.getInstance().isConfigured())
            {
                HealthSensor.getInstance().setBlueTooth(null);
            }
        }

        if (getDevice("glucose") != null)
        {
            if (! HealthGlucose.getInstance().isConfigured())
            {
                HealthGlucose.getInstance().setBlueTooth(
                        new BlueToothGlucose(context, getDevice("glucose")));
            }
        }
        else
        {
            if (HealthGlucose.getInstance().isConfigured())
            {
                HealthGlucose.getInstance().setBlueTooth(null);
            }
        }
    }

    public static void subscribeDevice(BlueTooth.BlueToothConnectCallback subscriber, String subtype)
    {
        if (subtype.equals("bpm") && (HealthBPM.getInstance() != null))
        {
            HealthBPM.subscribe(subscriber);
        }

        if (subtype.equals("oxy") && (HealthOxy.getInstance() != null))
        {
            HealthOxy.subscribe(subscriber);
        }

        if (subtype.equals("scale") && (HealthScale.getInstance() != null))
        {
            HealthScale.subscribe(subscriber);
        }

        if (subtype.equals("sensor") && (HealthSensor.getInstance() != null))
        {
            HealthSensor.subscribe(subscriber);
        }

        if (subtype.equals("glucose") && (HealthGlucose.getInstance() != null))
        {
            HealthGlucose.subscribe(subscriber);
        }
    }
}
