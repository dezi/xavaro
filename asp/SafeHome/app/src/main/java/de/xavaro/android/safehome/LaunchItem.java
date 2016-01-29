package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.os.Environment;
import android.provider.MediaStore;

import android.graphics.Color;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;

import android.view.View;
import android.view.Gravity;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

//
// Launch item view on home screen.
//

public class LaunchItem extends FrameLayout implements
        DitUndDat.InternetState.Callback
{
    private final String LOGTAG = "LaunchItem";

    public static LaunchItem createLaunchItem(Context context, LaunchGroup parent, JSONObject config)
    {
        LaunchItem item = null;

        String type = Json.getString(config, "type");

        if (Simple.equals(type, "health"      )) item = new LaunchItemHealth(context);
        if (Simple.equals(type, "alertcall"   )) item = new LaunchItemAlertcall(context);

        if (Simple.equals(type, "webconfig"   )) item = new LaunchItemWebFrame(context);
        if (Simple.equals(type, "webframe"    )) item = new LaunchItemWebFrame(context);

        if (Simple.equals(type, "ipradio"     )) item = new LaunchItemWebStream(context);
        if (Simple.equals(type, "iptelevision")) item = new LaunchItemWebStream(context);
        if (Simple.equals(type, "audioplayer" )) item = new LaunchItemWebStream(context);
        if (Simple.equals(type, "videoplayer" )) item = new LaunchItemWebStream(context);

        if (Simple.equals(type, "select"      )) item = new LaunchItemAdmin(context);
        if (Simple.equals(type, "install"     )) item = new LaunchItemAdmin(context);
        if (Simple.equals(type, "settings"    )) item = new LaunchItemAdmin(context);
        if (Simple.equals(type, "developer"   )) item = new LaunchItemAdmin(context);

        if (item == null) item = new LaunchItem(context);

        item.setConfig(parent, config);

        return item;
    }

    protected Context context;
    protected Handler handler;

    protected LayoutParams layout;
    protected LayoutParams oversize;

    protected LaunchGroup parent;
    protected JSONObject config;
    protected String type;
    protected String subtype;

    protected TextView label;
    protected ImageView icon;

    protected FrameLayout dimmer;
    protected FrameLayout overlay;
    protected ImageView overicon;
    protected LaunchGroup directory;

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

        layout = new LayoutParams(0, 0);
        setLayoutParams(layout);

        icon = new ImageView(context);
        icon.setPadding(0, 0, 0, 40);
        this.addView(icon);

        label = new TextView(context);
        label.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        label.setPadding(5, 5, 5, 5);
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        label.setTextSize(18f);
        this.addView(label);

        oversize = new LayoutParams(0, 0);
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

    public void setSize(int width, int height)
    {
        layout.width = width;
        layout.height = height;

        oversize.width = layout.width / 4;
        oversize.height = layout.height / 4;
    }

    public void setPosition(int left, int top)
    {
        layout.leftMargin = left;
        layout.topMargin = top;
    }

    public void setConfig(LaunchGroup parent, JSONObject config)
    {
        this.config = config;
        this.parent = parent;

        if (config.has("label")) label.setText(Json.getString(config, "label"));

        setBackgroundResource(R.drawable.shadow_black_400x400);
        setVisibility(VISIBLE);

        type = config.has("type") ? Json.getString(config, "type") : null;
        subtype = config.has("subtype") ? Json.getString(config, "subtype") : null;

        if (config.has("icon"))
        {
            String iconname = Json.getString(config, "icon");

            if (Simple.startsWith(iconname, "http://") || Simple.startsWith(iconname, "https://"))
            {
                Bitmap thumbnail = CacheManager.cacheThumbnail(context, iconname);

                if (thumbnail != null)
                {
                    icon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                    icon.setVisibility(VISIBLE);
                }
            }
            else
            {
                int resourceId = context.getResources().getIdentifier(iconname, "drawable", context.getPackageName());

                if (resourceId > 0)
                {
                    icon.setImageResource(resourceId);
                    icon.setVisibility(VISIBLE);
                }
            }
        }

        this.setConfig();
    }

    protected void setConfig()
    {
        String packageName = null;
        boolean hasProblem = false;
        ImageView targetIcon = icon;

        try
        {
            if (config.has("packagename"))
            {
                packageName = config.getString("packagename");

                GlobalConfigs.weLikeThis(context, packageName);
            }

            if (type.equals("xavaro"))
            {
                if (config.has("icontype"))
                {
                    String icontype = config.getString("icontype");

                    if (icontype.equals("user"))
                    {
                        icon.setImageResource(GlobalConfigs.IconResChatUser);
                        icon.setVisibility(VISIBLE);
                    }

                    if (icontype.equals("group"))
                    {
                        icon.setImageResource(GlobalConfigs.IconResChatGroup);
                        icon.setVisibility(VISIBLE);
                    }
                }
                else
                {
                    icon.setImageResource(GlobalConfigs.IconResCommunication);
                    icon.setVisibility(VISIBLE);
                }
            }

            if (type.equals("phone"))
            {
                if (config.has("phonenumber"))
                {
                    targetIcon = icon;

                    String phone = config.getString("phonenumber");
                    Bitmap thumbnail = ProfileImages.getWhatsAppProfileBitmap(context, phone);

                    if (thumbnail != null)
                    {
                        thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                        icon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                        icon.setVisibility(VISIBLE);
                        targetIcon = overicon;
                    }

                    if (config.has("subtype"))
                    {
                        String subtype = config.getString("subtype");

                        if (subtype.equals("text"))
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResPhoneAppText);
                            targetIcon.setVisibility(VISIBLE);
                        }
                        if (subtype.equals("voip"))
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResPhoneAppCall);
                            targetIcon.setVisibility(VISIBLE);
                        }
                    }
                }
                else
                {
                    icon.setImageResource(GlobalConfigs.IconResPhoneApp);
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

                        icon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                        icon.setVisibility(VISIBLE);
                        targetIcon = overicon;
                    }

                    if (config.has("subtype"))
                    {
                        String subtype = config.getString("subtype");

                        if (subtype.equals("chat"))
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResWhatsAppChat);
                            targetIcon.setVisibility(VISIBLE);
                        }
                        if (subtype.equals("voip"))
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResWhatsAppVoip);
                            targetIcon.setVisibility(VISIBLE);
                        }
                    }
                }
                else
                {
                    icon.setImageResource(GlobalConfigs.IconResWhatsApp);
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

                        icon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                        icon.setVisibility(VISIBLE);
                        targetIcon = overicon;
                    }

                    if (config.has("subtype"))
                    {
                        String subtype = config.getString("subtype");

                        if (subtype.equals("chat"))
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResSkypeChat);
                            targetIcon.setVisibility(VISIBLE);
                        }
                        if (subtype.equals("voip"))
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResSkypeVoip);
                            targetIcon.setVisibility(VISIBLE);
                        }
                        if (subtype.equals("vica"))
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResSkypeVica);
                            targetIcon.setVisibility(VISIBLE);
                        }
                    }
                }
                else
                {
                    icon.setImageResource(GlobalConfigs.IconResSkype);
                    icon.setVisibility(VISIBLE);
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
                Bitmap thumbnail = CacheManager.getIconFromAppStore(context, packageName);

                if (thumbnail != null)
                {
                    targetIcon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                }
                else
                {
                    targetIcon.setImageResource(R.drawable.stop_512x512);
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

        if (!DitUndDat.InternetState.isConnected)
        {
            if (type.equals("webframe")
                    || type.equals("audioplayer")
                    || type.equals("videoplayer"))
            {
                dimmer.setBackgroundColor(0xcccccccc);
            }
        }
    }

    protected void onMyOverlayClick()
    {
        //
        // To be overridden...
        //
    }

    protected void onMyClick()
    {
        if (config == null)
        {
            Toast.makeText(getContext(), "Nix configured.", Toast.LENGTH_LONG).show();

            return;
        }

        if (type == null)
        {
            Toast.makeText(getContext(), "Nix <type> configured.", Toast.LENGTH_LONG).show();

            return;
        }

        // @formatter:off
        if (type.equals("genericapp"   )) { launchGenericApp();     return; }
        if (type.equals("directory"    )) { launchDirectory();      return; }
        if (type.equals("whatsapp"     )) { launchWhatsApp();       return; }
        if (type.equals("phone"        )) { launchPhone();          return; }
        if (type.equals("xavaro"       )) { launchXavaro();         return; }
        if (type.equals("skype"        )) { launchSkype();          return; }
        // @formatter:on

        Toast.makeText(getContext(), "Nix launcher type <" + type + "> configured.", Toast.LENGTH_LONG).show();
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

    private void launchXavaro()
    {
        if (config.has("subtype"))
        {
            try
            {
                String subtype = config.getString("subtype");
                String ident = config.getString("identity");

                if (subtype.equals("chat"))
                {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("idremote", ident);
                    context.startActivity(intent);
                }
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null)
        {
            directory = new AppsGroup.XavaroGroup(context);
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

    protected void launchGenericApp()
    {
        if (!config.has("packagename"))
        {
            Toast.makeText(getContext(), "Nix <packagename> configured.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            String packagename = config.getString("packagename");

            ProcessManager.launchApp(context, packagename);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void launchDirectory()
    {
        if (!config.has("launchgroup"))
        {
            Toast.makeText(getContext(), "Nix <launchgroup> configured.", Toast.LENGTH_LONG).show();

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroup(context);

            try
            {
                directory.setConfig(this, config.getJSONObject("launchgroup"));
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}