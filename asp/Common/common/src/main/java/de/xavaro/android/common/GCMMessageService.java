package de.xavaro.android.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class GCMMessageService extends GcmListenerService
{
    private static final String LOGTAG = GCMMessageService.class.getSimpleName();

    @Override
    public void onCreate()
    {
        //
        // The listener service is either started when the application
        // is started or by Android operating system, when a GCM message
        // is coming in (wake up).
        //

        super.onCreate();

        //
        // Make sure our internal messaging service is also started up.
        //

        startService(new Intent(this, CommService.class));
    }

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        //
        // We come here eventually from a wake up through GCM.
        // If this is the very first message, our comm server might
        // not yet be fully started from onCreate method.
        //

        while (! CommService.getIsRunning())
        {
            Log.d(LOGTAG, "onMessageReceived: Waiting for " + CommService.class.getSimpleName());

            //
            // Hard synchronizing of threads.
            //

            Simple.sleep(100);
        }

        //
        // Extract message from bundle.
        //

        String message = data.getString("message");

        if (from.startsWith("/topics/"))
        {
            //
            // Message received from some topic. Feature
            // is not used at this time.
            //

            return;
        }

        if ((message != null) && message.startsWith("{\"base64\":"))
        {
            //
            // Message is serialized JSON and contains our base64 tag
            // on first position. Decode and deliver to comm service.
            //

            try
            {
                JSONObject jmess= new JSONObject(message);
                String base64 = jmess.getString("base64");
                byte[] rawdata = Base64.decode(base64, 0);

                CommService.getInstance().onRawMessageReceived(rawdata);
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }
    }

    public static boolean sendMessage(String receiver, byte[] message)
    {
        //
        // Message is either crypted or not. Anyway we are given bytes,
        // which we put into message as a base64 encoded string.
        //

        String base64 = Base64.encodeToString(message, 0).trim();

        JSONObject base64message = new JSONObject();
        Simple.JSONput(base64message, "base64", base64);

        return sendMessage(receiver, base64message);
    }

    private static int badge = 88;

    public static boolean testMessage()
    {
        String token = "f2fTysDraaQ:APA91bG_wuERj35amcc7227eVB8CzmRGCxn_IWSdatpClTSVXIRTktV04NGeRvNgRmm9jay0S-vo4q7_EosGR3i-wg0ipQBnz8ngdGtVZ3Rk2jvYloNrWFVzXq7W5DsFSPEQvS_qVrOV";
        String message = "Hallo Pallo";
        JSONArray tokens = null;

        try
        {
            JSONObject jData = new JSONObject();
            jData.put("message", message);
            jData.put("guid", UUID.randomUUID().toString());

            JSONObject jSub = new JSONObject();
            jSub.put("a", "1");
            jSub.put("b", "2");
            jData.put("myjson", jSub);

            JSONObject jAPS = new JSONObject();
            jAPS.put("alert", "zickezacke");

            //jData.put("aps", "zickezacke");

            JSONObject jGcmData = new JSONObject();
            jGcmData.put("data", jData);

            JSONObject noti = new JSONObject();
            //noti.put("title", "Schnullerbacke");
            //noti.put("sound", "1.wav");
            noti.put("badge", "7");
            //noti.put("body", "ich bin der bodey");

            jGcmData.put("priority", "high");
            jGcmData.put("content_available", true);
            //jGcmData.put("notification", noti);

            if (tokens != null)
            {
                jGcmData.put("registration_ids", tokens);
            }
            else
            {
                jGcmData.put("to", token);
            }

            Log.d(LOGTAG,"testMessage: vor post:" + Json.toPretty(jGcmData));

            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + Simple.dezify(Simple.getGCMapeyki()));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            InputStream inputStream = conn.getInputStream();
            String response = Simple.getAllInput(inputStream);

            outputStream.close();
            inputStream.close();

            Log.d(LOGTAG, "testMessage: nach post");

            boolean success = response.contains("\"success\":1");

            Log.d(LOGTAG,"testMessage: success:" + success + "=>" + token);

            return success;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean sendMessage(String receiver, JSONObject message)
    {
        //
        // Send one message to receivers. The receiver is a guid identity.
        //

        JSONArray tokens = null;

        Log.d(LOGTAG, "sendMessage");

        //
        // Try to obtain GCM token from our remote contacts.
        //

        String token = RemoteContacts.getGCMToken(receiver);

        if (token == null)
        {
            //
            // Try to obtain token from a remote group (alertgroup or other).

            tokens = RemoteGroups.getGCMTokens(receiver);

            if ((tokens == null) || (tokens.length() == 0))
            {
                Log.d(LOGTAG, "sendMessage: das war nix...");

                return false;
            }

            for (int inx = 0; inx < tokens.length(); inx++)
            {
                Log.d(LOGTAG, "=======================sendMessage:" + receiver + "=" + Json.getString(tokens, inx));
            }
        }

        try
        {
            //
            // Pack our message how Google likes it.
            //

            JSONObject jData = new JSONObject();
            jData.put("message", message);

            JSONObject jGcmData = new JSONObject();
            jGcmData.put("data", jData);

            //
            // Receiver token or tokens.
            //

            if (tokens != null)
            {
                jGcmData.put("registration_ids", tokens);
            }
            else
            {
                jGcmData.put("to", token);
            }

            //
            // IOS compliance flags.
            //

            jGcmData.put("priority", "high");
            jGcmData.put("content_available", true);

            //
            // GCM only sends via post api.
            //

            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //
            // Add authorization stuff.
            //

            conn.setRequestProperty("Authorization", "key=" + Simple.dezify(Simple.getGCMapeyki()));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            InputStream inputStream = conn.getInputStream();
            String response = Simple.getAllInput(inputStream);

            outputStream.close();
            inputStream.close();

            boolean success = response.contains("\"success\":1");

            Log.d(LOGTAG,"success:" + success + "=>" + token);

            return success;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean checkPlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(Simple.getAppContext());

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
