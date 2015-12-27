package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ArchievementManager implements
        DialogInterface.OnClickListener
{
    private static final String LOGTAG = ArchievementManager.class.getSimpleName();

    public static ArchievementManager instance;

    public static ArchievementManager initialize(Context context)
    {
        if (instance == null) instance = new ArchievementManager(context);

        return instance;
    }

    public static void show(String tag)
    {
        if (instance == null) return;
        instance.showInternal(tag);
    }

    public static void archieved(String tag)
    {
        if (instance == null) return;
        instance.archievedInternal(tag);
    }

    private Context context;
    private JSONObject config;

    private String currentTag;
    private String currentPositive;
    private String currentNegative;
    private String currentXpathpref;

    public ArchievementManager(Context context)
    {
        this.context = context;

        config = StaticUtils.readRawTextResourceJSON(context, R.raw.default_archievements);

        if ((config == null) || ! config.has("archievements"))
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
        if (tag.equals("noshow")) title = "Nicht mehr anzeigen";

        return title;
    }

    private void archievedInternal(String tag)
    {
        if (config == null) return;

        if (!config.has(tag))
        {
            OopsService.log(LOGTAG, "archievedInternal: archievement <" + tag + "> not found.");

            return;
        }

        currentTag = tag;
        currentXpathpref = "ArchievementManager/archievements/" + currentTag;

        String cpath = currentXpathpref + "/archieved";
        int count = SettingsManager.getXpathInt(cpath, true);
        SettingsManager.putXpath(cpath,++count);

        String lpath = currentXpathpref + "/lastarchieved";
        SettingsManager.putXpath(lpath, StaticUtils.nowAsISO());

        SettingsManager.flush();
    }

    private void showInternal(String tag)
    {
        if (config == null) return;

        if (! config.has(tag))
        {
            OopsService.log(LOGTAG,"showInternal: archievement <" + tag + "> not found.");

            return;
        }

        currentTag = tag;
        currentPositive = null;
        currentNegative = null;
        currentXpathpref = "ArchievementManager/archievements/" + currentTag;

        try
        {
            JSONObject archie = config.getJSONObject(currentTag);

            String title = getTitle(archie.getString("title"));
            String message = archie.getString("message");
            String[] buttons = archie.getString("buttons").split("\\|");

            JSONObject jo = SettingsManager.getXpathJSONObject(currentXpathpref, true);
            if (jo != null) Log.d(LOGTAG,jo.toString());
            Log.d(LOGTAG,"========================================");

            if (buttons.length > 0) currentPositive = buttons[ 0 ];
            if (buttons.length > 1) currentNegative = buttons[ 1 ];

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
                builder.setNegativeButton(getButton(currentNegative),this);
            }

            AlertDialog dialog = builder.create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24f);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTransformationMethod(null);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(24f);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTransformationMethod(null);

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
    }
}
