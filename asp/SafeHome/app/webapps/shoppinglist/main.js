WebAppRequest.onVoiceIntent = function(intent)
{
    console.log("WebAppRequest.onVoiceIntent: ====================>" + JSON.stringify(intent));
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

    sl.itemlist = [];
}

shoppinglist.brandSortPrio = function(brand)
{
    if (brand == "Eigenmarke") return "1" + brand;
    if (brand == "Hausmarke") return "2" + brand;

    return "9" + brand;
}

shoppinglist.sortCompare = function(a, b)
{
    var astring = "";

    if (a.isprice)
    {
        astring += WebLibSimple.padNum(a.product.storeobj.sort,8) + "|";
        astring += a.product.storeobj.store + "|";
        astring += a.product.text + "|";
        //astring += shoppinglist.brandSortPrio(a.brand) + "|";
        astring += a.basesort + "|";
    }
    else
    if (a.isproduct)
    {
        astring += WebLibSimple.padNum(a.storeobj.sort,8) + "|";
        astring += a.storeobj.store + "|";
        astring += a.text + "|";
    }
    else
    {
        astring += WebLibSimple.padNum(a.sort,8) + "|";
        astring += a.store + "|";
    }

    var bstring = "";

    if (b.isprice)
    {
        bstring += WebLibSimple.padNum(b.product.storeobj.sort,8) + "|";
        bstring += b.product.storeobj.store + "|";
        bstring += b.product.text + "|";
        //bstring += shoppinglist.brandSortPrio(b.brand) + "|";
        bstring += b.basesort + "|";
    }
    else
    if (b.isproduct)
    {
        bstring += WebLibSimple.padNum(b.storeobj.sort,8) + "|";
        bstring += b.storeobj.store + "|";
        bstring += b.text + "|";
    }
    else
    {
        bstring += WebLibSimple.padNum(b.sort,8) + "|";
        bstring += b.store + "|";
    }

    if (astring == bstring) return 0;
    return (astring > bstring) ? 1 : -1;
}

shoppinglist.createItemDiv = function(item)
{
    var sl = shoppinglist;

    var divOuter = document.createElement("div");
    divOuter.style.position = "relative";
    divOuter.style.display = "inline-block";
    divOuter.style.width = "100%";
    divOuter.style.height = "80px";

    divOuter.divInner = WebLibSimple.createDiv(0, 0, 0, 0, "divInner", divOuter);
    divOuter.divInner.style.marginTop = "4px";
    divOuter.divInner.style.marginBottom = "4px";
    divOuter.divInner.style.border = "1px solid grey";
    divOuter.divInner.style.backgroundColor = "#cccccc";

    var divIcon = WebLibSimple.createDiv(0, 0, null, 0, "divIcon", divOuter.divInner);
    divIcon.style.margin = "4px";

    var imgIcon = WebLibSimple.createImgWidHei(0, 0, "auto", "100%", "imgIcon", divIcon);

    var divSample = WebLibSimple.createDiv(64, 0, 64, null, "divSample", divOuter.divInner);
    divSample.style.overflow = "hidden";
    divSample.style.lineHeight = "54px";
    divSample.style.whiteSpace = "nowrap";
    divSample.style.textOverflow = "ellipsis";
    divSample.style.padding = "8px";

    var divKeywords = WebLibSimple.createDiv(64, null, 0, 0, "divKeywords", divOuter.divInner);
    WebLibSimple.setFontSpecs(divKeywords, 18, "normal", "#888888");
    divKeywords.style.padding = "8px";

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

    if (item.isprice)
    {
        divOuter.divInner.style.backgroundColor = "#eeeeee";
        divSample.innerHTML = item.displaytext;
        divSample.style.lineHeight = "40px";
        divKeywords.innerHTML = item.displayprice;
        divKeywords.style.color = "#ff4444";
        divMore.imgMore.src = "arrow_pick_270x270.png";
        divMore.onTouchClick = shoppinglist.onClickPick;
        imgIcon.src = item.icon ? item.icon :WebLibSimple.getNixPixImg();
    }
    else
    if (item.isproduct)
    {
        divOuter.divInner.style.backgroundColor = "#dddddd";
        divSample.innerHTML = item.text;
        divMore.imgMore.src = "search_300x300.png";
        divMore.onTouchClick = shoppinglist.onClickSearch;
        imgIcon.src = WebLibSimple.getNixPixImg();
    }
    else
    {
        divSample.style.fontSize = "32px";
        divSample.innerHTML = item.store;
        divMore.imgMore.src = "arrow_more_270x270.png";
        divMore.onTouchClick = shoppinglist.onClickMore;
        imgIcon.src = item.logo ? item.logo : WebLibSimple.getNixPixImg();
    }

    divOuter.item = item;

    return divOuter;
}

