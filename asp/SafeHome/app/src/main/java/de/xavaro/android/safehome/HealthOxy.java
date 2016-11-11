package de.xavaro.android.safehome;

import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.ActivityOldManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.EventManager;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.Json;

public class HealthOxy extends HealthBase
{
    private static final String LOGTAG = HealthOxy.class.getSimpleName();

    private static HealthOxy instance;

    public static HealthOxy getInstance()
    {
        if (instance == null) instance = new HealthOxy();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    private HealthOxy()
    {
        super();

        this.deviceType = "oxy";
    }

    @Override
    protected void evaluateEvents()
    {
        JSONArray events = EventManager.getComingEvents("webapps.medicator");
        if (events == null) return;

        if (actRecord == null) return;

        String actDts = Json.getString(actRecord, "dts");
        int actSat = Json.getInt(actRecord, "sat");
        int actPls = Json.getInt(actRecord, "pls");

        long now = Simple.nowAsTimeStamp();

        for (int inx = 0; inx < events.length(); inx++)
        {
            JSONObject event = Json.getObject(events, inx);

            if ((event == null) || Json.getBoolean(event, "taken")) continue;

            String date = Json.getString(event, "date");
            String medication = Json.getString(event, "medication");

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZO")) continue;

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
            Json.put(event, "saturation", actSat);
            Json.put(event, "puls", actPls);

            EventManager.updateComingEvent("webapps.medicator", event);

            Log.d(LOGTAG, "evaluateEvents: updated=" + event.toString());

            break;
        }

        NotifyManager.removeNotification("medicator.take.bloodoxygen");
        Simple.makePost(CommonConfigs.UpdateNotifications);

        //
        // Check for demo mode take pills after
        // oxygen measurement as the trigger.
        //

        if (Simple.getSharedPrefBoolean("developer.demomode.takepills"))
        {
            for (int inx = 0; inx < events.length(); inx++)
            {
                JSONObject event = Json.getObject(events, inx);
                if (event == null) continue;

                String date = Json.getString(event, "date");
                String medication = Json.getString(event, "medication");

                if ((date == null) || (medication == null)) continue;
                String mediform = medication.substring(medication.length() - 3, medication.length());
                if (mediform.startsWith("ZZ")) continue;

                long dts = Simple.getTimeStamp(date);

                if (Math.abs(now - dts) > 24 * 3600 * 1000) continue;

                Json.put(event, "date", Simple.timeStampAsISO(now + 5 * 60 * 1000));
                Json.put(event, "taken", false);
                Json.put(event, "completed", false);
                Json.put(event, "reminded", 0);

                EventManager.updateComingEvent("webapps.medicator", event);

                Log.d(LOGTAG, "evaluateEvents: faked=" + event.toString());
            }
        }
    }

    @Override
    protected void evaluateMessage()
    {
        if (actRecord == null) return;

        int sat = Json.getInt(actRecord, "sat");
        int pls = Json.getInt(actRecord, "pls");

        boolean iswarning = false;

        String sm = Simple.getTrans(R.string.health_oxy_spoken, sat);
        String lm = Simple.getTrans(R.string.health_oxy_logger, sat);
        String am = Simple.getTrans(R.string.health_oxy_assist, Simple.getOwnerName(), sat);

        String at = Simple.getTrans(R.string.health_oxy_pls, pls);

        if (Simple.getSharedPrefBoolean("health.oxy.alert.enable"))
        {
            //
            // Check alerts.
            //

            int lowSat = Simple.getSharedPrefInt("health.oxy.alert.lowsat");

            Log.d(LOGTAG, "evaluateMessage: sat=" + sat + " low=" + lowSat);

            if ((lowSat > 0) && (lowSat >= sat))
            {
                at += " " + Simple.getTrans(R.string.health_oxy_sat_low);

                iswarning = true;
            }

            int low = Simple.getSharedPrefInt("health.oxy.alert.lowpls");

            Log.d(LOGTAG, "evaluateMessage: tmp=" + pls + " low=" + low);

            if ((low > 0) && (low >= pls))
            {
                at += " " + Simple.getTrans(R.string.health_oxy_pls_low);

                iswarning = true;
            }

            int high = Simple.getSharedPrefInt("health.oxy.alert.highpls");

            Log.d(LOGTAG, "evaluateMessage: tmp=" + pls + " high=" + high);

            if ((high > 0) && (high <= pls))
            {
                at += " " + Simple.getTrans(R.string.health_oxy_pls_high);

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
        ActivityOldManager.recordActivity(lm);

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

        TextView satView = new TextView(Simple.getActContext());
        satView.setLayoutParams(Simple.layoutParamsWM());
        satView.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(satView, 40, 0, 0, 0);
        satView.setTextSize(Simple.getDeviceTextSize(24f));
        satView.setTypeface(null, Typeface.BOLD);
        satView.setId(android.R.id.content);
        view.addView(satView);

        LinearLayout iconLayout = new LinearLayout(Simple.getActContext());
        iconLayout.setLayoutParams(Simple.layoutParamsWM());
        iconLayout.setOrientation(LinearLayout.HORIZONTAL);
        iconLayout.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(iconLayout, 20, 0, 0, 0);
        view.addView(iconLayout);

        ImageView satAlertView = new ImageView(Simple.getActContext());
        satAlertView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        satAlertView.setId(android.R.id.icon1);
        Simple.setPadding(satAlertView, 20, 0, 0, 0);
        iconLayout.addView(satAlertView);

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

        int sat = Json.getInt(item, "sat");
        int pls = Json.getInt(item, "pls");

        TextView satView = (TextView) view.findViewById(android.R.id.content);

        ImageView satAlertView = (ImageView) view.findViewById(android.R.id.icon1);
        ImageView plsAlertView = (ImageView) view.findViewById(android.R.id.icon2);

        String display = Simple.getTrans(R.string.health_oxygene) + ": " + sat + " %";
        display += " — " + Simple.getTrans(R.string.health_pulse) + ": " + pls;

        satView.setText(display);

        if (!Simple.getSharedPrefBoolean("health.oxy.alert.enable"))
        {
            satAlertView.setVisibility(View.INVISIBLE);
            plsAlertView.setVisibility(View.INVISIBLE);
        }
        else
        {
            int lowSat = Simple.getSharedPrefInt("health.oxy.alert.lowsat");

            if ((lowSat > 0) && (lowSat >= sat))
            {
                satAlertView.setImageResource(R.drawable.health_oxy_sat_low_300x200);
                satAlertView.setVisibility(View.VISIBLE);
            }
            else
            {
                satAlertView.setVisibility(View.INVISIBLE);
            }

            int lowPls = Simple.getSharedPrefInt("health.oxy.alert.lowpls");
            int highPls = Simple.getSharedPrefInt("health.oxy.alert.highpls");

            if ((lowPls > 0) && (lowPls >= pls))
            {
                plsAlertView.setImageResource(R.drawable.health_oxy_pls_low_300x200);
                plsAlertView.setVisibility(View.VISIBLE);
            }
            else
            {
                if ((highPls > 0) && (highPls <= pls))
                {
                    plsAlertView.setImageResource(R.drawable.health_oxy_pls_high_300x200);
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
