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

import java.util.Locale;

import de.xavaro.android.common.ActivityOldManager;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.EventManager;
import de.xavaro.android.common.NotifyManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.Json;

public class HealthThermo extends HealthBase
{
    private static final String LOGTAG = HealthThermo.class.getSimpleName();

    private static HealthThermo instance;

    public static HealthThermo getInstance()
    {
        if (instance == null) instance = new HealthThermo();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    private HealthThermo()
    {
        super();

        this.deviceType = "thermo";
    }

    @Override
    protected void evaluateEvents()
    {
        JSONArray events = EventManager.getComingEvents("webapps.medicator");
        if (events == null) return;

        if (actRecord == null) return;

        String actDts = Json.getString(actRecord, "dts");
        double actTmp = Json.getDouble(actRecord, "tmp");

        long now = Simple.nowAsTimeStamp();

        for (int inx = 0; inx < events.length(); inx++)
        {
            JSONObject event = Json.getObject(events, inx);

            if ((event == null) || Json.getBoolean(event, "taken")) continue;

            String date = Json.getString(event, "date");
            String medication = Json.getString(event, "medication");

            if ((date == null) || (medication == null) || ! medication.endsWith(",ZZT")) continue;

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
            Json.put(event, "temp", actTmp);

            EventManager.updateComingEvent("webapps.medicator", event);

            Log.d(LOGTAG, "evaluateEvents: updated=" + event.toString());

            break;
        }

        NotifyManager.removeNotification("medicator.take.temperature");
        Simple.makePost(CommonConfigs.UpdateNotifications);
    }

    @Override
    protected void evaluateMessage()
    {
        if (actRecord == null) return;

        double tmp = Json.getDouble(actRecord, "tmp");
        String tmpstr = String.format(Locale.getDefault(), "%.1f", tmp);

        boolean iswarning = false;

        String sm = Simple.getTrans(R.string.health_thermo_spoken, tmpstr);
        String lm = Simple.getTrans(R.string.health_thermo_logger, tmpstr);
        String am = Simple.getTrans(R.string.health_thermo_assist, Simple.getOwnerName(), tmpstr);

        String at = "";

        if (Simple.getSharedPrefBoolean("health.thermo.alert.enable"))
        {
            //
            // Check alerts.
            //

            String lowstr = Simple.getSharedPrefString("health.thermo.alert.lowtemp");
            Double low = Simple.parseDouble(lowstr);

            Log.d(LOGTAG, "evaluateMessage: tmp=" + tmp + " low=" + low);

            if ((low > 0) && (low >= tmp))
            {
                at += " " + Simple.getTrans(R.string.health_thermo_temp_low);

                iswarning = true;
            }

            String highstr = Simple.getSharedPrefString("health.thermo.alert.hightemp");
            Double high = Simple.parseDouble(highstr);

            Log.d(LOGTAG, "evaluateMessage: tmp=" + tmp + " high=" + high);

            if ((high > 0) && (high <= tmp))
            {
                at += " " + Simple.getTrans(R.string.health_thermo_temp_high);

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

        TextView tempView = new TextView(Simple.getActContext());
        tempView.setLayoutParams(Simple.layoutParamsWM());
        tempView.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(tempView, 40, 0, 0, 0);
        tempView.setTextSize(Simple.getDeviceTextSize(24f));
        tempView.setTypeface(null, Typeface.BOLD);
        tempView.setId(android.R.id.content);
        view.addView(tempView);

        LinearLayout iconLayout = new LinearLayout(Simple.getActContext());
        iconLayout.setLayoutParams(Simple.layoutParamsWM());
        iconLayout.setOrientation(LinearLayout.HORIZONTAL);
        iconLayout.setGravity(Gravity.CENTER_VERTICAL);
        Simple.setPadding(iconLayout, 20, 0, 0, 0);
        view.addView(iconLayout);

        ImageView alertView = new ImageView(Simple.getActContext());
        alertView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
        alertView.setId(android.R.id.icon1);
        Simple.setPadding(alertView, 20, 0, 0, 0);
        iconLayout.addView(alertView);

        return view;
    }

    @Override
    public void populateListView(View view, int position, JSONObject item)
    {
        super.populateListView(view, position, item);

        String unt = Json.getString(item, "unt");
        double tmp = Json.getDouble(item, "tmp");
        String tmpstr = String.format(Locale.getDefault(), "%.1f", tmp);

        TextView tempView = (TextView) view.findViewById(android.R.id.content);
        ImageView alertView = (ImageView) view.findViewById(android.R.id.icon1);

        String display = Simple.getTrans(R.string.health_temperature) + ": " + tmpstr + "°";

        if (Simple.equals(unt, "C") || Simple.equals(unt, "c")) display += "C";
        if (Simple.equals(unt, "F") || Simple.equals(unt, "F")) display += "F";

        tempView.setText(display);

        if (!Simple.getSharedPrefBoolean("health.thermo.alert.enable"))
        {
            alertView.setVisibility(View.INVISIBLE);
        }
        else
        {
            String lowstr = Simple.getSharedPrefString("health.thermo.alert.lowtemp");
            Double low = Simple.parseDouble(lowstr);
            String highstr = Simple.getSharedPrefString("health.thermo.alert.hightemp");
            Double high = Simple.parseDouble(highstr);

            if ((low > 0) && (low >= tmp))
            {
                alertView.setImageResource(R.drawable.health_thermo_low_300x200);
                alertView.setVisibility(View.VISIBLE);
            }
            else
            {
                if ((high > 0) && (high <= tmp))
                {
                    alertView.setImageResource(R.drawable.health_thermo_high_300x200);
                    alertView.setVisibility(View.VISIBLE);
                }
                else
                {
                    alertView.setImageResource(R.drawable.health_thermo_dim_300x200);
                    alertView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
