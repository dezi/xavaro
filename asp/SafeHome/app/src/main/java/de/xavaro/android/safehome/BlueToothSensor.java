package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;

//
// Health data record format:
//
//  utc => UTC timestamp
//  stp => Number of steps
//  ext => Exercise time
//  goa => Goal value
//  gou => Goal unit (0 = steps, 1 = calories)
//  std => Width of one step in cm
//  diu => Distance unit (0 = km, 1 = miles)
//  cps => Calories burned per 10.000 steps
//  BMR => Basal metabolic rate
//  s00 => Activity values per 3 hours fragment
//  s03 => ...
//

public class BlueToothSensor extends BlueTooth
{
    private static final String LOGTAG = BlueToothSensor.class.getSimpleName();

    public BlueToothSensor(Context context)
    {
        super(context);
    }

    public BlueToothSensor(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("0000fed0-494c-4f47-4943-544543480000");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fed1-494c-4f47-4943-544543480000");
    }

    @Override
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fed2-494c-4f47-4943-544543480000");
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    @Override
    protected void enableDevice()
    {
        noFireOnWrite = true;

        super.enableDevice();

        Log.d(LOGTAG, "enableDevice: " + deviceName);

        //
        // Initialize sensor device with current
        // goal data and current time.
        //

        gattSchedule.add(new GattAction(getSetUserSettingWithGoalFromPreferences()));

        fireNext(false);

        //
        // Read todays stuff.
        //

        GattAction ga = new GattAction();

        ga.mode = GattAction.MODE_READ;
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);

        fireNext(false);

