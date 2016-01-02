package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

@SuppressWarnings({"UnusedAssignment", "unused"})
public class BlueToothScale extends BlueTooth
{
    private static final String LOGTAG = BlueToothScale.class.getSimpleName();

    public BlueToothScale(Context context)
    {
        super(context);
    }

    public BlueToothScale(Context context, String deviceTag)
    {
        super(context, deviceTag);
    }

    private static class Scales
    {
        public static final String SBF70 = "SBF70";
        public static final String BF710 = "Beurer BF710";
        public static final String GS485 = "Beurer GS485";
        public static final String SANITAS_SBF70 = "SANITAS SBF70";
    }

    @Override
    protected boolean isCompatibleDevice(String devicename)
    {
        return (devicename.equalsIgnoreCase(Scales.SBF70) ||
                devicename.equalsIgnoreCase(Scales.BF710) ||
                devicename.equalsIgnoreCase(Scales.GS485) ||
                devicename.equalsIgnoreCase(Scales.SANITAS_SBF70));
    }

    @Override
    protected boolean isCompatibleService(BluetoothGattService service)
    {
        return service.getUuid().toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected boolean isCompatiblePrimary(BluetoothGattCharacteristic characteristic)
    {
        return characteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected void enableDevice()
    {
        Log.d(LOGTAG,"enableDevice: " + currentPrimary);

        if (currentPrimary != null)
        {
            GattAction ga;

            //
            // Subscribe to normal data notification.
            //

            ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_NOTIFY;
            ga.characteristic = currentPrimary;

            gattSchedule.add(ga);

            //
            // Issue a status request to figure out
            // if device connects by itself or user.
            // Issue a force disconnect if device is
            // sleeping.
            //

            ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_WRITE;
            ga.data = getScaleSleepStatus();
            ga.characteristic = currentPrimary;

            gattSchedule.add(ga);

            fireNext();
        }
    }

    private JSONObject cbtemp;
    private JSONObject cbdata;
    private JSONArray cbtarr;
    private byte cbbyte;

    private void cbtempPut(String key, Object value)
    {
        try
        {
            cbtemp.put(key, value);
        }
        catch (JSONException ignore)
        {
        }
    }

    private void cbdataPut(String key, Object value)
    {
        try
        {
            cbdata.put(key, value);
        }
        catch (JSONException ignore)
        {
        }
    }

    @Override
    protected void parseResponse(byte[] rd, boolean intermediate)
    {
        cbdata = new JSONObject();

        boolean wantsack = parseData(rd);

        if (wantsack)
        {
            Log.d(LOGTAG, "parseResponse: send ack");

            GattAction ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_WRITE;
            ga.data = getAcknowledgementData(rd);
            ga.characteristic = currentPrimary;

            gattSchedule.add(0, ga);

            fireNext();
        }

        if (cbdata.keys().hasNext())
        {
            //
            // Object is not empty.
            //

            try
            {
                JSONObject data = new JSONObject();
                data.put("scale", cbdata);

                if (dataCallback != null)
                {
                    dataCallback.onBluetoothReceivedData(currentGatt.getDevice(), data);
                }
            }
            catch (JSONException ignore)
            {
            }
        }
    }

    private void testScalex()
    {
        if (currentPrimary != null)
        {
            GattAction ga;

            ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_WRITE;
            ga.data = getModuleVersion();
            ga.characteristic = currentPrimary;

            gattSchedule.add(ga);

            ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_WRITE;
            ga.data = getScaleStatusForUser(101);
            ga.characteristic = currentPrimary;

            gattSchedule.add(ga);

            ga = new GattAction();

            ga.gatt = currentGatt;
            ga.mode = GattAction.MODE_WRITE;
            ga.data = getSetDateTime();
            ga.characteristic = currentPrimary;

            gattSchedule.add(ga);

            fireNext();
        }
    }

    private int mMaximumUsers;
    private byte waterByte;
    private int didFinishMeasurementStatus;

    // @formatter:off
    private long[]   rawListOfUuids        = new long  [  8 ];
    private String[] rawUserListInitials   = new String[  8 ];
    // @formatter:on

    // @formatter:off

    private int[]    rawAmrArray           = new int   [ 50 ];
    private float[]  rawBmiArray           = new float [ 50 ];
    private int[]    rawBmrArray           = new int   [ 50 ];
    private float[]  rawBodyFatArray       = new float [ 50 ];
    private float[]  rawBoneMassArray      = new float [ 50 ];
    private int[]    rawImpedanceArray     = new int   [ 50 ];
    private int[]    rawMeasurementIdArray = new int   [ 50 ];
    private float[]  rawMuscleArray        = new float [ 50 ];
    private int[]    rawTimeStampArray     = new int   [ 50 ];
    private float[]  rawWaterArray         = new float [ 50 ];
    private double[] rawWeightArray        = new double[ 50 ];
    // @formatter:on

    public boolean parseData(byte[] rd)
    {
        Log.d(LOGTAG, "parseData: " + rd[ 0 ] + " " + rd[ 1 ]);
        Log.d(LOGTAG, "parseData: " + StaticUtils.hexBytesToString(rd));

        if (rd[ 0 ] == -16)
        {
            //
            // Special response used to disconnect sleeping
            // scale.
            //

            boolean status = (rd[ 1 ] != 0);

            if (status)
            {
                forceDisconnect();
            }
            else
            {
                if (connectCallback != null)
                {
                    //
                    // Scale equippment ist active, when it does
                    // not sleep.
                    //

                    connectCallback.onBluetoothActive(currentGatt.getDevice());
                }
            }

            cbdataPut("type", "ScaleSleepWithStatus");
            cbdataPut("status", status);

            return false;
        }

        if (rd[ 0 ] == -15)
        {
            cbdataPut("type", "ModuleVersion");
            cbdataPut("version", rd[ 1 ]);

            return false;
        }

        if (rd[ 0 ] == -32)
        {
            cbdataPut("type", "UnknownResponse");
            cbdataPut("data", rd[ 1 ]);

            return false;
        }

        if (rd[ 0 ] == -26)
        {
            cbdataPut("type", "DeviceReady");
            cbdataPut("status", rd[ 1 ]);

            return false;
        }

        if (rd[ 0 ] == -24)
        {
            boolean status = (rd[ 1 ] == -2 && rd[ 2 ] == -69 && rd[ 3 ] == -17);

            cbdataPut("type", "PairingCompleted");
            cbdataPut("status", status);

            return false;
        }

        if (rd[ 0 ] == -14)
        {
            long timeStamp = getTimeStampInMilliSeconds(convertBytesToInt(Arrays.copyOfRange(rd, 1, 5)));

            cbdataPut("type", "RemoteTimeStamp");
            cbdataPut("timeStamp", timeStamp);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 2 ] == 79) && (rd.length >= 5))
        {
            cbdataPut("type", "ScaleStatusWithBatteryLevel");

            cbdataPut("weightThreshold", ((float) unsignedByteToInt(rd[ 5 ])) / 10.0f);
            cbdataPut("bodyFatThreshold", ((float) unsignedByteToInt(rd[ 6 ])) / 10.0f);

            cbdataPut("batteryLevel", (float) unsignedByteToInt(rd[ 4 ]));

            cbdataPut("weightUnit", unsignedByteToInt(rd[ 7 ]));

            cbdataPut("userExists", (rd[ 8 ] == 0));
            cbdataPut("referWeightExists", (rd[ 9 ] == 0));
            cbdataPut("measurementExists", (rd[ 10 ] == 0));
            cbdataPut("scaleVersion", unsignedByteToInt(rd[ 11 ]));

            return true;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == 88))
        {
            cbdataPut("type", "LiveWeight");

            cbdataPut("status", (rd[ 2 ] != 1));
            cbdataPut("weight", ((double) unsignedBytesToInt(rd[ 3 ], rd[ 4 ])) / 20.0d);

            return true;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == 89))
        {
            if (rd[ 3 ] == 1)
            {
                cbtemp = new JSONObject();
                cbtempPut("type", "FinishMeasurementOnTimestamp");

                byte[] rawUuid = new byte[ 8 ];
                System.arraycopy(rd, 5, rawUuid, 0, 8);

                cbtempPut("Uuid", convertBytesToLong(rawUuid));
                cbtempPut("finished", convertByteToInt(rd[ 4 ]));

                Log.d(LOGTAG, "parseData: FinishMeasurementOnTimestamp part 1");
            }

            if (rd[ 3 ] == 2)
            {
                cbtempPut("timeStamp", convertBytesToInt(Arrays.copyOfRange(rd, 4, 8)));
                cbtempPut("weightArray", unsignedBytesToInt(rd[ 8 ], rd[ 9 ]) / 20.0d);
                cbtempPut("impedance", unsignedBytesToInt(rd[ 10 ], rd[ 11 ]));
                cbtempPut("bodyFat", unsignedBytesToInt(rd[ 12 ], rd[ 13 ]) / 10.0d);

                //
                // Parts split in the middle of data, nice.
                //

                cbbyte = rd[ 14 ];

                Log.d(LOGTAG, "parseData: FinishMeasurementOnTimestamp part 2");
            }

            if (rd[ 3 ] == 3)
            {
                cbtempPut("water", unsignedBytesToInt(cbbyte , rd[ 4 ]) / 10.0d);
                cbtempPut("muscle", unsignedBytesToInt(rd[ 5 ], rd[ 6 ]) / 10.0d);
                cbtempPut("boneMass", unsignedBytesToInt(rd[ 7 ], rd[ 8 ]) / 20.0d);
                cbtempPut("BMR", unsignedBytesToInt(rd[ 9 ], rd[ 10 ]));
                cbtempPut("AMR", unsignedBytesToInt(rd[ 11 ], rd[ 12 ]));
                cbtempPut("BMI", unsignedBytesToInt(rd[ 13 ], rd[ 14 ]) / 10.0d);

                Log.d(LOGTAG, "parseData: FinishMeasurementOnTimestamp part 3");

                cbdata = cbtemp;
                cbtemp = null;
            }

            return true;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 69))
        {
            Log.d(LOGTAG, "parseData: UserWithStatus");

            cbdataPut("type", "UserWithStatus");
            cbdataPut("status", rd[ 3 ]);

            if (rd.length >= 5)
            {
                cbdataPut("timeStamp", convertBytesToInt(Arrays.copyOfRange(rd, 4, 8)));
                cbdataPut("weight", unsignedBytesToInt(rd[ 8 ], rd[ 9 ]) / 20.0d);
                cbdataPut("bodyFat", unsignedBytesToInt(rd[ 8 ], rd[ 9 ]) / 10.0d);
            }
            else
            {
                cbdataPut("timeStamp", 0);
                cbdataPut("weight", 0.0d);
                cbdataPut("bodyFat", 0.0d);
            }

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 51))
        {
            cbdataPut("type", "MaximumUsers");
            cbdataPut("maxUser", rd[ 5 ]);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == 66))
        {
            int part = rd[ 3 ];

            if (part == 1)
            {
                //
                // Very first record.
                //

                cbtarr = new JSONArray();
            }

            if ((part % 2) == 1)
            {
                //
                // Part 1 of records.
                //

                cbtemp = new JSONObject();

                cbtempPut("timeStamp", convertBytesToInt(Arrays.copyOfRange(rd, 4, 8)));
                cbtempPut("weightArray", unsignedBytesToInt(rd[ 8 ], rd[ 9 ]) / 20.0d);
                cbtempPut("impedance", unsignedBytesToInt(rd[ 10 ], rd[ 11 ]));
                cbtempPut("bodyFat", unsignedBytesToInt(rd[ 12 ], rd[ 13 ]) / 10.0d);

                //
                // Parts split in the middle of data, nice.
                //

                cbbyte = rd[ 14 ];
            }

            if ((part % 2) == 0)
            {
                //
                // Part 2 of records.
                //

                cbtempPut("water", unsignedBytesToInt(cbbyte, rd[ 4 ]) / 10.0d);
                cbtempPut("muscle", unsignedBytesToInt(rd[ 5 ], rd[ 6 ]) / 10.0d);
                cbtempPut("boneMass", unsignedBytesToInt(rd[ 7 ], rd[ 8 ]) / 20.0d);
                cbtempPut("BMR", unsignedBytesToInt(rd[ 9 ], rd[ 10 ]));
                cbtempPut("AMR", unsignedBytesToInt(rd[ 11 ], rd[ 12 ]));
                cbtempPut("BMI", unsignedBytesToInt(rd[ 13 ], rd[ 14 ]) / 10.0d);

                cbtarr.put(cbtemp);
                cbtemp = null;
            }

            if (part == rd[ 2 ])
            {
                //
                // Very last record.
                //

                cbdata = new JSONObject();
                cbdataPut("type", "UserMeasurementsTimeStamps");
                cbdataPut("array", cbtarr);
                cbtarr = null;
            }

            return true;
        }





        if (rd[ 0 ] == -25)
        {
            int x;
            double weight;

            if (rd[ 1 ] == -16)
            {
                int status2;

                if (rd[ 2 ] == 48 || rd[ 2 ] == 49 ||
                        rd[ 2 ] == 50 || rd[ 2 ] == 51 ||
                        rd[ 2 ] == 64 || rd[ 2 ] == 65 ||
                        rd[ 2 ] == 67 || rd[ 2 ] == 68 ||
                        rd[ 2 ] == 70 || rd[ 2 ] == 72 ||
                        rd[ 2 ] == 73 || rd[ 2 ] == 74 ||
                        rd[ 2 ] == 75 || rd[ 2 ] == 77 ||
                        rd[ 2 ] == 78 || rd[ 2 ] == 86 ||
                        rd[ 2 ] == 53 || rd[ 2 ] == 54)
                {
                    if (rd[ 2 ] == 54)
                    {
                        char gender;
                        int activityIndex;
                        int[] birthDate = new int[ 3 ];
                        char[] initials = new char[ 3 ];
                        String initial = BuildConfig.FLAVOR;
                        status2 = convertByteToInt(rd[ 3 ]);

                        for (x = 0; x < 3; x++)
                        {
                            birthDate[ x ] = convertByteToInt(rd[ x + 7 ]);
                        }
                        birthDate[ 0 ] = birthDate[ 0 ] + 1900;
                        if (convertByteToInt(rd[ 11 ]) >= convertByteToInt(Byte.MIN_VALUE))
                        {
                            gender = 'M';
                        }
                        else
                        {
                            gender = 'F';
                        }
                        int height = convertByteToInt(rd[ 10 ]);
                        if (convertByteToInt(rd[ 11 ]) >= convertByteToInt(Byte.MIN_VALUE))
                        {
                            activityIndex = convertByteToInt(rd[ 11 ]) - convertByteToInt(Byte.MIN_VALUE);
                        }
                        else
                        {
                            activityIndex = convertByteToInt(rd[ 11 ]);
                        }
                        for (x = 0; x < 3; x++)
                        {
                            initials[ x ] = (char) rd[ x + 4 ];
                        }
                        initial = new String(initials);

                        //this.mBleScaleCallbacks.didGetUserInfoOfInitials(initial, birthDate, height, gender, activityIndex, status2);

                        return false;
                    }

                    if (rd[ 2 ] == 65)
                    {
                        if (rd[ 3 ] == 0)
                        {
                            // this.mBleScaleCallbacks.didGetUserMeasurementsTimeStamps(null, null, null, null, null, null, null, null, null, null, 0);
                        }

                        return false;
                    }

                    if (rd[ 2 ] == 70)
                    {
                        if (rd[ 3 ] == 1)
                        {
                            //this.mBleScaleCallbacks.didGetUnknownMeasurementsIDs(null, null, null, null, 0);
                        }
                    }

                    //this.mBleScaleCallbacks.scaleAcknowledgedCommand(convertByteToInt(recievedData[ 2 ]), convertByteToInt(recievedData[ 3 ]));

                    return false;
                }
                return false;
            }

            if (rd[ 1 ] == 52)
            {
                int count = convertByteToInt(rd[ 2 ]);
                byte[] rawUuid = new byte[ 8 ];
                for (int i = 0; i < 8; i++)
                {
                    rawUuid[ i ] = rd[ i + 4 ];
                }
                this.rawListOfUuids[ convertByteToInt(rd[ 3 ]) - 1 ] = convertBytesToLong(rawUuid);
                char[] rawInitial = new char[ 3 ];
                for (int i = 0; i < 3; i++)
                {
                    rawInitial[ i ] = (char) rd[ i + 12 ];
                }
                this.rawUserListInitials[ convertByteToInt(rd[ 3 ]) - 1 ] = new String(rawInitial);

                //this.mBluetoothLeApi.send_acknowledgement(rd);

                if (rd[ 2 ] == rd[ 3 ])
                {
                    long[] listOfUuids = new long[ count ];
                    String[] userListInitials = new String[ count ];
                    for (int i = 0; i < count; i++)
                    {
                        listOfUuids[ i ] = this.rawListOfUuids[ i ];
                        userListInitials[ i ] = this.rawUserListInitials[ i ];
                    }

                    //    this.mBleScaleCallbacks.didGetUUIDsListOfUsers(listOfUuids, userListInitials, count, this.mMaximumUsers);
                }

                return true;
            }


            if (rd[ 1 ] == 71)
            {
                int index = convertByteToInt(rd[ 3 ]) - 1;
                this.rawMeasurementIdArray[ index ] = convertByteToInt(rd[ 4 ]);
                this.rawTimeStampArray[ index ] = convertBytesToInt(Arrays.copyOfRange(rd, 5, 9));
                byte[] data = new byte[ 4 ];
                this.rawWeightArray[ index ] = ((double) convertBytesToInt(data)) / 20.0d;
                this.rawImpedanceArray[ index ] = (convertByteToInt(rd[ 11 ]) * 256) + convertByteToInt(rd[ 12 ]);

                //new Handler(this.mBluetoothLeApi.mContext.getMainLooper()).postDelayed(new C02341(rd), 10);

                if (rd[ 2 ] == rd[ 3 ])
                {
                    int totalMeasurementCount = convertByteToInt(rd[ 2 ]);
                    int[] measurementIdArray = new int[ totalMeasurementCount ];
                    int[] impedanceArrary = new int[ totalMeasurementCount ];
                    long[] timeStampArray = new long[ totalMeasurementCount ];
                    double[] weightArray = new double[ totalMeasurementCount ];
                    for (int i = 0; i < totalMeasurementCount; i++)
                    {
                        timeStampArray[ i ] = getTimeStampInMilliSeconds(this.rawTimeStampArray[ i ]);
                        measurementIdArray[ i ] = this.rawMeasurementIdArray[ i ];
                        impedanceArrary[ i ] = this.rawImpedanceArray[ i ];
                        weightArray[ i ] = this.rawWeightArray[ i ];
                    }

                    // this.mBleScaleCallbacks.didGetUnknownMeasurementsIDs(measurementIdArray, timeStampArray, weightArray, impedanceArrary, totalMeasurementCount);
                }

                return false;
            }

            if (rd[ 1 ] == 76)
            {
                Log.d(LOGTAG, "recievedData[1] : " + rd[ 1 ] + " : recievedData[3] : " + rd[ 3 ]);

                if (rd[ 3 ] == 1)
                {
                    this.rawTimeStampArray[ 0 ] = convertBytesToInt(Arrays.copyOfRange(rd, 4, 8));
                    byte[] data = new byte[ 4 ];
                    this.rawWeightArray[ 0 ] = ((double) convertBytesToInt(data)) / 20.0d;
                    this.rawImpedanceArray[ 0 ] = (convertByteToInt(rd[ 10 ]) * 256) + convertByteToInt(rd[ 11 ]);
                    this.rawBodyFatArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 12 ]) * 256) + convertByteToInt(rd[ 13 ]))) / 10.0d);
                    this.waterByte = rd[ 14 ];

                    //this.mBluetoothLeApi.send_acknowledgement(rd);
                }

                if (rd[ 3 ] == 2)
                {
                    this.rawWaterArray[ 0 ] = (float) (((double) ((convertByteToInt(this.waterByte) * 256) + convertByteToInt(rd[ 4 ]))) / 10.0d);
                    this.rawMuscleArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 5 ]) * 256) + convertByteToInt(rd[ 6 ]))) / 10.0d);
                    this.rawBoneMassArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 7 ]) * 256) + convertByteToInt(rd[ 8 ]))) / 20.0d);
                    this.rawBmrArray[ 0 ] = (convertByteToInt(rd[ 9 ]) * 256) + convertByteToInt(rd[ 10 ]);
                    this.rawAmrArray[ 0 ] = (convertByteToInt(rd[ 11 ]) * 256) + convertByteToInt(rd[ 12 ]);
                    this.rawBmiArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 13 ]) * 256) + convertByteToInt(rd[ 14 ]))) / 10.0d);

                    //this.mBluetoothLeApi.send_acknowledgement(rd);

                    long timeStamp = getTimeStampInMilliSeconds(this.rawTimeStampArray[ 0 ]);
                    //    this.mBleScaleCallbacks.didAssignMeasurementFromtimeStamp(timeStamp, this.rawWeightArray[ 0 ], this.rawImpedanceArray[ 0 ], this.rawBmiArray[ 0 ], this.rawBodyFatArray[ 0 ], this.rawWaterArray[ 0 ], this.rawMuscleArray[ 0 ], this.rawBoneMassArray[ 0 ], this.rawBmrArray[ 0 ], this.rawAmrArray[ 0 ]);
                }

                return false;
            }
        }

        return false;
    }


    private boolean isCompatibleDevice()
    {
        return isCompatibleDevice(this.modelName);
    }

    public byte[] getUserList()
    {
        Log.d(LOGTAG,"getUserList");

        byte[] data = new byte[ 2 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
        }
        else
        {
            data[ 0 ] = (byte) -9;
        }

        data[ 1 ] = (byte) 51;

        return data;
    }

    public byte[] getScaleStatusForUser(long uuid)
    {
        Log.d(LOGTAG,"getScaleStatusForUserBytesData-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
        }
        else
        {
            data[ 0 ] = (byte) -9;
        }

        data[ 1 ] = (byte) 79;

        byte[] uuidInBytes = convertLongToBytes(uuid);
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = uuidInBytes[ i ];
        }
        return data;
    }

    public byte[] getTxPower()
    {
        Log.d(LOGTAG,"getTxPower");

        byte[] data = new byte[ 2 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -20;
        }
        else
        {
            data[ 0 ] = (byte) -4;
        }

        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getSlowAdvertisement(boolean isSet, int interval)
    {
        Log.d(LOGTAG,"getSlowAdvertisementBytesData-->isSet : " + isSet + ", interval : " + interval);
        byte[] data = new byte[ 2 ];
        if (isSet)
        {
            if (isCompatibleDevice())
            {
                data[ 0 ] = (byte) -19;
                data[ 1 ] = (byte) interval;
            }
            else
            {
                data[ 0 ] = (byte) -3;
                data[ 1 ] = (byte) interval;
            }
        }
        else
            if (isCompatibleDevice())
            {
                data[ 0 ] = (byte) -18;
                data[ 1 ] = (byte) 2;
            }
            else
            {
                data[ 0 ] = (byte) -2;
                data[ 1 ] = (byte) 2;
            }
        return data;
    }

    public byte[] getCheckUserExists(long uuid)
    {
        Log.d(LOGTAG,"getCheckUserExistsBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 0;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte)  0;
        }
        byte[] rawUuid = convertLongToBytes(uuid);
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        return data;
    }

    public byte[] getCreateUser(long uuid, String initial, int[] birthDate, int height, char gender, int activityIndex)
    {
        Log.d(LOGTAG,"getCreateUserBytesData:"
                + " uuid=" + uuid
                + " initial=" + initial
                + " birthDate=" + Arrays.toString(birthDate)
                + " height=" + height
                + " gender=" + gender
                + " activityIndex=" + activityIndex);

        byte[] data = new byte[ 18 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
        }
        else
        {
            data[ 0 ] = (byte) -9;
        }

        data[ 1 ] = (byte) 49;

        byte[] rawUuid = convertLongToBytes(uuid);

        System.arraycopy(rawUuid, 0, data, 2, 8);

        while (initial.length() < 3) initial += " ";
        char[] rawInitial = initial.toCharArray();

        data[ 10 ] = (byte) rawInitial[ 0 ];
        data[ 11 ] = (byte) rawInitial[ 1 ];
        data[ 12 ] = (byte) rawInitial[ 2 ];
        data[ 13 ] = (byte) (birthDate[ 0 ] - 1900);
        data[ 14 ] = (byte) birthDate[ 1 ];
        data[ 15 ] = (byte) birthDate[ 2 ];
        data[ 16 ] = (byte) height;
        data[ 17 ] = (byte) ((gender == 'M' ? 128 : 0) + activityIndex);

        return data;
    }

    public byte[] getDeleteUser(long uuid)
    {
        Log.d(LOGTAG,"getDeleteUserBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 50;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 50;
        }
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        return data;
    }

    public byte[] getTakeUserMeasurement(long uuid)
    {
        Log.d(LOGTAG,"getTakeUserMeasurementBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 64;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 64;
        }
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        return data;
    }

    public byte[] getUserMeasurements(long uuid)
    {
        Log.d(LOGTAG,"getUserMeasurementsBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 65;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 65;
        }
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        return data;
    }

    public byte[] getDeleteUserMeasurements(long uuid)
    {
        Log.d(LOGTAG,"getDeleteUserMeasurementsBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 67;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 67;
        }
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        return data;
    }

    public byte[] getSetUserWeight(long uuid, float weight, float bodyFat, long timeStamp)
    {
        int i;
        Log.d(LOGTAG,"getSetUserWeightBytesData-->uuid : " + uuid + ", weight : " + weight + ", bodyFat : " + bodyFat + ", timeStamp : " + timeStamp);
        byte[] data = new byte[ 18 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 68;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 68;
        }
        for (i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        weight *= 20.0f;
        data[ 10 ] = (byte) ((int) (weight / 256.0f));
        data[ 11 ] = (byte) ((int) weight);
        bodyFat *= 10.0f;
        data[ 12 ] = (byte) ((int) (bodyFat / 256.0f));
        data[ 13 ] = (byte) ((int) bodyFat);
        int timeStampInSeconds = getTimeStampInSeconds(timeStamp);
        for (i = 0; i < 4; i++)
        {
            data[ i + 14 ] = (byte) (timeStampInSeconds >> ((3 - i) * 8));
        }
        return data;
    }

    public byte[] getUserWeightAndBodyFat(long uuid)
    {
        Log.d(LOGTAG,"getUserWeightAndBodyFatBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 69;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 69;
        }
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        return data;
    }

    public byte[] getUnknownMeasurements()
    {
        Log.d(LOGTAG, "getUnknownMeasurements");
        byte[] data = new byte[ 2 ];
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 70;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 70;
        }
        return data;
    }

    public byte[] getSaveAsUnknownMeasurementWithtimestamp(int timeStamp, float weight, int impedance)
    {
        Log.d(LOGTAG,"getSaveAsUnknownMeasurementWithtimestampBytesData-->timeStamp : " + timeStamp + ", weight : " + weight + ", impedance : " + impedance);
        byte[] data = new byte[ 10 ];
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 72;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 72;
        }
        for (int i = 0; i < 4; i++)
        {
            data[ i + 2 ] = (byte) (timeStamp >> ((3 - i) * 8));
        }
        weight *= 20.0f;
        data[ 6 ] = (byte) ((int) (weight / 256.0f));
        data[ 7 ] = (byte) ((int) weight);
        data[ 8 ] = (byte) (impedance / 256);
        data[ 9 ] = (byte) impedance;
        return data;
    }

    public byte[] getDeleteUnknownMeasurement(int measurementId)
    {
        Log.d(LOGTAG,"getDeleteUnknownMeasurementBytesData-->measurementId : " + measurementId);
        byte[] data = new byte[ 3 ];
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 73;
            data[ 2 ] = (byte) measurementId;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 73;
            data[ 2 ] = (byte) measurementId;
        }
        return data;
    }

    public byte[] getTakeGuestMeasurementWithInitials(String initial, int[] birthDate, int height, char gender, int activityIndex, int unit)
    {
        int i;
        Log.d(LOGTAG,"getTakeGuestMeasurementWithInitialsBytesData-->initial : " + initial + ", birthDate : " + Arrays.toString(birthDate) + ", height : " + height + ", gender : " + gender + ", activityIndex : " + activityIndex + ", unit : " + unit);
        byte[] data = new byte[ 11 ];
        byte rawGender = (byte) (((byte) (gender == 'M' ? 128 : 0)) + activityIndex);
        char[] rawInitial = initial.toCharArray();
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 74;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 74;
        }
        for (i = 0; i < 3; i++)
        {
            data[ i + 2 ] = (byte) rawInitial[ i ];
        }
        birthDate[ 0 ] = birthDate[ 0 ] - 1900;
        for (i = 0; i < 3; i++)
        {
            data[ i + 5 ] = (byte) birthDate[ i ];
        }
        data[ 8 ] = (byte) height;
        data[ 9 ] = rawGender;
        data[ 10 ] = (byte) unit;
        return data;
    }

    public byte[] getSetDateTime()
    {
        Log.d(LOGTAG,"getSetDateTime");

        byte[] data = new byte[ 5 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -23;
        }
        else
        {
            data[ 0 ] = (byte) -7;
        }

        byte[] rawTimeStamp = convertIntToBytes(getTimeStampInSeconds(new Date().getTime()));

        for (int i = 0; i < 4; i++)
        {
            data[ i + 1 ] = rawTimeStamp[ i ];
        }

        return data;
    }

    public byte[] getAssignMeasurementToUser(long uuid, int timeStamp, float weight, int impedance, int measurementId)
    {
        byte[] rawUuid;
        int i;
        Log.d(LOGTAG,"getAssignMeasurementToUserBytesData-->uuid : " + uuid + ", timeStamp : " + timeStamp + ", weight : " + weight + ", impedance : " + impedance + ", measurementId : " + measurementId);
        byte[] data = new byte[ 19 ];

        if (!isCompatibleDevice())
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 75;
            rawUuid = convertLongToBytes(uuid);
            for (i = 0; i < 8; i++)
            {
                data[ i + 2 ] = rawUuid[ i ];
            }
            for (i = 0; i < 4; i++)
            {
                data[ i + 10 ] = (byte) (timeStamp >> ((3 - i) * 8));
            }
            weight *= 20.0f;
            data[ 14 ] = (byte) ((int) (weight / 256.0f));
            data[ 15 ] = (byte) ((int) weight);
            data[ 16 ] = (byte) (impedance / 256);
            data[ 17 ] = (byte) impedance;
            data[ 18 ] = (byte) measurementId;
            return data;
        }

        data[ 0 ] = (byte) -25;
        data[ 1 ] = (byte) 75;
        rawUuid = convertLongToBytes(uuid);
        for (i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        for (i = 0; i < 4; i++)
        {
            data[ i + 10 ] = (byte) (timeStamp >> ((3 - i) * 8));
        }
        weight *= 20.0f;
        data[ 14 ] = (byte) ((int) (weight / 256.0f));
        data[ 15 ] = (byte) ((int) weight);
        data[ 16 ] = (byte) (impedance / 256);
        data[ 17 ] = (byte) impedance;
        data[ 18 ] = (byte) measurementId;
        return data;
    }

    public byte[] getSetUnit(int unit)
    {
        Log.d(LOGTAG,"getSetUnitBytesData-->unit : " + unit);
        byte[] data = new byte[ 3 ];
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 77;
            data[ 2 ] = (byte) unit;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 77;
            data[ 2 ] = (byte) unit;
        }
        return data;
    }

    public byte[] getSetReferDefinitionToWeightThreshold(float weightThreshold, float bodyFatThreshold)
    {
        Log.d(LOGTAG,"getSetReferDefinitionToWeightThresholdBytesData-->weightThreshold : " + weightThreshold + ", bodyFatThreshold : " + bodyFatThreshold);
        byte[] data = new byte[ 4 ];
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
        }
        else
        {
            data[ 0 ] = (byte) -9;
        }
        data[ 1 ] = (byte) 78;
        data[ 2 ] = (byte) ((int) (weightThreshold * 10.0f));
        data[ 3 ] = (byte) ((int) (bodyFatThreshold * 10.0f));
        return data;
    }

    public byte[] getChangeUserFromOldUUID(long oldUuid, long newUuid)
    {
        byte[] rawOldUuid;
        int i;
        byte[] rawNewUuid;
        Log.d(LOGTAG,"getChangeUserFromOldUUID-->oldUuid : " + oldUuid + ", newUuid : " + newUuid);
        byte[] data = new byte[ 18 ];

        if (! isCompatibleDevice())
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 86;
            rawOldUuid = convertLongToBytes(oldUuid);
            for (i = 0; i < 8; i++)
            {
                data[ i + 2 ] = rawOldUuid[ i ];
            }
            rawNewUuid = convertLongToBytes(newUuid);
            for (i = 0; i < 8; i++)
            {
                data[ i + 10 ] = rawNewUuid[ i ];
            }
            return data;
        }

        data[ 0 ] = (byte) -25;
        data[ 1 ] = (byte) 86;
        rawOldUuid = convertLongToBytes(oldUuid);
        for (i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawOldUuid[ i ];
        }
        rawNewUuid = convertLongToBytes(newUuid);
        for (i = 0; i < 8; i++)
        {
            data[ i + 10 ] = rawNewUuid[ i ];
        }
        return data;
    }

    public byte[] getUpdateUserBytesDate(long uuid, String initial, int[] birthDate, int height, char gender, int activityIndex)
    {
        int i;
        Log.d(LOGTAG,"getUpdateUserBytesDate-->uuid : " + uuid + ", initial : " + initial + ", birthDate : " + Arrays.toString(birthDate) + ", height : " + height + ", gender : " + gender + ", activityIndex : " + activityIndex);
        byte[] data = new byte[ 18 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        byte rawGender = (byte) (((byte) (gender == 'M' ? 128 : 0)) + activityIndex);
        char[] rawInitial = initial.toCharArray();
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
        }
        else
        {
            data[ 0 ] = (byte) -9;
        }
        data[ 1 ] = (byte) 53;
        for (i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        for (i = 0; i < rawInitial.length; i++)
        {
            data[ i + 10 ] = (byte) rawInitial[ i ];
        }
        birthDate[ 0 ] = birthDate[ 0 ] - 1900;
        for (i = 0; i < 3; i++)
        {
            data[ i + 13 ] = (byte) birthDate[ i ];
        }
        data[ 16 ] = (byte) height;
        data[ 17 ] = rawGender;
        return data;
    }

    public byte[] getScaleSleepStatus()
    {
        Log.d(LOGTAG, "getScaleSleepStatus");

        byte[] data = new byte[ 2 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -16;
        }
        else
        {
            data[ 0 ] = (byte) -32;
        }

        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getUserInfo(long uuid)
    {
        Log.d(LOGTAG,"getUserInfoBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
        }
        else
        {
            data[ 0 ] = (byte) -9;
        }

        data[ 1 ] = (byte) 54;

        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = rawUuid[ i ];
        }
        return data;
    }

    public byte[] getRemoteTimeStamp()
    {
        Log.d(LOGTAG,"getRemoteTimeStamp");
        byte[] data = new byte[ 2 ];
        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -14;
        }
        else
        {
            data[ 0 ] = (byte) -30;
        }
        data[ 1 ] = (byte) 2;
        return data;
    }

    public byte[] getModuleVersion()
    {
        Log.d(LOGTAG,"getModuleVersion");

        byte[] data = new byte[ 2 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -15;
        }
        else
        {
            data[ 0 ] = (byte) -31;
        }

        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getForceDisconnect()
    {
        Log.d(LOGTAG,"getForceDisconnect");

        byte[] data = new byte[ 2 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -22;
        }
        else
        {
            data[ 0 ] = (byte) -6;
        }

        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getAcknowledgementData(byte[] resp)
    {
        byte[] data = new byte[ 5 ];

        if (isCompatibleDevice())
        {
            data[ 0 ] = (byte) -25;
        }
        else
        {
            data[ 0 ] = (byte) -9;
        }

        data[ 1 ] = (byte) -15;
        data[ 2 ] = resp[ 1 ];
        data[ 3 ] = resp[ 2 ];
        data[ 4 ] = resp[ 3 ];

        return data;
    }

    //region Conversion helper

    public byte[] convertLongToBytes(long value)
    {
        byte[] data = new byte[ 8 ];
        for (int i = 0; i < 8; i++)
        {
            data[ i ] = (byte) ((int) (value >> ((7 - i) * 8)));
        }
        return data;
    }

    public byte[] convertIntToBytes(int value)
    {
        byte[] data = new byte[ 8 ];
        for (int i = 0; i < 4; i++)
        {
            data[ i ] = (byte) (value >> ((3 - i) * 8));
        }
        return data;
    }

    public static int convertBytesToInt(byte[] data)
    {
        if (data.length != 4)
        {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(data);
        buffer.flip();
        return buffer.getInt();
    }

    public static int convertByteToInt(byte data)
    {
        return data & 0xff;
    }

    public static long getTimeStampInMilliSeconds(int timeStampInSeconds)
    {
        return ((long) timeStampInSeconds) * 1000;
    }

    public static int getTimeStampInSeconds(long timeStampInMilliSeconds)
    {
        return (int) (timeStampInMilliSeconds / 1000);
    }

    //endregion Conversion helper
}
