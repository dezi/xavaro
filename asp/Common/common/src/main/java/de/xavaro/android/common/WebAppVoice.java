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

    public WebAppVoice(WebView webview)
    {
        this.webview = webview;

        if (SpeechRecognizer.isRecognitionAvailable(Simple.getAppContext()))
        {
            recognizer = SpeechRecognizer.createSpeechRecognizer(Simple.getAppContext());
            recognizer.setRecognitionListener(this);
        }
    }

    private final Runnable startSpeechRecognizerUI = new Runnable()
    {
        @Override
        public void run()
        {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizer.startListening(intent);
        }
    };

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
}
