voiceintents.createConfig = function()
{
    var vi = voiceintents;

    vi.config = {};

    if (WebAppUtility.isTablet())
    {
        vi.config.padding = 20;
        vi.config.spacing =  4;
        vi.config.bigtext = 24;
        vi.config.stdtext = 20;
        vi.config.keytext = 18;

    }
    else
    {
        vi.config.padding =  4;
        vi.config.spacing =  0;
        vi.config.bigtext = 16;
        vi.config.stdtext = 14;
        vi.config.keytext = 12;
    }

    vi.config.titleheight = 40;
    vi.config.trialheight = 60;
}

voiceintents.createFrame = function()
{
    WebLibSimple.setBGColor(document.body, "#ffffffee");

    var vi = voiceintents;

    var pad = vi.config.padding;
    var tithei = vi.config.titleheight;
    var tryhei = vi.config.trialheight;

    vi.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topDiv", document.body);
    WebLibSimple.setFontSpecs(vi.topDiv, vi.config.stdtext, "bold", "#666666");

    //
    // Title bar.
    //

    vi.titleDiv = WebLibSimple.createDivHeight(pad, pad, pad, tithei, "titleDiv" , vi.topDiv);
    WebLibSimple.setBGColor(vi.titleDiv, "#888888");
    WebLibSimple.setFontSpecs(vi.titleDiv, vi.config.bigtext, "bold", "#ffffff");
    vi.titleDiv.style.padding = "8px";

    vi.titabDiv = WebLibSimple.createAnyAppend("div", vi.titleDiv)
    vi.titabDiv.style.display = "table";
    vi.titabDiv.style.width   = "100%";
    vi.titabDiv.style.height  = "100%";

    vi.ticelDiv = WebLibSimple.createAnyAppend("div", vi.titabDiv)
    vi.ticelDiv.style.display = "table-cell";
    vi.ticelDiv.style.width   = "100%";
    vi.ticelDiv.style.height  = "100%";
    vi.ticelDiv.style.textAlign = "center";
    vi.ticelDiv.style.verticalAlign = "middle";
    vi.ticelDiv.innerHTML = "Auf diese Kommandos reagiert die Spracheingabe:"

    //
    // Bottom bar.
    //

    vi.testDiv = WebLibSimple.createDivHeight(pad, pad, pad, -tryhei, "testDiv" , vi.topDiv);
    WebLibSimple.setBGColor(vi.testDiv, "#888888");
    WebLibSimple.setFontSpecs(vi.testDiv, vi.config.bigtext, "bold", "#ffffff");
    vi.testDiv.style.padding = "8px";

    vi.padDiv = WebLibSimple.createDiv(16, 0, 80, 0, "padDiv", vi.testDiv);

    vi.tableDiv = WebLibSimple.createAnyAppend("div", vi.padDiv)
    vi.tableDiv.style.display = "table";
    vi.tableDiv.style.width   = "100%";
    vi.tableDiv.style.height  = "100%";

    vi.resultDiv = WebLibSimple.createAnyAppend("div", vi.tableDiv);
    vi.resultDiv.style.display = "table-cell";
    vi.resultDiv.style.width   = "100%";
    vi.resultDiv.style.height  = "100%";
    vi.resultDiv.style.verticalAlign = "middle";
    vi.resultDiv.style.color = "#cccccc";
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

    vi.imgEar = WebLibSimple.createAnyAppend("img", vi.divPadd);
    vi.imgEar.style.display = "none";
    vi.imgEar.style.position = "absolute";
    vi.imgEar.style.right = "0px";
    vi.imgEar.style.top = "0px";
    vi.imgEar.style.width = "auto";
    vi.imgEar.style.height = "100%";
    vi.imgEar.src = "voice_ear_256x256.png";

    //
    // Content div.
    //

    var top = tithei + 2 * pad + 2 * 8;
    var bot = tryhei + 2 * pad + 2 * 8;

    vi.contDiv = WebLibSimple.createDiv(pad, top, pad , bot, "contDiv", vi.topDiv);
    vi.contDiv.style.overflow = "hidden";

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

    voiceintents.adjustScroll();
}

