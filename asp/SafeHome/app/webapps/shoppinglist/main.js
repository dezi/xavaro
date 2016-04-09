WebAppRequest.onVoiceIntent = function(intent)
{
    console.log("WebAppRequest.onVoiceIntent: ====================>" + JSON.stringify(intent));

    var sl = shoppinglist;

    sl.voiceIntent.innerHTML = intent.command;

    //var results = WebAppPrices.getQuery("Dr|Oetker|Salami|Pizza");
    //sl.queryResult.innerHTML = WebAppUtility.getPrettyJson(results);

}

shoppinglist.createFrame = function()
{
    WebLibSimple.setBGColor(document.body, "#ffffffee");

    var sl = shoppinglist;

    sl.topDiv = WebLibSimple.createDiv(0, 0, 0, 0, "topDiv", document.body);
    WebLibSimple.setFontSpecs(sl.topDiv, 20, "bold", "#666666");

    sl.titleDiv = WebLibSimple.createAnyHeight("center", 20, 20, 20, 30, "titleDiv" , sl.topDiv);
    WebLibSimple.setBGColor(sl.titleDiv, "#888888");
    WebLibSimple.setFontSpecs(sl.titleDiv, 24, "bold", "#ffffff");
    sl.titleDiv.style.padding = "8px";
    sl.titleDiv.style.marginBottom = "8px";
    sl.titleDiv.innerHTML = "Einträge auf der Einkaufsliste"

    sl.contDiv = WebLibSimple.createDiv(20, 80, 20 , 110, "contDiv", sl.topDiv);
    sl.contDiv.style.overflow = "hidden";

    sl.testDiv = WebLibSimple.createDivHeight(20, 20, 20, -60, "testDiv" , sl.topDiv);
    WebLibSimple.setBGColor(sl.testDiv, "#888888");
    WebLibSimple.setFontSpecs(sl.testDiv, 24, "bold", "#ffffff");
    sl.testDiv.style.padding = "8px";
    sl.testDiv.style.marginTop = "8px";

    sl.resultDiv = WebLibSimple.createDiv(16, 0, 80, 0, "testDiv" , sl.testDiv);
    sl.resultDiv.style.color = "#cccccc";
    sl.resultDiv.style.lineHeight = "78px";
    sl.resultDiv.innerHTML = "Drücken Sie auf das Mikrofon um etwas hinzuzufügen"

    sl.divButton = WebLibSimple.createAnyAppend("div", sl.testDiv);
    sl.divButton.style.position = "absolute";
    sl.divButton.style.right = "0px";
    sl.divButton.style.top = "0px";
    sl.divButton.style.bottom = "0px";
    sl.divButton.style.width = "70px";

    sl.divButton.onTouchClick = shoppinglist.onClickAdd;

    sl.divPadd = WebLibSimple.createAnyAppend("div", sl.divButton);
    sl.divPadd.style.position = "absolute";
    sl.divPadd.style.right = "0px";
    sl.divPadd.style.top = "0px";
    sl.divPadd.style.bottom = "0px";
    sl.divPadd.style.margin = "8px";

    sl.imgTry = WebLibSimple.createAnyAppend("img", sl.divPadd);
    sl.imgTry.style.position = "absolute";
    sl.imgTry.style.right = "0px";
    sl.imgTry.style.top = "0px";
    sl.imgTry.style.width = "auto";
    sl.imgTry.style.height = "100%";
    sl.imgTry.src = "voice_mic_256x256.png";

    sl.listDiv = WebLibSimple.createDivHeight(0, 0, 0, null, "listDiv", sl.contDiv);
    sl.listDiv.scrollVertical = true;

    sl.itemDivs = [];
}

shoppinglist.addProduct = function(product)
{
    var sl = shoppinglist;

    var divOuter = WebLibSimple.createAnyAppend("div", sl.listDiv);
    divOuter.style.position = "relative";
    divOuter.style.display = "inline-block";
    divOuter.style.width = "100%";
    divOuter.style.height = "80px";
    divOuter.product = product;

    divOuter.divInner = WebLibSimple.createDiv(0, 0, 0, 0, "divInner", divOuter);
    divOuter.divInner.style.marginTop = "4px";
    divOuter.divInner.style.marginBottom = "4px";
    divOuter.divInner.style.border = "1px solid grey";
    divOuter.divInner.style.backgroundColor = "#dddddd";

    var divIcon = WebLibSimple.createDiv(0, 0, null, 0, "divIcon", divOuter.divInner);
    divIcon.style.margin = "4px";

    var imgIcon = WebLibSimple.createImgWidHei(0, 0, "auto", "100%", "imgIcon", divIcon);
    imgIcon.src = product.icon ? product.icon : WebLibSimple.getNixPixImg();

    var divSample = WebLibSimple.createDiv(64, 0, 0, null, "divSample", divOuter.divInner);
    divSample.style.padding = "8px";
    divSample.innerHTML = product.text;

    var divKeywords = WebLibSimple.createDiv(64, null, 0, 0, "divKeywords", divOuter.divInner);
    WebLibSimple.setFontSpecs(divKeywords, 18, "normal", "#888888");
    divKeywords.style.padding = "8px";
    divKeywords.innerHTML = "";

    if (product.subtypetag)
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

        if (((inx + 1) < sl.intents.length) && sl.intents[ inx + 1 ].subtypetag)
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
            divMore.intentIndex = sl.intentDivs.length;

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

    sl.intentDivs.push(divOuter);
}

shoppinglist.onClickAdd = function(target)
{
    WebAppUtility.makeClick();

    var sl = shoppinglist;

    if (! sl.trying)
    {
        sl.trying = true;
        WebAppVoice.startSpeechRecognizer();
    }
}

WebAppVoice.onReadyForSpeech = function()
{
    var sl = shoppinglist;

    sl.imgTry.src = "voice_ear_256x256.png";

    sl.resultDiv.style.color = "#cccccc";
    sl.resultDiv.innerHTML = "Bitte sprechen Sie nun..."
}

WebAppVoice.onBeginningOfSpeech = function()
{
}

WebAppVoice.onEndOfSpeech = function()
{
}

WebAppVoice.onError = function()
{
    var sl = shoppinglist;

    sl.trying = false;
    sl.imgTry.src = "voice_mic_256x256.png";

    sl.resultDiv.style.color = "#cccccc";
    sl.resultDiv.innerHTML = "Das hat nicht geklappt..."
}

WebAppVoice.onResults = function(results)
{
    var sl = shoppinglist;

    sl.trying = false;
    sl.imgTry.src = "voice_mic_256x256.png";

    sl.results = results;

    if (sl.results)
    {
        sl.resultDiv.style.color = "#ffffff";
        sl.resultDiv.innerHTML = sl.results[ 0 ].spoken + " (" + sl.results[ 0 ].confidence + "%)";

        var product = shoppinglist.parseProduct(sl.results[ 0 ].spoken);
        shoppinglist.addProduct(product);
    }
}
shoppinglist.createFrame();
