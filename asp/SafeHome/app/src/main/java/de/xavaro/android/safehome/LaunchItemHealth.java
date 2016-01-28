package de.xavaro.android.safehome;

import android.content.Context;
import android.os.Handler;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemHealth extends LaunchItem
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
                HealthGroup.subscribeDevice(this, "bpm");

                icon.setImageResource(GlobalConfigs.IconResHealthBPM);
                icon.setVisibility(VISIBLE);
            }

            if (subtype.equals("scale"))
            {
                if (handler == null) handler = new Handler();
                HealthGroup.subscribeDevice(this, "scale");

                icon.setImageResource(GlobalConfigs.IconResHealthScale);
                icon.setVisibility(VISIBLE);
            }

            if (subtype.equals("sensor"))
            {
                if (handler == null) handler = new Handler();
                HealthGroup.subscribeDevice(this, "sensor");

                icon.setImageResource(GlobalConfigs.IconResHealthSensor);
                icon.setVisibility(VISIBLE);
            }

            if (subtype.equals("glucose"))
            {
                if (handler == null) handler = new Handler();
                HealthGroup.subscribeDevice(this, "glucose");

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
            directory = new HealthGroup(context);
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
