package de.xavaro.android.common;

public interface VoiceIntentResolver
{
    boolean onResolveVoiceIntent(VoiceIntent voiceintent);
    void onExecuteVoiceIntent(VoiceIntent voiceintent);
}
