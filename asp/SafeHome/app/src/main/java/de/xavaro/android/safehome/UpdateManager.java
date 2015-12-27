package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class UpdateManager extends Thread
{
    private static final String LOGTAG = UpdateManager.class.getSimpleName();

    public static void selfUpdate(Context context, boolean force)
    {
        if (GlobalConfigs.BetaVersion) new UpdateManager(context,force).start();
    }

    private final Context context;
    private final boolean force;

    private UpdateManager(Context context, boolean force)
    {
        this.context = context;
        this.force = force;
    }

    @Override
    @SuppressLint("SetWorldReadable")
    public void run()
    {
        try
        {
            String packagename = context.getPackageName();
            File packagefile = new File(context.getCacheDir(), packagename + ".apk");

            if (packagefile.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                packagefile.delete();
            }

            String xpathmodif = "UpdateManager/Beta/" + packagename + "/LastModified";
            String lastModified = SettingsManager.getXpathString(xpathmodif);

            String url = "http://" + GlobalConfigs.BetaServerName + "/beta/" + packagename + ".apk";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            if ((lastModified != null) && ! force)
            {
                connection.setRequestProperty("If-Modified-Since",lastModified);
            }

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED)
            {
                Log.d(LOGTAG,"No new version available");

                return;
            }

            Log.d(LOGTAG, "Loading new version:" + lastModified + "=" + connection.getResponseCode());

            Map<String, List<String>> headers = connection.getHeaderFields();

            for (String headerKey : headers.keySet())
            {
                if (headerKey == null) continue;

                for (String headerValue : headers.get(headerKey))
                {
                    if (headerKey.equalsIgnoreCase("Last-Modified"))
                    {
                        lastModified = headerValue;
                        break;
                    }
                }
            }

            InputStream input = connection.getInputStream();
            OutputStream output = new FileOutputStream(packagefile);

            byte[] buffer = new byte[ 4096 ];
            int xfer;

            while ((xfer = input.read(buffer)) > 0)
            {
                output.write(buffer,0,xfer);
            }

            output.close();
            input.close();

            //noinspection ResultOfMethodCallIgnored
            packagefile.setReadable(true, false);

            Log.d(LOGTAG, "Installing new version: " + packagefile.length());

            String mimeType = "application/vnd.android.package-archive";

            Intent promptInstall = new Intent(Intent.ACTION_VIEW);
            promptInstall.setDataAndType(Uri.fromFile(packagefile), mimeType);
            promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(promptInstall);

            SettingsManager.putXpath(xpathmodif, lastModified);
            SettingsManager.flush();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
