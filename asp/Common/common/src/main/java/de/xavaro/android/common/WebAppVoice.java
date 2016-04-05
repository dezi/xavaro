package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.webkit.WebView;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WebAppVoice implements RecognitionListener
{
    private static final String LOGTAG = WebAppVoice.class.getSimpleName();

    private final WebView webview;
    private SpeechRecognizer recognizer;
    private VoiceIntent collected;

    public WebAppVoice(WebView webview)
    {
        this.webview = webview;

        if (SpeechRecognizer.isRecognitionAvailable(Simple.getAppContext()))
        {
            recognizer = SpeechRecognizer.createSpeechRecognizer(Simple.getAppContext());
            recognizer.setRecognitionListener(this);
        }
    }

    public void setCollectedIntents(VoiceIntent collected)
    {
        this.collected = collected;
    }

    @JavascriptInterface
    public String getCollectedIntents()
    {
        if (collected != null)
        {
            return collected.getMatches().toString();
        }

        return "[]";
    }

    @JavascriptInterface
    public boolean checkSpeechRecognizer()
    {
        return (recognizer != null);
    }

    @JavascriptInterface
    public boolean startSpeechRecognizer()
    {
        if (recognizer == null) return false;

        webview.getHandler().post(startSpeechRecognizerUI);

        return true;
    }

    @JavascriptInterface
    public String evaluateCommand(String command)
    {
        if (collected != null)
        {
            JSONArray intents = collected.getMatches();

            if (intents != null)
            {
                VoiceIntent evaluate = new VoiceIntent(command);

                for (int inx = 0; inx < intents.length(); inx++)
                {
                    JSONObject intent = Json.getObject(intents, inx);
                    String identifier = Json.getString(intent, "identifier");
                    if (identifier == null) continue;

                    evaluate.evaluateIntent(intent, identifier);
                }

                return evaluate.getMatches().toString();
            }
        }

        return "[]";
    }

    //region Recognition handling

    private final Runnable startSpeechRecognizerUI = new Runnable()
    {
        @Override
        public void run()
        {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizer.startListening(intent);
        }
    };

    private void makeCallbackScript(String callback)
    {
        makeCallbackScript(callback, null);
    }

    private void makeCallbackScript(String callback, String data)
    {
        String script = ""
                + "if (! WebAppVoice." + callback + ")"
                + "{"
                + "    WebAppVoice." + callback + " = function()"
                + "    {"
                + "    }"
                + "}";

        if (data == null)
        {
            script += "WebAppVoice." + callback + "();";
        }
        else
        {
            script += "WebAppVoice." + callback + "(" + data + ");";
        }

        webview.evaluateJavascript(script, null);
    }

    //endregion Recognition handling

    //region RecognitionListener interface

    @Override
    public void onReadyForSpeech(Bundle params)
    {
        makeCallbackScript("onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech()
    {
        makeCallbackScript("onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB)
    {
    }

    @Override
    public void onBufferReceived(byte[] buffer)
    {
    }

    @Override
    public void onEndOfSpeech()
    {
        makeCallbackScript("onEndOfSpeech");
    }

    @Override
    public void onError(int error)
    {
        makeCallbackScript("onError");
    }

    @Override
    public void onResults(Bundle results)
    {
        ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        float[] conf = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

        if ((text != null) && (conf != null))
        {
            JSONArray jsresults = new JSONArray();

            for (int inx = 0; inx < text.size(); inx++)
            {
                String spoken = text.get(inx);
                int percent = Math.round(conf[ inx ] * 100);

                JSONObject jsresult = new JSONObject();

                Json.put(jsresult, "spoken", spoken);
                Json.put(jsresult, "confidence", percent);

                Json.put(jsresults, jsresult);
            }

            makeCallbackScript("onResults", Json.toPretty(jsresults));
        }
        else
        {
            makeCallbackScript("onResults");
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults)
    {
    }

    @Override
    public void onEvent(int eventType, Bundle params)
    {
    }

    //endregion RecognitionListener interface
}
