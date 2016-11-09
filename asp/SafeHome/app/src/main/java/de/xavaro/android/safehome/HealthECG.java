package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.ActivityOldManager;
import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.Json;

public class HealthECG extends HealthBase
{
    private static final String LOGTAG = HealthECG.class.getSimpleName();

    private static HealthECG instance;

    public static HealthECG getInstance()
    {
        if (instance == null) instance = new HealthECG("ecg");

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    private HealthECG(String deviceType)
    {
        this.deviceType = deviceType;
    }

    @Override
    protected void evaluateEvents()
    {
    }

    @Override
    protected void evaluateMessage()
    {
        if (actRecord == null) return;

        int noi = Json.getInt(actRecord, "noi");
        int pls = Json.getInt(actRecord, "pls");
        int raf = Json.getInt(actRecord, "raf");
        int waf = Json.getInt(actRecord, "waf");

        String sm;
        String lm;
        String am;

        boolean iswarning = false;

        if (noi != 0)
        {
            sm = Simple.getTrans(R.string.health_ecg_noise_spoken);
            lm = Simple.getTrans(R.string.health_ecg_noise_logger);
            am = Simple.getTrans(R.string.health_ecg_noise_assist, Simple.getOwnerName());
        }
        else
        {
            sm = Simple.getTrans(R.string.health_ecg_valid_spoken);
            lm = Simple.getTrans(R.string.health_ecg_valid_logger);
            am = Simple.getTrans(R.string.health_ecg_valid_assist, Simple.getOwnerName());

            String at = Simple.getTrans(R.string.health_ecg_pls, pls);

            //
            // Check alerts.
            //

            if (Simple.getSharedPrefBoolean("health.ecg.alert.enable"))
            {
                //
                // Check rhythm and wave form.
                //

                if ((raf == 0) && (waf == 0))
                {
                    at += " " + Simple.getTrans(R.string.health_ecg_ruw_ok);
                }
                else
                {
                    if ((raf != 0) && (waf != 0))
                    {
                        at += " " + Simple.getTrans(R.string.health_ecg_ruw_bad);

                        iswarning = true;
                    }
                    else
                    {
                        if (raf == 0)
                        {
                            at += " " + Simple.getTrans(R.string.health_ecg_raf_ok);
                        }
                        else
                        {
                            at += " " + Simple.getTrans(R.string.health_ecg_raf_bad);

                            iswarning = true;
                        }

                        if (waf == 0)
                        {
                            at += " " + Simple.getTrans(R.string.health_ecg_waf_ok);
                        }
                        else
                        {
                            at += " " + Simple.getTrans(R.string.health_ecg_waf_bad);

                            iswarning = true;
                        }
                    }
                }

                //
                // Check puls.
                //

                int low = Simple.getSharedPrefInt("health.ecg.alert.lowpls");

                if ((low > 0) && (low >= pls))
                {
                    at += " " + Simple.getTrans(R.string.health_ecg_pls_low);

                    iswarning = true;
                }

                int high = Simple.getSharedPrefInt("health.ecg.alert.highpls");

                if ((high > 0) && (high <= pls))
                {
                    at += " " + Simple.getTrans(R.string.health_ecg_pls_high);

                    iswarning = true;
                }
            }

            sm += " " + at;
            lm += " " + at;
            am += " " + at;
        }

        Speak.speak(sm);
        ActivityOldManager.recordActivity(lm);

        handleAssistance(am, iswarning);
    }
}
