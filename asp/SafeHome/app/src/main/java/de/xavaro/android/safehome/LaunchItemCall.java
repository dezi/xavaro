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
import de.xavaro.android.common.BatteryManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PrepaidManager;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;

public class LaunchItemCall extends LaunchItem implements
        PrepaidManager.PrepaidManagerBalanceCallback,
        PrepaidManager.PrepaidManagerCashcodeCallback,
        NotifyIntent.NotifiyService
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
            String iconpath = "weblib|calls|" + iconurl;
            icon.setImageResource(iconpath);

            String subitem = Json.getString(config, "subitem");

            if (Simple.equals(subitem, "prepaid"))
            {
                int devpad16 = Simple.getDevicePixels(16);
                int devpad20 = Simple.getDevicePixels(20);
                int devpad36 = Simple.getDevicePixels(36);

                prepaidDateView = new TextView(getContext());
                prepaidDateView.setLayoutParams(Simple.layoutParamsMW());
                prepaidDateView.setPadding(0, devpad16, 0, 0);
                prepaidDateView.setTextSize(Simple.getDeviceTextSize(22f));
                prepaidDateView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                prepaidDateView.setTextColor(Color.WHITE);
                prepaidDateView.setTypeface(null, Typeface.BOLD);

                addView(prepaidDateView);

                prepaidMoneyView = new TextView(getContext());
                prepaidMoneyView.setLayoutParams(Simple.layoutParamsMM());
                prepaidMoneyView.setPadding(0, devpad20, 0, icon.getPaddingBottom() + devpad36);
                prepaidMoneyView.setTextSize(Simple.getDeviceTextSize(40f));
                prepaidMoneyView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                prepaidMoneyView.setTextColor(Color.WHITE);
                prepaidMoneyView.setTypeface(null, Typeface.BOLD);

                addView(prepaidMoneyView);

                String mdate = Simple.getSharedPrefString("monitoring.prepaid.stamp");
                long mstamp = (mdate == null) ? 0 : Simple.getTimeStamp(mdate);

                if ((Simple.nowAsTimeStamp() - mstamp) < (86400 * 1000))
                {
                    int money = Simple.getSharedPrefInt("monitoring.prepaid.money");
                    onPrepaidReceived(money, mdate);
                }
                else
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

            int devpad28 = Simple.getDevicePixels(Math.round(28 * scale));
            int devpad48 = Simple.getDevicePixels(Math.round(56 * scale));

            prepaidDateView.setPadding(0, devpad28, 0, 0);
            prepaidMoneyView.setPadding(0, devpad48, 0, 0);
        }
    }

    private final String notifyDelayPref = "notfify.delay.prepaid";

    @Override
    public NotifyIntent onGetNotifiyIntent()
    {
        if (Json.equals(config, "subitem", "prepaid"))
        {
            NotifyIntent intent = PrepaidManager.getNotifyEvent();

            intent.followRunner = launchPrepaidRequestRunner;
            intent.declineRunner = launchPrepaidRechargeRunner;

            return intent;
        }

        return null;
    }

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
