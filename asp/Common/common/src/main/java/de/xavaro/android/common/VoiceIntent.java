package de.xavaro.android.common;


import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class VoiceIntent
{
    private static final String LOGTAG = VoiceIntent.class.getSimpleName();

    private static JSONObject intents;

    private final JSONArray matches = new JSONArray();

    private String command;
    private String intent;

    public VoiceIntent(String command)
    {
        this.command = command;

        if (intents == null)
        {
            JSONObject global = WebLib.getLocaleConfig("intents");
            if (global != null) intents = Json.getObject(global, "intents");
        }

        if (intents == null)
        {
            Log.e(LOGTAG, "Constructor: cannot read intents definitions.");

            return;
        }

        if (command != null)
        {
            String cmp = " " + command.toLowerCase() + " ";

            Iterator<String> keysIterator = intents.keys();

            while (keysIterator.hasNext())
            {
                String intentkey = keysIterator.next();

                JSONArray targets = Json.getArray(intents, intentkey);
                if (targets == null) continue;

                for (int inx = 0; inx < targets.length(); inx++)
                {
                    String wrd = Json.getString(targets, inx);
                    if (wrd == null) continue;

                    if (cmp.contains(" " + wrd.toLowerCase() + " "))
                    {
                        intent = intentkey;
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    public String getCommand()
    {
        return command;
    }

    @Nullable
    public String getIntent()
    {
        return intent;
    }

    public JSONArray getMatches()
    {
        return matches;
    }

    public int getNumMatches()
    {
        return matches.length();
    }

    private boolean keepIfBetter(JSONObject match)
    {
        String target = Json.getString(match, "target");
        if (target == null) return false;

        boolean addme = (matches.length() == 0);

        for (int inx = 0; inx < matches.length(); inx++)
        {
            JSONObject oldmatch = Json.getObject(matches, inx);
            String oldtarget = Json.getString(oldmatch, "target");
            if (oldtarget == null) continue;

            if (target.length() == oldtarget.length())
            {
                addme = true;
                break;
            }

            if (target.length() > oldtarget.length())
            {
                Json.remove(matches, inx--);
                addme = true;
            }
        }

        if (addme) Json.put(matches, match);

        return addme;
    }

    public boolean evaluateIntents(JSONArray myintents, String identifier)
    {
        boolean foundone = false;

        if (myintents != null)
        {
            for (int inx = 0; inx < myintents.length(); inx++)
            {
                if (evaluateIntent(Json.getObject(myintents, inx), identifier))
                {
                    foundone = true;
                }
            }
        }

        return foundone;
    }

    public boolean evaluateIntent(JSONObject myintent, String identifier)
    {
        boolean foundone = false;

        if ((command != null) && (myintent != null) && Json.equals(myintent, "action", intent))
        {
            String cmp = " " + command.toLowerCase() + " ";

            JSONArray mykeywords = Json.getArray(myintent, "keywords");

            if (mykeywords != null)
            {
                for (int inx = 0; inx < mykeywords.length(); inx++)
                {
                    String wrd = Json.getString(mykeywords, inx);
                    if (wrd == null) continue;

                    if (cmp.contains(" " + wrd.toLowerCase() + " "))
                    {
                        JSONObject match = Json.clone(myintent);

                        Json.put(match, "target", wrd);
                        Json.put(match, "identifier", identifier);

                        Log.d(LOGTAG, "evaluateIntent: " + identifier + "=" + wrd);

                        if (keepIfBetter(match)) foundone = true;
                    }
                }
            }
        }

        return foundone;
    }
}
