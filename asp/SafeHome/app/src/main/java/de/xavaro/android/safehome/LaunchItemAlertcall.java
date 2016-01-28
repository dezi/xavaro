package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LaunchItemAlertcall extends LaunchItem
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
        launchAlertcallWarn();
    }

    private boolean onMyLongClick()
    {
        launchAlertcall();

        return true;
    }

    private void launchAlertcallWarn()
    {
        ArchievementManager.show("alertcall.shortclick");
    }

    private void launchAlertcall()
    {
        alertcallShowDialog();
    }

    private static final int ALERTCALL_COUNTDOWN = 0;
    private static final int ALERTCALL_CANCELED  = 1;
    private static final int ALERTCALL_EXECUTED  = 2;

    private AlertDialog alertcallDialog;
    private TextView alertcallTextview;
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

    private Runnable alertcallCountdown = new Runnable()
    {
        @Override
        public void run()
        {
            if (! alertcallDialog.isShowing()) return;

            if (alertcallSeconds == 0)
            {
                String message = "Der Hilferuf wird nun ausgelöst";
                alertcallTextview.setText(message);
                DitUndDat.SpeekDat.speak(message);

                alertcallDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Ok");
                alertcallDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(INVISIBLE);

                alertcallStatus = ALERTCALL_EXECUTED;

                Log.d(LOGTAG,"=====================> call");
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

}
