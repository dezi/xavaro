package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemAlertcall extends LaunchItem
        implements ChatManager.ChatMessageCallback
{
    private final static String LOGTAG = LaunchItemAlertcall.class.getSimpleName();

    public LaunchItemAlertcall(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
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
        icon.setVisibility(VISIBLE);
    }

    @Override
    protected void onMyClick()
    {
        ArchievementManager.show("alertcall.shortclick");
    }

    private boolean onMyLongClick()
    {
        alertcallShowDialog();

        return true;
    }

    private static final int ALERTCALL_COUNTDOWN = 0;
    private static final int ALERTCALL_CANCELED  = 1;
    private static final int ALERTCALL_EXECUTED  = 2;

    private AlertDialog alertcallDialog;
    private TextView alertcallTextview;
    private String alertcallUUID;
    private String alertcallText;
    private int alertcallSeconds;
    private int alertcallStatus;

    private void alertcallShowDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Hilfe rufen...");

        alertcallTextview = new TextView(context);
        alertcallTextview.setPadding(40, 40, 40, 40);
        alertcallTextview.setTextSize(24f);

        builder.setView(alertcallTextview);

        builder.setPositiveButton("Jetzt sofort", null);
        builder.setNeutralButton("Abbrechen", null);

        alertcallDialog = builder.create();
        alertcallDialog.show();

        alertcallDialog.setCancelable(false);

        Button positive = alertcallDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setTextSize(24f);
        positive.setTransformationMethod(null);
        positive.setOnClickListener(alertOnClickPositive);

        Button neutral = alertcallDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutral.setTextSize(24f);
        neutral.setTransformationMethod(null);
        neutral.setOnClickListener(alertOnClickNeutral);

        alertcallText = "Der Hilferuf wird in %d Sekunden ausgelöst";
        alertcallStatus = ALERTCALL_COUNTDOWN;
        alertcallSeconds = 20;

        if (handler == null) handler = new Handler();
        handler.post(alertcallCountdown);
    }

    private void alertcallExecute()
    {
        SharedPreferences sp = Simple.getSharedPrefs();

        String groupidentity = sp.getString("alertgroup.groupidentity", null);

        if (groupidentity == null)
        {
            String message = "Der Hilferuf konnte nicht ausgelöst werden";
            alertcallTextview.setText(message);
            DitUndDat.SpeekDat.speak(message);

            // todo

            return;
        }

        ChatManager.getInstance().subscribe(groupidentity, this);

        alertcallUUID = Simple.getUUID();
        String alerttext = "Es wird Hilfe benötigt";

        JSONObject alertmessage = new JSONObject();
        Json.put(alertmessage, "uuid", alertcallUUID);
        Json.put(alertmessage, "message", alerttext);
        Json.put(alertmessage, "priority", "alertcall");

        ChatManager.getInstance().sendOutgoingMessage(groupidentity, alertmessage);

        String message = "Der Hilferuf wurde ausgelöst";
        alertcallTextview.setText(message);
        DitUndDat.SpeekDat.speak(message);
    }

    private Runnable alertcallCountdown = new Runnable()
    {
        @Override
        public void run()
        {
            if (! alertcallDialog.isShowing()) return;

            if (alertcallSeconds == 0)
            {
                alertcallDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Ok");
                alertcallDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(INVISIBLE);

                alertcallStatus = ALERTCALL_EXECUTED;

                alertcallExecute();
            }
            else
            {
                String message = String.format(alertcallText, alertcallSeconds);

                alertcallTextview.setText(message);
                if ((alertcallSeconds % 5) == 0) DitUndDat.SpeekDat.speak(message);

                alertcallSeconds -= 1;
                handler.postDelayed(alertcallCountdown, 1000);
            }
        }
    };

    View.OnClickListener alertOnClickPositive = new View.OnClickListener()
    {
        @Override
        public void onClick(View dialog)
        {
            if (alertcallStatus == ALERTCALL_COUNTDOWN)
            {
                alertcallSeconds = 0;
            }

            if (alertcallStatus == ALERTCALL_CANCELED)
            {
                alertcallDialog.cancel();
            }

            if (alertcallStatus == ALERTCALL_EXECUTED)
            {
                alertcallDialog.cancel();
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

            alertcallTextview.setText(message);
            DitUndDat.SpeekDat.speak(message);

            alertcallStatus = ALERTCALL_CANCELED;

            alertcallDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Schliessen");
            alertcallDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(INVISIBLE);
        }
    };

    private Runnable speekReceivedMessage = new Runnable()
    {
        @Override
        public void run()
        {

        }
    };

    private Runnable speekReadMessage = new Runnable()
    {
        @Override
        public void run()
        {

        }
    };

    public void onProtocollMessages(JSONObject protocoll)
    {
    }

    public void onIncomingMessage(JSONObject message)
    {
        Log.d(LOGTAG, "onIncomingMessage:" + message.toString());
    }

    public void onSetMessageStatus(String uuid, String what)
    {
        Log.d(LOGTAG, "onSetMessageStatus:" + uuid + "=" + what);

        if (! alertcallUUID.equals(uuid)) return;
        if (what.equals("acks")) return;

        if (what.equals("recv"))
        {
            handler.postDelayed(speekReceivedMessage,3000);
        }

        if (what.equals("read"))
        {
            handler.removeCallbacks(speekReceivedMessage);
            handler.post(speekReadMessage);
        }
    }

    public void onRemoteStatus()
    {
    }
}
