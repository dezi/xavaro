package de.xavaro.android.common;

public interface VoiceIntentResolver
{
    boolean onResolveVoiceIntent(VoiceIntent voiceintent);
    boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index);
}
