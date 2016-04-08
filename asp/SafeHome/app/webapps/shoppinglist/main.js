WebAppRequest.onVoiceIntent = function(intent)
{
    console.log("WebAppRequest.onVoiceIntent: ====================>" + JSON.stringify(intent));

    var sl = shoppinglist;

    sl.voiceIntent.innerHTML = intent.command;
}

shoppinglist.createFrame = function()
{
    WebLibSimple.setBGColor(document.body, "#ffffffee");

    var sl = shoppinglist;

    sl.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topDiv", document.body);

    sl.nixYet = WebLibSimple.createAny("center", 0, "25%", 0, null, "nixYet", sl.topDiv);
    WebLibSimple.setFontSpecs(sl.nixYet, 48, "bold", "#666666");
    sl.nixYet.innerHTML = "Noch nicht implementiert...";

    sl.voiceIntent = WebLibSimple.createAny("center", 0, "50%", 0, null, "voiceIntent", sl.topDiv);
    WebLibSimple.setFontSpecs(sl.voiceIntent, 22, "bold", "#666666");
}

shoppinglist.createFrame();
