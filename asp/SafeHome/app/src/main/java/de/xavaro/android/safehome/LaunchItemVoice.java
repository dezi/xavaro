package de.xavaro.android.safehome;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import de.xavaro.android.common.Chooser;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.VoiceIntentRequester;
import de.xavaro.android.common.VoiceIntentResolver;

public class LaunchItemVoice extends LaunchItem implements
        RecognitionListener, VoiceIntentRequester
{
    private final static String LOGTAG = LaunchItemVoice.class.getSimpleName();

    private SpeechRecognizer recognizer;
    private VoiceIntent collected;
    private VoiceIntent intent;

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

    private final Runnable launchVoiceDelayed = new Runnable()
    {
        @Override
        public void run()
        {
            launchVoice();
        }
    };

    @Override
    protected void onMyClick()
    {
        if (type.equals("voice"))
        {
            if (! ArchievementManager.show("voice.longclick", launchVoiceDelayed))
            {
                launchVoice();
            }
        }
    }

    @Override
    protected boolean onMyLongClick()
    {
        if (collected == null)
        {
            collected = new VoiceIntent();
            Context appcontext = Simple.getAppContext();

            if ((appcontext != null) && (appcontext instanceof VoiceIntentResolver))
            {
                ((VoiceIntentResolver) appcontext).onCollectVoiceIntent(collected);
            }
        }

        Log.d(LOGTAG, "LaunchFrameVoice: intents:" + collected.getNumMatches());

        final LaunchFrameWebApp webappFrame = new LaunchFrameWebApp(context);
        webappFrame.setWebAppName("voiceintents");
        webappFrame.setParent(this);

        if (webappFrame.getWebAppView().voice != null)
        {
            webappFrame.getWebAppView().voice.setCollectedIntents(collected);
        }

        ((HomeActivity) context).addViewToBackStack(webappFrame);

        return true;
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
        intent = new VoiceIntent(spoken);

        String logline = intent.getIntent() + ":" + spoken + " (" + percent + ")";
        Simple.makeToast(logline);

        if (intent.getIntent() == null)
        {
            Speak.speak("Ich habe sie nicht verstanden");

            getHandler().postDelayed(launchVoiceDelayed, 1000);

            return;
        }

        Context app = Simple.getActContext();

        if ((app != null) && (app instanceof VoiceIntentResolver))
        {
            ((VoiceIntentResolver) app).onResolveVoiceIntent(intent);

            JSONArray matches = intent.getMatches();
            if (matches == null) return;

            if (intent.getNumMatches() == 0)
            {
                Speak.speak("Ich kann die gewünschte Aktion nicht finden");

                getHandler().postDelayed(launchVoiceDelayed, 1000);

                return;
            }

            if (intent.getNumMatches() > 1)
            {
                Speak.speak("Es gibt mehrere Aktionen die ich ausführen kann");

                Map<String, String> options = new LinkedHashMap<>();

                for (int inx = 0; inx < matches.length(); inx++)
                {
                    JSONObject match = Json.getObject(matches, inx);
                    String identifier = Json.getString(match, "identifier");
                    String response = Json.getString(match, "response");
                    if (response == null) continue;

                    options.put(identifier, response);

                    Log.d(LOGTAG, "onBestVoiceResult: ambigous:" + response);
                }

                Chooser chooser = new Chooser(options);
                chooser.setOnChooserResult(this);
                chooser.showDialog();

                return;
            }

            JSONObject match = Json.getObject(matches, 0);
            String response = Json.getString(match, "response");
            if (response != null) Speak.speak(response);

            //
            // Register for a new voice request to be
            // fired from voice intent executor.
            //

            intent.setRequester(this);

            //
            // Resolve executing instance.
            //

            ((VoiceIntentResolver) app).onExecuteVoiceIntent(intent, 0);
        }
    }

    @Override
    public void onRequestVoiceIntent()
    {
        //
        // Give user a little bit more time for next speech.
        //

        getHandler().postDelayed(launchVoiceDelayed, 1000);
    }

    @Override
    public void onChooserResult(String key)
    {
        JSONArray matches = intent.getMatches();
        if (matches == null) return;

        Context app = Simple.getAppContext();

        if ((app != null) && (app instanceof VoiceIntentResolver))
        {
            for (int inx = 0; inx < matches.length(); inx++)
            {
                JSONObject match = Json.getObject(matches, inx);
                String response = Json.getString(match, "response");
                String identifier = Json.getString(match, "identifier");

                if (! Simple.equals(key, identifier)) continue;

                if (response == null) response = "Ich führe die gewünschte Aktion aus.";

                Speak.speak(response);

                ((VoiceIntentResolver) app).onExecuteVoiceIntent(intent, inx);

                break;
            }
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