voiceintents.adjustScroll = function(firstMarked)
{
    var vi = voiceintents;

    //
    // Check if scroll needs resetting.
    //

    var displayheight = vi.listDiv.parentNode.clientHeight;
    var scrollheight = vi.listDiv.clientHeight;
    var offsettop = -vi.listDiv.offsetTop;

    console.log("voiceintents.adjustScroll:"
        + displayheight + "=" + scrollheight
        + "="
        + offsettop + "=" + firstMarked
        );

    if (scrollheight < displayheight)
    {
        //
        // View fits display. Reset list div offset to 0px.
        //

        vi.listDiv.style.top = "0px";
    }
    else
    {
        if (firstMarked != null)
        {
            var newtopoffset = firstMarked - (displayheight >> 1);
            if (newtopoffset < 0) newtopoffset = 0;

            if ((scrollheight - newtopoffset) < displayheight)
            {
                newtopoffset = - (displayheight - scrollheight);
            }
            else
            {
                if (newtopoffset > (scrollheight - displayheight))
                {
                    newtopoffset = scrollheight - displayheight;
                }
            }

            vi.listDiv.style.top = -newtopoffset + "px";
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
        divOuter.divInner.style.marginTop = vi.config.spacing + "px";
        divOuter.divInner.style.marginBottom = vi.config.spacing + "px";
        divOuter.divInner.style.border = "1px solid grey";
        divOuter.divInner.style.backgroundColor = "#dddddd";

        var divIcon = WebLibSimple.createDiv(0, 0, null, 0, "divIcon", divOuter.divInner);
        divIcon.style.margin = "4px";

        var imgIcon = WebLibSimple.createImgWidHei(0, 0, "auto", "100%", "imgIcon", divIcon);
        imgIcon.src = intent.icon ? intent.icon : WebAppRequest.loadResourceImage(intent.iconres);

        var divSample = WebLibSimple.createDiv(68, 0, 64, null, "divSample", divOuter.divInner);
        divSample.style.padding = "8px";
        divSample.innerHTML = intent.sample;

        var divKeywords = WebLibSimple.createDiv(68, null, 64, 0, "divKeywords", divOuter.divInner);
        WebLibSimple.setFontSpecs(divKeywords, vi.config.keytext, "normal", "#888888");
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

    //var pre = WebLibSimple.createAnyAppend("pre", vi.listDiv);
    //pre.innerHTML = WebAppUtility.getPrettyJson(JSON.stringify(vi.intents));
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

    vi.imgTry.style.display = "none";
    vi.imgEar.style.display = "block";

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

    vi.imgTry.style.display = "block";
    vi.imgEar.style.display = "none";

    vi.resultDiv.style.color = "#cccccc";
    vi.resultDiv.innerHTML = "Das hat nicht geklappt..."
}

WebAppVoice.onResults = function(results)
{
    var vi = voiceintents;

    vi.trying = false;

    vi.imgTry.style.display = "block";
    vi.imgEar.style.display = "none";

    vi.results = results;

    if (vi.results)
    {
        vi.resultDiv.style.color = "#ffffff";
        vi.resultDiv.innerHTML = vi.results[ 0 ].spoken + " (" + vi.results[ 0 ].confidence + "%)";

        vi.matches = JSON.parse(WebAppVoice.evaluateCommand(vi.results[ 0 ].spoken));

        var lastOuter = null;
        var firstMarked = 0;

        for (var inx = 0; inx < vi.intentDivs.length; inx++)
        {
            var divOuter = vi.intentDivs[ inx ];
            var intent = divOuter.intent;
            var ismatch = false;

            for (var cnt = 0; cnt < vi.matches.length; cnt++)
            {
                if (vi.matches[ cnt ].identifier == intent.identifier)
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

            if (ismatch && ! firstMarked) firstMarked = divOuter.offsetTop;
        }

        voiceintents.adjustScroll(firstMarked);
   }
}

voiceintents.createConfig();
voiceintents.createFrame();
