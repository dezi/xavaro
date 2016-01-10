package de.xavaro.android.safehome;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.xavaro.android.common.OopsService;

public class HealthScale extends HealthBase
{
    private static final String LOGTAG = HealthScale.class.getSimpleName();

    private static HealthScale instance;

    public static HealthScale getInstance()
    {
        if (instance == null) instance = new HealthScale();

        return instance;
    }

    public static void subscribe(BlueTooth.BlueToothConnectCallback subscriber)
    {
        getInstance().setConnectCallback(subscriber);
    }

    @Override
    public void onBluetoothReceivedData(String deviceName, JSONObject data)
    {
        Log.d(LOGTAG,"onBluetoothReceivedData: " + data.toString());

        if (! data.has("scale")) return;

        try
        {
            JSONObject command = new JSONObject();
            JSONObject mesg = data.getJSONObject("scale");
            String type = mesg.getString("type");

            if (type.equals("DeviceReady"))
            {
                //
                // Scale is awake and live. Set and get scale time.
                //

                command.put("command", "getSetDateTime");
            }

            if (type.equals("RemoteTimeStamp"))
            {
                //
                // Scale time has now been set proceed with
                // setting the scale to our user.
                //

                command.put("command", "getUserList");
            }

            if (type.equals("UserList"))
            {
                //
                // Todo: check number of users and delete if required.
                //
            }

            if (type.equals("UserListArray"))
            {
                //
                // Scale time has now been set proceed with
                // setting the scale to our user.
                //

                SharedPreferences sp = DitUndDat.SharedPrefs.sharedPrefs;
                long uuid = Long.parseLong(sp.getString("health.scale.userid", ""));
                boolean found = false;

                try
                {
                    if (mesg.has("array"))
                    {
                        JSONArray array = mesg.getJSONArray("array");

                        for (int inx = 0; inx < array.length(); inx++)
                        {
                            JSONObject user = array.getJSONObject(inx);

                            if (user.has("uuid") && (user.getLong("uuid") == uuid))
                            {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found)
                    {
                        command.put("command", "getUpdateUserFromPreferences");
                    }
                    else
                    {
                        command.put("command", "getCreateUserFromPreferences");
                    }

                    blueTooth.sendCommand(command);

                    //
                    // Above calls have no result. Just load the next command
                    // to set scale to active user.
                    //

                    command = new JSONObject();
                    command.put("command", "getTakeUserMeasurementFromPreferences");
                }
                catch (JSONException ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }

            if (type.equals("TakeUserMeasurement"))
            {
                DitUndDat.SpeekDat.speak("Die Waage ist nun für Sie eingestellt");
            }

            if (type.equals("LiveMeasurementOnTimestamp"))
            {
                long uuid = mesg.getLong("uuid");
                double weight = mesg.getDouble("weight");
                double impedance = mesg.getDouble("impedance");

                if (impedance == 0)
                {
                    DitUndDat.SpeekDat.speak("Die Waage kann nur ordentlich arbeiten, wenn sie Barfuss messen");
                    DitUndDat.SpeekDat.speak("Bitte ziehen sie Schuhe und Strümpfe aus und wiederholen sie die Messung");
                }
                else
                {
                    String[] weithParts = ("" + weight).split("\\.");

                    if (weithParts.length == 2)
                    {
                        DitUndDat.SpeekDat.speak("Ihr Gewicht beträgt "
                                + weithParts[ 0 ] + " Komma "
                                + weithParts[ 1 ] + " Kilogramm");
                    }
                    else
                    {
                        DitUndDat.SpeekDat.speak("Ihr Gewicht beträgt "
                                + weithParts[ 0 ] + " Kilogramm");
                    }

                    DitUndDat.SpeekDat.speak("Fettsack");

                    DitUndDat.SpeekDat.speak("Vielen Dank für ihre Messung");
                }
            }

            if (command.has("command")) blueTooth.sendCommand(command);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
