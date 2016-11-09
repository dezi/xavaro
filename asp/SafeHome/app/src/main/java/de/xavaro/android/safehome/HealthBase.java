package de.xavaro.android.safehome;

import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

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

    private boolean isConnected;
    private BlueTooth blueTooth;
    private BlueTooth.BlueToothConnectCallback connectCallback;

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

        if (isConnected && (connectCallback != null)) connectCallback.onBluetoothConnect(deviceName);
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
}
