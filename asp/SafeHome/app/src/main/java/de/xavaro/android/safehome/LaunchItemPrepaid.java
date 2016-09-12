package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;
import android.view.Gravity;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.AssistanceMessage;
import de.xavaro.android.common.NotifyIntent;
import de.xavaro.android.common.PrepaidManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

public class LaunchItemPrepaid extends LaunchItem implements
        PrepaidManager.PrepaidManagerBalanceCallback,
        PrepaidManager.PrepaidManagerCashcodeCallback,
        NotifyIntent.NotifyService
{
    private final static String LOGTAG = LaunchItemPrepaid.class.getSimpleName();

    public static JSONArray getConfig()
    {
        JSONArray launchitems = new JSONArray();

        String mode = Simple.getSharedPrefString("monitors.prepaid.mode");

        JSONObject launchitem = new JSONObject();

        Json.put(launchitem, "type", "prepaid");
        Json.put(launchitem, "label", "Prepaid SIM");
        Json.put(launchitem, "order", 100);

        if (! Simple.equals(mode, "home")) Json.put(launchitem, "notify", "only");

        Json.put(launchitems, launchitem);

        return launchitems;
    }


    public LaunchItemPrepaid(Context context)
    {
        super(context);
    }

    private TextView prepaidDateView;
    private TextView prepaidMoneyView;

    @Override
    protected void setConfig()
    {
        icon.setImageResource(R.drawable.prepaid_600x600);

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
                        launchPrepaidRequest();
                    }
                });
            }
            else
            {
                prepaidMoneyView.setText("No SIM");
            }
        }
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);

        //
        // Original font sizes based on 200 pixels height.
        //

        float scale = Simple.getNormalPixels(height - icon.getPaddingBottom()) / 200.0f;

        prepaidDateView.setTextSize(Simple.getDeviceTextSize(24f * scale));
        prepaidMoneyView.setTextSize(Simple.getDeviceTextSize(40f * scale));

        Simple.setPadding(prepaidDateView, 0, Math.round(20 * scale), 0, 0);
        Simple.setPadding(prepaidMoneyView, 0, Math.round(56 * scale), 0, 0);
    }

    private final String notifyDelayPref = "notfify.delay.prepaid";

    @Override
    public NotifyIntent onGetNotifiyIntent()
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
        launchPrepaidRequest();
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
        PrepaidManager.createPrepaidLoadDialog(this);

        return true;
    }
}