        startSyncSequence();
    }

    @Override
    public void sendCommand(JSONObject command)
    {
        String what = Json.getString(command,"command");

        if (Simple.equals(what,"getStepHistoryData"))
        {
            int day = Json.getInt(command, "day");

            gattSchedule.add(new GattAction(getStepHistoryData(day)));
        }

        if (Simple.equals(what,"getSleepHistoryData"))
        {
            int position = Json.getInt(command, "position");

            gattSchedule.add(new GattAction(getSleepHistoryData(position)));
        }

        if (Simple.equals(what,"getDisconnect"))
        {
            gattSchedule.add(new GattAction(GattAction.MODE_DISCONNECT));
        }
    }

    @Override
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

                byte[] data = new byte[ 4 ];

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 2 ];
                data[ 2 ] = rd[ 1 ];
                data[ 3 ] = rd[ 0 ];

                sensordata.put("steps", convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 7 ];
                data[ 3 ] = rd[ 6 ];

                sensordata.put("sleep", convertBytesToInt(data));
            }

            if (rd.length == 17)
            {
                //
                // Step history data.
                //

                sensordata.put("type", "StepHistoryData");

                int day = rd[ 0 ];

                long targetday = ((new Date().getTime() / 86400000L) - (day + 1)) * 86400000L;

                sensordata.put("utc", Simple.timeStampAsISO(targetday));

                byte[] data = new byte[ 4 ];

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 3 ];
                data[ 2 ] = rd[ 2 ];
                data[ 3 ] = rd[ 1 ];

                sensordata.put("stp", convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 5 ];
                data[ 3 ] = rd[ 4 ];

                sensordata.put("ext", convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = (byte) (rd[ 8 ] & 0x7f);
                data[ 2 ] = rd[ 7 ];
                data[ 3 ] = rd[ 6 ];

                sensordata.put("goa", convertBytesToInt(data));
                sensordata.put("gou", ((rd[ 8 ] & 0x80) == 0) ? 0 : 1);

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 10 ];
                data[ 3 ] = rd[  9 ];

                sensordata.put("std", convertBytesToInt(data) / 100f);
                sensordata.put("diu", ((rd[ 13 ] & 0x80) == 0) ? 0 : 1);

                data[ 0 ] = 0;
                data[ 1 ] = (byte) (rd[ 13 ] & 0x7f);
                data[ 2 ] = rd[ 12 ];
                data[ 3 ] = rd[ 11 ];

                sensordata.put("cps", convertBytesToInt(data) / 100f);

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 16 ];
                data[ 2 ] = rd[ 15 ];
                data[ 3 ] = rd[ 14 ];

                sensordata.put("BMR", convertBytesToInt(data) / 100f);

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
                long dayminutes = 1440 * 60;

                long postime = ((new Date().getTime() / 1000L) / tenminutes)  * tenminutes;
                postime -= position * tenminutes;

                long daytime = (postime / dayminutes) * dayminutes;

                postime *= 1000L;
                daytime *= 1000L;

                String activityday  = Simple.timeStampAsISO(daytime);
                String activityhour = "s" + Simple.get24HHourFromTimeStamp(postime);

                sensordata.put("utc", activityday);

                byte[] sd = new byte[ 18 ];
                int cnt = 0;

                for (int inx = rd.length - 1; inx >= 2; inx--) sd[ cnt++ ] = rd[ inx ];

                sensordata.put(activityhour, Simple.getHexBytesToString(sd));

                //
                // Store data.
                //

                JSONObject record = Json.clone(sensordata);
                Json.remove(record, "type");

                HealthData.addRecord("sensor", record);

                //
                // Round position time to next 3 hour fragment.
                //

                postime /= 1000L;
                postime = (postime / (3 * 3600)) * (3 * 3600);
                postime *= 1000L;

                String lastSavedAct = Simple.timeStampAsISO(postime);

                Json.put(syncStatus, "lastSavedAct", lastSavedAct);

                HealthData.putStatus("sensor", syncStatus);
            }

            JSONObject data = new JSONObject();
            data.put("sensor", sensordata);

            if (dataCallback != null) dataCallback.onBluetoothReceivedData(deviceName, data);
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
        syncStatus = HealthData.getStatus("sensor");

        long lastSavedDay = 0;
        long lastSavedAct = 0;

        if (syncStatus.has("lastSavedDay"))
        {
            lastSavedDay = Simple.getTimeStampFromISO(Json.getString(syncStatus, "lastSavedDay"));
            lastSavedDay /= 1000L;
            lastSavedDay /= 86400L;
        }

        if (syncStatus.has("lastSavedAct"))
        {
            lastSavedAct = Simple.getTimeStampFromISO(Json.getString(syncStatus, "lastSavedAct"));
            lastSavedAct /= 1000L;
            lastSavedAct /= 86400L;
        }

        //
        // Get no more than ten days back.
        //

        long today = new Date().getTime();
        today /= 1000L;
        today /= 86400L;

        long todoSavedDay = ((today - lastSavedDay) > 10) ? (today - 10) : lastSavedDay;
        long todoSavedAct = ((today - lastSavedAct) > 10) ? (today - 10) : lastSavedAct;

        //
        // Schedule activity days.
        //

        while (todoSavedDay < today)
        {
            todoSavedDay += 1;

            int day = (int) (today - todoSavedDay);

            Log.d(LOGTAG, "startSyncSequence: schedule day:" + day);

            gattSchedule.add(new GattAction(getStepHistoryData(day)));
        }

        //
        // Schedule sleep data positions.
        //

        long todaysecs = new Date().getTime();
        todaysecs /= 1000L;
        todaysecs -= today * 86400L;

        int todaypositions = ((int) todaysecs) / 600;

        int position = ((int) (today - todoSavedAct)) * 8 * 18;
        position += todaypositions;

        while (position > todaypositions)
        {
            Log.d(LOGTAG, "startSyncSequence: schedule position:" + position);

            gattSchedule.add(new GattAction(getSleepHistoryData(position)));

            position -= 18;
        }

        //
        // Disconnect after sync.
        //

        gattSchedule.add(new GattAction(GattAction.MODE_DISCONNECT));
    }

    private byte[] getSetUserSettingWithGoalFromPreferences()
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
