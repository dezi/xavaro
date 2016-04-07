WebAppRequest.onVoiceIntent = function(intent)
{
    console.log("WebAppRequest.onVoiceIntent: ====================>" + JSON.stringify(intent));

    var ca = calculator;

    ca.voiceIntent.innerHTML = intent.command;
}

calculator.createFrame = function()
{
    WebLibSimple.setBGColor(document.body, "#ffffffee");

    var ca = calculator;

    ca.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topDiv", document.body);

    ca.nixYet = WebLibSimple.createAny("center", 0, "25%", 0, null, "nixYet", ca.topDiv);
    WebLibSimple.setFontSpecs(ca.nixYet, 48, "bold", "#666666");
    ca.nixYet.innerHTML = "Noch nicht implementiert...";

    ca.voiceIntent = WebLibSimple.createAny("center", 0, "50%", 0, null, "voiceIntent", ca.topDiv);
    WebLibSimple.setFontSpecs(ca.voiceIntent, 22, "bold", "#666666");
}

calculator.createFrame();
