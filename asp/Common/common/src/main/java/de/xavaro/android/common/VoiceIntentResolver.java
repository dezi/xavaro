package de.xavaro.android.common;

public interface VoiceIntentResolver
{
    void onCollectVoiceIntent(VoiceIntent voiceintent);
    void onResolveVoiceIntent(VoiceIntent voiceintent);
    boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index);
}
