package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
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
        implements ChatManager.ChatMessageCallback
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
        ArchievementManager.show("alertcall.shortclick");
    }

    @Override
    protected boolean onMyLongClick()
    {
        alertcallShowDialog();

        return true;
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

        builder.setTitle("Hilfe rufen...");

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

        alertMessage = "Der Hilferuf wird in %d Sekunden ausgelöst";
        alertStatus = ALERTCALL_COUNTDOWN;
        alertCountdown = 20;

        if (handler == null) handler = new Handler();
        handler.post(alertcallCountdown);
    }

    private void alertcallExecute()
    {
        ChatManager.getInstance().subscribe(groupIdentity, this);

        alertMessageUUID = Simple.getUUID();
        String alerttext = "Es wird Hilfe benötigt";

        JSONObject alertmessage = new JSONObject();
        Json.put(alertmessage, "uuid", alertMessageUUID);
        Json.put(alertmessage, "message", alerttext);
        Json.put(alertmessage, "priority", "alertcall");

        ChatManager.getInstance().sendOutgoingMessage(groupIdentity, alertmessage);

        String message = "Der Hilferuf wurde ausgelöst";
        alertTextview.setText(message);
        Speak.speak(message);
    }

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

                ChatManager.getInstance().unsubscribe(groupIdentity, LaunchItemAlertcall.this);
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

            String message = "Der Hilferuf wurde abgebrochen";

            alertTextview.setText(message);
            Speak.speak(message);

            alertStatus = ALERTCALL_CANCELED;

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Schliessen");
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(INVISIBLE);
        }
    };

    private ArrayList<String> idremotesAlertReceived = new ArrayList<>();
    private ArrayList<String> idremotesAlertRead  = new ArrayList<>();

    private final Runnable speekFeedbackMessage = new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (speekFeedbackMessage)
            {
                for (String idremote : idremotesAlertReceived)
                {
                    if (!idremotesAlertRead.contains(idremote))
                    {
                        String message = RemoteContacts.getDisplayName(idremote);
                        message += " hat ihren Notruf empfangen";
                        Speak.speak(message);

                        message = alertTextview.getText() + ".\n" + message;
                        alertTextview.setText(message);
                    }
                }

                idremotesAlertReceived.clear();

                for (String idremote : idremotesAlertRead)
                {
                    String message = RemoteContacts.getDisplayName(idremote);
                    message += " hat ihren Notruf gelesen";
                    Speak.speak(message);

                    message = alertTextview.getText() + ".\n" + message;
                    alertTextview.setText(message);
                }

                idremotesAlertRead.clear();
            }
        }
    };

    public void onProtocollMessages(JSONObject protocoll)
    {
    }

    public void onIncomingMessage(JSONObject message)
    {
        Log.d(LOGTAG, "onIncomingMessage:" + message.toString());
    }

    public void onSetMessageStatus(String idremote, String uuid, String what)
    {
        if (! alertMessageUUID.equals(uuid)) return;

        synchronized (speekFeedbackMessage)
        {
            if (what.equals("recv"))
            {
                if (!idremotesAlertReceived.contains(idremote))
                    idremotesAlertReceived.add(idremote);

                handler.removeCallbacks(speekFeedbackMessage);
                handler.postDelayed(speekFeedbackMessage, 3000);
            }

            if (what.equals("read"))
            {
                if (!idremotesAlertReceived.contains(idremote))
                    idremotesAlertReceived.remove(idremote);

                if (!idremotesAlertRead.contains(idremote))
                    idremotesAlertRead.add(idremote);

                handler.removeCallbacks(speekFeedbackMessage);
                handler.postDelayed(speekFeedbackMessage, 3000);
            }
        }
    }

    public void onRemoteStatus()
    {
    }
}
