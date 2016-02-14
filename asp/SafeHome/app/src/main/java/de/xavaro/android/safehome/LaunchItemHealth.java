package de.xavaro.android.safehome;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import de.xavaro.android.common.Json;

public class LaunchItemHealth extends LaunchItem implements BlueTooth.BlueToothConnectCallback
{
    private final static String LOGTAG = LaunchItemHealth.class.getSimpleName();

    HealthFrame healthFrame;

    public LaunchItemHealth(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (handler == null) handler = new Handler();

        if (subtype == null)
        {
            icon.setImageResource(GlobalConfigs.IconResHealth);
        }
        else
        {
            if (subtype.equals("bpm"))
            {
                LaunchGroupHealth.subscribeDevice(this, "bpm");
                icon.setImageResource(GlobalConfigs.IconResHealthBPM);
            }

            if (subtype.equals("scale"))
            {
                LaunchGroupHealth.subscribeDevice(this, "scale");
                icon.setImageResource(GlobalConfigs.IconResHealthScale);
            }

            if (subtype.equals("sensor"))
            {
                LaunchGroupHealth.subscribeDevice(this, "sensor");
                icon.setImageResource(GlobalConfigs.IconResHealthSensor);
            }

            if (subtype.equals("glucose"))
            {
                LaunchGroupHealth.subscribeDevice(this, "glucose");
                icon.setImageResource(GlobalConfigs.IconResHealthGlucose);
            }

            overicon.setImageResource(GlobalConfigs.IconResBlueTooth);
        }
    }

    @Override
    protected void onMyClick()
    {
        launchHealth();
    }

    private void launchHealth()
    {
        if (subtype != null)
        {
            healthFrame = new HealthFrame(context);
            healthFrame.setSubtype(subtype);

            ((HomeActivity) context).addViewToBackStack(healthFrame);
        }
        else
        {
            if (directory == null)
            {
                directory = new LaunchGroupHealth(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }

            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }

    //region BlueTooth connect states

    public final Runnable bluetoothIsConnected = new Runnable()
    {
        @Override
        public void run()
        {
            overlay.setVisibility(VISIBLE);
        }
    };

    public final Runnable bluetoothIsDisconnected = new Runnable()
    {
        @Override
        public void run()
        {
            overlay.setVisibility(INVISIBLE);
        }
    };

    public void onBluetoothConnect(String deviceName)
    {
        Log.d(LOGTAG, "onBluetoothConnect: " + deviceName);

        handler.post(bluetoothIsConnected);
    }

    public void onBluetoothFakeConnect(String deviceName)
    {
        Log.d(LOGTAG, "onBluetoothFakeConnect: " + deviceName);

        handler.post(bluetoothIsConnected);
    }

    public void onBluetoothDisconnect(String deviceName)
    {
        Log.d(LOGTAG, "onBluetoothDisconnect: " + deviceName);

        handler.removeCallbacks(bluetoothIsConnected);
        handler.post(bluetoothIsDisconnected);
    }

    public void onBluetoothFakeDisconnect(String deviceName)
    {
        Log.d(LOGTAG, "onBluetoothFakeDisconnect: " + deviceName);

        handler.removeCallbacks(bluetoothIsConnected);
        handler.post(bluetoothIsDisconnected);
    }

    //endregion BlueTooth connect states
}
