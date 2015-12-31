package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGatt;
import android.util.Log;

import java.nio.ByteBuffer;
import java.security.acl.LastOwnerException;
import java.util.Arrays;
import java.util.Date;

@SuppressWarnings({"UnusedAssignment", "unused"})
public class BlueToothScale
{
    private static final String LOGTAG = BlueToothScale.class.getSimpleName();

    private static class Scales
    {
        public static final String SBF70 = "SBF70";
        public static final String BF710 = "Beurer BF710";
        public static final String GS485 = "Beurer GS485";
        public static final String SANITAS_SBF70 = "SANITAS SBF70";
    }

    private static class Sensors
    {
        public static final String SAS75 = "SAS75";
    }

    private static class BPMs
    {
        public static final String BM75 = "BM75";
        public static final String SBM37 = "SBM37";
        public static final String SBM67 = "SBM67";
        public static final String BEURER_BC57 = "BC57";
        public static final String SANITAS_SBM37 = "Sanitas SBM37";
        public static final String SANITAS_SBM67 = "BPM Smart";
    }

    public static boolean isCompatibleScale(String devicename)
    {
        return (devicename.equalsIgnoreCase(Scales.SBF70) ||
                devicename.equalsIgnoreCase(Scales.BF710) ||
                devicename.equalsIgnoreCase(Scales.GS485) ||
                devicename.equalsIgnoreCase(Scales.SANITAS_SBF70));
    }

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

