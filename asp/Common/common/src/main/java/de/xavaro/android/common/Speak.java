package de.xavaro.android.common;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;

public class Speak extends UtteranceProgressListener
        implements TextToSpeech.OnInitListener
{
    private static final String LOGTAG = Speak.class.getSimpleName();

    private static Speak instance;

    public static void speak(String text)
    {
        if (instance == null) instance = new Speak();

        instance.addQueue(text);
    }

    public static void speak(String text, SpeakDoneCallback callback)
    {
        if (instance == null) instance = new Speak();

        instance.callback = callback;
        instance.addQueue(text);
    }

    private TextToSpeech ttspeech;
    private final ArrayList<String> texts = new ArrayList<>();
    private SpeakDoneCallback callback;
    private boolean isInited;

    public Speak()
    {
        ttspeech = new TextToSpeech(Simple.getAppContext(), this);
        ttspeech.setOnUtteranceProgressListener(this);
        ttspeech.setPitch(1.0f);
    }

    public void addQueue(String text)
    {
        synchronized (texts)
        {
            texts.add(text);
        }

        if (isInited && ! ttspeech.isSpeaking()) queueAll();
    }

    private void queueAll()
    {
        synchronized (texts)
        {
            while (texts.size() > 0)
            {
                String text = texts.remove(0);

                ttspeech.speak(text, TextToSpeech.QUEUE_ADD, null, text);
            }
        }
    }

    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            queueAll();

            isInited = true;
        }
        else
        {
            Log.d(LOGTAG, "onInit: failed app=" + Simple.getAppContext());
        }
    }

    @Override
    public void onStart(String text)
    {
        Log.d(LOGTAG, "onStart: " + text);
    }

    @Override
    public void onDone(String text)
    {
        Log.d(LOGTAG,"onDone: " + text);

        if (callback != null)
        {
            callback.OnSpeakDone(text);
            callback = null;
        }

        queueAll();
    }

    @Override
    public void onError(String text)
    {
        Log.d(LOGTAG,"onError: " + text);
    }

    public interface SpeakDoneCallback
    {
        void OnSpeakDone(String text);
    }
}
