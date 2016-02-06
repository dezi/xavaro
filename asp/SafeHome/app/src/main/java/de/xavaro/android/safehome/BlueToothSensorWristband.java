package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

@SuppressWarnings({"UnusedParameters", "unused"})
public class BlueToothSensorWristband implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothSensorWristband.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothSensorWristband(BlueTooth parent)
    {
        this.parent = parent;
    }

    //region Interface BlueTooth.BlueToothPhysicalDevice

    public boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("0000fff0-0000-1000-8000-00805f9b34fb");
    }

    public boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fff2-0000-1000-8000-00805f9b34fb");
    }

    public boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    public boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000fff1-0000-1000-8000-00805f9b34fb");
    }

    public void enableDevice()
    {
        syncIntentional = false;
    }

    public void syncSequence()
    {
        syncSequenceInternal();
    }

    public void sendCommand(JSONObject command)
    {
    }

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        parseResponseInternal(rd, characteristic);
    }

    //endregion Interface BlueTooth.BlueToothPhysicalDevice

    //region Internals

    private void syncSequenceInternal()
    {
        BlueTooth.GattAction ga;

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setDateTime();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setTimeFormat();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setDayMode();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setBody();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setAlarm();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setGoal();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setTouchVibration();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setLanguage();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setUnits();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = setHorzVert();
        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
        ga.data = getActivityCount();
        parent.gattSchedule.add(ga);

        parent.fireNext(false);
    }

    public void parseResponseInternal(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        int command = rd[ 0 ] & 0xff;

        switch (command)
        {
            case receive_serialno:
                parseSerialno(rd);
                break;
            case receive_language:
                parseLanguage(rd);
                break;
            case receive_macaddress:
                parseMacAddress(rd);
                break;
            case receive_activity_count:
                parseActivityCount(rd);
                break;
            case receive_activity_value:
                parseActivityValue(rd);
                break;
            case receive_set_screen:
                parseSetScreen(rd);
                break;
            case receive_press_key:
                parsePressKey(rd);
                break;
            case receive_version_code:
                parseVersion(rd);
                break;
            case receive_ack_datetime:
                parseAckDateTime(rd);
                break;
            case receive_ack_call:
                parseAckCall(rd);
                break;
            case receive_ack_daymode:
                parseAckDayMode(rd);
                break;
            case receive_ack_goal:
                parseAckGoal(rd);
                break;
            case receive_ack_timeformat:
                parseAckTimeFormat(rd);
                break;
            case receive_ack_body:
                parseAckBody(rd);
                break;
            case receive_ack_touch_vib:
                parseAckTouchVibrate(rd);
                break;
            case receive_ack_alarm:
                parseAckAlarm(rd);
                break;
            case receive_ack_horzvert:
                parseAckHorzVert(rd);
                break;
            default:
                Log.d(LOGTAG, "parseResponseInternal: unknown command: " + command);
                break;
        }
    }

    //endregion Internals

    //region Parse responses

    public static final int receive_ack_datetime = 129;
    public static final int receive_ack_daymode = 130;
    public static final int receive_ack_alarm = 131;
    public static final int receive_macaddress = 136;
    public static final int receive_language = 138;
    public static final int receive_ack_goal = 139;
    public static final int receive_ack_body = 140;
    public static final int receive_ack_timeformat = 141;
    public static final int receive_activity_count = 144;
    public static final int receive_activity_value = 147;
    public static final int receive_ack_call = 160;
    public static final int receive_serialno = 193;
    public static final int receive_set_screen = 208;
    public static final int receive_press_key = 209;
    public static final int receive_ack_horzvert = 210;
    public static final int receive_ack_touch_vib = 212;
    public static final int receive_version_code = 240;

    public static final int TYPE_SCREEN_DISTANCETIME = 0;
    public static final int TYPE_SCREEN_DATE = 1;
    public static final int TYPE_SCREEN_DEVICEID = 17;
    public static final int TYPE_SCREEN_STEPS = 33;
    public static final int TYPE_SCREEN_BURNEDCALS = 224;

    private boolean syncIntentional;
    private int todaysSteps;

    public void parseSetScreen(byte[] rd)
    {
        Log.d(LOGTAG, "parseSetScreen: " + (rd[ 2 ] & 0xff));

        int screen = rd[ 2 ] & 0xff;

        if (screen == TYPE_SCREEN_DEVICEID)
        {
            //
            // User paged through screens on sensor. Use device
            // screen as command for starting history update.
            //

            if (parent.gattSchedule.size() == 0)
            {
                syncIntentional = true;

                parent.callOnBluetoothFakeConnect();

                BlueTooth.GattAction ga;

                ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE, parent.currentControl);
                ga.data = getActivityCount();
                parent.gattSchedule.add(ga);

                parent.fireNext(false);
            }
        }
    }

    public void parseLanguage(byte[] rd)
    {
        Log.d(LOGTAG, "parseLanguage: " + rd[ 2 ]);
    }

    public void parsePressKey(byte[] rd)
    {
        //
        // rd[ 2 ] == 1 => camera
        //

        Log.d(LOGTAG, "parsePressKey: " + rd[ 2 ]);
    }

    public void parseSerialno(byte[] rd)
    {
        Log.d(LOGTAG, "parseSerialno: " + Simple.getHexBytesToString(rd, 2, 4));
    }

    public void parseMacAddress(byte[] rd)
    {
        String macaddress = "";

        for (int inx = 1; inx <= 6; inx++)
        {
            if (macaddress.length() > 0) macaddress += ":";

            macaddress += String.format("%02x", rd[ inx ]);
        }

        Log.d(LOGTAG, "parseMacAddress: " + macaddress);
    }

    private void parseActivityCount(byte[] rd)
    {
        int[] ints = new int[ 20 ];

        for (int inx = 0; inx < 20; inx++)
        {
            ints[ inx ] = rd[ inx ] & 0xff;
        }

        int year = (ints[ 3 ] * 100) + ints[ 4 ];

        Log.d(LOGTAG,"parseActivityCount:"
                + " sn:" + ints[ 1 ]
                + " count:" + ints[ 2 ]
                + " date:" + year + "-" + ints[ 5 ] + "-" + ints[ 6 ]);

        int activitySn = ints[ 1 ];
        int activityCount = ints[ 2 ];

        //
        // Check actuality.
        //

        long lastReadDate = 0;

        JSONObject status = HealthData.getStatus("sensor");

        if (status.has("lastReadDate"))
        {
            lastReadDate = Simple.getTimeStampFromISO(Json.getString(status, "lastReadDate"));
        }

        //
        // Read activity records going backwards in time.
        //

        for (int inx = activityCount; inx > 0; inx--)
        {
            BlueTooth.GattAction ga;

            ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
            ga.data = getActivityValue((activitySn + inx) % activityCount, 18);
            parent.gattSchedule.add(ga);

            ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
            ga.data = getActivityValue((activitySn + inx) % activityCount, 12);
            parent.gattSchedule.add(ga);

            ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
            ga.data = getActivityValue((activitySn + inx) % activityCount, 6);
            parent.gattSchedule.add(ga);

            ga = new BlueTooth.GattAction(BlueTooth.GattAction.MODE_WRITE,parent.currentControl);
            ga.data = getActivityValue((activitySn + inx) % activityCount, 0);
            parent.gattSchedule.add(ga);

            if ((Simple.nowAsTimeStamp() - lastReadDate) < ((86400L * 1000L) / 2)) break;
        }

        todaysSteps = 0;
    }

    public void parseActivityValue(byte[] rd)
    {
        int[] ints = new int[ 6 ];

        for (int inx = 0; inx < ints.length; inx++)
        {
            ints[ inx ] = rd[ inx ] & 0xff;
        }

        if ((ints[ 1 ] == 0xff) || (ints[ 2 ] == 0xff)) return;

        int year = ints[ 2 ] + (ints[ 1 ] * 100);
        int month = ints[ 3 ];
        int day = ints[ 4 ];
        int hour = ints[ 5 ];

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, 0 , 0);

        String timestamp = Simple.timeStampAsISO(calendar.getTimeInMillis());

        byte[] todayact = new byte[ 12 ];
        System.arraycopy(rd, 6, todayact, 0, 12);

        String activity = Simple.getHexBytesToString(todayact);

        Log.d(LOGTAG, "parseActivityValue:" + timestamp + "=" + activity);

        //
        // Check today range dates.
        //

        calendar.setTime(new Date());

        int nowyear = calendar.get(Calendar.YEAR);
        int nowmonth = calendar.get(Calendar.MONTH) + 1;
        int nowday = calendar.get(Calendar.DAY_OF_MONTH);

        if ((year == nowyear) && (month == nowmonth) && (day == nowday))
        {
            for (int inx = 0; inx < todayact.length; inx += 2)
            {
                if ((todayact[ inx ] & 0xff) == 0xff) continue;

                int steps = ((todayact[ inx ] & 0xff) << 8) + (todayact[ inx + 1 ] & 0xff);

                todaysSteps += steps;
            }
        }

        //
        // Check out of range dates.
        //

        long tendaysago = new Date().getTime();
        tendaysago -= 86400 * 11 * 1000L;
        String maxoldest = Simple.timeStampAsISO(tendaysago);

        if (timestamp.compareTo(maxoldest) >= 0)
        {
            //
            // Store data.
            //

            JSONObject record = new JSONObject();

            Json.put(record, "dts", timestamp);
            Json.put(record, "wda", activity);

            HealthData.addRecord("sensor", record);

            //
            // Update status.
            //

            JSONObject status = HealthData.getStatus("sensor");

            String lastActivityDate = Json.getString(status, "lastActivityDate");

            if ((lastActivityDate == null) || (timestamp.compareTo(lastActivityDate) >= 0))
            {
                lastActivityDate = timestamp;
            }

            Json.put(status, "lastActivityDate", lastActivityDate);
            Json.put(status, "lastReadDate", Simple.nowAsISO());

            HealthData.putStatus("sensor", status);
        }

        if (parent.gattSchedule.size() == 0)
        {
            if (syncIntentional)
            {
                //
                // Send today steps message to user interface.
                //

                JSONObject sensordata = new JSONObject();

                Json.put(sensordata, "type", "TodaysData");
                Json.put(sensordata, "stp", todaysSteps);

                JSONObject data = new JSONObject();
                Json.put(data, "sensor", sensordata);

                if (parent.dataCallback != null)
                {
                    parent.dataCallback.onBluetoothReceivedData(parent.deviceName, data);
                }
            }

            //
            // Fake a disconnect.
            //

            parent.callOnBluetoothFakeDisconnect();
        }
    }

    private void parseVersion(byte[] rd)
    {
        String version = "";

        for (int inx = 1; inx < rd.length; inx += 1)
        {
            if (rd[ inx ] == 0) break;

            version += (char) rd[ inx ];
        }

        Log.d(LOGTAG, "parseVersion: " + version);
    }

    public void parseAckDateTime(byte[] rd)
    {
        Log.d(LOGTAG, "parseDateTime: ACK");
    }

    public void parseAckCall(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckCall: ACK");
    }

    public void parseAckDayMode(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckDayMode: ACK");
    }

    public void parseAckGoal(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckGoal: ACK");
    }

    public void parseAckTimeFormat(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckTimeFormat: ACK");
    }

    public void parseAckBody(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckBody: ACK");
    }

    public void parseAckTouchVibrate(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckTouchVibrate: ACK");
    }

    public void parseAckAlarm(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckAlarm: ACK");
    }

    public void parseAckHorzVert(byte[] rd)
    {
        Log.d(LOGTAG, "parseAckHorzVert: ACK");
    }

    //endregion Parse responses

    //region Get commands

    private static final int get_macaddress = 8;
    private static final int get_activity_count = 16;
    private static final int get_activity_value = 19;
    private static final int get_serialno = 65;
    private static final int get_sensor_type = 96;
    private static final int get_sensor_value = 97;
    private static final int get_version_value = 112;

    private byte[] getActivityCount()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = get_activity_count;

        return wd;
    }

    private byte[] getActivityValue(int sequence, int hour)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = get_activity_value;
        wd[ 1 ] = (byte) sequence;
        wd[ 2 ] = (byte) hour;

        return wd;
    }

    private byte[] getMacAddress()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = get_macaddress;

        return wd;
    }

    private byte[] getSensorType()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = get_sensor_type;

        return wd;
    }

    private byte[] getSensorValue(byte b)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = get_sensor_value;
        wd[ 1 ] = b;

        return wd;
    }

    private byte[] getVersionValue()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = get_version_value;

        return wd;
    }

    private byte[] getSerialno()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = get_serialno;
        wd[ 1 ] = 0;

        return wd;
    }

    //endregion Get commands

    //region Set commands

    private static final int set_date_time = 1;
    private static final int set_day_mode = 2;
    private static final int set_alarm = 3;
    private static final int set_language = 10;
    private static final int set_goal = 11;
    private static final int set_body = 12;
    private static final int set_timeformat = 13;
    private static final int set_call_number = 32;
    private static final int set_sms_number = 33;
    private static final int set_lost_mode = 35;
    private static final int set_camera = 80;
    private static final int set_horzvert = 82;
    private static final int set_touch_vibration = 84;
    private static final int set_units = 85;

    //region Set action commands

    private byte[] setCallTermination()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_call_number;
        wd[ 1 ] = 1;

        return wd;
    }

    private byte[] setCall(String phone)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_call_number;

        if (phone != null)
        {
            wd[ 1 ] = (byte) phone.getBytes().length;

            System.arraycopy(phone.getBytes(), 0, wd, 2, phone.getBytes().length);
        }

        return wd;
    }

    private byte[] setSMS(String phone)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_sms_number;

        if (phone != null)
        {
            wd[ 1 ] = (byte) phone.getBytes().length;

            System.arraycopy(phone.getBytes(), 0, wd, 2, phone.getBytes().length);
        }

        return wd;
    }

    private byte[] setCameraSelfie(boolean on)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_camera;
        wd[ 1 ] = 1;
        wd[ 2 ] = (byte) (on ? 81 : 80);

        return wd;
    }

    public static final byte TYPE_LOST_NEARBY = 0;
    public static final byte TYPE_LOST_DISTANT = 1;
    public static final byte TYPE_LOST_CRITICAL = 2;

    private byte[] setLostMode(int howlost)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_lost_mode;
        wd[ 1 ] = (byte) howlost;

        return wd;
    }

    //endregion Set action commands

    //region Set settings commands

    private byte[] setDateTime()
    {
        Log.d(LOGTAG, "setDateTime");

        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_date_time;

        Calendar instance = Calendar.getInstance();

        wd[ 1 ] = (byte) (instance.get(Calendar.YEAR) / 100);
        wd[ 2 ] = (byte) (instance.get(Calendar.YEAR) % 100);
        wd[ 3 ] = (byte) (instance.get(Calendar.MONTH) + 1);
        wd[ 4 ] = (byte) instance.get(Calendar.DAY_OF_MONTH);
        wd[ 5 ] = (byte) instance.get(Calendar.HOUR_OF_DAY);
        wd[ 6 ] = (byte) instance.get(Calendar.MINUTE);
        wd[ 7 ] = (byte) instance.get(Calendar.SECOND);

        return wd;
    }

    private byte[] setDayMode()
    {
        int daybeg = 6;
        int dayend = 21;

        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_day_mode;

        wd[ 1 ] = (byte) daybeg;
        wd[ 2 ] = (byte) dayend;

        return wd;
    }

    private byte[] setGoal()
    {
        int goal = 10000;

        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_goal;
        wd[ 1 ] = 1;
        wd[ 2 ] = (byte) ((goal >> 8) & 0xff);
        wd[ 3 ] = (byte) (goal & 0xff);

        return wd;
    }

    private byte[] setTimeFormat()
    {
        return setTimeFormat(true);
    }

    private byte[] setTimeFormat(boolean h24)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_timeformat;
        wd[ 1 ] = (byte) (h24 ? 0 : 1);

        return wd;
    }

    public static final byte TYPE_GENDER_FEMALE = 0;
    public static final byte TYPE_GENDER_MALE = 1;

    private byte[] setBody()
    {
        int gender = TYPE_GENDER_MALE;
        int weight = 70;
        int height = 175;

        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_body;
        wd[ 1 ] = (byte) height;
        wd[ 2 ] = (byte) weight;
        wd[ 3 ] = (byte) gender;

        return wd;
    }

    private byte[] setHorzVert()
    {
        return setHorzVert(true);
    }

    private byte[] setHorzVert(boolean horizontal)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_horzvert;
        wd[ 1 ] = 1;
        wd[ 2 ] = (byte) (horizontal ? 0 : 1);

        return wd;
    }

    private byte[] setTouchVibration()
    {
        return setTouchVibration(false, 0);
    }

    private byte[] setTouchVibration(boolean on, int millis)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_touch_vibration;
        wd[ 1 ] = 1;
        wd[ 2 ] = (byte) (on ? millis : 0);

        return wd;
    }

    private byte[] setAlarm()
    {
        int hour = 14;
        int minute = 12;
        int daymask = 0x7f;

        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_alarm;
        wd[ 1 ] = (byte) hour;
        wd[ 2 ] = (byte) minute;
        wd[ 3 ] = (byte) daymask;

        return wd;
    }

    public static final byte TYPE_LANGUAGE_ENGLISH = 1;
    public static final byte TYPE_LANGUAGE_SPANISH = 2;
    public static final byte TYPE_LANGUAGE_FRENCH = 3;
    public static final byte TYPE_LANGUAGE_PORTUGUESE = 7;
    public static final byte TYPE_LANGUAGE_RUSSIAN = 33;
    public static final byte TYPE_LANGUAGE_CHINESE_SIMPLIFIED = 16;
    public static final byte TYPE_LANGUAGE_CHINESE_TRADITIONAL = 17;

    private byte[] setLanguage()
    {
        return setLanguage(TYPE_LANGUAGE_ENGLISH);
    }

    private byte[] setLanguage(int language)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_language;
        wd[ 1 ] = 1;
        wd[ 2 ] = (byte) language;

        return wd;
    }

    private byte[] setUnits()
    {
        return setUnits(true);
    }

    private byte[] setUnits(boolean metric)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = set_units;
        wd[ 1 ] = 1;
        wd[ 2 ] = (byte) (metric ? 0 : 1);

        return wd;
    }

    //endregion Set settings commands

    //endregion Set commands
}
