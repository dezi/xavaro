package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.os.Environment;
import android.widget.Toast;
import android.net.Uri;

import java.io.File;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;

public class LaunchItemAdmin extends LaunchItem
{
    private final static String LOGTAG = LaunchItemAdmin.class.getSimpleName();

    public LaunchItemAdmin(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (type.equals("select") && (subtype != null))
        {
            String packageName = null;

            if (subtype.equals("home"))
            {
                packageName = DitUndDat.DefaultApps.getDefaultHome(context);

                icon.setImageResource(GlobalConfigs.IconResSelectHome);
            }

            if (subtype.equals("assist"))
            {
                packageName = DitUndDat.DefaultApps.getDefaultAssist(context);

                icon.setImageResource(GlobalConfigs.IconResSelectAssist);
            }

            if (packageName != null)
            {
                Drawable appIcon = VersionUtils.getIconFromApplication(context, packageName);

                if (appIcon != null)
                {
                    overicon.setImageDrawable(appIcon);
                    overlay.setVisibility(VISIBLE);
                }
            }
        }

        if (type.equals("settings") && (subtype != null))
        {
            if (subtype.equals("safehome"))
            {
                icon.setImageResource(GlobalConfigs.IconResSettingsSafehome);
            }

            if (subtype.equals("android"))
            {
                icon.setImageResource(GlobalConfigs.IconResSettingsAndroid);
            }
        }

        if (type.equals("developer"))
        {
            icon.setImageResource(GlobalConfigs.IconResTesting);
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("select"   )) launchSelect();
        if (type.equals("settings" )) launchSettings();
        if (type.equals("install"  )) launchInstall();
        if (type.equals("developer")) launchDeveloper();
    }

    private void launchSelect()
    {
        if (Simple.equals(subtype, "home"  )) DitUndDat.DefaultApps.setDefaultHome(context);
        if (Simple.equals(subtype, "assist")) DitUndDat.DefaultApps.setDefaultAssist(context);
    }

    private void launchSettings()
    {
        if (Simple.equals(subtype, "android"))
        {
            ProcessManager.launchApp(context, "com.android.settings");
        }

        if (Simple.equals(subtype, "safehome"))
        {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);
        }
    }

    private void launchInstall()
    {
        if (!config.has("packagename"))
        {
            Toast.makeText(getContext(), "Nix <packagename> configured.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String packagename = config.getString("packagename");

            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packagename, 0);

            if (appInfo != null)
            {
                //
                // Package is installed.
                //

                ProcessManager.launchApp(context, packagename);

                return;
            }

        }
        catch (Exception ignore)
        {
            //
            // Package is not installed.
            //
        }

        try
        {
            String packagename = config.getString("packagename");

            Intent goToMarket = new Intent(Intent.ACTION_VIEW);
            goToMarket.setData(Uri.parse("market://details?id=" + packagename));
            context.startActivity(goToMarket);
        }
        catch (Exception oops)
        {
            OopsService.log(LOGTAG, oops);
        }
    }

    private void launchDeveloper()
    {
        //DitUndDat.SharedPrefs.sharedPrefs.edit().clear().commit();

        //new BlueToothScale(context).getCreateUserFromPreferences();

        //DitUndDat.SpeekDat.speak("Die Sprachausgabe von Android funktioniert prima.");

        /*
        String xpath = "RemoteContacts/identities";
        PersistManager.delXpath(xpath);
        PersistManager.flush();
        */

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        //Simple.startActivityForResult(intent, 1);

        /*
        Log.d(LOGTAG, "launchDeveloper: " + Environment.getExternalStorageDirectory());
        Log.d(LOGTAG, "launchDeveloper: " + Environment.DIRECTORY_PICTURES);
        Simple.dumpDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        Simple.dumpDirectory("/storage/emulated/0");
        */
    }
}
