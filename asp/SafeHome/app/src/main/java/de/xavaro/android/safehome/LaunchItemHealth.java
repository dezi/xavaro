package de.xavaro.android.safehome;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import de.xavaro.android.common.Json;

public class LaunchItemHealth extends LaunchItem
    implements BlueTooth.BlueToothConnectCallback
{
    private final static String LOGTAG = LaunchItemAlertcall.class.getSimpleName();

    public LaunchItemHealth(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        String subtype = Json.getString(config, "subtype");

        if (subtype == null)
        {
            icon.setImageResource(GlobalConfigs.IconResHealth);
            icon.setVisibility(VISIBLE);
        }
        else
        {
            if (subtype.equals("bpm"))
            {
                if (handler == null) handler = new Handler();
                LaunchGroupHealth.subscribeDevice(this, "bpm");

                icon.setImageResource(GlobalConfigs.IconResHealthBPM);
                icon.setVisibility(VISIBLE);
            }

            if (subtype.equals("scale"))
            {
                if (handler == null) handler = new Handler();
                LaunchGroupHealth.subscribeDevice(this, "scale");

                icon.setImageResource(GlobalConfigs.IconResHealthScale);
                icon.setVisibility(VISIBLE);
            }

            if (subtype.equals("sensor"))
            {
                if (handler == null) handler = new Handler();
                LaunchGroupHealth.subscribeDevice(this, "sensor");

                icon.setImageResource(GlobalConfigs.IconResHealthSensor);
                icon.setVisibility(VISIBLE);
            }

            if (subtype.equals("glucose"))
            {
                if (handler == null) handler = new Handler();
                LaunchGroupHealth.subscribeDevice(this, "glucose");

                icon.setImageResource(GlobalConfigs.IconResHealthGlucose);
                icon.setVisibility(VISIBLE);
            }
        }

        icon.setVisibility(VISIBLE);
    }

    @Override
    protected void onMyClick()
    {
        launchHealth();
    }

    private void launchHealth()
    {
        if (config.has("subtype"))
        {
            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroupHealth(context);
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    //region BlueTooth connect states

    public final Runnable bluetoothIsConnected = new Runnable()
    {
        @Override
        public void run()
        {
            overicon.setImageResource(GlobalConfigs.IconResBlueTooth);
            overicon.setVisibility(VISIBLE);
            overlay.setVisibility(VISIBLE);
        }
    };

    public final Runnable bluetoothIsDisconnected = new Runnable()
    {
        @Override
        public void run()
        {
            overicon.setVisibility(INVISIBLE);
            overlay.setVisibility(INVISIBLE);
        }
    };

    public void onBluetoothConnect(String deviceName)
    {
        Log.d(LOGTAG, "onBluetoothConnect: " + deviceName);

        //
        // Post delayed in case of sleeping devices with
        // short time idle connect.
        //

        handler.post(bluetoothIsConnected);
    }

    public void onBluetoothDisconnect(String deviceName)
    {
        Log.d(LOGTAG, "onBluetoothDisconnect: " + deviceName);

        handler.removeCallbacks(bluetoothIsConnected);
        handler.post(bluetoothIsDisconnected);
    }

    //endregion BlueTooth connect states
}
