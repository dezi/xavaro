package de.xavaro.android.safehome;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

@SuppressWarnings({"unused"})
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

    private boolean isCompatibleDevice(String devicename)
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
    protected boolean isCompatibleSecondary(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    @Override
    protected boolean isCompatibleControl(BluetoothGattCharacteristic characteristic)
    {
        return false;
    }

    @Override
    protected void discoveredDevice()
    {
        //
        // Do nothing and check if scale is sleepwalking.
        //
    }

    @Override
    protected void enableDevice()
    {
        super.enableDevice();

        Log.d(LOGTAG, "enableDevice: " + deviceName);

        //
        // Issue a status request to figure out
        // if device connects by itself or user.
        // Issue a force disconnect if device is
        // sleeping.
        //

        gattSchedule.add(new GattAction(getScaleSleepStatus()));

        fireNext(true);
    }

    @Override
    protected void syncSequence()
    {
    }

    private Runnable disconnectScale = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "disconnectScale: force disconnect");

            currentGatt.disconnect();

            gattHandler.postDelayed(connectRunnable, 10000);
        }
    };

    private void reloadDisconnectTimer()
    {
        //
        // The scale does not disconnect by itself,
        // so we schedule a timer which disconnects
        // the device.
        //

        gattHandler.removeCallbacks(disconnectScale);
        gattHandler.postDelayed(disconnectScale, 60000);
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
    protected void parseResponse(byte[] rd, BluetoothGattCharacteristic characteristic)
    {
        cbdata = new JSONObject();

        boolean wantsack = parseData(rd);

        //
        // Satisfy multirecord acknowledgement requests.
        //

        if (wantsack)
        {
            Log.d(LOGTAG, "parseResponse: send ack");

            gattSchedule.add(0, new GattAction(getAcknowledgementData(rd)));

            fireNext(false);
        }

        //
        // Check if parse result needs to be delivered to callback.
        //

        if (cbdata.keys().hasNext())
        {
            //
            // Object is not empty. Deliver to callback.
            //

            if (dataCallback != null)
            {
                JSONObject data = new JSONObject();
                Json.put(data, "scale", cbdata);

                dataCallback.onBluetoothReceivedData(deviceName, data);
            }

            //
            // Store results if any.
            //

            if (Simple.equals(Json.getString(cbdata, "type"), "UserMeasurementsArray"))
            {
                if (cbdata.has("array"))
                {
                    JSONArray array = Json.getArray(cbdata, "array");

                    for (int inx = 0; inx < array.length(); inx++)
                    {
                        JSONObject oldrec = Json.getObject(array, inx);
                        JSONObject record = new JSONObject();

                        long timeStamp = Json.getInt(oldrec, "timeStamp") * 1000L;
                        String utciso = Simple.timeStampAsISO(timeStamp);

                        Json.put(record, "dts", utciso);
                        Json.put(record, "wei", Json.getInt(oldrec, "weight"));
                        Json.put(record, "imp", Json.getInt(oldrec, "impedance"));
                        Json.put(record, "fat", Json.getInt(oldrec, "bodyFat"));
                        Json.put(record, "wat", Json.getInt(oldrec, "water"));
                        Json.put(record, "mus", Json.getInt(oldrec, "muscle"));
                        Json.put(record, "bom", Json.getInt(oldrec, "boneMass"));
                        Json.put(record, "BMR", Json.getInt(oldrec, "BMR"));
                        Json.put(record, "AMR", Json.getInt(oldrec, "AMR"));
                        Json.put(record, "BMI", Json.getInt(oldrec, "BMI"));

                        HealthData.addRecord("scale", record);
                    }
                }
            }

            //
            // Execute next step in sync sequence.
            //

            nextSyncSequence();
        }

        reloadDisconnectTimer();
    }

    private void nextSyncSequence()
    {
        JSONObject nextCommand = new JSONObject();

        String type = Json.getString(cbdata, "type");

        if (Simple.equals(type, "DeviceReady"))
        {
            //
            // Scale is awake and live. Set and get scale time.
            //

            Json.put(nextCommand, "command", "getSetDateTime");
        }

        if (Simple.equals(type, "RemoteTimeStamp"))
        {
            //
            // Scale time has now been set proceed with
            // setting the scale to our user.
            //

            Json.put(nextCommand, "command", "getUserList");
        }

        if (Simple.equals(type, "UserList"))
        {
            if (cbdata.has("actUsers"))
            {
                int actUsers = Json.getInt(cbdata, "actUsers");

                if (actUsers == 0)
                {
                    Json.put(nextCommand, "command", "getCreateUserFromPreferences");
                }
            }
        }

        if (Simple.equals(type, "UserListArray"))
        {
            //
            // Scale time has now been set proceed with
            // setting the scale to our user.
            //

            SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;
            long uuid = Long.parseLong(sp.getString("health.scale.userid", ""));
            boolean found = false;

            if (cbdata.has("array"))
            {
                JSONArray array = Json.getArray(cbdata, "array");

                if (array != null)
                {
                    for (int inx = 0; inx < array.length(); inx++)
                    {
                        JSONObject user = Json.getObject(array, inx);
                        if (user == null) continue;

                        if (user.has("uuid") && (Json.getLong(user, "uuid") == uuid))
                        {
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (found)
            {
                Json.put(nextCommand, "command", "getUpdateUserFromPreferences");
            }
            else
            {
                Json.put(nextCommand, "command", "getCreateUserFromPreferences");
            }
        }

        if (Simple.equals(type, "UserCreated"))
        {
            Json.put(nextCommand, "command", "getTakeUserMeasurementFromPreferences");
        }

        if (Simple.equals(type, "UserUpdated"))
        {
            Json.put(nextCommand, "command", "getTakeUserMeasurementFromPreferences");
        }

        if (nextCommand.has("command")) sendCommand(nextCommand);
    }

    public boolean parseData(byte[] rd)
    {
        Log.d(LOGTAG, "parseData: " + rd[ 0 ] + " " + rd[ 1 ]);
        Log.d(LOGTAG, "parseData: " + Simple.getHexBytesToString(rd));

        //region Single byte main instructions

        if (rd[ 0 ] == -16)
        {
            //
            // Special response used to disconnect sleeping
            // scale. These types of scales are sleepwalking.
            // Means, they connect while sleeping w/o any user
            // action of the scale.
            //

            Log.d(LOGTAG, "parseData: ScaleSleepWithStatus");

            boolean sleep = (rd[ 1 ] != 0);

            if (sleep)
            {
                //
                // Sleepwalking force back to bed.
                //

                Log.d(LOGTAG, "parseData: ScaleSleepWithStatus is sleeping, ignore");

                callOnBluetoothFakeDisconnect();

                return false;
            }

            //
            // Initialize device to be ready.
            //

            if (connectCallback != null) connectCallback.onBluetoothConnect(deviceName);

            Log.d(LOGTAG, "parseData: ScaleSleepWithStatus is awake, make ready");

            gattSchedule.add(new GattAction(getDeviceReady()));

            fireNext(true);

            return false;
        }

        if (rd[ 0 ] == -14)
        {
            long timeStamp = getTimeStampInMilliSeconds(convertBytesToInt(Arrays.copyOfRange(rd, 1, 5)));

            cbdataPut("type", "RemoteTimeStamp");
            cbdataPut("timeStamp", timeStamp);
            cbdataPut("isodate", Simple.timeStampAsISO(timeStamp));

            return false;
        }

        if (rd[ 0 ] == -15)
        {
            cbdataPut("type", "ModuleVersion");
            cbdataPut("version", rd[ 1 ]);

            return false;
        }

        if (rd[ 0 ] == -24)
        {
            boolean status = (rd[ 1 ] == -2 && rd[ 2 ] == -69 && rd[ 3 ] == -17);

            cbdataPut("type", "PairingCompleted");
            cbdataPut("status", status);

            return false;
        }

        if (rd[ 0 ] == -26)
        {
            cbdataPut("type", "DeviceReady");
            cbdataPut("status", rd[ 1 ]);

            return false;
        }

        //endregion Single byte main instructions

        //region Single part data responses

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 54))
        {
            cbdataPut("type", "UserInfo");
            cbdataPut("status", rd[ 3 ]);

            if (rd.length > 5)
            {
                char[] initialchars = new char[ 3 ];
                initialchars[ 0 ] = (char) rd[ 4 ];
                initialchars[ 1 ] = (char) rd[ 5 ];
                initialchars[ 2 ] = (char) rd[ 6 ];

                int[] birthDate = new int[ 3 ];
                birthDate[ 0 ] = rd[ 7 ] + 1900;
                birthDate[ 1 ] = rd[ 8 ];
                birthDate[ 2 ] = rd[ 9 ];

                cbdataPut("initials", new String(initialchars));
                cbdataPut("birthDateYear", birthDate[ 0 ]);
                cbdataPut("birthDateMonth", birthDate[ 1 ]);
                cbdataPut("birthDateDay", birthDate[ 2 ]);
                cbdataPut("height", unsignedByteToInt(rd[ 10 ]));
                cbdataPut("gender", (unsignedByteToInt(rd[ 11 ]) >= 128) ? "M" : "F");
                cbdataPut("activityIndex", unsignedByteToInt(rd[ 11 ]) & 0x7f);
            }

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 49))
        {
            cbdataPut("type", "UserCreated");
            cbdataPut("status", rd[ 3 ]);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 50))
        {
            cbdataPut("type", "UserDeleted");
            cbdataPut("status", rd[ 3 ]);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 53))
        {
            cbdataPut("type", "UserUpdated");
            cbdataPut("status", rd[ 3 ]);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 64))
        {
            cbdataPut("type", "TakeUserMeasurement");
            cbdataPut("status", rd[ 3 ]);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 69))
        {
            cbdataPut("type", "UserWeightAndBodyFat");
            cbdataPut("status", rd[ 3 ]);

            if (rd.length > 5)
            {
                cbdataPut("timeStamp", convertBytesToInt(Arrays.copyOfRange(rd, 4, 8)));
                cbdataPut("weight", unsignedBytesToInt(rd[ 8 ], rd[ 9 ]) / 20.0d);
                cbdataPut("bodyFat", unsignedBytesToInt(rd[ 10 ], rd[ 11 ]) / 10.0d);
            }

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 79))
        {
            cbdataPut("type", "ScaleStatusForUser");
            cbdataPut("status", rd[ 3 ]);

            if (rd.length > 5)
            {
                cbdataPut("weightThreshold", ((float) unsignedByteToInt(rd[ 5 ])) / 10.0f);
                cbdataPut("bodyFatThreshold", ((float) unsignedByteToInt(rd[ 6 ])) / 10.0f);

                cbdataPut("batteryLevel", (float) unsignedByteToInt(rd[ 4 ]));

                cbdataPut("weightUnit", unsignedByteToInt(rd[ 7 ]));

                cbdataPut("userExists", (rd[ 8 ] == 0));
                cbdataPut("referWeightExists", (rd[ 9 ] == 0));
                cbdataPut("measurementExists", (rd[ 10 ] == 0));
                cbdataPut("scaleVersion", unsignedByteToInt(rd[ 11 ]));
            }

            return false;
        }

        //endregion Single part data responses

        //region Multi part data responses

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 51))
        {
            cbdataPut("type", "UserList");
            cbdataPut("status", rd[ 3 ]);

            if (rd.length > 5)
            {
                cbdataPut("actUsers", rd[ 4 ]);
                cbdataPut("maxUsers", rd[ 5 ]);
            }

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == 52))
        {
            int part = rd[ 3 ];

            if (part == 1)
            {
                //
                // Very first record.
                //

                cbtarr = new JSONArray();
            }

            cbtemp = new JSONObject();

            cbtempPut("uuid", convertBytesToLong(Arrays.copyOfRange(rd, 4, 12)));
            cbtempPut("initials", new String(Arrays.copyOfRange(rd, 12, 15)));

            cbtarr.put(cbtemp);
            cbtemp = null;

            if (part == rd[ 2 ])
            {
                //
                // Very last record.
                //

                cbdata = new JSONObject();
                cbdataPut("type", "UserListArray");
                cbdataPut("array", cbtarr);
                cbtarr = null;
            }

            return true;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 65))
        {
            cbdataPut("type", "UserMeasurements");
            cbdataPut("status", rd[ 3 ]);

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
                cbtempPut("weight", unsignedBytesToInt(rd[ 8 ], rd[ 9 ]) / 20.0d);
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
                cbdataPut("type", "UserMeasurementsArray");
                cbdataPut("array", cbtarr);
                cbtarr = null;
            }

            return true;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == -16) && (rd[ 2 ] == 70))
        {
            cbdataPut("type", "UnknownMeasurements");
            cbdataPut("status", rd[ 3 ]);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == 71))
        {
            int part = rd[ 3 ];

            if (part == 1)
            {
                //
                // Very first record.
                //

                cbtarr = new JSONArray();
            }

            cbtemp = new JSONObject();

            cbtempPut("mesurementId", unsignedByteToInt(rd[ 4 ]));
            cbtempPut("timeStamp", convertBytesToInt(Arrays.copyOfRange(rd, 5, 9)));
            cbtempPut("weight", unsignedBytesToInt(rd[ 9 ], rd[ 10 ]) / 20.0d);
            cbtempPut("impedance", unsignedBytesToInt(rd[ 11 ], rd[ 12 ]) / 20.0d);

            cbtarr.put(cbtemp);
            cbtemp = null;

            if (part == rd[ 2 ])
            {
                //
                // Very last record.
                //

                cbdata = new JSONObject();
                cbdataPut("type", "UnknownMeasurementsArray");
                cbdataPut("array", cbtarr);
                cbtarr = null;
            }

            return true;
        }

        //endregion Multi part data responses

        //region Live measurement

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == 88))
        {
            cbdataPut("type", "LiveWeight");

            cbdataPut("status", (rd[ 2 ] != 1));
            cbdataPut("weight", unsignedBytesToInt(rd[ 3 ], rd[ 4 ]) / 20.0d);

            return false;
        }

        if ((rd[ 0 ] == -25) && (rd[ 1 ] == 89))
        {
            if (rd[ 3 ] == 1)
            {
                cbtemp = new JSONObject();
                cbtempPut("type", "LiveMeasurementOnTimestamp");

                byte[] rawUuid = new byte[ 8 ];
                System.arraycopy(rd, 5, rawUuid, 0, 8);

                cbtempPut("uuid", convertBytesToLong(rawUuid));
                cbtempPut("finished", unsignedByteToInt(rd[ 4 ]));

                Log.d(LOGTAG, "parseData: LiveMeasurementOnTimestamp part 1");
            }

            if (rd[ 3 ] == 2)
            {
                cbtempPut("timeStamp", convertBytesToInt(Arrays.copyOfRange(rd, 4, 8)));
                cbtempPut("weight", unsignedBytesToInt(rd[ 8 ], rd[ 9 ]) / 20.0d);
                cbtempPut("impedance", unsignedBytesToInt(rd[ 10 ], rd[ 11 ]));
                cbtempPut("bodyFat", unsignedBytesToInt(rd[ 12 ], rd[ 13 ]) / 10.0d);

                //
                // Parts split in the middle of data, nice.
                //

                cbbyte = rd[ 14 ];

                Log.d(LOGTAG, "parseData: LiveMeasurementOnTimestamp part 2");
            }

            if (rd[ 3 ] == 3)
            {
                cbtempPut("water", unsignedBytesToInt(cbbyte, rd[ 4 ]) / 10.0d);
                cbtempPut("muscle", unsignedBytesToInt(rd[ 5 ], rd[ 6 ]) / 10.0d);
                cbtempPut("boneMass", unsignedBytesToInt(rd[ 7 ], rd[ 8 ]) / 20.0d);
                cbtempPut("BMR", unsignedBytesToInt(rd[ 9 ], rd[ 10 ]));
                cbtempPut("AMR", unsignedBytesToInt(rd[ 11 ], rd[ 12 ]));
                cbtempPut("BMI", unsignedBytesToInt(rd[ 13 ], rd[ 14 ]) / 10.0d);

                Log.d(LOGTAG, "parseData: LiveMeasurementOnTimestamp part 3");

                cbdata = cbtemp;
                cbtemp = null;
            }

            return true;
        }

        //endregion Live measurement

        cbdataPut("type", "UnspecificResponse");

        if (rd.length > 0) cbdataPut("rd0", rd[ 0 ]);
        if (rd.length > 1) cbdataPut("rd1", rd[ 1 ]);
        if (rd.length > 2) cbdataPut("rd2", rd[ 2 ]);
        if (rd.length > 3) cbdataPut("rd3", rd[ 3 ]);

        return false;
    }

    @Override
    public void sendCommand(JSONObject command)
    {
        try
        {
            String what = command.getString("command");

            if (what.equals("getSetDateTime"))
            {
                //
                // Issue resultles getSetDateTime plus resultful
                // getRemoteTimeStamp commands.
                //

                gattSchedule.add(new GattAction(getSetDateTime()));
                gattSchedule.add(new GattAction(getRemoteTimeStamp()));
            }

            if (what.equals("getDeleteUser"))
            {
                gattSchedule.add(new GattAction(getDeleteUser(18193)));
            }

            if (what.equals("getUserMeasurements"))
            {
                gattSchedule.add(new GattAction(getUserMeasurementsFromPreferences()));
            }

            if (what.equals("getUserList"))
            {
                gattSchedule.add(new GattAction(getUserList()));
            }

            if (what.equals("getCreateUserFromPreferences"))
            {
                gattSchedule.add(new GattAction(getCreateUserFromPreferences()));
            }

            if (what.equals("getUpdateUserFromPreferences"))
            {
                gattSchedule.add(new GattAction(getUpdateUserFromPreferences()));
            }

            if (what.equals("getTakeUserMeasurementFromPreferences"))
            {
                gattSchedule.add(new GattAction(getTakeUserMeasurementFromPreferences()));
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private boolean isCompatibleDevice()
    {
        return isCompatibleDevice(this.deviceName);
    }

    //region Command builders

    public byte[] getDeviceReady()
    {
        Log.d(LOGTAG, "getDeviceReady");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -26 : -10);
        data[ 1 ] = (byte) 1;

        return data;
    }

    public byte[] getScaleStatusForUser(long uuid)
    {
        Log.d(LOGTAG, "getScaleStatusForUser-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 79;

        byte[] uuidInBytes = convertLongToBytes(uuid);
        System.arraycopy(uuidInBytes, 0, data, 2, 8);
        return data;
    }

    public byte[] getTxPower()
    {
        Log.d(LOGTAG, "getTxPower");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -20 : -4);
        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getCheckUserExists(long uuid)
    {
        Log.d(LOGTAG, "getCheckUserExists-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 0;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        return data;
    }

    public byte[] getCreateUserFromPreferences()
    {
        return getMakeUserFromPreferences(49);
    }

    public byte[] getUpdateUserFromPreferences()
    {
        return getMakeUserFromPreferences(53);
    }

    public byte[] getCreateUser(long uuid, String initial, int[] birthDate, int height, char gender, int activityIndex)
    {
        return getMakeUser(49, uuid, initial, birthDate, height, gender, activityIndex);
    }

    public byte[] getUpdateUser(long uuid, String initial, int[] birthDate, int height, char gender, int activityIndex)
    {
        return getMakeUser(53, uuid, initial, birthDate, height, gender, activityIndex);
    }

    public byte[] getMakeUserFromPreferences(int command)
    {
        SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;
        long uuid = Integer.parseInt(sp.getString("health.scale.userid", ""));
        String initials = sp.getString("health.scale.initials", "");
        int height = sp.getInt("health.user.size", 0);
        char gender = sp.getString("health.user.gender", "").equals("male") ? 'M' : 'F';
        int activityIndex = Integer.parseInt(sp.getString("health.user.activity", "0"));

        String birthString = sp.getString("health.user.birthdate", "");
        String[] birthParts = birthString.split("\\.");
        int[] birthDate = new int[ 3 ];

        if (birthParts.length == 3)
        {
            birthDate[ 0 ] = Integer.parseInt(birthParts[ 0 ], 10);
            birthDate[ 1 ] = Integer.parseInt(birthParts[ 1 ], 10);
            birthDate[ 2 ] = Integer.parseInt(birthParts[ 2 ], 10);
        }

        return getMakeUser(command, uuid, initials, birthDate, height, gender, activityIndex);
    }

    public byte[] getMakeUser(int command, long uuid, String initials, int[] birthDate, int height, char gender, int activityIndex)
    {
        Log.d(LOGTAG, "getMakeUser:"
                + " command=" + command
                + " uuid=" + uuid
                + " initials=" + initials
                + " birthDate=" + Arrays.toString(birthDate)
                + " height=" + height
                + " gender=" + gender
                + " activityIndex=" + activityIndex);

        byte[] data = new byte[ 18 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) command;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        while (initials.length() < 3) initials += " ";
        char[] rawInitials = initials.toCharArray();

        data[ 10 ] = (byte) rawInitials[ 0 ];
        data[ 11 ] = (byte) rawInitials[ 1 ];
        data[ 12 ] = (byte) rawInitials[ 2 ];

        data[ 13 ] = (byte) (birthDate[ 0 ] - 1900);
        data[ 14 ] = (byte) birthDate[ 1 ];
        data[ 15 ] = (byte) birthDate[ 2 ];

        data[ 16 ] = (byte) height;

        data[ 17 ] = (byte) ((gender == 'M' ? 128 : 0) + activityIndex);

        return data;
    }

    public byte[] getDeleteUser(long uuid)
    {
        Log.d(LOGTAG, "getDeleteUser-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 50;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        return data;
    }

    public byte[] getTakeUserMeasurementFromPreferences()
    {
        SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;
        long uuid = Integer.parseInt(sp.getString("health.scale.userid", ""));

        return getTakeUserMeasurement(uuid);
    }

    public byte[] getTakeUserMeasurement(long uuid)
    {
        Log.d(LOGTAG,"getTakeUserMeasurement-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 64;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        return data;
    }

    public byte[] getUserList()
    {
        Log.d(LOGTAG,"getUserList");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 51;

        return data;
    }

    public byte[] getUserMeasurementsFromPreferences()
    {
        SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;
        long uuid = Integer.parseInt(sp.getString("health.scale.userid", ""));

        return getUserMeasurements(uuid);
    }

    public byte[] getUserMeasurements(long uuid)
    {
        Log.d(LOGTAG,"getUserMeasurements-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 65;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        return data;
    }

    public byte[] getDeleteUserMeasurements(long uuid)
    {
        Log.d(LOGTAG,"getDeleteUserMeasurements-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 67;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        return data;
    }

    public byte[] getSetUserWeight(long uuid, float weight, float bodyFat, long timeStamp)
    {
        Log.d(LOGTAG, "getSetUserWeight-->uuid : " + uuid + ", weight : " + weight + ", bodyFat : " + bodyFat + ", timeStamp : " + timeStamp);

        byte[] data = new byte[ 18 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 68;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        int intWeight = (int) (weight * 20.0f);
        data[ 10 ] = (byte) ((intWeight >> 8) & 0xff);
        data[ 11 ] = (byte) (intWeight & 0xff);

        int intBodyFat = (int) (bodyFat * 10.0f);
        data[ 12 ] = (byte) ((intBodyFat >> 8) & 0xff);
        data[ 13 ] = (byte) (intBodyFat & 0xff);

        int timeStampInSeconds = getTimeStampInSeconds(timeStamp);

        for (int i = 0; i < 4; i++)
        {
            data[ i + 14 ] = (byte) (timeStampInSeconds >> ((3 - i) * 8));
        }

        return data;
    }

    public byte[] getUserWeightAndBodyFat(long uuid)
    {
        Log.d(LOGTAG,"getUserWeightAndBodyFat-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 69;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        return data;
    }

    public byte[] getUnknownMeasurements()
    {
        Log.d(LOGTAG, "getUnknownMeasurements");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 70;

        return data;
    }

    public byte[] getDeleteUnknownMeasurement(int measurementId)
    {
        Log.d(LOGTAG,"getDeleteUnknownMeasurement-->measurementId : " + measurementId);

        byte[] data = new byte[ 3 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 73;
        data[ 2 ] = (byte) measurementId;

        return data;
    }

    public byte[] getTakeGuestMeasurementWithInitials(String initials, int[] birthDate, int height, char gender, int activityIndex, int unit)
    {
        Log.d(LOGTAG,"getTakeGuestMeasurementWithInitials-->initial : " + initials + ", birthDate : " + Arrays.toString(birthDate) + ", height : " + height + ", gender : " + gender + ", activityIndex : " + activityIndex + ", unit : " + unit);

        byte[] data = new byte[ 11 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 74;

        while (initials.length() < 3) initials += " ";
        char[] rawInitials = initials.toCharArray();

        data[ 2 ] = (byte) rawInitials[ 0 ];
        data[ 3 ] = (byte) rawInitials[ 1 ];
        data[ 4 ] = (byte) rawInitials[ 2 ];

        data[ 5 ] = (byte) (birthDate[ 0 ] - 1900);
        data[ 6 ] = (byte) birthDate[ 1 ];
        data[ 7 ] = (byte) birthDate[ 2 ];

        data[ 8 ] = (byte) height;

        data[ 9 ] = (byte) ((gender == 'M' ? 128 : 0) + activityIndex);

        data[ 10 ] = (byte) unit;

        return data;
    }

    public byte[] getSetDateTime()
    {
        Log.d(LOGTAG,"getSetDateTime");

        byte[] data = new byte[ 5 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -23 : -7);

        byte[] rawTimeStamp = convertIntToBytes(getTimeStampInSeconds(new Date().getTime()));
        System.arraycopy(rawTimeStamp, 0, data, 1, 4);

        return data;
    }

    public byte[] getSetUnit(int unit)
    {
        Log.d(LOGTAG,"getSetUnit-->unit : " + unit);

        byte[] data = new byte[ 3 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 77;
        data[ 2 ] = (byte) unit;

        return data;
    }

    public byte[] getSetReferDefinitionToWeightThreshold(float weightThreshold, float bodyFatThreshold)
    {
        Log.d(LOGTAG,"getSetReferDefinitionToWeightThreshold-->weightThreshold : " + weightThreshold + ", bodyFatThreshold : " + bodyFatThreshold);

        byte[] data = new byte[ 4 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 78;

        data[ 2 ] = (byte) (weightThreshold * 10.0f);
        data[ 3 ] = (byte) (bodyFatThreshold * 10.0f);

        return data;
    }

    public byte[] getScaleSleepStatus()
    {
        Log.d(LOGTAG, "getScaleSleepStatus");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -16 : -32);
        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getUserInfo(long uuid)
    {
        Log.d(LOGTAG,"getUserInfo-->uuid : " + uuid);

        byte[] data = new byte[ 10 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) 54;

        System.arraycopy(convertLongToBytes(uuid), 0, data, 2, 8);

        return data;
    }

    public byte[] getRemoteTimeStamp()
    {
        Log.d(LOGTAG,"getRemoteTimeStamp");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -14 : -30);
        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getModuleVersion()
    {
        Log.d(LOGTAG,"getModuleVersion");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -15 : -31);
        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getForceDisconnect()
    {
        Log.d(LOGTAG,"getForceDisconnect");

        byte[] data = new byte[ 2 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -22 : -6);
        data[ 1 ] = (byte) 2;

        return data;
    }

    public byte[] getAcknowledgementData(byte[] resp)
    {
        byte[] data = new byte[ 5 ];

        data[ 0 ] = (byte) (isCompatibleDevice() ? -25 : -9);
        data[ 1 ] = (byte) -15;

        data[ 2 ] = resp[ 1 ];
        data[ 3 ] = resp[ 2 ];
        data[ 4 ] = resp[ 3 ];

        return data;
    }
}
