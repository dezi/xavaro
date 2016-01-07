package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import de.xavaro.android.common.OopsService;

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
    public void sendCommand(JSONObject command)
    {
        try
        {
            String what = command.getString("command");

            if (what.equals("getStepHistoryData"))
            {
                int day = command.getInt("day");

                gattSchedule.add(new GattAction(getStepHistoryData(day)));
            }

            if (what.equals("getSleepHistoryData"))
            {
                int position = command.getInt("position");

                gattSchedule.add(new GattAction(getSleepHistoryData(position)));
            }

            if (what.equals("getDisconnect"))
            {
                gattSchedule.add(new GattAction(GattAction.MODE_DISCONNECT));
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    @Override
    protected void enableDevice()
    {
        super.enableDevice();

        Log.d(LOGTAG, "enableDevice: " + deviceName);

        //
        // Initialize sensor device with current
        // goal data and current time.
        //

        gattSchedule.add(new GattAction(getSetUserSettingWithGoalFromPreferences()));

        //
        // Read todays stuff.
        //

        GattAction ga = new GattAction();

        ga.mode = GattAction.MODE_READ;
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);

        fireNext(true);
    }

    private final int ACTIVITY_DEEPSLEEP = 16;
    private final int ACTIVITY_LIGHTSLEEP = 32;

    @Override
    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        Log.d(LOGTAG, "parseResponse: " + StaticUtils.hexBytesToString(rd));

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

            if (rd.length == 20)
            {
                //
                // Sleep history data.
                //

                int position = (rd[ 0 ] & 0x7f) + (rd[ 1 ] << 7);

                sensordata.put("type", "SleepHistoryData");
                sensordata.put("position", position);
                sensordata.put("recvtime", StaticUtils.nowAsISO());

                JSONArray detail = new JSONArray();

                //
                // Detail data is reversed. Make a kind of
                // runlength encoding on awake sequences.
                //

                int awake = 0;

                for (int inx = rd.length - 1; inx >= 2; inx--)
                {
                    if (rd[ inx ] == -1)
                    {
                        awake++;
                    }
                    else
                    {
                        if (awake > 0)
                        {
                            detail.put(-awake);
                            awake = 0;
                        }

                        detail.put(rd[ inx ] & 0xff);
                    }
                }

                if (awake > 0) detail.put(-awake);

                sensordata.put("detail", detail);
            }

            if (rd.length == 17)
            {
                //
                // Step history data.
                //

                sensordata.put("type", "StepHistoryData");
                sensordata.put("day", rd[ 0 ]);

                byte[] data = new byte[ 4 ];

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 3 ];
                data[ 2 ] = rd[ 2 ];
                data[ 3 ] = rd[ 1 ];

                sensordata.put("steps", convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 5 ];
                data[ 3 ] = rd[ 4 ];

                sensordata.put("exercisetime", convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = (byte) (rd[ 8 ] & 0x7f);
                data[ 2 ] = rd[ 7 ];
                data[ 3 ] = rd[ 6 ];

                sensordata.put("goal", convertBytesToInt(data));

                data[ 0 ] = 0;
                data[ 1 ] = 0;
                data[ 2 ] = rd[ 10 ];
                data[ 3 ] = rd[  9 ];

                sensordata.put("stepdistance", convertBytesToInt(data) / 100f);

                data[ 0 ] = 0;
                data[ 1 ] = (byte) (rd[ 13 ] & 0x7f);
                data[ 2 ] = rd[ 12 ];
                data[ 3 ] = rd[ 11 ];

                sensordata.put("ca10kstep", convertBytesToInt(data) / 100f);

                data[ 0 ] = 0;
                data[ 1 ] = rd[ 16 ];
                data[ 2 ] = rd[ 15 ];
                data[ 3 ] = rd[ 14 ];

                sensordata.put("BMR", convertBytesToInt(data) / 100f);

                sensordata.put("goalUnit", ((rd[ 8 ] & 0x80) == 0) ? 0 : 1);
                sensordata.put("distanceUnit", ((rd[ 13 ] & 0x80) == 0) ? 0 : 1);
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

        return getSetUserSettingWithGoal(goal, goalUnit, distanceWalkCM, caloriePer10000Steps, BMR, timeFormat, distanceUnit);
    }

    public byte[] getSetUserSettingWithGoal(
            int goal, int goalUnit,
            float distanceWalkCM, float caloriePer10000Steps, float BMR,
            int timeFormat, int distanceUnit)
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
