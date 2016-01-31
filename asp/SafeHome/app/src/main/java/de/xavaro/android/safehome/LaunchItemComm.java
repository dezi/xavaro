package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

public class LaunchItemComm extends LaunchItem
{
    private final static String LOGTAG = LaunchItemComm.class.getSimpleName();

    public LaunchItemComm(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        ImageView targetIcon = icon;

        if (type.equals("phone"))
        {
            if (config.has("phonenumber"))
            {
                String phone = Json.getString(config, "phonenumber");
                Bitmap thumbnail = ProfileImages.getWhatsAppProfileBitmap(context, phone);

                if (thumbnail != null)
                {
                    thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                    icon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                    targetIcon = overicon;
                }

                if (Simple.equals(subtype, "text"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResPhoneAppText);
                }
                if (Simple.equals(subtype, "voip"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResPhoneAppCall);
                }
            }
            else
            {
                icon.setImageResource(GlobalConfigs.IconResPhoneApp);
            }
        }

        if (type.equals("skype"))
        {
            GlobalConfigs.likeSkype = true;

            if (config.has("skypename"))
            {
                String skypename = Json.getString(config, "skypename");
                Bitmap thumbnail = ProfileImages.getSkypeProfileBitmap(context, skypename);

                if (thumbnail != null)
                {
                    thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                    icon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                    targetIcon = overicon;
                }

                if (Simple.equals(subtype, "chat"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResSkypeChat);
                }
                if (Simple.equals(subtype, "voip"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResSkypeVoip);
                }
                if (Simple.equals(subtype, "vica"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResSkypeVica);
                }
            }
            else
            {
                icon.setImageResource(GlobalConfigs.IconResSkype);
            }
        }

        if (type.equals("whatsapp"))
        {
            GlobalConfigs.likeWhatsApp = true;

            if (config.has("waphonenumber"))
            {
                String phone = Json.getString(config, "waphonenumber");
                Bitmap thumbnail = ProfileImages.getWhatsAppProfileBitmap(context, phone);

                if (thumbnail != null)
                {
                    thumbnail = StaticUtils.getCircleBitmap(thumbnail);

                    icon.setImageDrawable(new BitmapDrawable(context.getResources(), thumbnail));
                    targetIcon = overicon;
                }

                if (Simple.equals(subtype, "chat"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResWhatsAppChat);
                }
                if (Simple.equals(subtype, "voip"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResWhatsAppVoip);
                }
            }
            else
            {
                icon.setImageResource(GlobalConfigs.IconResWhatsApp);
            }
        }

        if (type.equals("xavaro"))
        {
            if (config.has("subtype"))
            {
                if (config.has("chattype"))
                {
                    String chattype = Json.getString(config, "chattype");

                    if (Simple.equals(chattype, "user"))
                    {
                        icon.setImageResource(GlobalConfigs.IconResCommChatUser);
                    }
                    if (Simple.equals(chattype, "group"))
                    {
                        icon.setImageResource(GlobalConfigs.IconResCommChatGroup);
                    }
                }
            }
            else
            {
                icon.setImageResource(GlobalConfigs.IconResCommunication);
                icon.setVisibility(VISIBLE);
            }
        }

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("phone"   )) launchPhone();
        if (type.equals("skype"   )) launchSkype();
        if (type.equals("xavaro"  )) launchXavaro();
        if (type.equals("whatsapp")) launchWhatsApp();
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
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null)  directory = new LaunchGroupComm.PhoneGroup(context);

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
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null) directory = new LaunchGroupComm.SkypeGroup(context);

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchWhatsApp()
    {
        if (config.has("waphonenumber"))
        {
            try
            {
                String waphonenumber = Json.getString(config, "waphonenumber");
                Uri uri = Uri.parse("smsto:" + waphonenumber);
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.setPackage("com.whatsapp");
                context.startActivity(Intent.createChooser(sendIntent, ""));
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null) directory = new LaunchGroupComm.WhatsappGroup(context);

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchXavaro()
    {
        if (config.has("identity"))
        {
            try
            {
                String ident = Json.getString(config, "identity");

                if (subtype.equals("chat"))
                {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("idremote", ident);
                    context.startActivity(intent);
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null) directory = new LaunchGroupComm.XavaroGroup(context);

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}