package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebLib;

public class LaunchItemCall extends LaunchItem implements AccessibilityService.MessageServiceCallback
{
    private final static String LOGTAG = LaunchItemCall.class.getSimpleName();

    public LaunchItemCall(Context context)
    {
        super(context);
    }

    private TextView prepaidView;
    private boolean isPrepaidLoad;

    @Override
    protected void setConfig()
    {
        if (config.has("subitem"))
        {
            String iconurl = Json.getString(config, "icon");
            icon.setImageDrawable(WebLib.getIconDrawable("calls", iconurl));

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

                String mdate = Simple.getSharedPrefString("monitoring.prepaid.stamp");
                long mstamp = (mdate == null) ? 0 : Simple.getTimeStamp(mdate);

                if ((Simple.nowAsTimeStamp() - mstamp) < (86400 * 1000))
                {
                    int money = Simple.getSharedPrefInt("monitoring.prepaid.money");
                    onPrepaidReceived(money, false);
                }

                addView(prepaidView);
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

    private final Runnable unsubscribeMessage = new Runnable()
    {
        @Override
        public void run()
        {
            AccessibilityService.unsubscribe(LaunchItemCall.this);
        }
    };

    public void onPrepaidReceived(int money, boolean save)
    {
        if (save)
        {
            Simple.setSharedPrefString("monitoring.prepaid.stamp", Simple.nowAsISO());
            Simple.setSharedPrefInt("monitoring.prepaid.money", money);
        }

        String value = String.format("%.02f", money / 100f) + Simple.getCurrencySymbol();

        prepaidView.setText(value);
    }

    @Override
    public int onMessageReceived(JSONObject message)
    {
        Log.d(LOGTAG, "onMessageReceived:" + message.toString());

        if ((prepaidView != null) && Json.equals(message, "app", "com.android.phone"))
        {
            String text = Json.getString(message, "text");
            String value = Simple.getMatch("([0-9,.]+)[ ]*(EUR|€|USD|$)", text);

            if (value != null)
            {
                int money = Math.round(100 * Float.parseFloat(value.replace(",", ".")));

                onPrepaidReceived(money, true);

                Simple.removePost(unsubscribeMessage);
                Simple.makePost(unsubscribeMessage);

                //return isPrepaidLoad ? 0 : AccessibilityService.GLOBAL_ACTION_BACK;

                return 0;
            }
        }

        return 0;
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

                    createPrepaidLoadDialog();

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
                    isPrepaidLoad = false;
                    AccessibilityService.subscribe(this);
                    Simple.makePost(unsubscribeMessage, 5000);
                }

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
        else
        {
            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }

    //region Prepaid load dialog

    private AlertDialog dialog;
    private EditText cashcode;

    public void createPrepaidLoadDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Prepaid Guthaben Code:");

        builder.setNegativeButton("Abbrechen", null);
        builder.setPositiveButton("Aufladen", null);

        dialog = builder.create();

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(20, 8, 20, 8);
        content.setLayoutParams(Simple.layoutParamsMM());

        cashcode = new EditText(getContext());
        cashcode.setInputType(InputType.TYPE_CLASS_PHONE);
        cashcode.setLayoutParams(Simple.layoutParamsMM());
        content.addView(cashcode);

        dialog.setView(content);
        dialog.show();

        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negative.setTextSize(24f);

        negative.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onCancelClick();
            }
        });

        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setTextSize(24f);

        positive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onLoadClick();
            }
        });
    }

    public void onCancelClick()
    {
        dialog.cancel();
        dialog = null;
        cashcode = null;
    }

    public void onLoadClick()
    {
        if (config.has("subitem"))
        {
            String prepaidload = Json.getString(config, "prepaidload");

            if (prepaidload != null)
            {
                prepaidload = prepaidload + cashcode.getText();
                if (! prepaidload.endsWith("#")) prepaidload += "#";
                prepaidload = prepaidload.replace("#", "%23");

                final String cbprepaidload = prepaidload;

                isPrepaidLoad = true;
                AccessibilityService.subscribe(this);
                Simple.makePost(unsubscribeMessage, 5000);

                Simple.makePost(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            isPrepaidLoad = true;

                            Uri uri = Uri.parse("tel:" + cbprepaidload);
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
                });
            }
        }

        dialog.cancel();
        dialog = null;
        cashcode = null;
    }

    //region Prepaid load dialog
}
