package de.xavaro.android.safehome;

import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.support.annotation.Nullable;

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

import android.net.Uri;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;

import android.view.SurfaceHolder;
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

import java.util.ArrayList;
import java.util.List;

//
// Launch item view on home screen.
//

public class LaunchItem extends FrameLayout implements
        ProxyPlayer.Callback,
        SurfaceHolder.Callback,
        DitUndDat.InternetState.Callback
{
    private final String LOGTAG = "LaunchItem";

    private Context context;
    private Handler handler;

    private LayoutParams layout;
    private LayoutParams oversize;

    private LaunchGroup parent;
    private JSONObject config;
    private JSONObject settings;

    private TextView label;
    private ImageView icon;

    private FrameLayout dimmer;
    private FrameLayout overlay;
    private ImageView overicon;

    private String type;
    private LaunchGroup directory;
    private WebFrame webframe;

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
        overlay.setVisibility(INVISIBLE);
        this.addView(overlay);

        overicon = new DitUndDat.ImageAntiAliasView(context);
        overlay.addView(overicon);

        dimmer = new FrameLayout(context);
        dimmer.setBackgroundColor(Color.TRANSPARENT);
        this.addView(dimmer);

        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onMyClick();
            }
        });

        overlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onMyOverlayClick();
            }
        });

        DitUndDat.InternetState.subscribe(this);
    }

    @Nullable
    public LaunchGroup getLaunchGroup()
    {
        return this.parent;
    }

    public void setSize(int width,int height)
    {
        layout.width  = width;
        layout.height = height;

        oversize.width  = layout.width  / 4;
        oversize.height = layout.height / 4;
    }

    public void setPosition(int left,int top)
    {
        layout.leftMargin = left;
        layout.topMargin  = top;
    }

    public void setConfig(LaunchGroup parent,JSONObject config)
    {
        this.config = config;
        this.parent = parent;

        String packageName = null;
        boolean hasProblem = false;
        ImageView targetIcon = icon;

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

            if (! config.has("type"))
            {
                if (config.has("audiourl")) type = "audioplayer";
                if (config.has("videourl")) type = "videoplayer";
            }
            else
            {
                type = config.getString("type");

                if (type.equals("select_home"))
                {
                    packageName = DitUndDat.DefaultApps.getDefaultHome(context);

                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSelectHome));
                    icon.setVisibility(VISIBLE);
                    targetIcon = overicon;
                }

                if (type.equals("select_assist"))
                {
                    packageName = DitUndDat.DefaultApps.getDefaultAssist(context);

                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSelectAssist));
                    icon.setVisibility(VISIBLE);
                    targetIcon = overicon;
                }

                if (type.equals("developer"))
                {
                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResTesting));
                    icon.setVisibility(VISIBLE);
                }

                if (type.equals("settings") && config.has("subtype"))
                {
                    String subtype = config.getString("subtype");

                    if (subtype.equals("safehome"))
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSettingsSafehome));
                        icon.setVisibility(VISIBLE);
                    }

                    if (subtype.equals("android"))
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSettingsAndroid));
                        icon.setVisibility(VISIBLE);
                    }
                }

                if (type.equals("firewall"))
                {
                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResFireWall));
                    icon.setVisibility(VISIBLE);
                }

                if (type.equals("iptelevision"))
                {
                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResIPTelevision));
                    icon.setVisibility(VISIBLE);
                }

                if (type.equals("ipradio"))
                {
                    icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResIPRadio));
                    icon.setVisibility(VISIBLE);
                }

                if (type.equals("webconfig") && config.has("subtype"))
                {
                    String subtype = config.getString("subtype");

                    if (subtype.equals("newspaper"))
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWebConfigNewspaper));
                        icon.setVisibility(VISIBLE);
                    }

                    if (subtype.equals("magazine"))
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWebConfigMagazine));
                        icon.setVisibility(VISIBLE);
                    }

                    if (subtype.equals("pictorial"))
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWebConfigPictorial));
                        icon.setVisibility(VISIBLE);
                    }

                    if (subtype.equals("shopping"))
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWebConfigShopping));
                        icon.setVisibility(VISIBLE);
                    }

                    if (subtype.equals("erotics"))
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWebConfigErotics));
                        icon.setVisibility(VISIBLE);
                    }
                }
                if (type.equals("phone"))
                {
                    GlobalConfigs.likeWhatsApp = true;

                    if (config.has("phonenumber"))
                    {
                        targetIcon = icon;

                        String phone = config.getString("phonenumber");
                        Bitmap thumbnail = ProfileImages.getWhatsAppProfileBitmap(context, phone);

                        if (thumbnail != null)
                        {
                            thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                            icon.setImageDrawable(new BitmapDrawable(context.getResources(),thumbnail));
                            icon.setVisibility(VISIBLE);
                            targetIcon = overicon;
                        }

                        if (config.has("subtype"))
                        {
                            String subtype = config.getString("subtype");

                            if (subtype.equals("text"))
                            {
                                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResPhoneAppText));
                                targetIcon.setVisibility(VISIBLE);
                            }
                            if (subtype.equals("voip"))
                            {
                                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResPhoneAppCall));
                                targetIcon.setVisibility(VISIBLE);
                            }
                        }
                    }
                    else
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResPhoneApp));
                        icon.setVisibility(VISIBLE);
                    }
                }

                if (type.equals("whatsapp"))
                {
                    GlobalConfigs.likeWhatsApp = true;

                    if (config.has("waphonenumber"))
                    {
                        targetIcon = icon;

                        String phone = config.getString("waphonenumber");
                        Bitmap thumbnail = ProfileImages.getWhatsAppProfileBitmap(context, phone);

                        if (thumbnail != null)
                        {
                            thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                            icon.setImageDrawable(new BitmapDrawable(context.getResources(),thumbnail));
                            icon.setVisibility(VISIBLE);
                            targetIcon = overicon;
                        }

                        if (config.has("subtype"))
                        {
                            String subtype = config.getString("subtype");

                            if (subtype.equals("chat"))
                            {
                                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWhatsAppChat));
                                targetIcon.setVisibility(VISIBLE);
                            }
                            if (subtype.equals("voip"))
                            {
                                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWhatsAppVoip));
                                targetIcon.setVisibility(VISIBLE);
                            }
                        }
                    }
                    else
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResWhatsApp));
                        icon.setVisibility(VISIBLE);
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

                        if (config.has("subtype"))
                        {
                            String subtype = config.getString("subtype");

                            if (subtype.equals("chat"))
                            {
                                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSkypeChat));
                                targetIcon.setVisibility(VISIBLE);
                            }
                            if (subtype.equals("voip"))
                            {
                                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSkypeVoip));
                                targetIcon.setVisibility(VISIBLE);
                            }
                            if (subtype.equals("vica"))
                            {
                                targetIcon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSkypeVica));
                                targetIcon.setVisibility(VISIBLE);
                            }
                        }
                    }
                    else
                    {
                        icon.setImageDrawable(VersionUtils.getDrawableFromResources(context, GlobalConfigs.IconResSkype));
                        icon.setVisibility(VISIBLE);
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

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);

        setBackgroundResource(hasProblem ? R.drawable.shadow_alert_400x400 : R.drawable.shadow_black_400x400);
    }

    //region Media playback stuff.

    //
    // Flag if media playing is active here.
    //

    private boolean isPlayingMedia;
    private boolean isPlayingAudio;
    private boolean isPlayingVideo;

    //
    // Bubble up launch items for player control.
    //

    private ArrayList<LaunchItem> isPlayingParents = new ArrayList<>();

    //region ProxyPlayer.Callback interface.

    public void onPlaybackPrepare()
    {
        clearAndPost(playbackPrepare);
    }

    public void onPlaybackStartet()
    {
        clearAndPost(playbackStartet);
    }

    public void onPlaybackPaused()
    {
        clearAndPost(playbackPaused);
    }

    public void onPlaybackResumed()
    {
        clearAndPost(playbackResumed);
    }

    public void onPlaybackFinished()
    {
        clearAndPost(playbackFinished);
    }

    public void onPlaybackMeta(String meta)
    {
        Log.d(LOGTAG, "onPlaybackMeta: " + meta);
    }

    private void clearAndPost(Runnable start)
    {
        handler.removeCallbacks(playbackPrepare);
        handler.removeCallbacks(playbackStartet);
        handler.removeCallbacks(playbackPaused);
        handler.removeCallbacks(playbackResumed);
        handler.removeCallbacks(playbackFinished);

        handler.postDelayed(start,5);
    }

    //
    // Required handlers for thread change.
    //

    private final Runnable playbackPrepare = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackPrepare");

            for (LaunchItem li : isPlayingParents)
            {
                li.setPlaybackPrepare();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackPrepare();
            }
        }
    };

    private final Runnable playbackStartet = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackStartet");

            for (LaunchItem li : isPlayingParents)
            {
                li.setPlaybackStartet();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackStartet();
            }
        }
    };

    private final Runnable playbackPaused = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackPaused");

            for (LaunchItem li : isPlayingParents)
            {
                li.setPlaybackPaused();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackPaused();
            }
        }
    };

    private final Runnable playbackResumed = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackResumed");

            for (LaunchItem li : isPlayingParents)
            {
                li.setPlaybackResumed();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackResumed();
            }
        }
    };

    private final Runnable playbackFinished = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "playbackFinished");

            for (LaunchItem li : isPlayingParents)
            {
                li.setPlaybackFinished();
            }

            if (isPlayingVideo)
            {
                VideoSurface.getInstance().onPlaybackFinished();
            }
        }
    };

    //endregion ProxiPlayer callback interface.

    //region Media playback control.

    public void setPlaybackPrepare()
    {
        Log.d(LOGTAG,"setPlaybackPrepare:" + label.getText());

        isPlayingMedia = true;

        setSpinner(true);
    }

    public void setPlaybackStartet()
    {
        Log.d(LOGTAG,"setPlaybackStartet:" + label.getText());

        setSpinner(false);

        oversize.width  = layout.width  / 3;
        oversize.height = layout.height / 3;

        overicon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.player_pause_190x190));
        overicon.setVisibility(VISIBLE);
        overlay.setVisibility(VISIBLE);
    }

    public void setPlaybackPaused()
    {
        overicon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.player_play_190x190));
    }

    public void setPlaybackResumed()
    {
        overicon.setImageDrawable(VersionUtils.getDrawableFromResources(context, R.drawable.player_pause_190x190));
    }

    public void setPlaybackFinished()
    {
        setSpinner(false);

        oversize.width  = layout.width  / 4;
        oversize.height = layout.height / 4;

        overicon.setVisibility(INVISIBLE);
        overlay.setVisibility(INVISIBLE);

        isPlayingMedia = false;
    }

    private ProgressBar spinner;

    private void setSpinner(boolean visible)
    {
        if ((spinner != null) && (spinner.getParent() != null))
        {
            ((ViewGroup) spinner.getParent()).removeView(spinner);
        }

        if (visible)
        {
            if (spinner == null)
            {
                spinner = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
                spinner.getIndeterminateDrawable().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
                spinner.setPadding(40, 40, 40, 80);
            }

            this.addView(spinner);
        }
    }

    //endregion Media playback control.

    public void onInternetChanged()
    {
        if (DitUndDat.InternetState.isWifi)
        {
            dimmer.setBackgroundColor(Color.TRANSPARENT);

            return;
        }

        if (DitUndDat.InternetState.isMobile)
        {
            if (type.equals("audioplayer") || type.equals("videoplayer"))
            {
                dimmer.setBackgroundColor(0xcccccccc);
            }
        }

        if (! DitUndDat.InternetState.isConnected)
        {
            if (type.equals("webframe")
                    || type.equals("audioplayer")
                    || type.equals("videoplayer"))
            {
                dimmer.setBackgroundColor(0xcccccccc);
            }
        }
    }

    private void onMyOverlayClick()
    {
        if (isPlayingMedia)
        {
            ProxyPlayer pp = ProxyPlayer.getInstance();

            if (pp.isPlaying())
            {
                pp.playerPause();
            }
            else
            {
                pp.playerResume();
            }
        }
    }

    private void onMyClick()
    {
        if (config == null)
        {
            Toast.makeText(getContext(),"Nix configured.",Toast.LENGTH_LONG).show();

            return;
        }

        if (type == null)
        {
            Toast.makeText(getContext(),"Nix <type> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        // @formatter:off
        if (type.equals("select_home"  )) { launchSelectHome();   return; }
        if (type.equals("select_assist")) { launchSelectAssist(); return; }
        if (type.equals("firewall"     )) { launchFireWall();     return; }
        if (type.equals("iptelevision" )) { launchIPTelevision(); return; }
        if (type.equals("ipradio"      )) { launchIPRadio();      return; }
        if (type.equals("webconfig"    )) { launchWebConfig();    return; }
        if (type.equals("audioplayer"  )) { launchAudioPlayer();  return; }
        if (type.equals("videoplayer"  )) { launchVideoPlayer();  return; }
        if (type.equals("genericapp"   )) { launchGenericApp();   return; }
        if (type.equals("directory"    )) { launchDirectory();    return; }
        if (type.equals("developer"    )) { launchDeveloper();    return; }
        if (type.equals("settings"     )) { launchSettings();     return; }
        if (type.equals("install"      )) { launchInstall();      return; }
        if (type.equals("webframe"     )) { launchWebframe();     return; }
        if (type.equals("whatsapp"     )) { launchWhatsApp();     return; }
        if (type.equals("phone"        )) { launchPhone();        return; }
        if (type.equals("skype"        )) { launchSkype();        return; }

        // @formatter:on

        Toast.makeText(getContext(),"Nix launcher type <" + type + "> configured.",Toast.LENGTH_LONG).show();
    }

    private void launchSelectHome()
    {
        DitUndDat.DefaultApps.setDefaultHome(context);
    }

    private void launchSelectAssist()
    {
        DitUndDat.DefaultApps.setDefaultAssist(context);
    }

    private void launchFireWall()
    {
    }

    private void launchPhone()
    {
        if (config.has("phonenumber"))
        {
            try
            {
                String phonenumber = config.getString("phonenumber");
                String subtype = config.has("subtype") ? config.getString("subtype") : "text";

                if (subtype.equals("text"))
                {
                    Uri uri = Uri.parse("smsto:" + phonenumber);
                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setPackage("com.android.mms");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }

                if (subtype.equals("voip"))
                {
                    Uri uri = Uri.parse("tel:" + phonenumber);
                    Intent sendIntent = new Intent(Intent.ACTION_CALL, uri);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setPackage("com.android.server.telecom");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            return;
        }

        if (directory == null)
        {
            directory = new AppsGroup.PhoneGroup(context);
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchWhatsApp()
    {
        if (config.has("waphonenumber"))
        {
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

            return;
        }

        if (directory == null)
        {
            directory = new AppsGroup.WhatsappGroup(context);
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchSkype()
    {
        if (config.has("skypename"))
        {
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

                if (subtype.equals("vica"))
                {
                    uri = Uri.parse("skype:" + skypename + "?call&video=true");
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

            return;
        }

        if (directory == null)
        {
            directory = new AppsGroup.SkypeGroup(context);
        }

        ((HomeActivity) context).addViewToBackStack(directory);
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

            HomeActivity.kioskService.addOneShot(packagename);

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
        if (! config.has("subtype"))
        {
            Toast.makeText(getContext(),"Nix <subtype> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String subtype = config.getString("subtype");

            if (subtype.equals("android"))
            {
                String packagename = "com.android.settings";
                HomeActivity.kioskService.addOneShot(packagename);
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packagename);
                context.startActivity(launchIntent);
            }

            if (subtype.equals("safehome"))
            {
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG,ex);
        }
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
                directory.setConfig(this,config.getJSONObject("launchgroup"));
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchIPTelevision()
    {
        if (directory == null)
        {
            directory = new WebStream(context, "webiptv");
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchIPRadio()
    {
        if (directory == null)
        {
            directory = new WebStream(context, "webradio");
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchWebConfig()
    {
        if (! config.has("subtype"))
        {
            Toast.makeText(getContext(),"Nix <subtype> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            if (directory == null)
            {
                String subtype = config.getString("subtype");

                directory = new WebStream(context, "webconfig", subtype);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        ((HomeActivity) context).addViewToBackStack(directory);
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
                String url=WebFrame.getConfigUrl(context, name);

                webframe = new WebFrame(context);
                webframe.setLoadURL(name, url);
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

    private void launchVideoPlayer()
    {
        if (! config.has("videourl"))
        {
            Toast.makeText(getContext(),"Nix <videourl> configured.",Toast.LENGTH_LONG).show();

            return;
        }

        if (handler == null) handler = new Handler();

        try
        {
            String videourl = config.getString("videourl");

            isPlayingParents = new ArrayList<>();

            LaunchItem bubble = this;

            while (bubble != null)
            {
                isPlayingParents.add(bubble);

                if (bubble.getLaunchGroup() == null) break;

                bubble = bubble.getLaunchGroup().getLaunchItem();
            }

            ProxyPlayer.getInstance().setVideoUrl(context, videourl, this);
            isPlayingVideo = true;

            VideoSurface.getInstance();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        ProxyPlayer.getInstance().setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
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

            isPlayingParents = new ArrayList<>();

            LaunchItem bubble = this;

            while (bubble != null)
            {
                isPlayingParents.add(bubble);

                if (bubble.getLaunchGroup() == null) break;

                bubble = bubble.getLaunchGroup().getLaunchItem();
            }

            ProxyPlayer.getInstance().setAudioUrl(context,audiourl,this);
            isPlayingAudio = true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchDeveloper()
    {
        //DitUndDat.SharedPrefs.sharedPrefs.edit().clear().commit();
    }
}
