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

            if (Simple.sharedPrefEquals("health.bpm.icon", "home")) Json.put(home, entry);
            if (Simple.sharedPrefEquals("health.bpm.icon", "folder")) Json.put(adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.ecg.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "ecg");
            Json.put(entry, "label", "EKG");
            Json.put(entry, "order", 1000);

            if (Simple.sharedPrefEquals("health.ecg.icon", "home")) Json.put(home, entry);
            if (Simple.sharedPrefEquals("health.ecg.icon", "folder")) Json.put(adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.oxy.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "oxy");
            Json.put(entry, "label", "Blutsauerstoff");
            Json.put(entry, "order", 1000);

            if (Simple.sharedPrefEquals("health.oxy.icon", "home")) Json.put(home, entry);
            if (Simple.sharedPrefEquals("health.oxy.icon", "folder")) Json.put(adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.scale.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "scale");
            Json.put(entry, "label", "Gewicht");
            Json.put(entry, "order", 1010);

            if (Simple.sharedPrefEquals("health.scale.icon", "home")) Json.put(home, entry);
            if (Simple.sharedPrefEquals("health.scale.icon", "folder")) Json.put(adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.thermo.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "thermo");
            Json.put(entry, "label", "Temperatur");
            Json.put(entry, "order", 1000);

            if (Simple.sharedPrefEquals("health.thermo.icon", "home")) Json.put(home, entry);
            if (Simple.sharedPrefEquals("health.thermo.icon", "folder")) Json.put(adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.sensor.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "sensor");
            Json.put(entry, "label", "Aktivität");
            Json.put(entry, "order", 1020);

            if (Simple.sharedPrefEquals("health.sensor.icon", "home")) Json.put(home, entry);
            if (Simple.sharedPrefEquals("health.sensor.icon", "folder")) Json.put(adir, entry);
        }

        if (Simple.getSharedPrefBoolean("health.glucose.enable"))
        {
            JSONObject entry = new JSONObject();

            Json.put(entry, "type", "health");
            Json.put(entry, "subtype", "glucose");
            Json.put(entry, "label", "Blutzucker");
            Json.put(entry, "order", 1030);

            if (Simple.sharedPrefEquals("health.glucose.icon", "home")) Json.put(home, entry);
            if (Simple.sharedPrefEquals("health.glucose.icon", "folder")) Json.put(adir, entry);
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
            String bluetoothDevice = Simple.getSharedPrefString("health." + subtype + ".device");

            if ((bluetoothDevice != null) && ! bluetoothDevice.equals("unknown"))
            {
                return bluetoothDevice;
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

        if (getDevice("ecg") != null)
        {
            if (! HealthECG.getInstance().isConfigured())
            {
                HealthECG.getInstance().setBlueTooth(
                        new BlueToothECG(context, getDevice("ecg")));
            }
        }
        else
        {
            if (HealthECG.getInstance().isConfigured())
            {
                HealthECG.getInstance().setBlueTooth(null);
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

        if (getDevice("thermo") != null)
        {
            if (! HealthThermo.getInstance().isConfigured())
            {
                HealthThermo.getInstance().setBlueTooth(
                        new BlueToothThermo(context, getDevice("thermo")));
            }
        }
        else
        {
            if (HealthThermo.getInstance().isConfigured())
            {
                HealthThermo.getInstance().setBlueTooth(null);
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

        if (subtype.equals("ecg") && (HealthECG.getInstance() != null))
        {
            HealthECG.subscribe(subscriber);
        }

        if (subtype.equals("oxy") && (HealthOxy.getInstance() != null))
        {
            HealthOxy.subscribe(subscriber);
        }

        if (subtype.equals("scale") && (HealthScale.getInstance() != null))
        {
            HealthScale.subscribe(subscriber);
        }

        if (subtype.equals("thermo") && (HealthThermo.getInstance() != null))
        {
            HealthThermo.subscribe(subscriber);
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
