package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Gravity;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;

import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.CommService;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotificationService;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PrepaidManager;
import de.xavaro.android.common.ProfileImages;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SimpleStorage;
import de.xavaro.android.common.SystemIdentity;
import de.xavaro.android.common.VoiceIntent;

public class LaunchItemCommXavaro extends LaunchItemComm implements
        CommService.CommServiceCallback,
        PrepaidManager.PrepaidManagerCashcodeCallback
{
    private final static String LOGTAG = LaunchItemCommXavaro.class.getSimpleName();

    private TextView prepaidDateView;
    private TextView prepaidMoneyView;

    public LaunchItemCommXavaro(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        ImageView targetIcon = icon;

        if (config.has("subtype"))
        {
            if (Json.equals(config, "subtype", "padm"))
            {
                icon.setImageResource(CommonConfigs.IconResPrepaidAssist);

                prepaidDateView = new TextView(getContext());
                prepaidDateView.setGravity(Gravity.CENTER_HORIZONTAL);
                prepaidDateView.setTypeface(null, Typeface.BOLD);
                prepaidDateView.setTextColor(Color.WHITE);

                addView(prepaidDateView);

                prepaidMoneyView = new TextView(getContext());
                prepaidMoneyView.setGravity(Gravity.CENTER_HORIZONTAL);
                prepaidMoneyView.setTypeface(null, Typeface.BOLD);
                prepaidMoneyView.setTextColor(Color.WHITE);

                addView(prepaidMoneyView);
            }
            else
            {
                String ident = Json.getString(config, "identity");
                File profile = ProfileImages.getProfileFile(ident);

                if ((profile != null) && ! isNoProfile())
                {
                    icon.setImageResource(profile.toString(), true);
                    targetIcon = overicon;
                }

                if (Json.equals(config, "chattype", "user"))
                {
                    if (! isNoFunction()) targetIcon.setImageResource(GlobalConfigs.IconResCommChatUser);
                    if (isNoProfile()) labelText = "Xavaro – Chat";
                }

                if (Json.equals(config, "chattype", "group"))
                {
                    if (Json.equals(config, "grouptype", "alertcall"))
                    {
                        String owner = Json.getString(config, "groupowner");
                        profile = ProfileImages.getProfileFile(owner);

                        if ((profile != null) && ! isNoProfile())
                        {
                            icon.setImageResource(profile.toString(), true);
                            targetIcon = overicon;
                        }

                        targetIcon.setImageResource(GlobalConfigs.IconResCommChatAlert);
                        if (isNoProfile()) labelText = "Assistenz – Gruppe";
                    }
                    else
                    {
                        targetIcon.setImageResource(GlobalConfigs.IconResCommChatGroup);
                        if (isNoProfile()) labelText = "Xavaro – Gruppe";
                    }
                }
            }
        }
        else
        {
            icon.setImageResource(GlobalConfigs.IconResCommunication);
        }

        if (targetIcon == overicon) overlay.setVisibility(VISIBLE);

        Simple.makePost(onNotification);
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);

        if (Json.equals(config, "subtype", "padm"))
        {
            //
            // Original font sizes based on 200 pixels height.
            //

            float scale = Simple.getNormalPixels(height - icon.getPaddingBottom()) / 200.0f;

            prepaidDateView.setTextSize(Simple.getDeviceTextSize(24f * scale));
            prepaidMoneyView.setTextSize(Simple.getDeviceTextSize(40f * scale));

            Simple.setPadding(prepaidDateView, 0, Math.round(20 * scale), 0, 0);
            Simple.setPadding(prepaidMoneyView, 0, Math.round(56 * scale), 0, 0);
        }
    }

    private void checkPrepaidBalance()
    {
        String owner = Json.getString(config, "groupowner");
        String pfix = "monitoring.prepaid.remote.";

        String mdate = Simple.getSharedPrefString(pfix + "stamp:" + owner);
        long mstamp = (mdate == null) ? 0 : Simple.getTimeStamp(mdate);

        if ((Simple.nowAsTimeStamp() - mstamp) < (86400 * 1000))
        {
            int money = Simple.getSharedPrefInt(pfix + "money:" + owner);

            onPrepaidReceived(money, mdate);
        }
        else
        {
            //
            // Request prepaid balance now.
            //

            Simple.makePost(new Runnable()
            {
                @Override
                public void run()
                {
                    launchXavaro();
                }
            });
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        if (Simple.equals(subtype, "padm"))
        {
            CommService.subscribeMessage(this, "recvPrepaidBalance");

            checkPrepaidBalance();
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if (Simple.equals(subtype, "padm"))
        {
            CommService.unsubscribeMessage(this, "recvPrepaidBalance");
        }
    }

    @Override
    protected boolean onMyLongClick()
    {
        return launchXavaroLong() || super.onMyLongClick();
    }

    @Override
    protected void onMyClick()
    {
        if (isNoFunction() || (totalnews != mainnews))
        {
            super.launchContacts();
        }
        else
        {
            launchXavaro();
        }
    }

    @Override
    public void onPrepaidCashcodeReceived(String cashcode)
    {
        String groupowner = Json.getString(config, "groupowner");

        JSONObject prepaidmess = new JSONObject();

        Json.put(prepaidmess, "type", "sendPrepaidBalance");
        Json.put(prepaidmess, "idremote", groupowner);
        Json.put(prepaidmess, "cashcode", cashcode);

        CommService.sendEncrypted(prepaidmess, true);

        Simple.makeToast("Der Cashcode wurde übertragen.");
        Simple.makeToast("Bitte einen Moment Gelduld.");
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (super.onExecuteVoiceIntent(voiceintent, index))
        {
            launchXavaro();

            return true;
        }

        return false;
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
                        String text = Json.getString(cbmessage, "text");
                        String ctxt = (text == null) ? "" : text.substring(1, text.length() - 1);
                        if (ctxt.endsWith(", OK")) ctxt = ctxt.substring(0, ctxt.length() - 4);

                        int money = Json.getInt(cbmessage, "money");

                        if (money >= 0)
                        {
                            String date = Json.getString(cbmessage, "date");
                            onPrepaidReceived(money, date);
                            if (! ctxt.isEmpty()) Simple.makeToast(ctxt);
                        }
                        else
                        {
                            String label = Json.getString(config, "label");
                            if (! ctxt.isEmpty()) Simple.makeAlert(ctxt, label);
                        }
                    }
                });
            }
        }
    }

    public void onPrepaidReceived(int money, String date)
    {
        long stamp = Simple.getTimeStamp(date);
        String dom = Simple.getLocalDayOfMonth(stamp) + ". " + Simple.getLocalMonth(stamp);
        prepaidDateView.setText(dom);

        String value = String.format("%.02f", money / 100f) + Simple.getCurrencySymbol();
        prepaidMoneyView.setText(value);
    }

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

                CommService.sendEncrypted(prepaidmess, true);

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

            return true;
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

                    return true;
                }
            }
        }

        return false;
    }
}
