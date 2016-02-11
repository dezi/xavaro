package de.xavaro.android.safehome;

import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

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

        JSONObject mesg = Json.getObject(data, "scale");
        if (mesg == null) return;

        String type = Json.getString(mesg, "type");

        if (Simple.equals(type, "TakeUserMeasurement"))
        {
            lastRecord = mesg;
            handler.post(messageSpeaker);
        }

        if (Simple.equals(type, "LiveMeasurementOnTimestamp"))
        {
            lastRecord = mesg;
            handler.post(messageSpeaker);
        }
    }

    private JSONObject lastRecord;

    private final Runnable messageSpeaker = new Runnable()
    {
        @Override
        public void run()
        {
            if (lastRecord == null) return;

            String type = Json.getString(lastRecord, "type");

            if (Simple.equals(type, "TakeUserMeasurement"))
            {
                Speak.speak("Die Waage ist nun für Sie eingestellt");
            }

            if (Simple.equals(type, "LiveMeasurementOnTimestamp"))
            {
                double weight = Json.getDouble(lastRecord, "weight");
                double impedance = Json.getDouble(lastRecord, "impedance");

                if (impedance == 0)
                {
                    Speak.speak("Die Waage kann nur ordentlich arbeiten, "
                            + "wenn sie Barfuss messen");

                    Speak.speak("Bitte ziehen sie Schuhe und Strümpfe aus "
                            + "und wiederholen sie die Messung");
                }
                else
                {
                    String[] weithParts = ("" + weight).split("\\.");

                    if (weithParts.length == 2)
                    {
                        Speak.speak("Ihr Gewicht beträgt "
                                + weithParts[ 0 ] + " Komma "
                                + weithParts[ 1 ] + " Kilogramm");
                    }
                    else
                    {
                        Speak.speak("Ihr Gewicht beträgt "
                                + weithParts[ 0 ] + " Kilogramm");
                    }
                }
            }
        }
    };
}
