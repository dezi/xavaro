package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.VoiceIntentResolver;

public class LaunchItemVoice extends LaunchItem implements RecognitionListener
{
    private final static String LOGTAG = LaunchItemVoice.class.getSimpleName();

    private SpeechRecognizer recognizer;

    public LaunchItemVoice(Context context)
    {
        super(context);
    }

    public static JSONArray getConfig()
    {
        JSONArray launchitems = new JSONArray();

        boolean available = SpeechRecognizer.isRecognitionAvailable(Simple.getAppContext());

        if (available)
        {
            JSONObject launchitem = new JSONObject();

            Json.put(launchitem, "type", "voice");
            Json.put(launchitem, "label", "Sprechen");
            Json.put(launchitem, "order", 170);

            Json.put(launchitems, launchitem);
        }

        return launchitems;
    }

    @Override
    protected void setConfig()
    {
        icon.setImageResource(CommonConfigs.IconResVoice);
        overicon.setImageResource(CommonConfigs.IconResVoiceListen);
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("voice")) launchVoice();
    }

    private void launchVoice()
    {
        if (recognizer == null)
        {
            recognizer = SpeechRecognizer.createSpeechRecognizer(Simple.getAppContext());
            recognizer.setRecognitionListener(this);
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer.startListening(intent);
    }

    private final Runnable voiceIsListening = new Runnable()
    {
        @Override
        public void run()
        {
            overlay.setVisibility(VISIBLE);
        }
    };

    private final Runnable voiceIsInactive = new Runnable()
    {
        @Override
        public void run()
        {
            overlay.setVisibility(INVISIBLE);
        }
    };

    private void onBestVoiceResult(String spoken, int percent)
    {
        VoiceIntent intent = new VoiceIntent(spoken);

        String logline = intent.getIntent() + ":" + spoken + " (" + percent + ")";
        Simple.makeToast(logline);

        if (intent.getIntent() == null)
        {
            Speak.speak("Ich habe sie nicht verstanden");
            return;
        }

        Context app = Simple.getAppContext();

        if ((app != null) && app instanceof VoiceIntentResolver)
        {
            boolean result = ((VoiceIntentResolver) app).onResolveVoiceIntent(intent);

            if (! result)
            {
                Speak.speak("Ich kann die gewünschte Aktion nicht finden");
                return;
            }

            JSONArray matches = intent.getMatches();
            if (matches == null) return;

            if (intent.getNumMatches() > 1)
            {
                Speak.speak("Es gibt mehrere Aktionen die ich ausführen kann");

                for (int inx = 0; inx < matches.length(); inx++)
                {
                    JSONObject match = Json.getObject(matches, inx);
                    String response = Json.getString(match, "response");

                    Log.d(LOGTAG, "onBestVoiceResult: ambigous:" + response);
                }

                return;
            }

            JSONObject match = Json.getObject(matches, 0);
            String response = Json.getString(match, "response");
            if (response == null) response = "Ich führe die gewünschte Aktion aus.";

            Speak.speak(response);

            ((VoiceIntentResolver) app).onExecuteVoiceIntent(intent, 0);
        }
    }

    //region RecognitionListener

    @Override
    public void onReadyForSpeech(Bundle params)
    {
        Log.d(LOGTAG, "onReadyForSpeech:");

        getHandler().post(voiceIsListening);
    }

    @Override
    public void onBeginningOfSpeech()
    {
        Log.d(LOGTAG, "onBeginningOfSpeech:");
    }

    @Override
    public void onRmsChanged(float rmsdB)
    {
        //Log.d(LOGTAG, "onRmsChanged:" + rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer)
    {
        Log.d(LOGTAG, "onBufferReceived:");
    }

    @Override
    public void onEndOfSpeech()
    {
        Log.d(LOGTAG, "onEndOfSpeech:");
    }

    @Override
    public void onError(int error)
    {
        Log.d(LOGTAG, "onError:");

        getHandler().post(voiceIsInactive);
    }

    @Override
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(LOGTAG, "onPartialResults:");
    }

    @Override
    public void onEvent(int eventType, Bundle params)
    {
        Log.d(LOGTAG, "onEvent:");
    }

    @Override
    public void onResults(Bundle results)
    {
        Log.d(LOGTAG, "onResults: " + results);

        ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        float[] conf = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

        if ((text != null) && (conf != null))
        {
            for (int inx = 0; inx < text.size(); inx++)
            {
                String spoken = text.get(inx);
                int percent = Math.round(conf[ inx ] * 100);

                String logline = spoken + " (" + percent + "%)";

                Log.d(LOGTAG, "result=" + logline);

                if (inx == 0) onBestVoiceResult(spoken, percent);
            }
        }

        getHandler().post(voiceIsInactive);
    }

    //endregion
}
