package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.ImageView;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;

import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PrepaidManager;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SystemIdentity;
import de.xavaro.android.common.VoiceIntent;

public class LaunchItemComm extends LaunchItem implements
        CommService.CommServiceCallback,
        PrepaidManager.PrepaidManagerCashcodeCallback
{
    private final static String LOGTAG = LaunchItemComm.class.getSimpleName();

    private TextView prepaidView;

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
                File profile = ProfileImages.getProfileFile(phone);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), true);
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
                File profile = ProfileImages.getProfileFile(skypename);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), true);
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
                File profile = ProfileImages.getProfileFile(phone);

                if (profile != null)
                {
                    icon.setImageResource(profile.toString(), true);
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
                if (Json.equals(config, "subtype", "padm"))
                {
                    String owner = Json.getString(config, "groupowner");
                    File profile = ProfileImages.getProfileFile(owner);

                    if (profile != null)
                    {
                        overicon.setImageResource(profile.toString(), true);
                        overlay.setVisibility(VISIBLE);
                    }

                    icon.setImageResource(CommonConfigs.IconResPrepaid);

                    prepaidView = new TextView(getContext());
                    prepaidView.setLayoutParams(Simple.layoutParamsMM());
                    prepaidView.setPadding(0, 20, 0, icon.getPaddingBottom() + 36);
                    prepaidView.setTextSize(Simple.getDeviceTextSize(40f));
                    prepaidView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                    prepaidView.setTextColor(Color.WHITE);
                    prepaidView.setTypeface(null, Typeface.BOLD);

                    addView(prepaidView);
                }
                else
                {
                    String ident = Json.getString(config, "identity");
                    File profile = ProfileImages.getProfileFile(ident);

                    if (profile != null)
                    {
                        icon.setImageResource(profile.toString(), true);
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
                            String owner = Json.getString(config, "groupowner");
                            profile = ProfileImages.getProfileFile(owner);

                            if (profile != null)
                            {
                                icon.setImageResource(profile.toString(), true);
                                targetIcon = overicon;
                            }

                            targetIcon.setImageResource(GlobalConfigs.IconResCommChatAlert);
                        }
                        else
                        {
                            targetIcon.setImageResource(GlobalConfigs.IconResCommChatGroup);
                        }
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
    public void onPrepaidCashcodeReceived(String cashcode)
    {
        String groupowner = Json.getString(config, "groupowner");

        JSONObject prepaidmess = new JSONObject();

        Json.put(prepaidmess, "type", "sendPrepaidBalance");
        Json.put(prepaidmess, "idremote", groupowner);
        Json.put(prepaidmess, "cashcode", cashcode);

        CommService.subscribeMessage(this, "recvPrepaidBalance");
        CommService.sendEncrypted(prepaidmess, true);
        Simple.makePost(unsubscribeMessage, 20 * 1000);

        Simple.makeToast("Der Cashcode wurde übertragen.");
        Simple.makeToast("Bitte einen Moment Gelduld.");
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

    @Override
    public void onMessageReceived(JSONObject message)
    {
        Log.d(LOGTAG, "onMessageReceived:" + message.toString());

        if (Json.equals(message, "type", "recvPrepaidBalance"))
        {
            String rmidentity = Json.getString(message, "identity");

            String gpidentity = Json.getString(config, "identity");
            String owidentity = Json.getString(config, "groupowner");

            Log.d(LOGTAG, "=========================== remote=" + rmidentity);
            Log.d(LOGTAG, "=========================== goup=" + gpidentity);
            Log.d(LOGTAG, "=========================== owner=" + owidentity);

            if (Simple.equals(rmidentity, gpidentity) || Simple.equals(rmidentity, owidentity))
            {
                //
                // This message belongs to this launch item.
                //

                final JSONObject cbmessage = message;

                Simple.makePost(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String label = Json.getString(config, "label");
                        String text = Json.getString(cbmessage, "text");
                        int money = Json.getInt(cbmessage, "money");

                        String ctxt = (text == null) ? "" : text.substring(1, text.length() - 1);
                        if (ctxt.endsWith(", OK")) ctxt = ctxt.substring(0, ctxt.length() - 4);

                        if (money >= 0)
                        {
                            onPrepaidReceived(money);

                            if (!ctxt.isEmpty()) Simple.makeToast(ctxt);
                        }
                        else
                        {
                            if (!ctxt.isEmpty()) Simple.makeAlert(ctxt, label);
                        }
                    }
                });

                Simple.removePost(unsubscribeMessage);
                Simple.makePost(unsubscribeMessage);
            }
        }
    }

    public void onPrepaidReceived(int money)
    {
        String value = String.format("%.02f", money / 100f) + Simple.getCurrencySymbol();
        prepaidView.setText(value);
    }

    private final Runnable unsubscribeMessage = new Runnable()
    {
        @Override
        public void run()
        {
            CommService.unsubscribeMessage(LaunchItemComm.this, "recvPrepaidBalance");
        }
    };

    private void launchXavaro()
    {
        if (config.has("identity"))
        {
            if (subtype.equals("chat"))
            {
                try
                {
                    String ident = Json.getString(config, "identity");
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("idremote", ident);
                    context.startActivity(intent);
                }
                catch (Exception ex)
                {
                    OopsService.log(LOGTAG, ex);
                }
            }

            if (subtype.equals("padm"))
            {
                String groupowner = Json.getString(config, "groupowner");

                JSONObject prepaidmess = new JSONObject();

                Json.put(prepaidmess, "type", "sendPrepaidBalance");
                Json.put(prepaidmess, "idremote", groupowner);

                CommService.subscribeMessage(this, "recvPrepaidBalance");
                CommService.sendEncrypted(prepaidmess, true);
                Simple.makePost(unsubscribeMessage, 20 * 1000);

                Simple.makeToast("Die Guthabenanfrage wurde übertragen.");
                Simple.makeToast("Bitte einen Moment Gelduld.");
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
        if (subtype.equals("padm"))
        {
            PrepaidManager.createPrepaidLoadDialog(this);
        }

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

                JSONObject member = RemoteGroups.getGroupMember(groupidentity, identity);

                String skypecallback = Json.getString(member, "skypecallback");
                boolean skypeenable = Json.getBoolean(member, "skypeenable");

                if (skypeenable && (skypecallback != null))
                {
                    String groupowner = RemoteGroups.getGroupOwner(groupidentity);

                    JSONObject skypecall = new JSONObject();

                    Json.put(skypecall, "type", "skypeCallback");
                    Json.put(skypecall, "idremote", groupowner);
                    Json.put(skypecall, "groupidentity", groupidentity);
                    Json.put(skypecall, "skypecallback", skypecallback);

                    CommService.sendEncrypted(skypecall, true);

                    Simple.makeToast("Der Skype Rückruf wurde übertragen.");
                    Simple.makeToast("Bitte einen Moment Gelduld.");
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
