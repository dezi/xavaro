package de.xavaro.android.common;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TestDat
{
    private static final String LOGTAG = TestDat.class.getSimpleName();

    public static void testdat()
    {
        doPost(getApiParams());
    }

    public static void doPost(Map<String, String> params)
    {
        try
        {
            URLConnection connection = new URL(getApiPath()).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);

            String content = "";

            /*

                    "id=" + URLEncoder.encode("username", "utf8") +
                            "&num=" + URLEncoder.encode("password", "utf8") +
                            "&remember=" + URLEncoder.encode("on", "utf8") +
                            "&output=" + URLEncoder.encode("xml", "utf8");
            */

            for (Map.Entry<String, ?> entry : params.entrySet())
            {
                if (content.length() > 0) content = content + "&";

                content += entry.getKey() + "=" + entry.getValue();
            }

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + content.getBytes().length);

            Log.d(LOGTAG, "===================>" + content);

            OutputStream output = connection.getOutputStream();
            output.write(content.getBytes());
            output.close();

            byte[] buffer = new byte[ 8192 ];

            InputStream input = connection.getInputStream();
            int xfer = input.read(buffer);
            input.close();

            String text = new String(buffer, 0, xfer);
            Log.d(LOGTAG, "===================<" + text);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static String getApiPath()
    {
        return "https://api.couchfunk.de/api/live_stream";
    }

    public static Map<String, String> getApiParams()
    {
        Map<String, String> params = new HashMap<>();
        params.put("access_level", "open" /*getAccessLevel() */);
        params.put("channel_id" /*CFBundleHelper.KEY_CHANNEL_ID */, "10235");
        params.put("stream_type", "hls");
        params.put("timestamp" /*DatabaseHelper.COLUMN_TIMESTAMP */, "" + (Simple.nowAsTimeStamp() / 1000));
        params.put("device_uuid", "00000000-0000-4000-b000-000000000000" /*UUIDManager.getUUID(CFApplication.getAppContext()) */);

        try
        {
            params = addSignature(params, Simple.getAppContext());
        }
        catch (InvalidKeyException e)
        {
        }
        catch (NoSuchAlgorithmException e2)
        {
        }
        return params;
    }

    public static Map<String, String> addSignature(Map<String, String> params, Context context) throws InvalidKeyException, NoSuchAlgorithmException
    {
        if (params == null)
        {
            params = new HashMap<>();
        }

        params.put("app", "E6DA0A43D3797857ABB0" /*context.getString(C1229R.string.app_id) */);

        TreeSet<String> sortedKeys = new TreeSet<>(params.keySet());
        List<String> values = new ArrayList<>();

        Iterator it = sortedKeys.iterator();

        while (it.hasNext())
        {
            values.add(params.get((String) it.next()));
        }

        String concatenated = TextUtils.join("~", values);
        String secretKey = "F211BE9447E5DB4BD1846821AA7D5C1BB1729431CB3C5C1B74BAFDFD8F0BF29D"; //*C1229R.string.app_secret_key

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
        byte[] byteSignature = sha256_HMAC.doFinal(concatenated.getBytes());

        StringBuilder signature = new StringBuilder();
        for (byte bite : byteSignature)
        {
            signature.append(Integer.toString((bite & 255) + 256, 16).substring(1));
        }
        params.put("signature", signature.toString());
        return params;
    }

}
