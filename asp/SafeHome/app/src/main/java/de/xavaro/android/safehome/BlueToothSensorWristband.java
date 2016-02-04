package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;

public class BlueToothSensorWristband implements BlueTooth.BlueToothPhysicalDevice
{
    private static final String LOGTAG = BlueToothSensorWristband.class.getSimpleName();

    private final BlueTooth parent;

    public BlueToothSensorWristband(BlueTooth parent)
    {
        this.parent = parent;
    }

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
        BlueTooth.GattAction ga;

        //
        // Notify primary.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_NOTIFY;
        ga.characteristic = parent.currentPrimary;

        parent.gattSchedule.add(ga);

        //
        // Read version string.
        //

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.characteristic = parent.currentControl;
        ga.data = getVersionValue();

        parent.gattSchedule.add(ga);

        ga = new BlueTooth.GattAction();

        ga.mode = BlueTooth.GattAction.MODE_WRITE;
        ga.characteristic = parent.currentControl;
        ga.data = getDateTimeValue();

        parent.gattSchedule.add(ga);

        parent.fireNext(false);
    }

    public void sendCommand(JSONObject command)
    {
    }

    public void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        int command = rd[ 0 ] & 0xff;

    }

    public static final int ctrl_gsensor = 83;
    public static final int ctrl_key = 81;
    public static final int ctrl_screen = 80;
    public static final int delete_activity = 20;
    public static final int get_activity = 19;
    public static final int get_activity_count = 16;
    public static final int get_mac = 8;
    public static final int get_sensor = 96;
    public static final int get_sensor_value = 97;
    public static final int get_version_code = 112;
    public static final int get_zoon = 65;
    public static final int receive_activity = 147;
    public static final int receive_activity_count = 144;
    public static final int receive_alarm = 131;
    public static final int receive_date_time = 129;
    public static final int receive_day_mode = 130;
    public static final int receive_goal = 139;
    public static final int receive_language = 138;
    public static final int receive_press_key = 209;
    public static final int receive_profile = 140;
    public static final int receive_set_screen = 208;
    public static final int receive_time_format = 141;
    public static final int receive_version_code = 240;
    public static final int receive_zoon = 193;
    public static final int set_date_time = 1;
    public static final int set_day_mode = 2;
    public static final int set_alarm = 3;
    public static final int set_body = 12;
    public static final int set_callin = 32;
    public static final int set_hour = 13;
    public static final int set_hv = 82;
    public static final int set_language = 10;
    public static final int set_target = 11;
    public static final int set_time_format = 13;
    public static final int set_touch_vibration = 84;
    public static final int set_message = 33;
    public static final int set_beacon_startstop = 67;

    public static String parseVersion(byte[] rd)
    {
        String version = "";

        for (int inx = 1; inx < rd.length; inx += 1)
        {
            if (rd[ inx ] == 0) break;

            version += (char) rd[ inx ];
        }

        return version;
    }

    public static byte[] getVersionValue()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = 112;

        return wd;
    }

    public static byte[] getActivityCountValue()
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = 16;
        return wd;
    }

    public static byte[] getActivityValue(int i, int i2)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = (byte) 19;
        wd[ 1 ] = (byte) i;
        wd[ 2 ] = (byte) i2;
        return wd;
    }

    public static byte[] getAlarmValue(Context context)
    {
        int b = 0;
        int c = 0;
        int repeatDate = 0;

        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = 3;
        wd[ 1 ] = (byte) b;
        wd[ 2 ] = (byte) c;
        wd[ 3 ] = (byte) repeatDate;

        return wd;
    }

    public static byte[] getCallTerminationValue()
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = (byte) 32;
        wd[ 1 ] = (byte) 1;

        return wd;
    }

    public static byte[] getCallValue(String phone)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = (byte) 32;

        if (phone != null)
        {
            wd[ 1 ] = (byte) 0;
            wd[ 2 ] = (byte) phone.getBytes().length;

            System.arraycopy(phone.getBytes(), 0, wd, 2, phone.getBytes().length);
        }

        return wd;
    }

    public static byte[] getSMSValue(String phone)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = (byte) 33;

        if (phone != null)
        {
            wd[ 1 ] = (byte) 0;
            wd[ 2 ] = (byte) phone.getBytes().length;

            System.arraycopy(phone.getBytes(), 0, wd, 2, phone.getBytes().length);
        }

        return wd;
    }

    public static byte[] getCameraValue(int i)
    {
        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = (byte) 80;
        wd[ 1 ] = (byte) 1;
        wd[ 2 ] = (i == 0) ? (byte) 81 : (byte) 80;

        return wd;
    }

    public static byte[] getDateTimeValue()
    {
        byte[] wd = new byte[ 20 ];

        Calendar instance = Calendar.getInstance();

        wd[ 0 ] = (byte) 1;
        wd[ 1 ] = (byte) (instance.get(Calendar.YEAR) / 100);
        wd[ 2 ] = (byte) (instance.get(Calendar.YEAR) % 100);
        wd[ 3 ] = (byte) (instance.get(Calendar.MONTH) + 1);
        wd[ 4 ] = (byte) instance.get(Calendar.DAY_OF_MONTH);
        wd[ 5 ] = (byte) instance.get(Calendar.HOUR_OF_DAY);
        wd[ 6 ] = (byte) instance.get(Calendar.MINUTE);
        wd[ 7 ] = (byte) instance.get(Calendar.SECOND);

        return wd;
    }

    public static byte[] getDayModeValue(Context context)
    {
        int i = 9;
        int i2 = 21;

        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = (byte) 2;
        wd[ 1 ] = (byte) i;
        wd[2] = (byte) i2;
        return wd;
    }

    public static byte[] getGoalValue()
    {
        int i = 10000;

        byte[] wd = new byte[ 20 ];
        String toBinaryString = Integer.toBinaryString(i);
        if (toBinaryString != null)
        {
            int length = toBinaryString.length();
            if (length < 16)
            {
                int i2 = 0;
                while (i2 < 16 - length)
                {
                    i2 += 1;
                    toBinaryString = "0" + toBinaryString;
                }
            }
        }
        String substring = toBinaryString.substring(0, 8);
        toBinaryString = toBinaryString.substring(8, 16);
        wd[ 0 ] = (byte) 11;
        wd[ 1 ] = (byte) 1;
        wd[2] = (byte) Integer.parseInt(substring, 2);
        wd[3] = (byte) Integer.parseInt(toBinaryString, 2);
        return wd;
    }

    public static byte[] getHVvalue(boolean z)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = (byte) 82;
        wd[ 1 ] = (byte) 1;
        wd[ 2 ] = (byte) (z ? 1 : 0);
        return wd;
    }

    public static byte[] getLanguageValue(Context context)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = (byte) 10;
        wd[ 1 ] = (byte) 1;
        wd[ 2 ] = (byte) 1;
        return wd;
    }

    public static byte[] getLostModeValue(int i)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = (byte) 35;
        wd[ 1 ] = (byte) i;
        return wd;
    }

    public static byte[] getProfileValue(Context context)
    {
        double DEFAULT_WEIGHT = 70.0d;
        double DEFAULT_HEIGHT = 175.0d;

        int a = 1;
        double c = DEFAULT_WEIGHT;
        double d = DEFAULT_HEIGHT;

        byte[] wd = new byte[ 20 ];

        wd[ 0 ] = (byte) 12;
        wd[ 1 ] = (byte) ((int) d);
        wd[ 2 ] = (byte) ((int) c);
        wd[ 3 ] = (byte) a;

        return wd;
    }

    public static byte[] getSensorType()
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = (byte) 96;
        return wd;
    }

    public static byte[] getSensorValue(byte b)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = 97;
        wd[ 1 ] = b;
        return wd;
    }

    public static byte[] getTimeFormatValue(boolean ampm)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = 13;
        wd[ 1 ] = (byte) (ampm ? 0 : 1);
        return wd;
    }

    public static byte[] getTouchVibrationvalue(boolean z, int i)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = 84;
        wd[ 1 ] = 1;
        wd[ 1 ] = (byte) (z ? i : 0);
        return wd;
    }

    public static byte[] getUnitValue(int i)
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = 85;
        wd[ 1 ] = 1;
        wd[ 2 ] = (byte) i;
        return wd;
    }

    public static byte[] getZoonValue()
    {
        byte[] wd = new byte[ 20 ];
        wd[ 0 ] = 65;
        wd[ 1 ] = 0;
        return wd;
    }
}
