package de.xavaro.android.common;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GCMMessageService extends GcmListenerService
{
    private static final String LOGTAG = GCMMessageService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        String message = data.getString("message");

        Log.d(LOGTAG, "From: " + from);
        Log.d(LOGTAG, "Message: " + message);

        if (from.startsWith("/topics/"))
        {
            // message received from some topic.
        }
        else
        {
            // normal downstream message.
        }
    }

    public static boolean sendMessage(String receiver, String message)
    {
        try
        {
            JSONObject jData = new JSONObject();
            jData.put("message", message);

            JSONObject jGcmData = new JSONObject();
            jGcmData.put("to",receiver);
            jGcmData.put("data", jData);

            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + Simple.dezify(CommonStatic.gcm_apeyki));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            InputStream inputStream = conn.getInputStream();

            StringBuilder string = new StringBuilder();
            byte[] buffer = new byte[ 4096 ];
            int xfer;

            while ((xfer = inputStream.read(buffer)) > 0)
            {
                string.append(new String(buffer, 0, xfer));
            }

            inputStream.close();

            Log.d(LOGTAG, "sendUpstream" + string.toString());

            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(Simple.getContext());

        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (apiAvailability.isUserResolvableError(resultCode))
            {
                Log.d(LOGTAG, "This could be supported." + resultCode);
            }
            else
            {
                Log.d(LOGTAG, "This device is not supported.");
            }

            return false;
        }

        Log.d(LOGTAG, "This device is supported.");

        return true;
    }
}
