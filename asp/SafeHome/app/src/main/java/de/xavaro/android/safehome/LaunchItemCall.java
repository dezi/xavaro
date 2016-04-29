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
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.PrepaidManager;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;

public class LaunchItemCall extends LaunchItem implements
        PrepaidManager.PrepaidManagerBalanceCallback,
        PrepaidManager.PrepaidManagerCashcodeCallback
{
    private final static String LOGTAG = LaunchItemCall.class.getSimpleName();

    public LaunchItemCall(Context context)
    {
        super(context);
    }

    private TextView prepaidView;

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
                prepaidView = new TextView(getContext());
                prepaidView.setLayoutParams(Simple.layoutParamsMM());
                prepaidView.setPadding(0, 20, 0, icon.getPaddingBottom() + 36);
                prepaidView.setTextSize(Simple.getDeviceTextSize(40f));
                prepaidView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                prepaidView.setTextColor(Color.WHITE);
                prepaidView.setTypeface(null, Typeface.BOLD);

                addView(prepaidView);

                String mdate = Simple.getSharedPrefString("monitoring.prepaid.stamp");
                long mstamp = (mdate == null) ? 0 : Simple.getTimeStamp(mdate);

                if ((Simple.nowAsTimeStamp() - mstamp) < (86400 * 1000))
                {
                    onPrepaidReceived(Simple.getSharedPrefInt("monitoring.prepaid.money"));
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

    public void onPrepaidReceived(int money)
    {
        String value = String.format("%.02f", money / 100f) + Simple.getCurrencySymbol();

        prepaidView.setText(value);
    }

    @Override
    public void onPrepaidBalanceReceived(String text, int money, JSONObject slug)
    {
        if (money >= 0)
        {
            onPrepaidReceived(money);

            JSONObject recvPrepaidBalance = new JSONObject();

            Json.put(recvPrepaidBalance, "type", "recvPrepaidBalance");
            Json.put(recvPrepaidBalance, "text", text);
            Json.put(recvPrepaidBalance, "money", money);
            Json.put(recvPrepaidBalance, "date", Simple.nowAsISO());

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
        return launchPrepaidLoad();
    }

    @Override
    protected void onMyClick()
    {
        launchCall();
    }

    private boolean launchPrepaidLoad()
    {
        if (config.has("subitem"))
        {
            String subitem = Json.getString(config, "subitem");

            if (Simple.equals(subitem, "prepaid"))
            {
                String prepaidload = Json.getString(config, "prepaidload");

                if (prepaidload != null)
                {
                    Log.d(LOGTAG,"launchPrepaidLoad:" + prepaidload);

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
                    PrepaidManager.makeRequest(this, false, null, null);
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
