package de.xavaro.android.common;


import android.support.annotation.Nullable;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class VoiceIntent
{
    private static final String LOGTAG = VoiceIntent.class.getSimpleName();

    private static JSONObject global;
    private static JSONObject actions;
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

        if (global == null)
        {
            global = WebLib.getLocaleConfig("intents");

            if (global != null)
            {
                actions = Json.getObject(global, "actions");
                intents = Json.getObject(global, "intents");
            }
        }

        if (actions == null)
        {
            Log.e(LOGTAG, "VoiceIntent: cannot read actions definitions.");

            return;
        }

        if (command != null)
        {
            String cmp = " " + command.toLowerCase() + " ";

            Iterator<String> keysIterator = actions.keys();

            while (keysIterator.hasNext())
            {
                String intentkey = keysIterator.next();

                JSONArray targets = Json.getArray(actions, intentkey);
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
    public JSONObject getIntent(String key)
    {
        JSONObject json = Json.getObject(intents, key);
        return (json != null) ? Json.clone(json) : null;
    }

    @Nullable
    public JSONArray getIntents(String key)
    {
        JSONArray json = Json.getArray(intents, key);
        return (json != null) ? Json.clone(json) : null;
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
        String ident = Json.getString(match, "identifier");
        int score = Json.getInt(match, "score");

        boolean addme = (matches.length() == 0);

        for (int inx = 0; inx < matches.length(); inx++)
        {
            JSONObject oldmatch = Json.getObject(matches, inx);
            if (oldmatch == null) continue;

            String oldident = Json.getString(oldmatch, "identifier");
            int oldscore = Json.getInt(oldmatch, "score");

            if ((score == oldscore) && ! Simple.equals(ident, oldident))
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

    public boolean evaluateIntent(JSONObject myintent)
    {
        return evaluateIntent(null, myintent);
    }

    public boolean evaluateIntents(JSONObject config, JSONArray myintents)
    {
        boolean foundone = false;

        if (myintents != null)
        {
            for (int inx = 0; inx < myintents.length(); inx++)
            {
                if (evaluateIntent(config, Json.getObject(myintents, inx)))
                {
                    foundone = true;
                }
            }
        }

        return foundone;
    }

    public boolean evaluateIntent(JSONObject config, JSONObject myintent)
    {
        boolean foundone = false;

        if ((command != null) && (myintent != null) && Json.equals(myintent, "action", intent))
        {
            if (config != null) prepareIntent(config, myintent, false);

            String cmp = " " + command.toLowerCase() + " ";

            String response = Json.getString(myintent, "response");
            String identifier = Json.getString(myintent, "identifier");
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
                        Log.d(LOGTAG, "evaluateIntent: " + identifier + "=" + wrd + ":" + response);

                        Json.put(targets, wrd);
                        score++;
                    }
                }

                if (score > 0)
                {
                    if (myintent.has("subtypetag"))
                    {
                        //
                        // Give a score bonus for having a tag or a name,
                        // which is more specific than an entry w/o tag.
                        //

                        score += 1;
                    }

                    JSONObject match = Json.clone(myintent);

                    Json.put(match, "score", score);
                    Json.put(match, "targets", targets);

                    if (keepIfBetter(match)) foundone = true;
                }
            }
        }

        return foundone;
    }

    public void collectIntents(JSONObject config, JSONArray intents)
    {
        if (intents != null)
        {
            for (int inx = 0; inx < intents.length(); inx++)
            {
                collectIntent(config, Json.getObject(intents, inx));
            }
        }
    }

    public void collectIntent(JSONObject config, JSONObject intent)
    {
        if ((config == null) || (intent == null)) return;

        JSONObject match = Json.clone(intent);

        prepareIntent(config, match, true);

        //
        // Check for duplicates. Duplicates occur if a launchitem
        // is displayed on the home screen and in the folder of
        // the application area it belongs to.
        //

        for (int inx = 0; inx < matches.length(); inx++)
        {
            JSONObject oldmatch = Json.getObject(matches, inx);

            if (Json.equals(match, "identifier", oldmatch) &&
                    Json.equals(match, "type", oldmatch) &&
                    Json.equals(match, "subtype", oldmatch) &&
                    Json.equals(match, "subtypetag", oldmatch))
            {
                return;
            }
        }

        Json.put(matches, match);
    }

    private void prepareIntent(JSONObject config, JSONObject intent, boolean prepicon)
    {
        if (intent != null)
        {
            if (config.has("identifier"))
            {
                Json.put(intent, "identifier", Json.getString(config, "identifier"));
            }

            if (config.has("type"))
            {
                Json.put(intent, "type", Json.getString(config, "type"));
            }

            if (config.has("subtype"))
            {
                Json.put(intent, "subtype", Json.getString(config, "subtype"));
            }

            if (config.has("apkname"))
            {
                Json.put(intent, "subtypetag", Json.getString(config, "apkname"));
            }

            if (config.has("name"))
            {
                Json.put(intent, "subtypetag", Json.getString(config, "name"));
            }

            if (config.has("phonenumber"))
            {
                Json.put(intent, "subtypetag", Json.getString(config, "phonenumber"));
            }

            if (config.has("waphonenumber"))
            {
                Json.put(intent, "subtypetag", Json.getString(config, "waphonenumber"));
            }

            if (config.has("identity"))
            {
                Json.put(intent, "subtypetag", Json.getString(config, "identity"));
            }

            if (config.has("skypename"))
            {
                Json.put(intent, "subtypetag", Json.getString(config, "skypename"));
            }

            if (intent.has("tag"))
            {
                Json.put(intent, "subtypetag", Json.getString(intent, "tag"));
            }

            if (prepicon)
            {
                if (config.has("iconres"))
                {
                    int iconres = Json.getInt(config, "iconres");

                    if (iconres != 0)
                    {
                        Json.put(intent, "iconres", iconres);
                    }
                }

                if (config.has("icon"))
                {
                    String iconref = Json.getString(config, "icon");

                    if (Simple.startsWith(iconref, "http://") || Simple.startsWith(iconref, "https://"))
                    {
                        if (config.has("name"))
                        {
                            String iconname = Json.getString(config, "name");
                            String iconpath = CacheManager.getWebIconPath(iconname, iconref);
                            if (iconpath != null) Json.put(intent, "icon", "local://" + iconpath);
                        }
                    }
                    else
                    {
                        int resourceId = Simple.getIconResourceId(iconref);

                        if (resourceId > 0)
                        {
                            Json.put(intent, "icon", resourceId);
                        }
                    }
                }

                if (config.has("apkname"))
                {
                    String packageName = Json.getString(config, "apkname");

                    if (packageName != null)
                    {
                        String iconpath = CacheManager.getAppIconPath(packageName);
                        if (iconpath != null) Json.put(intent, "icon", "local://" + iconpath);
                    }
                }
            }
        }
    }
}
