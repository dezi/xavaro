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

    vi.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topDiv", document.body);
    WebLibSimple.setFontSpecs(vi.topDiv, 20, "bold", "#666666");

    vi.titleDiv = WebLibSimple.createAnyHeight("center", 20, 20, 20, 30, "titleDiv" , vi.topDiv);
    WebLibSimple.setBGColor(vi.titleDiv, "#888888");
    WebLibSimple.setFontSpecs(vi.titleDiv, 24, "bold", "#ffffff");
    vi.titleDiv.style.padding = "8px";
    vi.titleDiv.style.marginBottom = "8px";
    vi.titleDiv.innerHTML = "Auf diese Kommandos reagiert die Spracheingabe:"

    vi.contDiv = WebLibSimple.createDiv(20, 80, 20 , 0, "contDiv", vi.topDiv);
    vi.contDiv.style.overflow = "hidden";
}

voiceintents.onClickMore = function(target)
{
    WebAppUtility.makeClick();

    target.imgMore.src = "arrow_less_270x270.png";
    target.onTouchClick = voiceintents.onClickLess;

    var vi = voiceintents;
    var index = target.intentIndex;

    for (var inx = index + 1; inx < vi.intentDivs.length; inx++)
    {
        if (vi.intentDivs[ inx ].intent.subtypetag)
        {
            vi.intentDivs[ inx ].style.display = "block";
        }
        else
        {
            break;
        }
    }
}

voiceintents.onClickLess = function(target)
{
    WebAppUtility.makeClick();

    target.imgMore.src = "arrow_more_270x270.png";
    target.onTouchClick = voiceintents.onClickMore;

    var vi = voiceintents;
    var index = target.intentIndex;

    for (var inx = index + 1; inx < vi.intentDivs.length; inx++)
    {
        if (vi.intentDivs[ inx ].intent.subtypetag)
        {
            vi.intentDivs[ inx ].style.display = "none";
        }
        else
        {
            break;
        }
    }
}

voiceintents.createIntentData = function()
{
    var vi = voiceintents;

    vi.listDiv = WebLibSimple.createDivHeight(0, 0, 0, null, "listDiv", vi.contDiv);
    vi.listDiv.scrollVertical = true;

    vi.intentDivs = [];

    for (var inx = 0; inx < vi.intents.length; inx++)
    {
        var intent = vi.intents[ inx ];
        if (! intent.sample) continue;

        vi.intents.splice(inx--, 1);

        var divOuter = WebLibSimple.createAnyAppend("div", vi.listDiv);
        divOuter.style.position = "relative";
        divOuter.style.display = "inline-block";
        divOuter.style.width = "100%";
        divOuter.style.height = "80px";
        divOuter.intent = intent;

        var divInner = WebLibSimple.createAnyAppend("div", divOuter);
        divInner.style.position = "absolute";
        divInner.style.left = "0px";
        divInner.style.top = "0px";
        divInner.style.right = "0px";
        divInner.style.bottom = "0px";
        divInner.style.marginTop = "4px";
        divInner.style.marginBottom = "4px";
        divInner.style.border = "1px solid grey";
        divInner.style.backgroundColor = "#dddddd";

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
        divSample.style.right = "64px";
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

        if (intent.subtypetag)
        {
            //
            // Subtypes with tags are folded.
            //

            divInner.style.backgroundColor = "#eeeeee";
            divOuter.style.display = "none";
        }
        else
        {
            //
            // Subtypes w/o tags have an unfold icon.
            //

            if (((inx + 1) < vi.intents.length) && vi.intents[ inx + 1 ].subtypetag)
            {
                //
                // Pad a secondary padding div because otherwise
                // the clickable area gets to small.
                //

                var divMore = WebLibSimple.createAnyAppend("div", divInner);
                divMore.style.position = "absolute";
                divMore.style.right = "0px";
                divMore.style.top = "0px";
                divMore.style.bottom = "0px";
                divMore.style.width = "70px";

                divMore.onTouchClick = voiceintents.onClickMore;
                divMore.intentIndex = vi.intentDivs.length;

                divMore.divPadd = WebLibSimple.createAnyAppend("div", divMore);
                divMore.divPadd.style.position = "absolute";
                divMore.divPadd.style.right = "0px";
                divMore.divPadd.style.top = "0px";
                divMore.divPadd.style.bottom = "0px";
                divMore.divPadd.style.margin = "20px";

                divMore.imgMore = WebLibSimple.createAnyAppend("img", divMore.divPadd);
                divMore.imgMore.style.position = "absolute";
                divMore.imgMore.style.right = "0px";
                divMore.imgMore.style.top = "0px";
                divMore.imgMore.style.width = "auto";
                divMore.imgMore.style.height = "100%";
                divMore.imgMore.src = "arrow_more_270x270.png";
            }
        }

        vi.intentDivs.push(divOuter);
    }

    //var pre = WebLibSimple.createAnyAppend("pre", vi.listDiv);
    //pre.innerHTML = WebAppUtility.getPrettyJson(JSON.stringify(vi.intents));
}

voiceintents.createFrame();