shoppinglist.updateitemlist = function()
{
    var sl = shoppinglist;

    sl.itemlist.sort(shoppinglist.sortCompare);

    sl.listDiv.innerHTML = "";

    var laststore = null;
    var lastproduct = null;

    for (var pinx = 0; pinx < sl.itemlist.length; pinx++)
    {
        item = sl.itemlist[ pinx ];

        if (item.isprice)
        {
        }
        else
        if (item.isproduct)
        {
            if (item.storeobj != laststore)
            {
                item.storeobj.itemDiv = shoppinglist.createItemDiv(item.storeobj);
                sl.listDiv.appendChild(item.storeobj.itemDiv);
                sl.itemlist.splice(pinx++, 0, item.storeobj);
            }

            laststore = item.storeobj;

            lastproduct = item;
        }
        else
        {
            laststore = item;
        }

        if (! item.itemDiv) item.itemDiv = shoppinglist.createItemDiv(item);
        sl.listDiv.appendChild(item.itemDiv);
    }
}

shoppinglist.addItem = function(product)
{
    var sl = shoppinglist;

    sl.itemlist.push(product);

    shoppinglist.updateitemlist();
}

shoppinglist.onClickPick = function(target)
{
    WebAppUtility.makeClick();

    var sl = shoppinglist;

    while (target && ! target.item)
    {
        target = target.parentNode;
    }

    if (! target) return;
}

shoppinglist.onClickSearch = function(target)
{
    WebAppUtility.makeClick();

    var sl = shoppinglist;

    while (target && ! target.item)
    {
        target = target.parentNode;
    }

    if (! target) return;

    var product = target.item;

    var results = JSON.parse(WebAppPrices.getCategories(product.text));

    if (results.length == 0)
    {
        results = JSON.parse(WebAppPrices.getProducts(product.text));

        if (results)
        {
            for (var inx = 0; inx < results.length; inx++)
            {
                var price = shoppinglist.parseRealProduct(product, results[ inx ]);
                shoppinglist.addItem(price);
            }
        }

        return;
    }

    if (results.length < 7)
    {
        var categories = [];

        for (var inx = 0; inx < results.length; inx++)
        {
            var parts = results[ inx ].split("|");
            categories.push(parts[ 1 ])
        }

        console.log(JSON.stringify(categories));

        results = JSON.parse(WebAppPrices.getProductsFromCategories(JSON.stringify(categories)));

        if (results)
        {
            for (var inx = 0; inx < results.length; inx++)
            {
                var price = shoppinglist.parseRealProduct(product, results[ inx ]);
                shoppinglist.addItem(price);
            }
        }

        return;
    }

    var pre = WebLibSimple.createAnyAppend("pre", sl.listDiv);
    pre.style.fontSize = "16px";
    pre.innerHTML = results.join("\n");
}

shoppinglist.onClickMore = function(target)
{
    WebAppUtility.makeClick();

    target.imgMore.src = "arrow_less_270x270.png";
    target.onTouchClick = shoppinglist.onClickLess;

    var sl = shoppinglist;
    var store = target.item;
}

shoppinglist.onClickLess = function(target)
{
    WebAppUtility.makeClick();

    target.imgMore.src = "arrow_more_270x270.png";
    target.onTouchClick = shoppinglist.onClickMore;

    var sl = shoppinglist;
    var store = target.item;
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
        shoppinglist.addItem(product);
    }
}

shoppinglist.createFrame();

shoppinglist.addItem(shoppinglist.parseProduct("Batterien aus dem Aldi"));
shoppinglist.addItem(shoppinglist.parseProduct("Klopapier aus dem Aldi"));
shoppinglist.addItem(shoppinglist.parseProduct("Kartoffeln aus dem Aldi"));
shoppinglist.addItem(shoppinglist.parseProduct("Butter aus dem Penny"));