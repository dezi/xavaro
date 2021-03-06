package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;

import java.io.File;

import de.xavaro.android.common.ImageSmartView;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

public class LaunchItemComm extends LaunchItemNotify
{
    private final static String LOGTAG = LaunchItemComm.class.getSimpleName();

    public LaunchItemComm(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        ImageSmartView targetIcon = icon;

        if (type.equals("phone"))
        {
            if (config.has("phonenumber"))
            {
                String phone = Json.getString(config, "phonenumber");
                File profile = ProfileImages.getProfileFile(phone);

                if ((profile != null) && ! isNoProfile())
                {
                    targetIcon.setImageResource(profile.toString(), true);
                    targetIcon = overicon;
                }

                if (Simple.equals(subtype, "text"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResPhoneAppText);
                    if (isNoProfile()) labelText = "SMS – Nachricht";
                }
                if (Simple.equals(subtype, "voip"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResPhoneAppCall);
                    if (isNoProfile()) labelText = "Anrufen";
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
                File profile = ProfileImages.getProfileFile(skypename);

                if ((profile != null) && ! isNoProfile())
                {
                    targetIcon.setImageResource(profile.toString(), true);
                    targetIcon = overicon;
                }

                if (Simple.equals(subtype, "chat"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResSkypeChat);
                    if (isNoProfile()) labelText = "Skype – Chat";
                }

                if (Simple.equals(subtype, "voip"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResSkypeVoip);
                    if (isNoProfile()) labelText = "Skype – Anruf";
                }

                if (Simple.equals(subtype, "vica"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResSkypeVica);
                    if (isNoProfile()) labelText = "Skype – Video";
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
                File profile = ProfileImages.getProfileFile(phone);

                if ((profile != null) && ! isNoProfile())
                {
                    targetIcon.setImageResource(profile.toString(), true);
                    targetIcon = overicon;
                }

                if (Simple.equals(subtype, "chat"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResWhatsAppChat);
                    if (isNoProfile()) labelText = "WhatsApp – Chat";
                }
                if (Simple.equals(subtype, "voip"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResWhatsAppVoip);
                    if (isNoProfile()) labelText = "WhatsApp – Anruf";
                }
            }
            else
            {
                icon.setImageResource(GlobalConfigs.IconResWhatsApp);
            }
        }

        if (type.equals("contacts"))
        {
            icon.setImageResource(GlobalConfigs.IconResContacts);
        }

        if ((targetIcon == overicon) && ! isNoFunction()) overlay.setVisibility(VISIBLE);

        Simple.makePost(onNotification);
    }

    @Override
    protected void onMyClick()
    {
        if (isNoFunction() || (totalnews != mainnews))
        {
            launchContacts();
        }
        else
        {
            if (type.equals("phone")) launchPhone();
            if (type.equals("skype")) launchSkype();
            if (type.equals("whatsapp")) launchWhatsApp();
            if (type.equals("contacts")) launchContacts();
        }
    }

    protected boolean onMyLongClick()
    {
        Simple.makeClick();

        if (! launchContacts()) onMyClick();

        return true;
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (super.onExecuteVoiceIntent(voiceintent, index))
        {
            if (type.equals("phone"   )) launchPhone();
            if (type.equals("skype"   )) launchSkype();
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

    protected boolean launchContacts()
    {
        JSONArray launchItems = Json.getArray(config, "launchitems");

        if (launchItems != null)
        {
            LaunchGroup directory = new LaunchGroup(getContext());
            directory.setConfig(null, launchItems);

            String label = Json.getString(config, "label") + " – " + "Kontaktmöglichkeiten";
            if (Simple.equals(type, "contacts")) label = "Weitere Kontakte";

            ((HomeActivity) getContext()).addWorkerToBackStack(label, directory);

            return true;
        }

        return false;
    }
}
