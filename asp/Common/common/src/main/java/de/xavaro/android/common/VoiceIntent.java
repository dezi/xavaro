package de.xavaro.android.common;


import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class VoiceIntent
{
    private static final String LOGTAG = VoiceIntent.class.getSimpleName();

    private static JSONObject intents;

    private String command;
    private String response;
    private String intent;
    private String target;

    public VoiceIntent(String command)
    {
        this.command = command;

        if (intents == null)
        {
            JSONObject global = WebLib.getConfig("intents");
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

    @Nullable
    public String getTarget()
    {
        return target;
    }

    @Nullable
    public String getResponse()
    {
        return response;
    }

    public void setResponse(String response)
    {
        this.response = response;
    }

    public boolean evaluateIntent(String myintent, JSONArray mykeywords)
    {
        if (Simple.equals(intent, myintent) && (command != null) && (mykeywords != null))
        {
            String cmp = " " + command.toLowerCase() + " ";

            for (int inx = 0; inx < mykeywords.length(); inx++)
            {
                String wrd = Json.getString(mykeywords, inx);
                if (wrd == null) continue;

                if (cmp.contains(" " + wrd.toLowerCase() + " "))
                {
                    target = wrd;
                    return true;
                }
            }
        }

        return false;
    }
}
