package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;

import de.xavaro.android.common.ActivityOldManager;
import de.xavaro.android.common.HealthData;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.Json;

public class HealthECG extends HealthBase
{
    private static final String LOGTAG = HealthECG.class.getSimpleName();

    private static HealthECG instance;

    public static HealthECG getInstance()
    {
        if (instance == null) instance = new HealthECG();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    private HealthECG()
    {
        super();

        this.deviceType = "ecg";
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

            if (! at.isEmpty())
            {
                sm += " " + at;
                lm += " " + at;
                am += " " + at;
            }
        }

        Speak.speak(sm);
        ActivityOldManager.recordActivity(lm);

        handleAssistance(am, iswarning);

        if (connectCallback != null)
        {
            connectCallback.onBluetoothUpdated(deviceName);
        }
    }

    @Override
    @SuppressLint("RtlHardcoded")
    public View createListView()
    {
        LinearLayout view = (LinearLayout) super.createListView();

        TextView pulseView = new TextView(Simple.getActContext());
        pulseView.setLayoutParams(Simple.layoutParamsWM());
        pulseView.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(pulseView, 40, 0, 0, 0);
        pulseView.setTextSize(Simple.getDeviceTextSize(24f));
        pulseView.setTypeface(null, Typeface.BOLD);
        pulseView.setId(android.R.id.content);
        view.addView(pulseView);

        LinearLayout iconLayout = new LinearLayout(Simple.getActContext());
        iconLayout.setLayoutParams(Simple.layoutParamsWM());
        iconLayout.setOrientation(LinearLayout.HORIZONTAL);
        iconLayout.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(iconLayout, 20, 0, 0, 0);
        view.addView(iconLayout);

        ImageView noiView = new ImageView(Simple.getActContext());
        noiView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        noiView.setId(android.R.id.button1);
        Simple.setPadding(noiView, 20, 0, 0, 0);
        iconLayout.addView(noiView);

        ImageView rafView = new ImageView(Simple.getActContext());
        rafView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        rafView.setId(android.R.id.button2);
        Simple.setPadding(rafView, 20, 0, 0, 0);
        iconLayout.addView(rafView);

        ImageView wafView = new ImageView(Simple.getActContext());
        wafView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        wafView.setId(android.R.id.button3);
        Simple.setPadding(wafView, 20, 0, 0, 0);
        iconLayout.addView(wafView);

        ImageView alertView = new ImageView(Simple.getActContext());
        alertView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        alertView.setId(android.R.id.icon1);
        Simple.setPadding(alertView, 20, 0, 0, 0);
        iconLayout.addView(alertView);

        LinearLayout buttonLayout = new LinearLayout(Simple.getActContext());
        buttonLayout.setLayoutParams(Simple.layoutParamsMM());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        view.addView(buttonLayout);

        ImageView buttonView = new ImageView(Simple.getActContext());
        buttonView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(60),Simple.WC));
        buttonView.setId(android.R.id.toggle);
        Simple.setPadding(buttonView, 0, 0, 10, 0);
        buttonLayout.addView(buttonView);

        return view;
    }

    @Override
    public void populateListView(View view, int position, JSONObject item)
    {
        super.populateListView(view, position, item);

        int pls = Json.getInt(item, "pls");
        int noi = Json.getInt(item, "noi");
        int dap = Json.getInt(item, "dap");

        TextView pulseView = (TextView) view.findViewById(android.R.id.content);

        ImageView noiView = (ImageView) view.findViewById(android.R.id.button1);
        ImageView rafView = (ImageView) view.findViewById(android.R.id.button2);
        ImageView wafView = (ImageView) view.findViewById(android.R.id.button3);

        ImageView alertView = (ImageView) view.findViewById(android.R.id.icon1);
        ImageView buttonView = (ImageView) view.findViewById(android.R.id.toggle);

        if (noi != 0)
        {
            pulseView.setText("Puls" + ": " + "EE");

            noiView.setImageResource(R.drawable.health_ecg_ok_dim_300x200);

            rafView.setVisibility(View.INVISIBLE);
            wafView.setVisibility(View.INVISIBLE);
            alertView.setVisibility(View.INVISIBLE);
        }
        else
        {
            pulseView.setText("Puls" + ": " + pls);

            noiView.setImageResource(R.drawable.health_ecg_ok_300x200);

            if (dap == 0)
            {
                rafView.setImageResource(R.drawable.health_ecg_nodata_300x200);
                rafView.setVisibility(View.VISIBLE);
            }
            else
            {
                if (!Simple.getSharedPrefBoolean("health.ecg.alert.enable"))
                {
                    rafView.setVisibility(View.INVISIBLE);
                    wafView.setVisibility(View.INVISIBLE);
                    alertView.setVisibility(View.INVISIBLE);
                }
                else
                {
                    rafView.setImageResource((Json.getInt(item, "raf") == 0)
                            ? R.drawable.health_ecg_rhythm_dim_300x200
                            : R.drawable.health_ecg_rhythm_300x200);

                    wafView.setImageResource((Json.getInt(item, "waf") == 0)
                            ? R.drawable.health_ecg_wave_dim_300x200
                            : R.drawable.health_ecg_wave_300x200);

                    int low = Simple.getSharedPrefInt("health.ecg.alert.lowpls");
                    int high = Simple.getSharedPrefInt("health.ecg.alert.highpls");

                    if ((low > 0) && (low >= pls))
                    {
                        alertView.setImageResource(R.drawable.health_ecg_pls_low_300x200);
                        alertView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if ((high > 0) && (high <= pls))
                        {
                            alertView.setImageResource(R.drawable.health_ecg_pls_high_300x200);
                            alertView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            alertView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        }

        if (dap == 0)
        {
            buttonView.setImageResource(R.drawable.health_ecg_display_dim_256x256);
            buttonView.setOnClickListener(null);
        }
        else
        {
            final JSONObject cbitem = item;

            buttonView.setImageResource(R.drawable.health_ecg_display_256x256);
            buttonView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    makeDisplay(view, cbitem);
                }
            });
        }
    }

    private void makeDisplay(View view, JSONObject item)
    {
        Log.d(LOGTAG, "makeDisplay: json=" + item.toString());

        String extname = Json.getString(item, "exf");
        if (extname == null) return;

        File extfile = new File(HealthData.getDataDir(), extname);
        JSONObject data = Simple.getFileJSONObject(extfile);
        if (data == null) return;

        HealthECGDisplay display = new HealthECGDisplay(Simple.getActContext(), null);

        display.setConfig(data);

        ((HomeActivity) Simple.getActContext()).addViewToBackStack(display);
    }
}