    public static long convertBytesToLong(byte[] data)
    {
        if (data.length != 8)
        {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(data);
        buffer.flip();
        return buffer.getLong();
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
        //return Integer.parseInt(String.format("%X", new Object[]{Byte.valueOf(data)}), 16);

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

    public BlueToothScale(String model)
    {
        this.model = model;
    }

    private String model;
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

        if (rd[ 0 ] == -32)
        {
            return false;
        }

        if (rd[ 0 ] == -26)
        {
            Log.d(LOGTAG, "Device Ready");

            return false;
        }

        if (rd[ 0 ] == -24)
        {
            if (rd[ 1 ] == -2 && rd[ 2 ] == -69 && rd[ 3 ] == -17)
            {
                Log.d(LOGTAG, "Pairing Completed Successfully");

                return false;
            }

            Log.d(LOGTAG, "Pairing Completed Unsuccessfully");

            return false;
        }

        if (rd[ 0 ] == -16)
        {
            boolean status = false;

            if (rd[ 1 ] != 0)
            {
                status = true;
            }

            // this.mBleScaleCallbacks.didGetScaleSleepWithStatus(status);

            return false;
        }

        if (rd[ 0 ] == -15)
        {
            Log.d(LOGTAG, "parseData: ModuleVersion=" + rd[ 1 ]);

            // this.mBleScaleCallbacks.didGetModuleVersion(convertByteToInt(recievedData[ 1 ]));

            return false;
        }

        if (rd[ 0 ] == -14)
        {
            //timeStamp = getTimeStampInMilliSeconds(convertBytesToInt(Arrays.copyOfRange(recievedData, 1, 5)));

            //this.mBleScaleCallbacks.didGetRemoteTimeStamp(timeStamp);

            return false;
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
                    if (rd[ 2 ] == 51)
                    {
                        //this.mMaximumUsers = convertByteToInt(recievedData[ 5 ]);

                        if (rd[ 4 ] == 0)
                        {
                            //this.mBleScaleCallbacks.didGetUUIDsListOfUsers(null, null, 0, this.mMaximumUsers);
                        }

                        return false;
                    }

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

                if (rd[ 2 ] == 69 && rd.length >= 5)
                {
                    status2 = convertByteToInt(rd[ 3 ]);
                    long timeStamp = getTimeStampInMilliSeconds(convertBytesToInt(Arrays.copyOfRange(rd, 4, 8)));
                    byte[] data = new byte[ 4 ];
                    data[ 2 ] = rd[ 8 ];
                    data[ 3 ] = rd[ 9 ];
                    weight = ((double) convertBytesToInt(data)) / 20.0d;
                    float bodyFat = (((float) (((double) convertByteToInt(rd[ 10 ])) * 256.0d)) + ((float) convertByteToInt(rd[ 11 ]))) / 10.0f;

                    //this.mBleScaleCallbacks.didGetUserWithStatus(status2, timeStamp, weight, bodyFat);

                    return false;
                }

                if (rd[ 2 ] == 69 && rd.length < 5)
                {
                    status2 = convertByteToInt(rd[ 3 ]);

                    //this.mBleScaleCallbacks.didGetUserWithStatus(status2, 0, 0.0d, 0.0f);

                    return false;
                }

                if (rd[ 2 ] == 79 && rd.length >= 5)
                {
                    float weightThreshold = ((float) convertByteToInt(rd[ 5 ])) / 10.0f;
                    float bodyFatThreshold = ((float) convertByteToInt(rd[ 6 ])) / 10.0f;
                    float batteryLevel = (float) convertByteToInt(rd[ 4 ]);
                    int scaleVersion = convertByteToInt(rd[ 11 ]);
                    int unit = convertByteToInt(rd[ 7 ]);
                    boolean isUserExists = false;
                    boolean isUserReferWeightExists = false;
                    boolean isUserMeasurementExists = false;

                    if (rd[ 8 ] == 0)
                    {
                        isUserExists = true;
                    }
                    if (rd[ 9 ] == 0)
                    {
                        isUserReferWeightExists = true;
                    }
                    if (rd[ 10 ] == 0)
                    {
                        isUserMeasurementExists = true;
                    }

                    // this.mBleScaleCallbacks.didGetScaleStatusWithBatteryLevel(batteryLevel, weightThreshold, bodyFatThreshold, unit, isUserExists, isUserReferWeightExists, isUserMeasurementExists, scaleVersion);

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

                return false;
            }

            if (rd[ 1 ] == 66)
            {
                int messagePart = convertByteToInt(rd[ 3 ]) % 2;
                int measurementIndex;
                if (messagePart == 1)
                {
                    Log.d(LOGTAG, "First part message");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Measurement data length : " + rd.length);
                    stringBuilder.append("Data : ");
                    for (x = 0; x < rd.length; x++)
                    {
                        StringBuilder stringBuilder2 = stringBuilder;
                        stringBuilder2.append(String.format("%X, ", new Object[]{Byte.valueOf(rd[ x ])}));
                    }

                    measurementIndex = convertByteToInt(rd[ 3 ]) / 2;
                    this.rawTimeStampArray[ measurementIndex ] = convertBytesToInt(Arrays.copyOfRange(rd, 4, 8));
                    byte[] data = new byte[ 4 ];
                    this.rawWeightArray[ measurementIndex ] = ((double) convertBytesToInt(data)) / 20.0d;
                    this.rawImpedanceArray[ measurementIndex ] = (convertByteToInt(rd[ 10 ]) * 256) + convertByteToInt(rd[ 11 ]);
                    this.rawBodyFatArray[ measurementIndex ] = (float) (((double) ((convertByteToInt(rd[ 12 ]) * 256) + convertByteToInt(rd[ 13 ]))) / 10.0d);
                    this.waterByte = rd[ 14 ];

                    //this.mBluetoothLeApi.send_acknowledgement(rd);
                }

                if (messagePart == 0)
                {
                    Log.d("TTT", "Second part message");
                    measurementIndex = (convertByteToInt(rd[ 3 ]) / 2) - 1;
                    this.rawWaterArray[ measurementIndex ] = (float) (((double) ((this.waterByte * 256) + convertByteToInt(rd[ 4 ]))) / 10.0d);
                    this.rawMuscleArray[ measurementIndex ] = (float) (((double) ((convertByteToInt(rd[ 5 ]) * 256) + convertByteToInt(rd[ 6 ]))) / 10.0d);
                    this.rawBoneMassArray[ measurementIndex ] = (float) (((double) ((convertByteToInt(rd[ 7 ]) * 256) + convertByteToInt(rd[ 8 ]))) / 20.0d);
                    this.rawBmrArray[ measurementIndex ] = (convertByteToInt(rd[ 9 ]) * 256) + convertByteToInt(rd[ 10 ]);
                    this.rawAmrArray[ measurementIndex ] = (convertByteToInt(rd[ 11 ]) * 256) + convertByteToInt(rd[ 12 ]);
                    this.rawBmiArray[ measurementIndex ] = (float) (((double) ((convertByteToInt(rd[ 13 ]) * 256) + convertByteToInt(rd[ 14 ]))) / 10.0d);

                    //this.mBluetoothLeApi.send_acknowledgement(rd);

                    if (rd[ 2 ] == rd[ 3 ])
                    {
                        Log.d("TTT", "Last Packet->count->" + convertByteToInt(rd[ 2 ]));
                        int totalMeasurementCount = convertByteToInt(rd[ 2 ]) / 2;
                        float[] bmiArray = new float[ totalMeasurementCount ];
                        float[] bodyFatArray = new float[ totalMeasurementCount ];
                        float[] waterArray = new float[ totalMeasurementCount ];
                        float[] muscleArray = new float[ totalMeasurementCount ];
                        float[] boneMassArray = new float[ totalMeasurementCount ];
                        int[] bmrArray = new int[ totalMeasurementCount ];
                        int[] amrArray = new int[ totalMeasurementCount ];
                        int[] impedanceArray = new int[ totalMeasurementCount ];
                        long[] timeStampArray = new long[ totalMeasurementCount ];
                        double[] weightArray = new double[ totalMeasurementCount ];

                        for (int i = 0; i < totalMeasurementCount; i++)
                        {
                            timeStampArray[ i ] = getTimeStampInMilliSeconds(this.rawTimeStampArray[ i ]);
                            weightArray[ i ] = this.rawWeightArray[ i ];
                            impedanceArray[ i ] = this.rawImpedanceArray[ i ];
                            bmiArray[ i ] = this.rawBmiArray[ i ];
                            bodyFatArray[ i ] = this.rawBodyFatArray[ i ];
                            waterArray[ i ] = this.rawWaterArray[ i ];
                            muscleArray[ i ] = this.rawMuscleArray[ i ];
                            boneMassArray[ i ] = this.rawBoneMassArray[ i ];
                            bmrArray[ i ] = this.rawBmrArray[ i ];
                            amrArray[ i ] = this.rawAmrArray[ i ];
                        }

                        // this.mBleScaleCallbacks.didGetUserMeasurementsTimeStamps(timeStampArray, weightArray, impedanceArray, bmiArray, bodyFatArray, waterArray, muscleArray, boneMassArray, bmrArray, amrArray, totalMeasurementCount);
                    }
                }

                return false;
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

            if (rd[ 1 ] == 89)
            {
                if (rd[ 3 ] == 1)
                {
                    byte[] rawUuid = new byte[ 8 ];

                    for (int i = 0; i < 8; i++)
                    {
                        rawUuid[ i ] = rd[ i + 5 ];
                    }

                    this.rawListOfUuids[ 0 ] = convertBytesToLong(rawUuid);

                    this.didFinishMeasurementStatus = convertByteToInt(rd[ 4 ]);

                    Log.d(LOGTAG, "parseData: Finish(1): UUID=" + this.rawListOfUuids[ 0 ] + " Status=" + this.didFinishMeasurementStatus);
                }

                if (rd[ 3 ] == 2)
                {
                    this.rawTimeStampArray[ 0 ] = convertBytesToInt(Arrays.copyOfRange(rd, 4, 8));
                    byte[] data = new byte[ 4 ];
                    this.rawWeightArray[ 0 ] = ((double) convertBytesToInt(data)) / 20.0d;
                    this.rawImpedanceArray[ 0 ] = (convertByteToInt(rd[ 10 ]) * 256) + convertByteToInt(rd[ 11 ]);
                    this.rawBodyFatArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 12 ]) * 256) + convertByteToInt(rd[ 13 ]))) / 10.0d);
                    this.waterByte = rd[ 14 ];

