WebAppRequest.onVoiceIntent = function(intent)
{
    console.log("WebAppRequest.onVoiceIntent: ====================>" + intent.command);

    WebAppSpeak.speak("Frag mich nicht, ich bin schlecht in Mathe");
}


