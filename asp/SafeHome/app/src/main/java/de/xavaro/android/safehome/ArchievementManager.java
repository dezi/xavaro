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

import de.xavaro.android.common.AccessibilityService;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

public class ArchievementManager implements
        DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener
{
    private static final String LOGTAG = ArchievementManager.class.getSimpleName();

    private static final String xpathRoot = "ArchievementManager/archievements/";
    private static final Handler handler = new Handler();

    private static final int MODE_ARCHIVED = 0;
    private static final int MODE_REVOKED  = 1;
    private static final int MODE_RESET    = 2;

    private static JSONObject config;

    public static boolean show(String tag)
    {
        return new ArchievementManager(tag).showCheck();
    }

    public static boolean show(String tag, Runnable postonclose)
    {
        return new ArchievementManager(tag, postonclose).showCheck();
    }

    public static void archieved(String tag)
    {
        archievedInternal(tag, MODE_ARCHIVED);
    }

    public static void revoke(String tag)
    {
        archievedInternal(tag, MODE_REVOKED);
    }

    public static void reset(String tag)
    {
        archievedInternal(tag, MODE_RESET);
    }

    private static void archievedInternal(String tag, int mode)
    {
        if (config == null) readConfig();
        if (config == null) return;

        if (! config.has(tag))
        {
            OopsService.log(LOGTAG, "archievedInternal: archievement <" + tag + "> not found.");

            return;
        }

        String currentXpathpref = xpathRoot + tag;

        String cpath = currentXpathpref + "/archieved";
        int count = PersistManager.getXpathInt(cpath);
        PersistManager.putXpath(cpath, (mode == MODE_ARCHIVED) ? ++count : 0);

        String lpath = currentXpathpref + "/lastarchieved";
        PersistManager.putXpath(lpath, Simple.nowAsISO());

        if (mode == MODE_RESET)
        {
            cpath = currentXpathpref + "/positive";
            PersistManager.putXpath(cpath, 0);

            cpath = currentXpathpref + "/archieved";
            PersistManager.putXpath(cpath, 0);

            cpath = currentXpathpref + "/negative";
            PersistManager.putXpath(cpath, 0);
        }

        PersistManager.flush();

        Log.d(LOGTAG, "archievedInternal: " + tag);
    }

    private static void readConfig()
    {
        Context context = Simple.getAnyContext();

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

    private Context context;

    private String currentTag;
    private String currentPositive;
    private String currentNegative;
    private String currentNeutralb;
    private String currentXpathpref;

    private Runnable currentPost;

    public ArchievementManager(String tag)
    {
        this(tag, null);
    }

    public ArchievementManager(String tag, Runnable postonclose)
    {
        currentTag = tag;
        currentPost = postonclose;
        context = Simple.getAppContext();
        if (config == null) readConfig();
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

                int archieved = PersistManager.getXpathInt(xpath + "/archieved");
                if (archieved > 0) continue;

                int noshows = PersistManager.getXpathInt(xpath + "/negative");
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

    private boolean showCheck()
    {
        if (config == null) return false;

        if (currentTag.endsWith("*"))
        {
            currentTag = pickBestTag(currentTag);
            if (currentTag == null) return false;
        }

        if (! config.has(currentTag))
        {
            OopsService.log(LOGTAG, "showInternal: archievement <" + currentTag + "> not found.");

            return false;
        }

        currentPositive = null;
        currentNegative = null;
        currentNeutralb = null;
        currentXpathpref = "ArchievementManager/archievements/" + currentTag;

        try
        {
            JSONObject archie = config.getJSONObject(currentTag);
            String[] buttons = archie.getString("buttons").split("\\|");

            if (buttons.length > 0) currentPositive = buttons[ 0 ];
            if (buttons.length > 1) currentNegative = buttons[ 1 ];
            if (buttons.length > 2) currentNeutralb = buttons[ 2 ];

            if ((currentNegative != null) && currentNegative.equals("noshow"))
            {
                int archieved = PersistManager.getXpathInt(currentXpathpref + "/archieved");
                if (archieved > 0) return false;

                int noshows = PersistManager.getXpathInt(currentXpathpref + "/negative");
                if (noshows > 0) return false;
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        handler.postDelayed(showRunnable, 100);

        return true;
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
        try
        {
            JSONObject archie = config.getJSONObject(currentTag);

            String title = getTitle(archie.getString("title"));
            String message = archie.getString("message");

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

            dialog.setOnDismissListener(this);

            String dpath = currentXpathpref + "/displays";
            int displaycount = PersistManager.getXpathInt(dpath);
            PersistManager.putXpath(dpath, ++displaycount);

            String lpath = currentXpathpref + "/lastdisplay";
            PersistManager.putXpath(lpath, Simple.nowAsISO());

            PersistManager.flush();
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (currentPost != null)
        {
            handler.postDelayed(currentPost, 100);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if (which == DialogInterface.BUTTON_POSITIVE)
        {
            String path = currentXpathpref + "/positive";
            int poscount = PersistManager.getXpathInt(path);
            PersistManager.putXpath(path, ++poscount);
            PersistManager.flush();
        }

        if (which == DialogInterface.BUTTON_NEGATIVE)
        {
            String path = currentXpathpref + "/negative";
            int negcount = PersistManager.getXpathInt(path);
            PersistManager.putXpath(path, ++negcount);
            PersistManager.flush();
        }

        if ((which == DialogInterface.BUTTON_NEUTRAL) && currentNeutralb.equals("follow"))
        {
            handler.postDelayed(followRunnable, 100);
            currentPost = null;
        }
    }

    private Runnable followRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            follow();
        }
    };

    private void follow()
    {
        if (currentTag.equals("configure.settings.accessibility"))
        {
            AccessibilityService.selectAccessibility.run();
        }

        if (currentTag.equals("configure.settings.homebutton"))
        {
            DefaultApps.setDefaultHome();
        }

        if (currentTag.equals("configure.settings.assistbutton"))
        {
            DefaultApps.setDefaultAssist();
        }
    }
}
