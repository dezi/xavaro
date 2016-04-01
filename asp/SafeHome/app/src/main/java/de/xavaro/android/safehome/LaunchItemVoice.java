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
import de.xavaro.android.common.WebLib;

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

    //region RecognitionListener

    @Override
    public void onReadyForSpeech(Bundle params)
    {
        Log.d(LOGTAG, "onReadyForSpeech:");
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
                String logline = text.get(inx) + " (" + Math.round(conf[ inx ] * 100) + "%)";
                if (inx == 0) Simple.makeToast(logline);
                Log.d(LOGTAG, "result=" + logline);
            }
        }
    }

    //endregion
}
