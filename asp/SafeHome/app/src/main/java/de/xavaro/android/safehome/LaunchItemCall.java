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
import de.xavaro.android.common.PrepaidManager;
import de.xavaro.android.common.ProcessManager;
import de.xavaro.android.common.Simple;

public class LaunchItemCall extends LaunchItem implements PrepaidManager.PrepaidManagerCallback
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
        onPrepaidReceived(money);
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
        final String loadcode = cashcode.getText().toString();

        Simple.makePost(new Runnable()
        {
            @Override
            public void run()
            {
                PrepaidManager.makeRequest(LaunchItemCall.this, false, null, loadcode);
            }
        });

        dialog.cancel();
        dialog = null;
        cashcode = null;
    }

    //region Prepaid load dialog
}
