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

    vi.contDiv = WebLibSimple.createDiv(20, 80, 20 , 110, "contDiv", vi.topDiv);
    vi.contDiv.style.overflow = "hidden";

    vi.testDiv = WebLibSimple.createDivHeight(20, 20, 20, -60, "testDiv" , vi.topDiv);
    WebLibSimple.setBGColor(vi.testDiv, "#888888");
    WebLibSimple.setFontSpecs(vi.testDiv, 24, "bold", "#ffffff");
    vi.testDiv.style.padding = "8px";
    vi.testDiv.style.marginTop = "8px";

    vi.resultDiv = WebLibSimple.createDiv(16, 0, 80, 0, "testDiv" , vi.testDiv);
    vi.resultDiv.style.color = "#cccccc";
    vi.resultDiv.style.lineHeight = "78px";
    vi.resultDiv.innerHTML = "Hier können sie es ausprobieren..."

    vi.divButton = WebLibSimple.createAnyAppend("div", vi.testDiv);
    vi.divButton.style.position = "absolute";
    vi.divButton.style.right = "0px";
    vi.divButton.style.top = "0px";
    vi.divButton.style.bottom = "0px";
    vi.divButton.style.width = "70px";

    vi.divButton.onTouchClick = voiceintents.onClickTry;

    vi.divPadd = WebLibSimple.createAnyAppend("div", vi.divButton);
    vi.divPadd.style.position = "absolute";
    vi.divPadd.style.right = "0px";
    vi.divPadd.style.top = "0px";
    vi.divPadd.style.bottom = "0px";
    vi.divPadd.style.margin = "8px";

    vi.imgTry = WebLibSimple.createAnyAppend("img", vi.divPadd);
    vi.imgTry.style.position = "absolute";
    vi.imgTry.style.right = "0px";
    vi.imgTry.style.top = "0px";
    vi.imgTry.style.width = "auto";
    vi.imgTry.style.height = "100%";
    vi.imgTry.src = "voiceintents_256x256.png";

    vi.intents = JSON.parse(WebAppVoice.getCollectedIntents());
    vi.intents.sort(voiceintents.sortCompare);
    vi.createIntentData();
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

        divOuter.divInner = WebLibSimple.createDiv(0, 0, 0, 0, "divInner", divOuter);
        divOuter.divInner.style.marginTop = "4px";
        divOuter.divInner.style.marginBottom = "4px";
        divOuter.divInner.style.border = "1px solid grey";
        divOuter.divInner.style.backgroundColor = "#dddddd";

        var divIcon = WebLibSimple.createDiv(0, 0, null, 0, "divIcon", divOuter.divInner);
        divIcon.style.margin = "4px";

        var imgIcon = WebLibSimple.createImgWidHei(0, 0, "auto", "100%", "imgIcon", divIcon);
        imgIcon.src = intent.icon ? intent.icon : WebAppRequest.loadResourceImage(intent.iconres);

        var divSample = WebLibSimple.createDiv(64, 0, 0, null, "divSample", divOuter.divInner);
        divSample.style.padding = "8px";
        divSample.innerHTML = intent.sample;

        var divKeywords = WebLibSimple.createDiv(64, null, 0, 0, "divKeywords", divOuter.divInner);
        WebLibSimple.setFontSpecs(divKeywords, 18, "normal", "#888888");
        divKeywords.style.padding = "8px";
        divKeywords.innerHTML = intent.keywords.join(", ");

        if (intent.subtypetag)
        {
            //
            // Subtypes with tags are folded.
            //

            divOuter.divInner.style.backgroundColor = "#eeeeee";
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

                var divMore = divOuter.divMore = WebLibSimple.createAnyAppend("div", divOuter.divInner);
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

    var pre = WebLibSimple.createAnyAppend("pre", vi.listDiv);
    pre.innerHTML = WebAppUtility.getPrettyJson(JSON.stringify(vi.intents));
}

voiceintents.onClickTry = function(target)
{
    WebAppUtility.makeClick();

    var vi = voiceintents;

    if (! vi.trying)
    {
        vi.trying = true;
        WebAppVoice.startSpeechRecognizer();
    }
}

WebAppVoice.onReadyForSpeech = function()
{
    var vi = voiceintents;

    vi.imgTry.src = "voice_ear_256x256.png";

    vi.resultDiv.style.color = "#cccccc";
    vi.resultDiv.innerHTML = "Bitte sprechen Sie nun..."
}

WebAppVoice.onBeginningOfSpeech = function()
{
}

WebAppVoice.onEndOfSpeech = function()
{
}

WebAppVoice.onError = function()
{
    var vi = voiceintents;

    vi.trying = false;
    vi.imgTry.src = "voiceintents_256x256.png";

    vi.resultDiv.style.color = "#cccccc";
    vi.resultDiv.innerHTML = "Das hat nicht geklappt..."
}

WebAppVoice.onResults = function(results)
{
    var vi = voiceintents;

    vi.trying = false;
    vi.imgTry.src = "voiceintents_256x256.png";

    vi.results = results;

    if (vi.results)
    {
        vi.resultDiv.style.color = "#ffffff";
        vi.resultDiv.innerHTML = vi.results[ 0 ].spoken + " (" + vi.results[ 0 ].confidence + "%)";

        vi.matches = JSON.parse(WebAppVoice.evaluateCommand(vi.results[ 0 ].spoken));

        var lastOuter = null;

        for (var inx = 0; inx < vi.intentDivs.length; inx++)
        {
            var divOuter = vi.intentDivs[ inx ];
            var intent = divOuter.intent;
            var ismatch = false;

            for (var cnt = 0; cnt < vi.matches.length; cnt++)
            {
                if ((vi.matches[ cnt ].type == intent.type) &&
                    (vi.matches[ cnt ].subtype == intent.subtype) &&
                    (vi.matches[ cnt ].subtypetag == intent.subtypetag))
                {
                    ismatch = true;
                    break;
                }
            }

            if (vi.intentDivs[ inx ].intent.subtypetag)
            {
                divOuter.divInner.style.backgroundColor = ismatch ? "#eeffee" : "#eeeeee";
                divOuter.style.display = ismatch ? "block" : "none";

                if (ismatch && lastOuter && lastOuter.divMore)
                {
                    lastOuter.divMore.imgMore.src = "arrow_less_270x270.png";
                    lastOuter.divMore.onTouchClick = voiceintents.onClickLess;
                }
            }
            else
            {
                divOuter.divInner.style.backgroundColor = ismatch ? "#ddffdd" : "#dddddd";

                if (divOuter.divMore)
                {
                    divOuter.divMore.imgMore.src = "arrow_more_270x270.png";
                    divOuter.divMore.onTouchClick = voiceintents.onClickMore;
                }

                lastOuter = divOuter;
            }
        }
    }
}

voiceintents.createFrame();
