package de.xavaro.android.safehome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Gravity;
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

    private FrameLayout.LayoutParams layout;

    private JSONObject config;

    private TextView label;
    private ImageView icon;

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

        setBackgroundResource(R.drawable.shadow_400x400);
        setVisibility(INVISIBLE);

        layout = new FrameLayout.LayoutParams(0,0);
        setLayoutParams(layout);

        icon = new ImageView(context);
        //icon.setBackgroundColor(0x88888888);
        icon.setPadding(0, 0, 0, 40);
        icon.setVisibility(INVISIBLE);
        this.addView(icon);

        label = new TextView(context);
        label.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        label.setPadding(5, 5, 5, 5);
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        label.setTextSize(18f);
        this.addView(label);
    }

    public void setSize(int width,int height)
    {
        layout.width  = width;
        layout.height = height;
    }

    public void setPosition(int left,int top)
    {
        layout.leftMargin = left;
        layout.topMargin  = top;
    }

    public void setConfig(JSONObject config)
    {
        this.config = config;

        try
        {
            label.setText(config.getString("label"));
            setVisibility(VISIBLE);

            String packageName = config.has("packagename") ? config.getString("packagename") : null;

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
            }

            if (packageName != null)
            {
                if (packageName.equals("org.wikipedia"))
                {
                    icon.setImageDrawable(StaticUtils.getDrawableFromResources(context, R.drawable.wikipedia_390x390));
                }
                else
                {
                    try
                    {
                        ApplicationInfo appInfo = getContext().getPackageManager().getApplicationInfo(packageName, 0);
                        Resources res = getContext().getPackageManager().getResourcesForApplication(appInfo);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            Drawable appIcon = res.getDrawableForDensity(appInfo.icon, DisplayMetrics.DENSITY_XXXHIGH, null);
                            icon.setImageDrawable(appIcon);
                        } else
                        {
                            Configuration appConfig = res.getConfiguration();
                            appConfig.densityDpi = DisplayMetrics.DENSITY_XXXHIGH;
                            DisplayMetrics dm = res.getDisplayMetrics();
                            res.updateConfiguration(appConfig, dm);

                            //noinspection deprecation
                            Drawable appIcon = res.getDrawable(appInfo.icon);
                            icon.setImageDrawable(appIcon);
                        }
                    }
                    catch (Exception ex)
                    {
                        icon.setImageDrawable(StaticUtils.getDrawableFromResources(context, R.drawable.stop_512x512));
                    }
                }

                icon.setVisibility(VISIBLE);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
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
            if (type.equals("whatsapp"     )) { launchWhatsApp();     return; }
            if (type.equals("genericapp"   )) { launchGenericApp();   return; }

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
}
