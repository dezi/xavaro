package de.xavaro.android.common;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

@SuppressWarnings("unused")
public class WebAppBeta
{
    private static final String LOGTAG = WebAppBeta.class.getSimpleName();

    @JavascriptInterface
    @SuppressLint("SetWorldReadable")
    public boolean getBetaDownload(String version)
    {
        try
        {
            String remotename = Simple.getPackageName() + "." + version + ".apk";
            String localname = Simple.getPackageName() + ".apk";

            File localfile = new File(Simple.getCacheDir(), localname);

            if (localfile.exists() && localfile.delete())
            {
                Log.d(LOGTAG,"getBetaDownload: deleted old: " + localfile);
            }

            String url = "http://"
                    + CommonConfigs.BetaServerName + ":"
                    + CommonConfigs.BetaServerPort
                    + "/beta/" + remotename;

            Log.d(LOGTAG,"getBetaDownload: Download: " + url);

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                OopsService.log(LOGTAG, "Download failed: " + url);
                return false;
            }

            InputStream input = connection.getInputStream();
            OutputStream output = new FileOutputStream(localfile);

            byte[] buffer = new byte[ 4096 ];
            int xfer;

            while ((xfer = input.read(buffer)) > 0)
            {
                output.write(buffer,0,xfer);
            }

            output.close();
            input.close();

            //noinspection ResultOfMethodCallIgnored
            localfile.setReadable(true, false);

            return true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    @JavascriptInterface
    public void makeBetaInstall()
    {
        try
        {
            WebAppCache.nukeWebAppCache();

            String localname = Simple.getPackageName() + ".apk";
            File localfile = new File(Simple.getCacheDir(), localname);

            Log.d(LOGTAG, "makeBetaInstall: version: " + localfile.length());

            String mimeType = "application/vnd.android.package-archive";
            Intent promptInstall = new Intent(Intent.ACTION_VIEW);
            promptInstall.setDataAndType(Uri.fromFile(localfile), mimeType);
            promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Simple.getAppContext().startActivity(promptInstall);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }
}
