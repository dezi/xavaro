package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;

import android.view.View;
import android.view.Gravity;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

//
// Launch item view on home screen.
//

public class LaunchItem extends FrameLayout implements CommonCallback
{
    private final String LOGTAG = "LaunchItem";

    private static ProxyPlayer proxyPlayer;
    private static ProgressBar spinner;

    private Context context;

    private LayoutParams layout;

    private JSONObject config;
    private JSONObject settings;

    private TextView label;
    private ImageView icon;
    
    private FrameLayout overlay;
    private LayoutParams oversize;
    private ImageView overicon;
    private Handler handler;

    private LaunchGroup directory;
    private WebFrame webframe;
    private WebRadio webradio;

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
            if (config.has("label"))
            {
                label.setText(config.getString("label"));
                Log.d(LOGTAG, config.getString("label"));
                setVisibility(VISIBLE);
            }

            if (config.has("packagename"))
            {
                packageName = config.getString("packagename");

                GlobalConfigs.weLikeThis(context,packageName);
            }

            if (config.has("icon"))
            {
                String iconname = config.getString("icon");

                if (iconname.startsWith("http://") || iconname.startsWith("https://"))
                {
                    Bitmap thumbnail = CacheManager.cacheThumbnail(context, iconname);

                    if (thumbnail != null)
                    {
                        icon.setImageDrawable(new BitmapDrawable(context.getResources(),thumbnail));
                        icon.setVisibility(VISIBLE);
                    }
                }
                else
                {
                    int resourceId = context.getResources().getIdentifier(iconname, "drawable", context.getPackageName());

                    if (resourceId > 0)
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, resourceId));
                        icon.setVisibility(VISIBLE);
                    }
                }
            }

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
                    GlobalConfigs.likeWhatsApp = true;

                    if (config.has("waphonenumber"))
                    {
                        String phone = config.getString("waphonenumber");
                        Bitmap thumbnail = ProfileImages.getWhatsAppProfileBitmap(context, phone);

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
                    GlobalConfigs.likeSkype = true;

                    if (config.has("skypename"))
                    {
                        String skypename = config.getString("skypename");
                        Bitmap thumbnail = ProfileImages.getSkypeProfileBitmap(context, skypename);

                        if (thumbnail != null)
                        {
                            thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                            icon.setImageDrawable(new BitmapDrawable(context.getResources(),thumbnail));
                            icon.setVisibility(VISIBLE);
                            targetIcon = overicon;
                        }
                    }
                }

                if (type.equals("webframe"))
                {
                    if (config.has("name"))
                    {
                        String name = config.getString("name");

                        label.setText(WebFrame.getConfigLabel(context, name));
                        setVisibility(VISIBLE);

                        icon.setImageDrawable(WebFrame.getConfigIconDrawable(context, name));
                        icon.setVisibility(VISIBLE);

                        targetIcon = overicon;
                    }
                }

                if (type.equals("webradio"))
                {
                    if (config.has("name"))
                    {
                        String name = config.getString("name");

                        label.setText(WebRadio.getConfigLabel(context, name));
                        setVisibility(VISIBLE);

                        icon.setImageDrawable(WebRadio.getConfigIconDrawable(context, name));
                        icon.setVisibility(VISIBLE);

                        targetIcon = overicon;
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
            Drawable appIcon = VersionUtils.getIconFromApplication(context, packageName);

            if (appIcon != null)
            {
                targetIcon.setImageDrawable(appIcon);
            }
            else
            {
                Bitmap thumbnail = StaticUtils.getIconFromAppStore(context, packageName);

                if (thumbnail != null)
                {
                    targetIcon.setImageDrawable(new BitmapDrawable(context.getResources(),thumbnail));
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

    @Override
    public void onStartingActivity(Object obj)
    {
        Log.d(LOGTAG, "onStartingActivity");

        handler.postDelayed(startingActivity, 10);
    }

    private final Runnable startingActivity = new Runnable()
    {
        @Override
        public void run()
        {
            spinner.setVisibility(VISIBLE);

            Log.d(LOGTAG, "startingActivity");
        }
    };

    @Override
    public void onFinishedActivity(Object obj)
    {
        Log.d(LOGTAG, "onFinishedActivity");

        handler.postDelayed(finishedActivity, 10);
    }

    private final Runnable finishedActivity = new Runnable()
    {
        @Override
        public void run()
        {
            spinner.setVisibility(INVISIBLE);

            Log.d(LOGTAG, "finishedActivity");
        }
    };

    private void onMyClick()
    {
        if (config == null)
        {
            Toast.makeText(getContext(),"Nix configured.",Toast.LENGTH_LONG).show();

            return;
        }

        String type = null;

        try
        {
            if (config.has("audiourl"))
            {
                type = "audioplayer";
            }

            if (config.has("type"))
            {
                type = config.getString("type");
            }
        }
        catch (JSONException ignore)
        {
        }

        if (type == null)
        {
            Toast.makeText(getContext(),"Nix <type> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        // @formatter:off
        if (type.equals("select_home"  )) { launchSelectHome();   return; }
        if (type.equals("select_assist")) { launchSelectAssist(); return; }
        if (type.equals("audioplayer"  )) { launchAudioPlayer();  return; }
        if (type.equals("genericapp"   )) { launchGenericApp();   return; }
        if (type.equals("directory"    )) { launchDirectory();    return; }
        if (type.equals("developer"    )) { launchDeveloper();    return; }
        if (type.equals("settings"     )) { launchSettings();     return; }
        if (type.equals("install"      )) { launchInstall();      return; }
        if (type.equals("webframe"     )) { launchWebframe();     return; }
        if (type.equals("webradio"     )) { launchWebradio();     return; }
        if (type.equals("whatsapp"     )) { launchWhatsApp();     return; }
        if (type.equals("skype"        )) { launchSkype();        return; }

        // @formatter:on

        Toast.makeText(getContext(),"Nix launcher type <" + type + "> configured.",Toast.LENGTH_LONG).show();
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

    private void launchInstall()
    {
        if (! config.has("packagename"))
        {
            Toast.makeText(getContext(),"Nix <packagename> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String packagename = config.getString("packagename");

            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packagename, 0);

            if (appInfo != null) launchGenericApp();
        }

        catch (Exception ignore)
        {
            //
            // Package is not installed.
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

            ((HomeActivity) context).kioskService.addOneShot(packagename);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Settings Password");

        final EditText input = new EditText(context);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setPadding(40, 40, 40, 40);
        input.setTextSize(48f);

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //m_Text = input.getText().toString();

                ((HomeActivity) context).addViewToBackStack(directory);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24f);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(24f);
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

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchWebradio()
    {
        if (! config.has("name"))
        {
            Toast.makeText(getContext(),"Nix <name> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            if (webradio == null)
            {
                String name = config.getString("name");

                webradio = new WebRadio(context);
                webradio.setName(name);
            }

            ((HomeActivity) context).addViewToBackStack(webradio);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchWebframe()
    {
        if (! config.has("name"))
        {
            Toast.makeText(getContext(),"Nix <name> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            if (webframe == null)
            {
                String name = config.getString("name");
                String url = WebFrame.getConfigUrl(context,name);

                webframe = new WebFrame(context);
                webframe.setLoadURL(name,url);
            }

            ((HomeActivity) context).addViewToBackStack(webframe);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchDeveloperOld1()
    {
        //StaticUtils.getAllInstalledApps(context);

        //StaticUtils.JSON2String(StaticUtils.getAllInstalledApps(context), true);

        /*
        String app_pkg_name = "marcone.toddlerlock";

        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + app_pkg_name));
        context.startActivity(intent);
        */

        /*
        Intent intentOpenBluetoothSettings = new Intent(Settings.ACTION_DATE_SETTINGS);
        context.startActivity(intentOpenBluetoothSettings);
        */

        /*
        Intent goToMarket = new Intent(Intent.ACTION_VIEW);
        goToMarket.setData(Uri.parse("market://details?id=org.wikipedia"));
        context.startActivity(goToMarket);
        */

        /*
        Intent goToMarket = new Intent(Intent.ACTION_VIEW);
        goToMarket.setData(Uri.parse("market://details?id=com.whatsapp"));
        context.startActivity(goToMarket);
        */
    }

    private void launchDeveloperException()
    {
        try
        {
            int x = 0;
            int y = 2 / x;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        /*
        String mypack = StaticUtils.getDefaultEmail(context);

        Log.d(LOGTAG,"Email=" + mypack);

        android.webkit.CookieManager wkCookieManager = android.webkit.CookieManager.getInstance();
        wkCookieManager.removeAllCookies(null);
        */
    }

    private void launchDeveloperVideo()
    {
        MediaPlayer mPlayer = new MediaPlayer();

        try
        {
            mPlayer.setDataSource(context,Uri.parse("http://daserste_live-lh.akamaihd.net/i/daserste_de@91204/index_320_av-p.m3u8?sd=10&rebase=on"));

            mPlayer.setOnTimedTextListener(
                    new MediaPlayer.OnTimedTextListener()
                    {
                        @Override
                        public void onTimedText(MediaPlayer mp, TimedText text)
                        {
                            Log.d("PLLLLLLL", text.getText());
                        }

                    });
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        try
        {
            mPlayer.prepare();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        mPlayer.start();
    }

    private void launchAudioPlayer()
    {
        if (! config.has("audiourl"))
        {
            Toast.makeText(getContext(),"Nix <audiourl> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String audiourl = config.getString("audiourl");

            if (handler == null) handler = new Handler();

            if (spinner == null)
            {
                spinner = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
                spinner.setPadding(40, 40, 40, 80);
                spinner.getIndeterminateDrawable().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
                spinner.setVisibility(INVISIBLE);
            }

            if (spinner.getParent() != null)
            {
                ((ViewGroup) spinner.getParent()).removeView(spinner);
            }

            this.addView(spinner);

            if (proxyPlayer == null) proxyPlayer = new ProxyPlayer();

            proxyPlayer.setCallback(this);
            proxyPlayer.setAudioUrl(context,audiourl);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchDeveloper()
    {
    }
}
