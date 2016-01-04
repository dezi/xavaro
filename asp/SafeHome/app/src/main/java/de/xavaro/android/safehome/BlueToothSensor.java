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

public class BlueToothSensor extends BlueTooth
{
    private static final String LOGTAG = BlueToothSensor.class.getSimpleName();

    private static class Sensors
    {
        public static final String SAS75 = "SAS75";
        public static final String PR102 = "PR102";
    }

    public BlueToothSensor(Context context)
    {
        super(context);
    }

    public BlueToothSensor(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    @Override
    protected boolean isCompatibleDevice(String devicename)
    {
        return devicename.equalsIgnoreCase(Sensors.SAS75)
                || devicename.equalsIgnoreCase(Sensors.PR102);
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
    public void sendCommand(JSONObject command)
    {
    }

    @Override
    protected void discoveredDevice()
    {
        if (connectCallback != null) connectCallback.onBluetoothConnect(deviceName);
    }

    @Override
    protected void enableDevice()
    {
        Log.d(LOGTAG,"enableDevice: " + currentPrimary);

        //
        // Subscribe to normal data notification.
        //

        gattSchedule.add(new GattAction(GattAction.MODE_NOTIFY));

        //
        // Initialize sensor device with current
        // goal data and current time.
        //

        gattSchedule.add(new GattAction(getSetUserSettingWithGoalFromPreferences()));

        GattAction ga = new GattAction();

        ga.mode = GattAction.MODE_READ;
        ga.characteristic = currentSecondary;

        gattSchedule.add(ga);

        /*
        gattSchedule.add(new GattAction(getStepHistoryData(0)));
        gattSchedule.add(new GattAction(getStepHistoryData(1)));
        gattSchedule.add(new GattAction(getStepHistoryData(2)));
        gattSchedule.add(new GattAction(getStepHistoryData(3)));
        gattSchedule.add(new GattAction(getStepHistoryData(4)));
        gattSchedule.add(new GattAction(getStepHistoryData(5)));
        gattSchedule.add(new GattAction(getStepHistoryData(6)));
        gattSchedule.add(new GattAction(getStepHistoryData(7)));
        gattSchedule.add(new GattAction(getStepHistoryData(8)));
        gattSchedule.add(new GattAction(getStepHistoryData(9)));
        gattSchedule.add(new GattAction(getStepHistoryData(10)));
        */

        fireNext();
    }

    @Override
    public void parseResponse(byte[] rd, boolean intermediate)
    {
        Log.d(LOGTAG, "parseResponse: " + StaticUtils.hexBytesToString(rd));

        try
        {
            JSONObject bpmdata = new JSONObject();

            // Write 8D230E570400000000000000A0860101
            // Read  3F000000 000C 0006 000000
            //       72000000 000C 0006 000000
            //       72000000 000C 0006 000000
            //       72000000 000C 0006 000000
            // Step history: 0000000000005704000000000000A08601
            // Step history: 0000000000005704000000000000A08601
            // Step history: 010000000000570400B80BD00700A08601

            /*
            0000000000005704000000000000A08601
            0000000000005704000000000000A08601

            010000000000570400B80BD00700A08601
            00 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6
            010000000000570400B80BD00700A08601

            020000000000080701001973B980F74C02
            020000000000080701001973B980F74C02

            030000000000080701001973B980F74C02
            030000000000080701001973B980F74C02

            040000000000080701001973B980F74C02
            040000000000080701001973B980F74C02

            050000000000080701001973B980F74C02
            050000000000080701001973B980F74C02

            060000000000080701001973B980F74C02
            060000000000080701001973B980F74C02

            070000000000080701001973B980F74C02
            070000000000080701001973B980F74C02

            080000000000080701001973B980F74C02
            080000000000080701001973B980F74C02

            09F500000100080701001973B980F74C02
            09F500000100080701001973B980F74C02

            0A0000000000080701001973B980F74C02
            0A0000000000080701001973B980F74C02
            */

            // Sleep history: 8000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

            JSONObject data = new JSONObject();
            data.put("sensor", bpmdata);

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

        SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;

        String timeFormatString = sp.getString(keyprefix + ".units.timedisp", "24h");
        String distanceUnitString = sp.getString(keyprefix + ".units.distance", "m");
        String goalUnitString = sp.getString(keyprefix + ".goals.sensor", "steps");

        Log.d(LOGTAG,"getSetUserSettingWithGoalFromPreferences: du=" + distanceUnitString + " gu=" + goalUnitString);

        int timeFormat = timeFormatString.equals("24h") ? 1 : 0;
        int distanceUnit = distanceUnitString.equals("m") ? 1 : 0;
        int goalUnit = goalUnitString.equals("steps") ? 0 : 1;
        int distanceWalk = sp.getInt(keyprefix + ".goals.steps", 0);
        int calorieBurned = sp.getInt(keyprefix + ".goals.calories", 0);

        int BMR = 1000;
        int goal = 1000;

        Log.d(LOGTAG,"getSetUserSettingWithGoalFromPreferences:"
                + " distanceWalk=" + distanceWalk
                + " calorieBurned=" + calorieBurned);

        return getSetUserSettingWithGoal(goal, distanceWalk, calorieBurned, BMR, timeFormat, distanceUnit, goalUnit);
    }

    public byte[] getSetUserSettingWithGoal(
            int goal, int distanceWalk, int calorieBurned, int BMR,
            int timeFormat, int distanceUnit, int goalUnit)
    {
        Log.d(LOGTAG,"getSetUserSettingWithGoal");

        byte[] data = new byte[16];

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

        distanceWalk = 2000;

        if (distanceUnit == 1) distanceWalk = (int) (distanceWalk * 62.1371192d);

        data[ 6 ] = (byte) (distanceWalk & 0xff);
        data[ 7 ] = (byte) ((distanceWalk >> 8) & 0xff);

        data[  8 ] = (byte) (calorieBurned & 0xff);
        data[  9 ] = (byte) ((calorieBurned >> 8) & 0xff);
        data[ 10 ] = (byte) ((calorieBurned >> 16) & 0xff);
        data[ 11 ] = (byte) ((calorieBurned >> 24) & 0x7f);

        if (distanceUnit == 1) data[ 11 ] |= (byte) 0x80;

        BMR *= 100;

        data[ 12 ] = (byte) (BMR & 0xff);
        data[ 13 ] = (byte) ((BMR >> 8) & 0xff);
        data[ 14 ] = (byte) ((BMR >> 16) & 0xff);
        data[ 15 ] = (byte) 1;

        return data;
    }

    private byte[] getStepHistoryData(int day)
    {
        byte[] data = new byte[ 1 ];

        data[ 0 ] = (byte) day;

        return data;
    }

    private byte[] getSleepHistoryData()
    {
        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) 0x80;
        data[ 1 ] = (byte) 0;

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
