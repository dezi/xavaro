package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;

public class BlueToothSensorSanitas implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothSensorSanitas.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothSensorSanitas(BlueTooth parent)
    {
        this.parent = parent;
    }

    public boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("0000fed0-494c-4f47-4943-544543480000");
    }

    public boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fed1-494c-4f47-4943-544543480000");
    }

    public boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fed2-494c-4f47-4943-544543480000");
    }

    public boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    public void enableDevice()
    {
        parent.noFireOnWrite = true;
    }

    public void syncSequence()
    {
        startSyncSequence();
    }

    public void sendCommand(JSONObject command)
    {
        String what = Json.getString(command,"command");

        if (Simple.equals(what,"getStepHistoryData"))
        {
            int day = Json.getInt(command, "day");

            parent.gattSchedule.add(new BlueTooth.GattAction(getStepHistoryData(day)));
        }

        if (Simple.equals(what,"getSleepHistoryData"))
        {
            int position = Json.getInt(command, "position");

            parent.gattSchedule.add(new BlueTooth.GattAction(getSleepHistoryData(position)));
        }

        if (Simple.equals(what,"getDisconnect"))
        {
            parent.gattSchedule.add(new BlueTooth.GattAction(BlueTooth.GattAction.MODE_DISCONNECT));
        }
    }

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + Simple.getHexBytesToString(rd));

        if ((rd[ 0 ] == (byte) 0xff) && (rd.length == 1))
        {
            Log.d(LOGTAG, "parseResponse: Invalid command");

            return;
        }

        try
        {
            JSONObject sensordata = new JSONObject();

            if (rd.length == 11)
            {
                //
                // Todays data.
                //

                sensordata.put("type", "TodaysData");

                Calendar calendar = Calendar.getInstance();

                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                long targetday = calendar.getTimeInMillis();

                sensordata.put("dts", Simple.timeStampAsISO(targetday));

                byte[] data = new byte[ 4 ];

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 2 ];
                data[ 2 ] = rd[ 1 ];
                data[ 3 ] = rd[ 0 ];

                sensordata.put("stp", BlueTooth.convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 7 ];
                data[ 3 ] = rd[ 6 ];

                sensordata.put("sle", BlueTooth.convertBytesToInt(data));

                //
                // Store data.
                //

                JSONObject record = Json.clone(sensordata);
                Json.remove(record, "type");

                HealthData.addRecord("sensor", record);
            }

            if (rd.length == 17)
            {
                //
                // Step history data.
                //

                sensordata.put("type", "StepHistoryData");

                int day = rd[ 0 ];

                Calendar calendar = Calendar.getInstance();

                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                long targetday = calendar.getTimeInMillis();

                targetday /= 1000L;
                targetday -= (day + 1) * 86400;
                targetday *= 1000L;

                sensordata.put("dts", Simple.timeStampAsISO(targetday));

                byte[] data = new byte[ 4 ];

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 3 ];
                data[ 2 ] = rd[ 2 ];
                data[ 3 ] = rd[ 1 ];

                sensordata.put("stp", BlueTooth.convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 5 ];
                data[ 3 ] = rd[ 4 ];

                sensordata.put("ext", BlueTooth.convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = (byte) (rd[ 8 ] & 0x7f);
                data[ 2 ] = rd[ 7 ];
                data[ 3 ] = rd[ 6 ];

                sensordata.put("goa", BlueTooth.convertBytesToInt(data));
                sensordata.put("gou", ((rd[ 8 ] & 0x80) == 0) ? 0 : 1);

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 10 ];
                data[ 3 ] = rd[  9 ];

                sensordata.put("std", BlueTooth.convertBytesToInt(data) / 100f);
                sensordata.put("diu", ((rd[ 13 ] & 0x80) == 0) ? 0 : 1);

                data[ 0 ] = 0;
                data[ 1 ] = (byte) (rd[ 13 ] & 0x7f);
                data[ 2 ] = rd[ 12 ];
                data[ 3 ] = rd[ 11 ];

                sensordata.put("cps", BlueTooth.convertBytesToInt(data) / 100f);

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 16 ];
                data[ 2 ] = rd[ 15 ];
                data[ 3 ] = rd[ 14 ];

                sensordata.put("BMR", BlueTooth.convertBytesToInt(data) / 100f);

                //
                // Store data.
                //

                JSONObject record = Json.clone(sensordata);
                Json.remove(record, "type");

                HealthData.addRecord("sensor", record);

                String lastSavedDay = Simple.timeStampAsISO(targetday);

                Json.put(syncStatus, "lastSavedDay", lastSavedDay);

                HealthData.putStatus("sensor", syncStatus);
            }

            if (rd.length == 20)
            {
                //
                // Sleep history data.
                //

                sensordata.put("type", "SleepHistoryData");

                int position = (rd[ 0 ] & 0x7f) + (rd[ 1 ] << 7);

                long tenminutes = 10 * 60;
                long postime = ((new Date().getTime() / 1000L) / tenminutes)  * tenminutes;
                postime -= position * tenminutes;
                postime *= 1000L;

                sensordata.put("dts", Simple.timeStampAsISO(postime));

                byte[] sd = new byte[ 18 ];

                int cnt = 0;
                for (int inx = rd.length - 1; inx >= 2; inx--) sd[ cnt++ ] = rd[ inx ];

                sensordata.put("sda", Simple.getHexBytesToString(sd));

                //
                // Store data.
                //

                JSONObject record = Json.clone(sensordata);
                Json.remove(record, "type");

                HealthData.addRecord("sensor", record);

                String lastSavedAct = Simple.timeStampAsISO(postime);

                Json.put(syncStatus, "lastSavedAct", lastSavedAct);

                HealthData.putStatus("sensor", syncStatus);
            }

            JSONObject data = new JSONObject();
            data.put("sensor", sensordata);

            if (parent.dataCallback != null) parent.dataCallback.onBluetoothReceivedData(parent.deviceName, data);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private JSONObject syncStatus;

    // private final int ACTIVITY_DEEPSLEEP = 16;
    // private final int ACTIVITY_LIGHTSLEEP = 32;

    private void startSyncSequence()
    {
        //
        // Initialize sensor device with current
        // goal data and current time.
        //

        parent.gattSchedule.add(new BlueTooth.GattAction(getSetUserSettingWithGoal()));

        //
        // Read todays stuff.
        //

        BlueTooth.GattAction ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_READ;
        ga.characteristic = parent.currentSecondary;

        parent.gattSchedule.add(ga);

        parent.fireNext(false);

        //
        // Get sync status.
        //

        syncStatus = HealthData.getStatus("sensor");

        //
        // Compute time stuff for step day history records.
        //

        long lastSavedDay = 0;

        if (syncStatus.has("lastSavedDay"))
        {
            lastSavedDay = Simple.getTimeStamp(Json.getString(syncStatus, "lastSavedDay"));
            lastSavedDay /= 1000L;
        }

        //
        // Get no more than ten days back.
        //

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long today = calendar.getTimeInMillis();
        today /= 1000L;

        long tendays = 86400 * 10;

        long todoSavedDay = ((today - lastSavedDay) > tendays) ? (today - tendays) : lastSavedDay;

        //
        // Schedule activity days.
        //

        while (todoSavedDay < today)
        {
            todoSavedDay += 86400;

            int day = (int) ((today - todoSavedDay) / 86400);

            Log.d(LOGTAG, "startSyncSequence: schedule day:" + day);

            parent.gattSchedule.add(new BlueTooth.GattAction(getStepHistoryData(day)));
        }

        //
        // Schedule sleep data positions.
        //

        long lastSavedAct = 0;

        if (syncStatus.has("lastSavedAct"))
        {
            lastSavedAct = Simple.getTimeStamp(Json.getString(syncStatus, "lastSavedAct"));
            lastSavedAct /= 1000L;
        }

        long now = new Date().getTime();

        calendar.setTimeInMillis(now);
        calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE) / 10) * 10);
        calendar.set(Calendar.SECOND, 0);

        long nowtenminutes = calendar.getTimeInMillis() / 1000L;

        calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY) / 3) * 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long nowthreehours = calendar.getTimeInMillis() / 1000L;

        int startposition = (int) ((nowtenminutes - nowthreehours) / (10 * 60));
        int lastposition = startposition + (10 * 8 * 18);

        while (lastposition >= startposition)
        {
            long positiontime = nowtenminutes - (lastposition * 10 * 60);

            if (positiontime >= lastSavedAct)
            {
                Log.d(LOGTAG, "startSyncSequence: schedule position:" + lastposition);

                parent.gattSchedule.add(new BlueTooth.GattAction(getSleepHistoryData(lastposition)));
            }

            lastposition -= 18;
        }

        //
        // Disconnect after sync.
        //

        parent.gattSchedule.add(new BlueTooth.GattAction(BlueTooth.GattAction.MODE_DISCONNECT));

        parent.fireNext(false);
    }

    private byte[] getSetUserSettingWithGoal()
    {
        String keyprefix = "health.sensor";

        //
        // Default values from device.
        //
        // Todo: need to be derived from history or preferences.
        //

        float BMR = 1507.75f;
        float distanceWalkCM = 103.00f;
        float caloriePer10000Steps = 474.75f;

        SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

        String timeFormatString = sp.getString(keyprefix + ".units.timedisp", "24h");
        String distanceUnitString = sp.getString(keyprefix + ".units.distance", "m");
        String goalUnitString = sp.getString(keyprefix + ".goals.sensor", "steps");

        int timeFormat = timeFormatString.equals("24h") ? 1 : 0;
        int distanceUnit = distanceUnitString.equals("m") ? 1 : 0;
        int goalUnit = goalUnitString.equals("steps") ? 0 : 1;

        int goal = sp.getInt(keyprefix + ".goals.steps", 0);
        if (goalUnit == 1) goal = 100 * sp.getInt(keyprefix + ".goals.calories", 0);

        return getSetUserSettingWithGoal(goal, goalUnit, timeFormat, distanceUnit,
                distanceWalkCM, caloriePer10000Steps, BMR);
    }

    private byte[] getSetUserSettingWithGoal(
            int goal, int goalUnit, int timeFormat, int distanceUnit,
            float distanceWalkCM, float caloriePer10000Steps, float BMR)
    {
        Log.d(LOGTAG,"getSetUserSettingWithGoal");

        byte[] data = new byte[ 16 ];

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        data[ 0 ] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        data[ 1 ] = (byte) calendar.get(Calendar.MINUTE);
        data[ 2 ] = (byte) calendar.get(Calendar.SECOND);

        if (timeFormat == 1) data[ 0 ] |= (byte) 0x80;

        data[ 3 ] = (byte) (goal & 0xff);
        data[ 4 ] = (byte) ((goal >> 8) & 0xff);
        data[ 5 ] = (byte) ((goal >> 16) & 0x7f);

        if (goalUnit == 1) data[ 5 ] |= (byte) 0x80;

        int distanceWalk = (int) (distanceWalkCM * 100.0f);
        if (distanceUnit == 1) distanceWalk = (int) (distanceWalkCM * 62.1371192f);

        data[ 6 ] = (byte) (distanceWalk & 0xff);
        data[ 7 ] = (byte) ((distanceWalk >> 8) & 0xff);

        int calorieBurned = (int) (caloriePer10000Steps * 100.0f);

        data[  8 ] = (byte) (calorieBurned & 0xff);
        data[  9 ] = (byte) ((calorieBurned >> 8) & 0xff);
        data[ 10 ] = (byte) ((calorieBurned >> 16) & 0xff);
        data[ 11 ] = (byte) ((calorieBurned >> 24) & 0x7f);

        if (distanceUnit == 1) data[ 11 ] |= (byte) 0x80;

        int intBMR = (int) (BMR * 100.0f);

        data[ 12 ] = (byte) (intBMR & 0xff);
        data[ 13 ] = (byte) ((intBMR >> 8) & 0xff);
        data[ 14 ] = (byte) ((intBMR >> 16) & 0xff);

        data[ 15 ] = (byte) 1;

        return data;
    }

    private byte[] getStepHistoryData(int day)
    {
        byte[] data = new byte[ 1 ];

        data[ 0 ] = (byte) day;

        return data;
    }

    private byte[] getSleepHistoryData(int position)
    {
        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (0x80 + (position & 0x7f));
        data[ 1 ] = (byte) (position >> 7);

        return data;
    }

    @SuppressWarnings("unused")
    public byte[] getSetAlaram(boolean isON, Date alarmTime)
    {
        byte[] temp = new byte[ 4 ];

        temp[ 0 ] = (byte) 0;
        temp[ 1 ] = (byte) (isON ? 0x80 : 0x00);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(alarmTime);

        temp[ 2 ] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        temp[ 3 ] = (byte) calendar.get(Calendar.MINUTE);

        return temp;
    }
}
