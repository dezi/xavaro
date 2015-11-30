package de.xavaro.android.safehome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.View;
import android.view.Gravity;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

//
// Launch item view on home screen.
//

public class LaunchItem extends FrameLayout
{
    private final String LOGTAG = "LaunchItem";

    private Context context;

    private LayoutParams layout;

    private JSONObject config;
    private JSONObject settings;

    private TextView label;
    private ImageView icon;
    
    private FrameLayout overlay;
    private LayoutParams oversize;
    private ImageView overicon;

    private LaunchGroup directory;

    public LaunchItem(Context context)
    {
        super(context);

        myInit(context);
    }

    public LaunchItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public LaunchItem(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    private void myInit(Context context)
    {
        this.context = context;

        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onMyClick();
            }
        });

        setVisibility(INVISIBLE);

        layout = new LayoutParams(0,0);
        setLayoutParams(layout);

        icon = new ImageView(context);
        icon.setPadding(0, 0, 0, 40);
        icon.setVisibility(INVISIBLE);
        this.addView(icon);

        label = new TextView(context);
        label.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        label.setPadding(5, 5, 5, 5);
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        label.setTextSize(18f);
        this.addView(label);

        oversize = new LayoutParams(0,0);
        oversize.gravity = Gravity.END + Gravity.TOP;

        overlay = new FrameLayout(context);
        overlay.setLayoutParams(oversize);
        this.addView(overlay);

        overicon = new ImageView(context);
        overicon.setVisibility(INVISIBLE);
        overlay.addView(overicon);
    }

    public void setSize(int width,int height)
    {
        layout.width  = width;
        layout.height = height;

        oversize.width  = width  / 4;
        oversize.height = height / 4;
    }

    public void setPosition(int left,int top)
    {
        layout.leftMargin = left;
        layout.topMargin  = top;
    }

    public void setConfig(JSONObject config)
    {
        String packageName = null;
        boolean hasProblem = false;
        ImageView targetIcon = icon;

        this.config = config;

        try
        {
            label.setText(config.getString("label"));
            setVisibility(VISIBLE);

            Log.d(LOGTAG, config.getString("label"));

            packageName = config.has("packagename") ? config.getString("packagename") : null;

            if (config.has("type"))
            {
                String type = config.getString("type");

                if (type.equals("select_home"))
                {
                    packageName = StaticUtils.getDefaultHome(context);
                }

                if (type.equals("select_assist"))
                {
                    packageName = StaticUtils.getDefaultAssist(context);
                }

                if (type.equals("developer"))
                {
                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.developer_400x400));
                    icon.setVisibility(VISIBLE);
                }

                if (type.equals("settings"))
                {
                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.settings_512x512));
                    icon.setVisibility(VISIBLE);
                }

                if (type.equals("whatsapp"))
                {
                    if (config.has("waphonenumber"))
                    {
                        String phone = config.getString("waphonenumber");
                        Bitmap thumbnail = StaticUtils.getWhatsAppProfileBitmap(context, phone);

                        if (thumbnail != null)
                        {
                            thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                            icon.setImageDrawable(new BitmapDrawable(context.getResources(),thumbnail));
                            icon.setVisibility(VISIBLE);
                            targetIcon = overicon;
                        }
                    }
                }

                if (type.equals("skype"))
                {
                    if (config.has("skypename"))
                    {
                        String skypename = config.getString("skypename");
                        Bitmap thumbnail = StaticUtils.getSkypeProfileBitmap(context, skypename);

                        if (thumbnail != null)
                        {
                            thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                            icon.setImageDrawable(new BitmapDrawable(context.getResources(),thumbnail));
                            icon.setVisibility(VISIBLE);
                            targetIcon = overicon;
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if (packageName != null)
        {
            if (packageName.equals("org.wikipedia"))
            {
                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.wikipedia_390x390));
            }
            else
            {
                Drawable appIcon = VersionUtils.getIconFromApplication(context, packageName);

                if (appIcon != null)
                {
                    targetIcon.setImageDrawable(appIcon);
                }
                else
                {
                    targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.stop_512x512));
                    hasProblem = true;
                }
            }

            targetIcon.setVisibility(VISIBLE);
        }

        setBackgroundResource(hasProblem ? R.drawable.shadow_alert_400x400 : R.drawable.shadow_black_400x400);
    }

    private void onMyClick()
    {
        if (config == null)
        {
            Toast.makeText(getContext(),"Nix configured.",Toast.LENGTH_LONG).show();

            return;
        }

        if (! config.has("type"))
        {
            Toast.makeText(getContext(),"Nix <type> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String type = config.getString("type");

            // @formatter:off
            if (type.equals("select_home"  )) { launchSelectHome();   return; }
            if (type.equals("select_assist")) { launchSelectAssist(); return; }
            if (type.equals("genericapp"   )) { launchGenericApp();   return; }
            if (type.equals("directory"    )) { launchDirectory();    return; }
            if (type.equals("developer"    )) { launchDeveloper();    return; }
            if (type.equals("settings"     )) { launchSettings();     return; }
            if (type.equals("whatsapp"     )) { launchWhatsApp();     return; }
            if (type.equals("skype"        )) { launchSkype();        return; }

            // @formatter:on

            Toast.makeText(getContext(),"Nix launcher type <" + type + "> configured.",Toast.LENGTH_LONG).show();
        }
        catch (JSONException ignore)
        {
        }
    }

    private void launchSelectHome()
    {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, FakeHome.class);
        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);

        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void launchSelectAssist()
    {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, FakeAssist.class);
        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent startMain = new Intent(Intent.ACTION_ASSIST);
        startMain.addCategory(Intent.CATEGORY_DEFAULT);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);

        pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void launchWhatsApp()
    {
        if (! config.has("waphonenumber"))
        {
            Toast.makeText(getContext(),"Nix <waphonenumber> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String waphonenumber = config.getString("waphonenumber");
            Uri uri = Uri.parse("smsto:" + waphonenumber);
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.setPackage("com.whatsapp");
            context.startActivity(Intent.createChooser(sendIntent, ""));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchSkype()
    {
        if (! config.has("skypename"))
        {
            Toast.makeText(getContext(),"Nix <skypename> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String skypename = config.getString("skypename");
            String subtype = config.has("subtype") ? config.getString("subtype") : "chat";

            Uri uri = Uri.parse("skype:" + skypename);

            if (subtype.equals("chat"))
            {
                uri = Uri.parse("skype:" + skypename + "?chat");
            }

            if (subtype.equals("call"))
            {
                uri = Uri.parse("skype:" + skypename + "?call");
            }

            Intent skype = new Intent(Intent.ACTION_VIEW);
            skype.setData(uri);
            skype.setPackage("com.skype.raider");
            context.startActivity(skype);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchGenericApp()
    {
        if (! config.has("packagename"))
        {
            Toast.makeText(getContext(),"Nix <packagename> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String packagename = config.getString("packagename");
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packagename);
            context.startActivity(launchIntent);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchSettings()
    {
        if (settings == null)
        {
            settings = StaticUtils.readRawTextResourceJSON(context, R.raw.default_settings);

            if ((settings == null) || ! settings.has("launchgroup"))
            {
                Toast.makeText(context, "Keine <launchgroup> gefunden.", Toast.LENGTH_LONG).show();

                return;
            }
        }

        if (directory == null)
        {
            directory = new LaunchGroup(context);

            try
            {
                directory.setConfig(settings.getJSONObject("launchgroup"));
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }

        ((HomeActivity) context).addLauncherToBackStack(directory);
    }

    private void launchDirectory()
    {
        if (! config.has("launchgroup"))
        {
            Toast.makeText(getContext(),"Nix <launchgroup> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroup(context);

            try
            {
                directory.setConfig(config.getJSONObject("launchgroup"));
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }

        ((HomeActivity) context).addLauncherToBackStack(directory);
    }

    private void launchDeveloper()
    {
        //StaticUtils.getAllInstalledApps(context);

        StaticUtils.JSON2String(StaticUtils.getAllInstalledApps(context), true);
    }
}
