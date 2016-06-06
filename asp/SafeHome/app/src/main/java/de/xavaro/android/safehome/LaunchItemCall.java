package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import org.json.JSONObject;

import de.xavaro.android.common.AssistanceMessage;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PrepaidManager;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;

public class LaunchItemCall extends LaunchItem implements
        PrepaidManager.PrepaidManagerBalanceCallback,
        PrepaidManager.PrepaidManagerCashcodeCallback,
        NotifyIntent.NotifyService
{
    private final static String LOGTAG = LaunchItemCall.class.getSimpleName();

    public LaunchItemCall(Context context)
    {
        super(context);
    }

    private TextView prepaidDateView;
    private TextView prepaidMoneyView;

    @Override
    protected void setConfig()
    {
        if (config.has("subitem"))
        {
            String iconurl = Json.getString(config, "icon");
            icon.setImageResource(iconurl);

            String subitem = Json.getString(config, "subitem");

            if (Simple.equals(subitem, "prepaid"))
            {
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

                String mdate = Simple.getSharedPrefString("monitoring.prepaid.stamp");
                long mstamp = (mdate == null) ? 0 : Simple.getTimeStamp(mdate);

                int money = Simple.getSharedPrefInt("monitoring.prepaid.money");
                onPrepaidReceived(money, mdate);

                if ((Simple.nowAsTimeStamp() - mstamp) >= (86400 * 1000))
                {
                    if (Simple.isSimReady())
                    {
                        Simple.makePost(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                launchCall();
                            }
                        });
                    }
                    else
                    {
                        prepaidMoneyView.setText("No SIM");
                    }
                }
            }
        }
        else
        {
            if (Simple.equals(subtype, "important"))
            {
                icon.setImageResource(GlobalConfigs.IconResCallImportant);
            }

            if (directory == null)
            {
                directory = new LaunchGroupCalls(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }
        }
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);

        if (Json.equals(config, "subitem", "prepaid"))
        {
            //
            // Original font sizes based on 200 pixels height.
            //

            float scale = height / 200.0f;

            prepaidDateView.setTextSize(Simple.getDeviceTextSize(22f * scale));
            prepaidMoneyView.setTextSize(Simple.getDeviceTextSize(40f * scale));

            prepaidDateView.setPadding(0, Math.round(28 * scale), 0, 0);
            prepaidMoneyView.setPadding(0, Math.round(56 * scale), 0, 0);
        }
    }

    private final String notifyDelayPref = "notfify.delay.prepaid";

    @Override
    public NotifyIntent onGetNotifiyIntent()
    {
        if (Json.equals(config, "subitem", "prepaid"))
        {
            NotifyIntent intent = PrepaidManager.getNotifyEvent();

            if (intent.importance == NotifyIntent.INFOONLY)
            {
                intent.followRunner = launchPrepaidRequestRunner;
                intent.declineRunner = launchPrepaidRechargeRunner;
            }

            if ((intent.importance == NotifyIntent.REMINDER) ||
                    (intent.importance == NotifyIntent.WARNING) ||
                    (intent.importance == NotifyIntent.ASSISTANCE))
            {
                intent.declineText = "Morgen erinnern";
                intent.declineRunner = declineRunner;
                intent.followRunner = launchPrepaidRechargeRunner;

                if (isDeclined()) return null;
            }

            return intent;
        }

        return null;
    }

    private boolean isDeclined()
    {
        String delaydate = Simple.getSharedPrefString(notifyDelayPref);

        if (delaydate != null)
        {
            if (Simple.compareTo(delaydate, Simple.nowAsISO()) > 0)
            {
                return true;
            }

            Simple.removeSharedPref(notifyDelayPref);
        }

        return false;
    }

    private final Runnable declineRunner = new Runnable()
    {
        @Override
        public void run()
        {
            //
            // Store delay notification date.
            //

            String delaydate = Simple.timeStampAsISO(Simple.nowAsTimeStamp() + 24 * 3600 * 1000);
            Simple.setSharedPrefString(notifyDelayPref, delaydate);
        }
    };

    public void onPrepaidReceived(int money, String date)
    {
        long stamp = Simple.getTimeStamp(date);
        String dom = Simple.getLocalDayOfMonth(stamp) + ". " + Simple.getLocalMonth(stamp);
        prepaidDateView.setText(dom);

        String value = String.format("%.02f", money / 100f) + Simple.getCurrencySymbol();
        prepaidMoneyView.setText(value);
    }

    @Override
    public void onPrepaidBalanceReceived(String text, int money, JSONObject slug)
    {
        if (money >= 0)
        {
            String date = Simple.nowAsISO();

            onPrepaidReceived(money, date);

            JSONObject recvPrepaidBalance = new JSONObject();

            Json.put(recvPrepaidBalance, "type", "recvPrepaidBalance");
            Json.put(recvPrepaidBalance, "text", text);
            Json.put(recvPrepaidBalance, "money", money);
            Json.put(recvPrepaidBalance, "date", date);

            AssistanceMessage.sendInfoMessage(recvPrepaidBalance);
        }
    }

    @Override
    public void onPrepaidCashcodeReceived(String cashcode)
    {
        PrepaidManager.makeRequest(this, false, null, cashcode);
    }

    @Override
    protected boolean onMyLongClick()
    {
        return launchPrepaidRecharge();
    }

    @Override
    protected void onMyClick()
    {
        launchCall();
    }

    private final Runnable launchPrepaidRequestRunner = new Runnable()
    {
        @Override
        public void run()
        {
            launchPrepaidRequest();
        }
    };
    
    private final Runnable launchPrepaidRechargeRunner = new Runnable()
    {
        @Override
        public void run()
        {
            launchPrepaidRecharge();
        }
    };

    private void launchPrepaidRequest()
    {
        PrepaidManager.makeRequest(this, false, null, null);
    }

    private boolean launchPrepaidRecharge()
    {
        if (config.has("subitem"))
        {
            String subitem = Json.getString(config, "subitem");

            if (Simple.equals(subitem, "prepaid"))
            {
                String prepaidload = Json.getString(config, "prepaidload");

                if (prepaidload != null)
                {
                    Log.d(LOGTAG,"launchPrepaidRecharge:" + prepaidload);

                    PrepaidManager.createPrepaidLoadDialog(this);

                    return true;
                }
            }
        }

        return false;
    }

    private void launchCall()
    {
        if (config.has("subitem"))
        {
            String subitem = Json.getString(config, "subitem");
            String phonenumber = Json.getString(config, "phonenumber");

            if ((subitem != null) && (phonenumber != null))
            {
                phonenumber = phonenumber.replace("#", "%23");

                if (subitem.equals("prepaid"))
                {
                    launchPrepaidRequest();
                }
                else
                {
                    try
                    {
                        Uri uri = Uri.parse("tel:" + phonenumber);
                        Intent sendIntent = new Intent(Intent.ACTION_CALL, uri);
                        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sendIntent.setPackage("com.android.server.telecom");

                        ProcessManager.launchIntent(Intent.createChooser(sendIntent, ""));
                    }
                    catch (Exception ex)
                    {
                        OopsService.log(LOGTAG, ex);
                    }
                }
            }
        }
        else
        {
            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }
}
