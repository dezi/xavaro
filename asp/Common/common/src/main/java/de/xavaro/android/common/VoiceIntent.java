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

    public VoiceIntent()
    {
        this(null);
    }

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

    public JSONObject getMatch(int index)
    {
        if ((matches != null) && (index < matches.length()))
        {
            return Json.getObject(matches, index);
        }

        return null;
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
        int score = Json.getInt(match, "score");

        boolean addme = (matches.length() == 0);

        for (int inx = 0; inx < matches.length(); inx++)
        {
            JSONObject oldmatch = Json.getObject(matches, inx);
            if (oldmatch == null) continue;

            int oldscore = Json.getInt(oldmatch, "score");

            if (score == oldscore)
            {
                addme = true;
                break;
            }

            if (score > oldscore)
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
                JSONArray targets = new JSONArray();

                int score = 0;

                for (int inx = 0; inx < mykeywords.length(); inx++)
                {
                    String wrd = Json.getString(mykeywords, inx);
                    if (wrd == null) continue;

                    if (cmp.contains(" " + wrd.toLowerCase() + " "))
                    {
                        Log.d(LOGTAG, "evaluateIntent: " + identifier + "=" + wrd);

                        Json.put(targets, wrd);
                        score++;
                    }
                }

                if (score > 0)
                {
                    JSONObject match = Json.clone(myintent);

                    Json.put(match, "score", score);
                    Json.put(match, "targets", targets);
                    Json.put(match, "identifier", identifier);

                    if (keepIfBetter(match)) foundone = true;
                }
            }
        }

        return foundone;
    }

    public boolean collectIntents(JSONArray myintents, String identifier)
    {
        boolean foundone = false;

        if (myintents != null)
        {
            for (int inx = 0; inx < myintents.length(); inx++)
            {
                if (collectIntent(Json.getObject(myintents, inx), identifier))
                {
                    foundone = true;
                }
            }
        }

        return foundone;
    }

    public boolean collectIntent(JSONObject myintent, String identifier)
    {
        if (myintent == null) return false;

        JSONObject match = Json.clone(myintent);
        Json.put(match, "identifier", identifier);
        Json.put(matches, match);

        return true;
    }
}