                    Log.d(LOGTAG, "parseData: Finish(2): TS=" + this.rawTimeStampArray[ 0 ]);
                }

                if (rd[ 3 ] == 3)
                {
                    this.rawWaterArray[ 0 ] = (float) (((double) ((convertByteToInt(this.waterByte) * 256) + convertByteToInt(rd[ 4 ]))) / 10.0d);
                    this.rawMuscleArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 5 ]) * 256) + convertByteToInt(rd[ 6 ]))) / 10.0d);
                    this.rawBoneMassArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 7 ]) * 256) + convertByteToInt(rd[ 8 ]))) / 20.0d);
                    this.rawBmrArray[ 0 ] = (convertByteToInt(rd[ 9 ]) * 256) + convertByteToInt(rd[ 10 ]);
                    this.rawAmrArray[ 0 ] = (convertByteToInt(rd[ 11 ]) * 256) + convertByteToInt(rd[ 12 ]);
                    this.rawBmiArray[ 0 ] = (float) (((double) ((convertByteToInt(rd[ 13 ]) * 256) + convertByteToInt(rd[ 14 ]))) / 10.0d);

                    Log.d(LOGTAG, "parseData: Finish(3): BMI=" + this.rawBmiArray[ 0 ]);

                    long timeStamp = getTimeStampInMilliSeconds(this.rawTimeStampArray[ 0 ]);
                    //this.mBleScaleCallbacks.didFinishMeasurementOnTimestamp(timeStamp, this.rawWeightArray[ 0 ], this.rawImpedanceArray[ 0 ], this.rawBmiArray[ 0 ], this.rawBodyFatArray[ 0 ], this.rawWaterArray[ 0 ], this.rawMuscleArray[ 0 ], this.rawBoneMassArray[ 0 ], this.rawBmrArray[ 0 ], this.rawAmrArray[ 0 ], this.rawListOfUuids[ 0 ], this.didFinishMeasurementStatus);
                }

                return true;
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

            if (rd[ 1 ] == 88)
            {
                boolean status = (rd[ 2 ] != 1);

                byte[] data = new byte[ 4 ];
                data[ 2 ] = rd[ 3 ];
                data[ 3 ] = rd[ 4 ];
                weight = ((double) convertBytesToInt(data)) / 20.0d;

                Log.d(LOGTAG, "parseData: Weight=" + weight + " Status=" + status);

                //this.mBleScaleCallbacks.didGetLiveWeight(weight, status);

                return true;
            }
        }

        return false;
    }

    public byte[] getUserListBytesData(String scaleName)
    {
        Log.d(LOGTAG,"getUserListBytesData");
        byte[] data = new byte[ 2 ];
        if (isCompatibleScale(model))
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 51;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 51;
        }
        return data;
    }

    public byte[] getScaleStatusForUserBytesData(long uuid)
    {
        Log.d(LOGTAG,"getScaleStatusForUserBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        if (isCompatibleScale(model))
        {
            data[ 0 ] = (byte) -25;
            data[ 1 ] = (byte) 79;
        }
        else
        {
            data[ 0 ] = (byte) -9;
            data[ 1 ] = (byte) 79;
        }
        byte[] uuidInBytes = convertLongToBytes(uuid);
        for (int i = 0; i < 8; i++)
        {
            data[ i + 2 ] = uuidInBytes[ i ];
        }
        return data;
    }

    public byte[] getTxPowerBytesData()
    {
        Log.d(LOGTAG,"getTxPowerBytesData");

        byte[] data = new byte[ 2 ];

        if (isCompatibleScale(model))
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

    public byte[] getSlowAdvertisementBytesData(boolean isSet, int interval)
    {
        Log.d(LOGTAG,"getSlowAdvertisementBytesData-->isSet : " + isSet + ", interval : " + interval);
        byte[] data = new byte[ 2 ];
        if (isSet)
        {
            if (isCompatibleScale(model))
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
            if (isCompatibleScale(model))
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

    public byte[] getCheckUserExistsBytesData(long uuid)
    {
        Log.d(LOGTAG,"getCheckUserExistsBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        if (isCompatibleScale(model))
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

    public byte[] getCreateUserBytesData(long uuid, String initial, int[] birthDate, int height, char gender, int activityIndex)
    {
        Log.d(LOGTAG,"getCreateUserBytesData:"
                + " uuid=" + uuid
                + " initial=" + initial
                + " birthDate=" + Arrays.toString(birthDate)
                + " height=" + height
                + " gender=" + gender
                + " activityIndex=" + activityIndex);

        byte[] data = new byte[ 18 ];

        if (isCompatibleScale(model))
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

    public byte[] getDeleteUserBytesData(long uuid)
    {
        Log.d(LOGTAG,"getDeleteUserBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleScale(model))
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

    public byte[] getTakeUserMeasurementBytesData(long uuid)
    {
        Log.d(LOGTAG,"getTakeUserMeasurementBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleScale(model))
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

    public byte[] getUserMeasurementsBytesData(long uuid)
    {
        Log.d(LOGTAG,"getUserMeasurementsBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleScale(model))
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

    public byte[] getDeleteUserMeasurementsBytesData(long uuid)
    {
        Log.d(LOGTAG,"getDeleteUserMeasurementsBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleScale(model))
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

    public byte[] getSetUserWeightBytesData(long uuid, float weight, float bodyFat, long timeStamp)
    {
        int i;
        Log.d(LOGTAG,"getSetUserWeightBytesData-->uuid : " + uuid + ", weight : " + weight + ", bodyFat : " + bodyFat + ", timeStamp : " + timeStamp);
        byte[] data = new byte[ 18 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleScale(model))
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

    public byte[] getUserWeightAndBodyFatBytesData(long uuid)
    {
        Log.d(LOGTAG,"getUserWeightAndBodyFatBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleScale(model))
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

    public byte[] getUnknownMeasurementsBytesData(String scaleName)
    {
        Log.d(LOGTAG, "getUnknownMeasurementsBytesData");
        byte[] data = new byte[ 2 ];
        if (isCompatibleScale(model))
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

    public byte[] getSaveAsUnknownMeasurementWithtimestampBytesData(int timeStamp, float weight, int impedance)
    {
        Log.d(LOGTAG,"getSaveAsUnknownMeasurementWithtimestampBytesData-->timeStamp : " + timeStamp + ", weight : " + weight + ", impedance : " + impedance);
        byte[] data = new byte[ 10 ];
        if (isCompatibleScale(model))
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

    public byte[] getDeleteUnknownMeasurementBytesData(int measurementId)
    {
        Log.d(LOGTAG,"getDeleteUnknownMeasurementBytesData-->measurementId : " + measurementId);
        byte[] data = new byte[ 3 ];
        if (isCompatibleScale(model))
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

    public byte[] getTakeGuestMeasurementWithInitialsBytesData(String initial, int[] birthDate, int height, char gender, int activityIndex, int unit)
    {
        int i;
        Log.d(LOGTAG,"getTakeGuestMeasurementWithInitialsBytesData-->initial : " + initial + ", birthDate : " + Arrays.toString(birthDate) + ", height : " + height + ", gender : " + gender + ", activityIndex : " + activityIndex + ", unit : " + unit);
        byte[] data = new byte[ 11 ];
        byte rawGender = (byte) (((byte) (gender == 'M' ? 128 : 0)) + activityIndex);
        char[] rawInitial = initial.toCharArray();
        if (isCompatibleScale(model))
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

    public byte[] getSetDateTimeBytesData()
    {
        Log.d(LOGTAG,"getSetDateTimeBytesData");

        byte[] data = new byte[ 5 ];

        if (isCompatibleScale(model))
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

    public byte[] getAssignMeasurementToUserBytesData(long uuid, int timeStamp, float weight, int impedance, int measurementId)
    {
        byte[] rawUuid;
        int i;
        Log.d(LOGTAG,"getAssignMeasurementToUserBytesData-->uuid : " + uuid + ", timeStamp : " + timeStamp + ", weight : " + weight + ", impedance : " + impedance + ", measurementId : " + measurementId);
        byte[] data = new byte[ 19 ];

        if (!isCompatibleScale(model))
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

    public byte[] getSetUnitBytesData(int unit)
    {
        Log.d(LOGTAG,"getSetUnitBytesData-->unit : " + unit);
        byte[] data = new byte[ 3 ];
        if (isCompatibleScale(model))
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

    public byte[] getSetReferDefinitionToWeightThresholdBytesData(float weightThreshold, float bodyFatThreshold)
    {
        Log.d(LOGTAG,"getSetReferDefinitionToWeightThresholdBytesData-->weightThreshold : " + weightThreshold + ", bodyFatThreshold : " + bodyFatThreshold);
        byte[] data = new byte[ 4 ];
        if (isCompatibleScale(model))
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

        if (! isCompatibleScale(model))
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
        if (isCompatibleScale(model))
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

    public byte[] getScaleSleepStatusBytesData(String scaleName)
    {
        Log.d(LOGTAG, "getScaleSleepStatusBytesData");
        byte[] data = new byte[ 2 ];
        if (isCompatibleScale(model))
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

    public byte[] getUserInfoBytesData(long uuid)
    {
        Log.d(LOGTAG,"getUserInfoBytesData-->uuid : " + uuid);
        byte[] data = new byte[ 10 ];
        byte[] rawUuid = convertLongToBytes(uuid);
        if (isCompatibleScale(model))
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

    public byte[] getRemoteTimeStampBytesData(String scaleName)
    {
        Log.d(LOGTAG,"getRemoteTimeStampBytesData");
        byte[] data = new byte[ 2 ];
        if (isCompatibleScale(model))
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

    public byte[] getModuleVersionBytesData()
    {
        Log.d(LOGTAG,"getModuleVersionBytesData");

        byte[] data = new byte[ 2 ];

        if (isCompatibleScale(model))
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

    public byte[] getForceDisconnectBytesData(String scaleName)
    {
        Log.d(LOGTAG,"getForceDisconnectBytesData");
        byte[] data = new byte[ 2 ];
        if (isCompatibleScale(model))
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

        if (isCompatibleScale(model))
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
}
