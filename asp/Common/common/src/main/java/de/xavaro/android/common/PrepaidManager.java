package de.xavaro.android.common;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

public class PrepaidManager implements AccessibilityService.MessageServiceCallback
{
    private static final String LOGTAG = PrepaidManager.class.getSimpleName();

    public static boolean makeRequest(
            PrepaidManagerCallback callback,
            boolean quiet,
            JSONObject slug,
            String loadcode
    )
    {
        if (! AccessibilityService.checkEnabled()) return false;

        PrepaidManager pm = new PrepaidManager();
        return pm.makeRequestInternal(callback, quiet, slug, loadcode);
    }

    private PrepaidManagerCallback callback;
    private JSONObject slug;
    private boolean quiet;

    private boolean makeRequestInternal(
            PrepaidManagerCallback callback,
             boolean quiet,
            JSONObject slug,
            String loadcode
            )
    {
        String phonenumber;

        if (loadcode == null)
        {
            phonenumber = Simple.getSharedPrefString("calls.monitors.phonenumber:prepaid");
            if (phonenumber == null) return false;
        }
        else
        {
            phonenumber = Simple.getSharedPrefString("calls.monitors.prepaidload:prepaid");
            if (phonenumber == null) return false;

            phonenumber += loadcode + "#";
        }

        this.callback = callback;
        this.quiet = quiet;
        this.slug = slug;

        phonenumber = phonenumber.replace("#", "%23");

        AccessibilityService.subscribe(this);
        Simple.makePost(unsubscribeMessage, 5000);

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

                if (callback != null) callback.onPrepaidBalanceReceived(text, money, slug);

                return quiet ? AccessibilityService.GLOBAL_ACTION_BACK : 0;
            }
        }

        return 0;
    }

    public interface PrepaidManagerCallback
    {
        void onPrepaidBalanceReceived(String text, int money, JSONObject slug);
    }
}
