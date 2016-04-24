package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import org.json.JSONObject;

import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SystemIdentity;
import de.xavaro.android.common.VoiceIntent;

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
                Drawable drawable = ProfileImages.getContactsProfileDrawable(phone, true);

                if (drawable != null)
                {
                    icon.setImageDrawable(drawable);
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
            CommonConfigs.likeSkype = true;

            if (config.has("skypename"))
            {
                String skypename = Json.getString(config, "skypename");
                Drawable thumbnail = ProfileImages.getSkypeProfileDrawable(skypename, true);

                if (thumbnail != null)
                {
                    icon.setImageDrawable(thumbnail);
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
            CommonConfigs.likeWhatsApp = true;

            if (config.has("waphonenumber"))
            {
                String phone = Json.getString(config, "waphonenumber");
                Drawable drawable = ProfileImages.getWhatsAppProfileDrawable(phone, true);

                if (drawable != null)
                {
                    icon.setImageDrawable(drawable);
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
                String ident = Json.getString(config, "identity");
                Drawable drawable = ProfileImages.getXavaroProfileDrawable(ident, true);

                if (drawable != null)
                {
                    icon.setImageDrawable(drawable);
                    targetIcon = overicon;
                }

                if (Json.equals(config, "chattype", "user"))
                {
                    targetIcon.setImageResource(GlobalConfigs.IconResCommChatUser);
                }

                if (Json.equals(config, "chattype", "group"))
                {
                    if (Json.equals(config, "grouptype", "alertcall"))
                    {
                        icon.setImageResource(GlobalConfigs.IconResCommChatAlert);
                    }
                    else
                    {
                        icon.setImageResource(GlobalConfigs.IconResCommChatGroup);
                    }
                }
            }
            else
            {
                icon.setImageResource(GlobalConfigs.IconResCommunication);
            }
        }

        if (type.equals("contacts"))
        {
            icon.setImageResource(GlobalConfigs.IconResContacts);
        }

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);
    }

    @Override
    protected boolean onMyLongClick()
    {
        if (type.equals("xavaro")) launchXavaroLong();

        return true;
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("phone"   )) launchPhone();
        if (type.equals("skype"   )) launchSkype();
        if (type.equals("xavaro"  )) launchXavaro();
        if (type.equals("whatsapp")) launchWhatsApp();
        if (type.equals("contacts")) launchContacts();
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (super.onExecuteVoiceIntent(voiceintent, index))
        {
            if (type.equals("phone"   )) launchPhone();
            if (type.equals("skype"   )) launchSkype();
            if (type.equals("xavaro"  )) launchXavaro();
            if (type.equals("whatsapp")) launchWhatsApp();
            if (type.equals("contacts")) launchContacts();

            return true;
        }

        return false;
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

                    ProcessManager.launchIntent(Intent.createChooser(sendIntent, ""));
                }

                if (subtype.equals("voip"))
                {
                    Uri uri = Uri.parse("tel:" + phonenumber);
                    Intent sendIntent = new Intent(Intent.ACTION_CALL, uri);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setPackage("com.android.server.telecom");

                    ProcessManager.launchIntent(Intent.createChooser(sendIntent, ""));
                }
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroupComm.PhoneGroup(context);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
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

                ProcessManager.launchIntent(skype);
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroupComm.SkypeGroup(context);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

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

                ProcessManager.launchIntent(sendIntent);
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }

            return;
        }

        if (directory == null)
        {
            directory = new LaunchGroupComm.WhatsappGroup(context);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

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

        if (directory == null)
        {
            directory = new LaunchGroupComm.XavaroGroup(context);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private boolean launchXavaroLong()
    {
        if (config.has("grouptype"))
        {
            if (Json.equals(config, "grouptype", "alertcall"))
            {
                //
                // Check if user is allowed to initiate a
                // skype call back from the remote side.
                //

                String groupidentity = Json.getString(config, "identity");
                String identity = SystemIdentity.getIdentity();
                String skypecallback = RemoteGroups.getSkypeCallback(groupidentity, identity);

                if (skypecallback != null)
                {
                    String groupowner = RemoteGroups.getGroupOwner(groupidentity);

                    JSONObject skypecall = new JSONObject();

                    Json.put(skypecall, "type", "skypeCallback");
                    Json.put(skypecall, "idremote", groupowner);
                    Json.put(skypecall, "groupidentity", groupidentity);
                    Json.put(skypecall, "skypecallback", skypecallback);

                    CommService.sendEncryptedReliable(skypecall, true);

                    Simple.makeToast("Der Skype Rückruf wurde übertragen. "
                            + "Bitte einen Moment Gelduld.");
                }
            }
        }

        return false;
    }

    private void launchContacts()
    {
        if (directory == null)
        {
            directory = new LaunchGroup(context);
            directory.setConfig(this, Json.getArray(config, "launchitems"));
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
