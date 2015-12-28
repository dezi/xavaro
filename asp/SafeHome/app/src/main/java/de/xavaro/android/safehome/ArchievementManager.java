package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ArchievementManager implements
        DialogInterface.OnClickListener
{
    private static final String LOGTAG = ArchievementManager.class.getSimpleName();

    private static final Handler handler = new Handler();

    public static ArchievementManager instance;

    public static ArchievementManager initialize(Context context)
    {
        if (instance == null) instance = new ArchievementManager(context);

        return instance;
    }

    public static void show(String tag)
    {
        if (instance == null) return;

        instance.setTag(tag);

        handler.postDelayed(instance.showRunnable, 100);
    }

    public static void archieved(String tag)
    {
        if (instance == null) return;

        instance.setTag(tag);

        instance.archievedInternal(false);
    }

    public static void revoke(String tag)
    {
        if (instance == null) return;

        instance.setTag(tag);

        instance.archievedInternal(true);
    }

    private Context context;
    private JSONObject config;

    private String currentTag;
    private String currentPositive;
    private String currentNegative;
    private String currentNeutralb;
    private String currentXpathpref;

    public ArchievementManager(Context context)
    {
        this.context = context;

        config = StaticUtils.readRawTextResourceJSON(context, R.raw.default_archievements);

        if ((config == null) || !config.has("archievements"))
        {
            Toast.makeText(context, "Keine <archievements> gefunden.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            config = config.getJSONObject("archievements");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private void setTag(String tag)
    {
        currentTag = tag;
    }

    @Nullable
    private String getTitle(String tag)
    {
        String title = null;

        if (tag.equals("important")) title = "Wichtiger Hinweis:";

        return title;
    }

    @Nullable
    private String getButton(String tag)
    {
        String title = null;

        if (tag.equals("ok")) title = "Ok";
        if (tag.equals("next")) title = "Weiter";
        if (tag.equals("later")) title = "Später";
        if (tag.equals("follow")) title = "Gute Idee";
        if (tag.equals("noshow")) title = "Nicht mehr anzeigen";

        return title;
    }

    private void archievedInternal(boolean revoke)
    {
        if (config == null) return;

        if (!config.has(currentTag))
        {
            OopsService.log(LOGTAG, "archievedInternal: archievement <" + currentTag + "> not found.");

            return;
        }

        currentXpathpref = "ArchievementManager/archievements/" + currentTag;

        String cpath = currentXpathpref + "/archieved";
        int count = SettingsManager.getXpathInt(cpath, true);
        SettingsManager.putXpath(cpath,revoke ? 0 : ++count);

        String lpath = currentXpathpref + "/lastarchieved";
        SettingsManager.putXpath(lpath, StaticUtils.nowAsISO());

        SettingsManager.flush();

        Log.d(LOGTAG,"archievedInternal: " + currentTag);
    }

    @Nullable
    private String pickBestTag(String prefix)
    {
        String besttag = null;
        int bestprio = 0;

        prefix = prefix.substring(0, prefix.length() - 1);

        Iterator<String> keysIterator = config.keys();

        while (keysIterator.hasNext())
        {
            String tag = keysIterator.next();

            if (! tag.startsWith(prefix)) continue;

            try
            {
                JSONObject archie = config.getJSONObject(tag);

                String xpath = "ArchievementManager/archievements/" + tag;

                int archieved = SettingsManager.getXpathInt(xpath + "/archieved", true);
                if (archieved > 0) continue;

                int noshows = SettingsManager.getXpathInt(xpath + "/negative", true);
                if (noshows > 0) continue;

                int prio = archie.has("prio") ? archie.getInt("prio") : 0;

                if (prio >= bestprio)
                {
                    besttag = tag;
                    bestprio = prio;
                }
            }
            catch (JSONException ignore)
            {
            }
        }

        return besttag;
    }

    private final Runnable showRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            showInternal();
        }
    };

    private void showInternal()
    {
        if (config == null) return;

        if (currentTag.endsWith("*"))
        {
            currentTag = pickBestTag(currentTag);
            if (currentTag == null) return;
        }

        if (! config.has(currentTag))
        {
            OopsService.log(LOGTAG,"showInternal: archievement <" + currentTag + "> not found.");

            return;
        }

        currentPositive = null;
        currentNegative = null;
        currentNeutralb = null;
        currentXpathpref = "ArchievementManager/archievements/" + currentTag;

        try
        {
            JSONObject archie = config.getJSONObject(currentTag);

            String title = getTitle(archie.getString("title"));
            String message = archie.getString("message");
            String[] buttons = archie.getString("buttons").split("\\|");

            JSONObject jo = SettingsManager.getXpathJSONObject(currentXpathpref, true);
            if (jo != null) Log.d(LOGTAG,"showInternal: " + currentTag + "=" + jo.toString());
            Log.d(LOGTAG,"========================================");

            if (buttons.length > 0) currentPositive = buttons[ 0 ];
            if (buttons.length > 1) currentNegative = buttons[ 1 ];
            if (buttons.length > 2) currentNeutralb = buttons[ 2 ];

            if ((currentNegative != null) && currentNegative.equals("noshow"))
            {
                int archieved = SettingsManager.getXpathInt(currentXpathpref + "/archieved", true);
                if (archieved > 0) return;

                int noshows = SettingsManager.getXpathInt(currentXpathpref + "/negative", true);
                if (noshows > 0) return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle(title);

            final TextView textview = new TextView(context);

            textview.setPadding(40, 40, 40, 40);
            textview.setTextSize(24f);
            textview.setText(message);

            builder.setView(textview);

            if (currentPositive != null)
            {
                builder.setPositiveButton(getButton(currentPositive),this);
            }

            if (currentNegative != null)
            {
                builder.setNegativeButton(getButton(currentNegative), this);
            }

            if (currentNeutralb != null)
            {
                builder.setNeutralButton(getButton(currentNeutralb), this);
            }

            AlertDialog dialog = builder.create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24f);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTransformationMethod(null);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(24f);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTransformationMethod(null);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(24f);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTransformationMethod(null);

            String dpath = currentXpathpref + "/displays";
            int displaycount = SettingsManager.getXpathInt(dpath, true);
            SettingsManager.putXpath(dpath, ++displaycount);

            String lpath = currentXpathpref + "/lastdisplay";
            SettingsManager.putXpath(lpath, StaticUtils.nowAsISO());

            SettingsManager.flush();
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG,ex);
        }
    }

    public void onClick(DialogInterface dialog, int which)
    {
        if (which == DialogInterface.BUTTON_POSITIVE)
        {
            String path = currentXpathpref + "/positive";
            int poscount = SettingsManager.getXpathInt(path, true);
            SettingsManager.putXpath(path,++poscount);
            SettingsManager.flush();
        }

        if (which == DialogInterface.BUTTON_NEGATIVE)
        {
            String path = currentXpathpref + "/negative";
            int negcount = SettingsManager.getXpathInt(path, true);
            SettingsManager.putXpath(path,++negcount);
            SettingsManager.flush();
        }

        if ((which == DialogInterface.BUTTON_NEUTRAL) && currentNeutralb.equals("follow"))
        {
            handler.postDelayed(instance.folloRunnable, 100);
        }
    }

    private Runnable folloRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            follow();
        }
    };

    private void follow()
    {
        if (currentTag.equals("configure.settings.homebutton"))
        {
            DitUndDat.DefaultApps.setDefaultHome(context);
        }

        if (currentTag.equals("configure.settings.assistbutton"))
        {
            DitUndDat.DefaultApps.setDefaultAssist(context);
        }
    }
}
