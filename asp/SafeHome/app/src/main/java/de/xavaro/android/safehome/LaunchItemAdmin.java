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

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VersionUtils;

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
                packageName = DefaultApps.getDefaultHome();

                icon.setImageResource(GlobalConfigs.IconResSelectHome);
            }

            if (subtype.equals("assist"))
            {
                packageName = DefaultApps.getDefaultAssist();

                icon.setImageResource(GlobalConfigs.IconResSelectAssist);
            }

            if (packageName != null)
            {
                Drawable appIcon = VersionUtils.getIconFromApplication(packageName);

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
                icon.setImageResource(CommonConfigs.IconResSettingsAndroid);
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
        if (Simple.equals(subtype, "home"  )) DefaultApps.setDefaultHome();
        if (Simple.equals(subtype, "assist")) DefaultApps.setDefaultAssist();
    }

    private void launchSettings()
    {
        if (Simple.equals(subtype, "android"))
        {
            ProcessManager.launchApp("com.android.settings");
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

                ProcessManager.launchApp(packagename);

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

    private void launchDeveloper1()
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

    LaunchFrameDeveloper developerFrame;

    private void launchDeveloper()
    {
        if (developerFrame == null)
        {
            developerFrame = new LaunchFrameDeveloper(context, this);
            developerFrame.setSubtype(subtype);
        }

        ((HomeActivity) context).addViewToBackStack(developerFrame);
    }
}
