package de.xavaro.android.common;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.View;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

public class PrepaidManager implements AccessibilityService.MessageServiceCallback
{
    private static final String LOGTAG = PrepaidManager.class.getSimpleName();

    public static boolean makeRequest(
            PrepaidManagerBalanceCallback callback,
            boolean quiet,
            JSONObject slug,
            String cashcode
    )
    {
        if (! AccessibilityService.checkEnabled()) return false;

        PrepaidManager pm = new PrepaidManager();
        return pm.makeRequestInternal(callback, quiet, slug, cashcode);
    }

    public static void createPrepaidLoadDialog(PrepaidManagerCashcodeCallback cashcodeCallback)
    {
        PrepaidManager pm = new PrepaidManager();
        pm.createPrepaidLoadDialogInternal(cashcodeCallback);
    }

    private PrepaidManagerBalanceCallback balanceCallback;
    private JSONObject slug;
    private boolean quiet;

    private boolean makeRequestInternal(
            PrepaidManagerBalanceCallback callback,
             boolean quiet,
            JSONObject slug,
            String cashcode
            )
    {
        String phonenumber;

        if (cashcode == null)
        {
            phonenumber = Simple.getSharedPrefString("calls.monitors.phonenumber:prepaid");
            if (phonenumber == null) return false;
        }
        else
        {
            phonenumber = Simple.getSharedPrefString("calls.monitors.prepaidload:prepaid");
            if (phonenumber == null) return false;

            phonenumber += cashcode + "#";
        }

        this.balanceCallback = callback;
        this.quiet = quiet;
        this.slug = slug;

        phonenumber = phonenumber.replace("#", "%23");

        AccessibilityService.subscribe(this);
        Simple.makePost(unsubscribeMessage, 15 * 1000);

        try
        {
            Uri uri = Uri.parse("tel:" + phonenumber);
            Intent sendIntent = new Intent(Intent.ACTION_CALL, uri);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.setPackage("com.android.server.telecom");

            ProcessManager.launchIntent(Intent.createChooser(sendIntent, ""));

            return true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    private final Runnable unsubscribeMessage = new Runnable()
    {
        @Override
        public void run()
        {
            AccessibilityService.unsubscribe(PrepaidManager.this);
        }
    };

    @Override
    public int onAccessibilityMessageReceived(JSONObject message)
    {
        Log.d(LOGTAG, "onAccessibilityMessageReceived:" + message.toString());

        if (Json.equals(message, "app", "com.android.phone"))
        {
            String text = Json.getString(message, "text");
            String value = Simple.getMatch("([0-9,.]+)[ ]*(EUR|€|USD|$)", text);

            if (value != null)
            {
                int money = Math.round(100 * Float.parseFloat(value.replace(",", ".")));

                Simple.setSharedPrefString("monitoring.prepaid.stamp", Simple.nowAsISO());
                Simple.setSharedPrefInt("monitoring.prepaid.money", money);

                Simple.removePost(unsubscribeMessage);
                Simple.makePost(unsubscribeMessage);

                if (balanceCallback != null) balanceCallback.onPrepaidBalanceReceived(text, money, slug);

                return quiet ? AccessibilityService.GLOBAL_ACTION_BACK : 0;
            }

            if ((text != null) && text.endsWith(", OK]"))
            {
                Simple.removePost(unsubscribeMessage);
                Simple.makePost(unsubscribeMessage);

                if (balanceCallback != null) balanceCallback.onPrepaidBalanceReceived(text, -1, slug);

                return quiet ? AccessibilityService.GLOBAL_ACTION_BACK : 0;
            }
        }

        return 0;
    }

    public interface PrepaidManagerBalanceCallback
    {
        void onPrepaidBalanceReceived(String text, int money, JSONObject slug);
    }

    public interface PrepaidManagerCashcodeCallback
    {
        void onPrepaidCashcodeReceived(String cashcode);
    }

    //region Prepaid load dialog

    private PrepaidManagerCashcodeCallback cashcodeCallback;
    private AlertDialog dialog;
    private EditText cashcode;

    public void createPrepaidLoadDialogInternal(PrepaidManagerCashcodeCallback cashcodeCallback)
    {
        this.cashcodeCallback = cashcodeCallback;

        AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getActContext());

        builder.setTitle("Prepaid Guthaben Code:");
        builder.setNegativeButton("Abbrechen", null);
        builder.setPositiveButton("Aufladen", null);

        dialog = builder.create();

        LinearLayout content = new LinearLayout(Simple.getActContext());
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(20, 8, 20, 8);
        content.setLayoutParams(Simple.layoutParamsMM());

        cashcode = new EditText(Simple.getActContext());
        cashcode.setInputType(InputType.TYPE_CLASS_PHONE);
        cashcode.setLayoutParams(Simple.layoutParamsMM());
        content.addView(cashcode);

        dialog.setView(content);
        dialog.show();

        Simple.adjustAlertDialog(dialog);

        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negative.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onCancelClick();
            }
        });

        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onLoadClick();
            }
        });
    }

    private final Runnable doCallback = new Runnable()
    {
        @Override
        public void run()
        {
            if (cashcodeCallback != null)
            {
                String loadcode = cashcode.getText().toString();
                cashcodeCallback.onPrepaidCashcodeReceived(loadcode);
            }
        }
    };

    private void onCancelClick()
    {
        dialog.cancel();
    }

    private void onLoadClick()
    {
        Simple.makePost(doCallback);

        dialog.cancel();
    }

    //endregion Prepaid load dialog

    private static String lastMessage;
    private static int lastImportance;
    private static String lastSpokenTimePref;

    public static NotifyIntent getNotifyEvent()
    {
        checkWarnings();

        int money = Simple.getSharedPrefInt("monitoring.prepaid.money");
        String moneyvalue = Simple.getCurrencyValue(money);

        NotifyIntent intent = new NotifyIntent();

        if (lastMessage == null)
        {
            intent.title =  Simple.getTrans(R.string.prepaid_manager_info, moneyvalue);
            intent.importance = NotifyIntent.INFOONLY;
            intent.followText = Simple.getTrans(R.string.prepaid_manager_request);
            intent.declineText = Simple.getTrans(R.string.prepaid_manager_recharge);
        }
        else
        {
            String lowmess = Simple.getTrans(R.string.prepaid_manager_infolow, moneyvalue);

            intent.title = lowmess + " " + lastMessage;
            intent.importance = lastImportance;
            intent.spokenTimePref = lastSpokenTimePref;
            intent.spokenRepeatMinutes = getRepeatMinutes();
            intent.followText = Simple.getTrans(R.string.prepaid_manager_recharge);
        }

        return intent;
    }

    private static void resetWarnings()
    {
        //
        // Check if an assistance warning was send.
        //

        String date = Simple.getSharedPrefString("monitors.prepaid.lastassist");

        if (date != null)
        {
            //
            // Tell assistance problem is solved.
            //

            int money = Simple.getSharedPrefInt("monitoring.prepaid.money");
            String moneyval = Simple.getCurrencyValue(money);
            String owner = Simple.getOwnerName();

            String text1 = Simple.getTrans(R.string.prepaid_manager_assist_clear, owner);
            String text2 = Simple.getTrans(R.string.prepaid_manager_assist_level, moneyval);

            AssistanceMessage.informAssistance(text1 + " " + text2);
        }

        //
        // Reset all warning times.
        //

        lastMessage = null;
        lastImportance = 0;
        lastSpokenTimePref = null;

        Simple.removeSharedPref("monitors.prepaid.lastremind");
        Simple.removeSharedPref("monitors.prepaid.lastwarn");
        Simple.removeSharedPref("monitors.prepaid.lastassist");
    }

    private static int getRepeatMinutes()
    {
        int repeatval = 0;
        String repeat = Simple.getSharedPrefString("monitors.prepaid.repeat");
        if ((repeat != null) && ! repeat.equals("once")) repeatval = Integer.parseInt(repeat);

        return repeatval * 60;
    }

    private static void checkWarnings()
    {
        int money = Simple.getSharedPrefInt("monitoring.prepaid.money");

        int remindval = 0;
        int warnval = 0;
        int assistval = 0;

        String remind = Simple.getSharedPrefString("monitors.prepaid.remind");
        if ((remind != null) && ! remind.equals("never")) remindval = Integer.parseInt(remind) * 100;

        String warn = Simple.getSharedPrefString("monitors.prepaid.warn");
        if ((warn != null) && ! warn.equals("never")) warnval = Integer.parseInt(warn) * 100;

        String assist = Simple.getSharedPrefString("monitors.prepaid.assistance");
        if ((assist != null) && ! assist.equals("never")) assistval = Integer.parseInt(assist) * 100;

        Log.d(LOGTAG, "checkWarnings:" + money + "/" + remindval + "/" + warnval + "/" + assistval);

        if ((money > remindval) && (money > warnval) && (money > assistval))
        {
            if (money > remindval) resetWarnings();

            return;
        }

        //
        // Some warnings might be due.
        //

        if ((money <= remindval) && (money > warnval))
        {
            lastMessage = Simple.getTrans(R.string.prepaid_manager_remind);
            lastImportance = NotifyIntent.REMINDER;
            lastSpokenTimePref = "monitors.prepaid.lastremind";
        }

        if (money <= warnval)
        {
            lastMessage = Simple.getTrans(R.string.prepaid_manager_warn);
            lastImportance = NotifyIntent.WARNING;
            lastSpokenTimePref = "monitors.prepaid.lastwarn";
        }

        if (money <= assistval)
        {
            lastMessage = Simple.getTrans(R.string.prepaid_manager_warn)
                    + " "
                    + Simple.getTrans(R.string.prepaid_manager_assist);

            lastImportance = NotifyIntent.ASSISTANCE;
            lastSpokenTimePref = "monitors.prepaid.lastwarn";

            //
            // Handle assistance calls quiet in background.
            //

            int repeat = getRepeatMinutes() * 60 * 1000;
            String date = Simple.getSharedPrefString("monitors.prepaid.lastassist");
            String ddue = Simple.timeStampAsISO(Simple.nowAsTimeStamp() - repeat);

            if ((date == null) || ((repeat > 0) && (date.compareTo(ddue) <= 0)))
            {
                //
                // Perform assistance warning now.
                //

                String owner = Simple.getOwnerName();
                String moneyval = Simple.getCurrencyValue(money);

                String text1 = Simple.getTrans(R.string.prepaid_manager_assist_warn, owner);
                String text2 = Simple.getTrans(R.string.prepaid_manager_assist_level, moneyval);

                AssistanceMessage.informAssistance(text1 + " " + text2);

                Simple.setSharedPrefString("monitors.prepaid.lastassist", Simple.nowAsISO());
            }
        }
    }
}
