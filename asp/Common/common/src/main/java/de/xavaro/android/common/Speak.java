package de.xavaro.android.common;

import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;

public class Speak extends UtteranceProgressListener implements TextToSpeech.OnInitListener
{
    private static final String LOGTAG = Speak.class.getSimpleName();

    private static Speak instance;

    public static void speak(String text)
    {
        speak(text, -1, null);
    }

    public static void speak(String text, int volume)
    {
        speak(text, volume, null);
    }

    public static void speak(String text, SpeakDoneCallback callback)
    {
        speak(text, -1, callback);
    }

    public static void speak(String text, int volume, SpeakDoneCallback callback)
    {
        if (instance == null) instance = new Speak();

        SpeakData data = new SpeakData(text);

        data.volume = volume;
        data.callback = callback;

        instance.addQueue(data);
    }

    public static void shutdown()
    {
        if (instance != null)
        {
            instance.shutdownInstance();
            instance = null;
        }
    }

    private static class SpeakData
    {
        public SpeakData(String text)
        {
            this.text = text;
        }

        public String text;
        public int volume = -1;
        public int oldvolume = -1;
        public SpeakDoneCallback callback;
    }

    private final ArrayList<SpeakData> texts = new ArrayList<>();
    private TextToSpeech ttspeech;
    private SpeakData current;
    private boolean isInited;

    public Speak()
    {
        ttspeech = new TextToSpeech(Simple.getAnyContext(), this);
        ttspeech.setOnUtteranceProgressListener(this);
        ttspeech.setPitch(1.0f);
    }

    public void shutdownInstance()
    {
        ttspeech.shutdown();
        ttspeech = null;
        isInited = false;
    }

    public void addQueue(SpeakData data)
    {
        synchronized (texts)
        {
            texts.add(data);
        }

        if (isInited && ! ttspeech.isSpeaking()) queueNext();
    }

    private void queueNext()
    {
        synchronized (texts)
        {
            if ((texts.size() > 0) && ! ttspeech.isSpeaking())
            {
                current = texts.remove(0);

                if (current.volume > 0)
                {
                    current.oldvolume = Simple.getSpeechVolume();
                    Simple.raiseSpeechVolume(current.volume);
                }

                ttspeech.speak(current.text, TextToSpeech.QUEUE_ADD, null, current.text);
            }
        }
    }

    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            isInited = true;

            queueNext();
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

        if (current != null)
        {
            if (current.oldvolume >= 0) Simple.setSpeechVolume(current.oldvolume);
            if (current.callback != null) current.callback.OnSpeakDone(text);
            current = null;
        }

        queueNext();
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
