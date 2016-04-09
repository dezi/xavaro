WebAppRequest.onVoiceIntent = function(intent)
{
    var sl = shoppinglist;

    sl.intent = intent;
    sl.action = sl.intent.action;
    sl.command = sl.intent.command;

    console.log("WebAppRequest.onVoiceIntent: command:" + sl.command);

    //
    // Clean command from action triggers.
    //

    sl.cleancmd = sl.command;

    if (sl.intent.triggers)
    {
        for (var inx in sl.intent.triggers)
        {
            var trigger = sl.intent.triggers[ inx ];
            var regex = new RegExp(trigger, "i");

            sl.cleancmd = sl.cleancmd.replace(regex, "").trim();
        }
    }

    console.log("WebAppRequest.onVoiceIntent: clean:" + sl.cleancmd);

    //
    // Check clean rest of command.
    //

    sl.answer = WebLibStrings.getFormat(sl.intent.answer, sl.cleancmd);
    WebAppSpeak.speak(sl.answer, true);

    console.log("WebAppRequest.onVoiceIntent: answer:" + sl.answer);
}

WebAppSpeak.onSpeakDone = function(text)
{
    console.log("WebAppSpeak.onSpeakDone: fettig:" + text);

    WebAppRequest.requestVoiceIntent();
}