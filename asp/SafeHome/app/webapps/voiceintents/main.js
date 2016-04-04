voiceintents.onVoiceIntentData = function(intents)
{
    voiceintents.intents = intents;

    voiceintents.intents.sort(voiceintents.sortCompare);

    voiceintents.createIntentData();
}

voiceintents.sortCompare = function(a, b)
{
    var astring = "";
    if (a.type) astring += a.type + "|";
    if (a.subtype) astring += a.subtype + "|";
    if (a.subtypetag) astring += a.subtypetag + "|";

    var bstring = "";
    if (b.type) bstring += b.type + "|";
    if (b.subtype) bstring += b.subtype + "|";
    if (b.subtypetag) bstring += b.subtypetag + "|";

    if (astring == bstring) return 0;

    return (astring > bstring) ? 1 : -1;
}

voiceintents.createFrame = function()
{
    WebLibSimple.setBGColor(document.body, "#ffffffee");

    var vi = voiceintents;

    vi.topDiv = WebLibSimple.createAnyAppend("div", document.body);
    WebLibSimple.setFontSpecs(vi.topDiv, 20, "bold", "#666666");
    vi.topDiv.style.padding = "20px";

    var div = WebLibSimple.createAnyAppend("center", vi.topDiv);
    WebLibSimple.setBGColor(div, "#888888");
    WebLibSimple.setFontSpecs(div, 24, "bold", "#ffffff");
    div.style.padding = "8px";
    div.style.marginBottom = "8px";
    div.innerHTML = "Auf diese Kommandos reagiert die Spracheingabe:"
}

voiceintents.createIntentData = function()
{
    var vi = voiceintents;

    vi.listDiv = WebLibSimple.createAnyAppend("div", vi.topDiv);

    for (var inx = 0; inx < vi.intents.length; inx++)
    {
        var intent = vi.intents[ inx ];
        if (! intent.sample) continue;

        //vi.intents.splice(inx--, 1);

        var divOuter = WebLibSimple.createAnyAppend("div", vi.listDiv);
        divOuter.style.position = "relative";
        divOuter.style.display = "inline-block";
        divOuter.style.width = "100%";
        divOuter.style.height = "80px";

        var divInner = WebLibSimple.createAnyAppend("div", divOuter);
        divInner.style.position = "absolute";
        divInner.style.left = "0px";
        divInner.style.top = "0px";
        divInner.style.right = "0px";
        divInner.style.bottom = "0px";
        divInner.style.marginTop = "4px";
        divInner.style.marginBottom = "4px";
        divInner.style.border = "1px solid grey";
        divInner.style.backgroundColor = "#eeeeee";

        var divIcon = WebLibSimple.createAnyAppend("div", divInner);
        divIcon.style.position = "absolute";
        divIcon.style.left = "0px";
        divIcon.style.top = "0px";
        divIcon.style.bottom = "0px";
        divIcon.style.margin = "4px";

        var imgIcon = WebLibSimple.createAnyAppend("img", divIcon);
        imgIcon.style.position = "absolute";
        imgIcon.style.left = "0px";
        imgIcon.style.top = "0px";
        imgIcon.style.width = "auto";
        imgIcon.style.height = "100%";
        imgIcon.src = intent.icon ? intent.icon : WebAppRequest.loadResourceImage(intent.iconres);

        var divSample = WebLibSimple.createAnyAppend("div", divInner);
        divSample.style.position = "absolute";
        divSample.style.left = "64px";
        divSample.style.top = "0px";
        divSample.style.right = "0px";
        divSample.style.padding = "8px";
        divSample.innerHTML = intent.sample;

        var divKeywords = WebLibSimple.createAnyAppend("div", divInner);
        WebLibSimple.setFontSpecs(divKeywords, 18, "normal", "#888888");
        divKeywords.style.position = "absolute";
        divKeywords.style.left = "64px";
        divKeywords.style.right = "0px";
        divKeywords.style.bottom = "0px";
        divKeywords.style.padding = "8px";
        divKeywords.innerHTML = intent.keywords.join(", ");
    }

    var pre = WebLibSimple.createAnyAppend("pre", vi.topDiv);
    pre.innerHTML = WebAppUtility.getPrettyJson(JSON.stringify(vi.intents));
}

voiceintents.createFrame();
