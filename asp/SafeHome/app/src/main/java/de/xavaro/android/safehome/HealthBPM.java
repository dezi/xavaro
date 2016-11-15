package de.xavaro.android.safehome;

import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.ActivityOldManager;
import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.EventManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class HealthBPM extends HealthBase
{
    private static final String LOGTAG = HealthBPM.class.getSimpleName();

    private static HealthBPM instance;

    public static HealthBPM getInstance()
    {
        if (instance == null) instance = new HealthBPM();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    private HealthBPM()
    {
        super();

        this.deviceType = "bpm";
    }

    @Override
    protected void evaluateEvents()
    {
        JSONArray events = EventManager.getComingEvents("webapps.medicator");
        if (events == null) return;

        if (actRecord == null) return;

        String actDts = Json.getString(actRecord, "dts");
        int actDia = Json.getInt(actRecord, "dia");
        int actSys = Json.getInt(actRecord, "sys");
        int actPls = Json.getInt(actRecord, "pls");

        long now = Simple.nowAsTimeStamp();

        for (int inx = 0; inx < events.length(); inx++)
        {
            JSONObject event = Json.getObject(events, inx);

            if ((event == null) || Json.getBoolean(event, "taken")) continue;

            String date = Json.getString(event, "date");
            String medication = Json.getString(event, "medication");

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZB")) continue;

            //
            // Check event and measurement dates.
            //

            long mts = Simple.getTimeStamp(date);
            if (Math.abs(now - mts) > 2 * 3600 * 1000) continue;

            long dts = Simple.getTimeStamp(actDts);
            if (Math.abs(dts - mts) > 2 * 3600 * 1000) continue;

            //
            // Event is suitable.
            //

            Json.put(event, "taken", true);
            Json.put(event, "takendate", actDts);
            Json.put(event, "diastolic", actDia);
            Json.put(event, "systolic", actSys);
            Json.put(event, "puls", actPls);

            EventManager.updateComingEvent("webapps.medicator", event);

            Log.d(LOGTAG, "evaluateEvents: updated=" + event.toString());

            break;
        }

        NotifyManager.removeNotification("medicator.take.bloodpressure");
        Simple.makePost(CommonConfigs.UpdateNotifications);
    }

    @Override
    protected void evaluateMessage()
    {
        if (actRecord == null) return;

        int sys = Json.getInt(actRecord, "sys");
        int dia = Json.getInt(actRecord, "dia");
        int pls = Json.getInt(actRecord, "pls");

        boolean iswarning = false;

        String sm = Simple.getTrans(R.string.health_bpm_spoken, sys, dia);
        String lm = Simple.getTrans(R.string.health_bpm_logger, sys, dia);
        String am = Simple.getTrans(R.string.health_bpm_assist, Simple.getOwnerName(), sys, dia);

        String at = Simple.getTrans(R.string.health_bpm_pls, pls);

        if (Simple.getSharedPrefBoolean("health.bpm.alert.enable"))
        {
            //
            // Check alerts.
            //

            try
            {
                String lowBP = Simple.getSharedPrefString("health.bpm.alert.lowbp");

                if (lowBP != null)
                {
                    String[] lp = lowBP.split(":");

                    if ((lp.length == 2) &&
                            ((Integer.parseInt(lp[ 0 ]) >= sys) ||
                                    (Integer.parseInt(lp[ 1 ]) >= dia)))
                    {
                        at += " " + Simple.getTrans(R.string.health_bpm_bp_low);

                        iswarning = true;
                    }
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            try
            {
                String highBP = Simple.getSharedPrefString("health.bpm.alert.highbp");

                if (highBP != null)
                {
                    String[] hp = highBP.split(":");

                    if ((hp.length == 2) &&
                            ((Integer.parseInt(hp[ 0 ]) <= sys) ||
                                    (Integer.parseInt(hp[ 1 ]) <= dia)))
                    {
                        at += " " + Simple.getTrans(R.string.health_bpm_bp_high);

                        iswarning = true;
                    }
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            int lowPls = Simple.getSharedPrefInt("health.bpm.alert.lowpls");

            if ((lowPls > 0) && (lowPls >= pls))
            {
                at += " " + Simple.getTrans(R.string.health_bpm_pls_low);

                iswarning = true;
            }

            int highPls = Simple.getSharedPrefInt("health.bpm.alert.highpls");

            if ((highPls > 0) && (highPls <= pls))
            {
                at += " " + Simple.getTrans(R.string.health_bpm_pls_high);

                iswarning = true;
            }
        }

        if (! at.isEmpty())
        {
            sm += " " + at.trim();
            lm += " " + at.trim();
            am += " " + at.trim();
        }

        Speak.speak(sm);
        handleActivity(lm, iswarning, GlobalConfigs.IconResHealthBPM);
        handleAssistance(am, iswarning);

        if (connectCallback != null)
        {
            connectCallback.onBluetoothUpdated(deviceName);
        }
    }

    @Override
    public View createListView()
    {
        LinearLayout view = (LinearLayout) super.createListView();

        TextView bpmView = new TextView(Simple.getActContext());
        bpmView.setLayoutParams(Simple.layoutParamsWM());
        bpmView.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(bpmView, 40, 0, 0, 0);
        bpmView.setTextSize(Simple.getDeviceTextSize(24f));
        bpmView.setTypeface(null, Typeface.BOLD);
        bpmView.setId(android.R.id.content);
        view.addView(bpmView);

        LinearLayout iconLayout = new LinearLayout(Simple.getActContext());
        iconLayout.setLayoutParams(Simple.layoutParamsWM());
        iconLayout.setOrientation(LinearLayout.HORIZONTAL);
        iconLayout.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(iconLayout, 20, 0, 0, 0);
        view.addView(iconLayout);

        ImageView bpmAlertView = new ImageView(Simple.getActContext());
        bpmAlertView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        bpmAlertView.setId(android.R.id.icon1);
        Simple.setPadding(bpmAlertView, 20, 0, 0, 0);
        iconLayout.addView(bpmAlertView);

        ImageView plsAlertView = new ImageView(Simple.getActContext());
        plsAlertView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        plsAlertView.setId(android.R.id.icon2);
        Simple.setPadding(plsAlertView, 20, 0, 0, 0);
        iconLayout.addView(plsAlertView);

        return view;
    }

    @Override
    public void populateListView(View view, int position, JSONObject item)
    {
        super.populateListView(view, position, item);

        int sys = Json.getInt(item, "sys");
        int dia = Json.getInt(item, "dia");
        int pls = Json.getInt(item, "pls");

        TextView bpmView = (TextView) view.findViewById(android.R.id.content);

        ImageView bpmAlertView = (ImageView) view.findViewById(android.R.id.icon1);
        ImageView plsAlertView = (ImageView) view.findViewById(android.R.id.icon2);

        String display = Simple.getTrans(R.string.health_bpshort) + ": " + sys + ":" + dia;
        display += " — " + Simple.getTrans(R.string.health_pulse) + ": " + pls;

        bpmView.setText(display);

        if (!Simple.getSharedPrefBoolean("health.bpm.alert.enable"))
        {
            bpmAlertView.setVisibility(View.INVISIBLE);
            plsAlertView.setVisibility(View.INVISIBLE);
        }
        else
        {
            boolean setBP = false;

            String lowBP = Simple.getSharedPrefString("health.bpm.alert.lowbp");

            if (lowBP != null)
            {
                try
                {
                    String[] lp = lowBP.split(":");

                    if ((lp.length == 2) &&
                            ((Integer.parseInt(lp[ 0 ]) >= sys) ||
                                    (Integer.parseInt(lp[ 1 ]) >= dia)))
                    {
                        bpmAlertView.setImageResource(R.drawable.health_bpm_bp_low_300x200);
                        bpmAlertView.setVisibility(View.VISIBLE);
                        setBP = true;
                    }
                }
                catch (Exception ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }

            String highBP = Simple.getSharedPrefString("health.bpm.alert.highbp");

            if (highBP != null)
            {
                try
                {
                    String[] hp = highBP.split(":");

                    if ((hp.length == 2) &&
                            ((Integer.parseInt(hp[ 0 ]) <= sys) ||
                                    (Integer.parseInt(hp[ 1 ]) <= dia)))
                    {
                        bpmAlertView.setImageResource(R.drawable.health_bpm_bp_high_300x200);
                        bpmAlertView.setVisibility(View.VISIBLE);
                        setBP = true;
                    }
                }
                catch (Exception ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }

            if (! setBP)
            {
                bpmAlertView.setImageResource(R.drawable.health_bpm_bp_dim_300x200);
                bpmAlertView.setVisibility(View.VISIBLE);
            }

            int lowPls = Simple.getSharedPrefInt("health.bpm.alert.lowpls");
            int highPls = Simple.getSharedPrefInt("health.bpm.alert.highpls");

            if ((lowPls > 0) && (lowPls >= pls))
            {
                plsAlertView.setImageResource(R.drawable.health_bpm_pls_low_300x200);
                plsAlertView.setVisibility(View.VISIBLE);
            }
            else
            {
                if ((highPls > 0) && (highPls <= pls))
                {
                    plsAlertView.setImageResource(R.drawable.health_bpm_pls_high_300x200);
                    plsAlertView.setVisibility(View.VISIBLE);
                }
                else
                {
                    plsAlertView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
