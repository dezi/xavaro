package de.xavaro.android.safehome;

import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import de.xavaro.android.common.ActivityManager;
import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public abstract class HealthBase implements
        BlueTooth.BlueToothDataCallback,
        BlueTooth.BlueToothConnectCallback
{
    private static final String LOGTAG = HealthBase.class.getSimpleName();

    protected String deviceName;
    protected String deviceType;

    protected String actDts;
    protected JSONObject actRecord;

    protected boolean isConnected;
    protected BlueTooth blueTooth;
    protected BlueTooth.BlueToothConnectCallback connectCallback;

    protected final Handler handler = new Handler();

    public boolean isConfigured()
    {
        return (blueTooth != null);
    }

    public void setBlueTooth(BlueTooth blueTooth)
    {
        if (blueTooth == null)
        {
            if (this.blueTooth != null)
            {
                this.blueTooth.close();
                this.blueTooth = null;
            }
        }
        else
        {
            this.blueTooth = blueTooth;

            this.blueTooth.setDataCallback(this);
            this.blueTooth.setConnectCallback(this);

            this.blueTooth.connectRunnable.run();
        }
    }

    public void setConnectCallback(BlueTooth.BlueToothConnectCallback subscriber)
    {
        connectCallback = subscriber;

        if (isConnected && (connectCallback != null))
            connectCallback.onBluetoothConnect(deviceName);
    }

    public void onBluetoothConnect(String deviceName)
    {
        this.deviceName = deviceName;

        isConnected = true;

        if (connectCallback != null) connectCallback.onBluetoothConnect(deviceName);
    }

    public void onBluetoothDisconnect(String deviceName)
    {
        isConnected = false;

        if (connectCallback != null) connectCallback.onBluetoothDisconnect(deviceName);
    }

    public void onBluetoothFakeConnect(String deviceName)
    {
        if (connectCallback != null) connectCallback.onBluetoothFakeConnect(deviceName);
    }

    public void onBluetoothFakeDisconnect(String deviceName)
    {
        if (connectCallback != null) connectCallback.onBluetoothFakeDisconnect(deviceName);
    }

    public void onBluetoothUpdated(String deviceName)
    {
        if (connectCallback != null) connectCallback.onBluetoothUpdated(deviceName);
    }

    public void onBluetoothReceivedData(String deviceName, JSONObject data)
    {
        Log.d(LOGTAG, "onBluetoothReceivedData: dev=" + deviceName + " typ=" + deviceType + " json=" + data.toString());

        //
        // The results come in unordered.
        //

        if (!data.has(deviceType)) return;
        data = Json.getObject(data, deviceType);
        if (data == null) return;

        String dts = Json.getString(data, "dts");
        if (dts == null) return;

        if ((actDts == null) || (actDts.compareTo(dts) <= 0))
        {
            actDts = dts;
            actRecord = data;

            Simple.removePost(messageHandler);
            Simple.makePost(messageHandler, 500);
        }
    }

    private Runnable messageHandler = new Runnable()
    {
        @Override
        public void run()
        {
            evaluateMessage();
            evaluateEvents();
        }
    };

    protected abstract void evaluateMessage();
    protected abstract void evaluateEvents();

    protected void handleAssistance(String text, boolean iswarning)
    {
        long dts = Simple.getTimeStamp(actDts);
        String date = Simple.getLocaleDateLong(dts);
        String time = Simple.getLocaleTime(dts);
        String datetext = Simple.getTrans(R.string.health_measure_datetime, date, time) + " " + text;

        Log.d(LOGTAG, "handleAssistance: text=" + datetext);

        if (! Simple.getSharedPrefBoolean("alertgroup.enable")) return;

        String groupIdentity = Simple.getSharedPrefString("alertgroup.groupidentity");
        if (groupIdentity == null) return;

        String mode = Simple.getSharedPrefString("health." + deviceType + ".assist.mode");

        if ((Simple.equals(mode, "warn") && iswarning) || Simple.equals(mode, "always"))
        {
            JSONObject assistMessage = new JSONObject();
            Json.put(assistMessage, "uuid", Simple.getUUID());
            Json.put(assistMessage, "message", datetext);

            if (iswarning) Json.put(assistMessage, "priority", "alertinfo");

            ChatManager.getInstance().sendOutgoingMessage(groupIdentity, assistMessage);

            Log.d(LOGTAG, "handleAssistance: send=" + datetext);
        }
    }

    protected void handleActivity(String text, boolean iswarning, int iconres)
    {
        JSONObject activity = new JSONObject();

        Json.put(activity, "date", Simple.nowAsISO());
        Json.put(activity, "text", text);
        Json.put(activity, "type", "health");
        Json.put(activity, "subtype", deviceType);
        Json.put(activity, "action", "measure");
        Json.put(activity, "mode", iswarning ? "warning" : "normal");
        Json.put(activity, "icid", iconres);

        ActivityManager.getInstance().recordActivity(activity);
    }

    public View createListView()
    {
        LinearLayout view = new LinearLayout(Simple.getActContext());
        view.setLayoutParams(Simple.layoutParamsMW());
        view.setOrientation(LinearLayout.HORIZONTAL);
        Simple.setPadding(view, 10, 10, 10, 10);

        LinearLayout dateLayout = new LinearLayout(Simple.getActContext());
        dateLayout.setLayoutParams(Simple.layoutParamsWW());
        dateLayout.setOrientation(LinearLayout.VERTICAL);
        dateLayout.setId(android.R.id.primary);
        view.addView(dateLayout);

        TextView dateView = new TextView(Simple.getActContext());
        dateView.setLayoutParams(Simple.layoutParamsWW());
        dateView.setTextSize(Simple.getDeviceTextSize(24f));
        dateView.setTypeface(null, Typeface.BOLD);
        dateView.setId(android.R.id.text1);
        dateLayout.addView(dateView);

        TextView timeView = new TextView(Simple.getActContext());
        timeView.setLayoutParams(Simple.layoutParamsWW());
        timeView.setTextSize(Simple.getDeviceTextSize(20f));
        timeView.setTypeface(null, Typeface.BOLD);
        timeView.setId(android.R.id.text2);
        dateLayout.addView(timeView);

        return view;
    }

    public void populateListView(View view, int position, JSONObject item)
    {
        long dts = Simple.getTimeStamp(Json.getString(item, "dts"));

        TextView dateView = (TextView) view.findViewById(android.R.id.text1);
        TextView timeView = (TextView) view.findViewById(android.R.id.text2);

        dateView.setText(Simple.getLocaleDateMedium(dts));
        timeView.setText(Simple.getLocaleTime(dts));
    }
}
