package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class LaunchItemAlertcall extends LaunchItem
{
    private final static String LOGTAG = LaunchItemAlertcall.class.getSimpleName();

    public static JSONArray getConfig()
    {
        JSONArray launchitems = new JSONArray();

        if (Simple.getSharedPrefBoolean("alertgroup.enable"))
        {
            JSONObject launchitem = new JSONObject();

            Json.put(launchitem, "type", "alertcall");
            Json.put(launchitem, "label", Simple.getTrans(R.string.alertcall_label));
            Json.put(launchitem, "order", 100);

            Json.put(launchitems, launchitem);
        }

        return launchitems;
    }

    public LaunchItemAlertcall(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        SharedPreferences sp = Simple.getSharedPrefs();

        groupIdentity = sp.getString("alertgroup.groupidentity", null);

        setLongClickable(true);

        setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                return onMyLongClick();
            }
        });

        icon.setImageResource(GlobalConfigs.IconResAlertcall);
    }

    @Override
    protected void onMyClick()
    {
        launchAlertChat(false);
    }

    @Override
    protected boolean onMyLongClick()
    {
        alertcallShowDialog();

        return true;
    }

    private void launchAlertChat(boolean hotalert)
    {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("idremote", groupIdentity);
        intent.putExtra("alertcall", hotalert);
        if (hotalert) intent.putExtra("alertMessageUUID", alertMessageUUID);

        context.startActivity(intent);
    }

    private static final int ALERTCALL_COUNTDOWN = 0;
    private static final int ALERTCALL_CANCELED  = 1;
    private static final int ALERTCALL_EXECUTED  = 2;

    private String groupIdentity;
    private AlertDialog alertDialog;
    private TextView alertTextview;
    private String alertMessageUUID;
    private String alertMessage;
    private int alertCountdown;
    private int alertStatus;

    private void alertcallShowDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Assistenz rufen...");

        alertTextview = new TextView(context);
        alertTextview.setPadding(40, 40, 40, 40);
        alertTextview.setTextSize(24f);

        builder.setView(alertTextview);

        builder.setPositiveButton("Jetzt sofort", null);
        builder.setNeutralButton("Abbrechen", null);

        alertDialog = builder.create();
        alertDialog.show();

        alertDialog.setCancelable(false);

        Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setTextSize(24f);
        positive.setTransformationMethod(null);
        positive.setOnClickListener(alertOnClickPositive);

        Button neutral = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutral.setTextSize(24f);
        neutral.setTransformationMethod(null);
        neutral.setOnClickListener(alertOnClickNeutral);

        alertMessage = "Der Assistenzruf wird in %d Sekunden ausgelöst";
        alertStatus = ALERTCALL_COUNTDOWN;
        alertCountdown = 20;

        if (handler == null) handler = new Handler();
        handler.post(alertcallCountdown);
    }

    private void alertcallExecute()
    {
        alertMessageUUID = Simple.getUUID();
        String alerttext = "Ich benötige Assistenz";

        JSONObject alertmessage = new JSONObject();
        Json.put(alertmessage, "uuid", alertMessageUUID);
        Json.put(alertmessage, "message", alerttext);
        Json.put(alertmessage, "priority", "alertcall");

        ChatManager.getInstance().sendOutgoingMessage(groupIdentity, alertmessage);

        String message = "Der Assistenzruf wurde ausgelöst";
        alertTextview.setText(message);
        Speak.speak(message);

        postDelayed(openAlertChat, 200);
    }

    private Runnable openAlertChat = new Runnable()
    {
        @Override
        public void run()
        {
            alertDialog.cancel();

            launchAlertChat(true);
        }
    };

    private Runnable alertcallCountdown = new Runnable()
    {
        @Override
        public void run()
        {
            if (! alertDialog.isShowing()) return;

            if (alertCountdown == 0)
            {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Ok");
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(INVISIBLE);

                alertStatus = ALERTCALL_EXECUTED;

                alertcallExecute();
            }
            else
            {
                String message = String.format(alertMessage, alertCountdown);

                alertTextview.setText(message);
                if ((alertCountdown % 5) == 0) Speak.speak(message);

                alertCountdown -= 1;
                handler.postDelayed(alertcallCountdown, 1000);
            }
        }
    };

    View.OnClickListener alertOnClickPositive = new View.OnClickListener()
    {
        @Override
        public void onClick(View dialog)
        {
            if (alertStatus == ALERTCALL_COUNTDOWN)
            {
                alertCountdown = 0;
            }

            if (alertStatus == ALERTCALL_CANCELED)
            {
                alertDialog.cancel();
            }

            if (alertStatus == ALERTCALL_EXECUTED)
            {
                alertDialog.cancel();
            }
        }
    };

    View.OnClickListener alertOnClickNeutral = new View.OnClickListener()
    {
        @Override
        public void onClick(View dialog)
        {
            handler.removeCallbacks(alertcallCountdown);

            String message = "Der Assistenzruf wurde abgebrochen";

            alertTextview.setText(message);
            Speak.speak(message);

            alertStatus = ALERTCALL_CANCELED;

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Schliessen");
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(INVISIBLE);
        }
    };
}
