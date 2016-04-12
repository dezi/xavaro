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

shoppinglist.sortCompareString = function(ab)
{
    var abstring = "";

    if (ab.isprice)
    {
        abstring += WebLibSimple.padNum(ab.product.storesort,8) + "|";
        abstring += ab.product.storename + "|";
        abstring += ab.product.text + "|";
        abstring += ab.catinx + "|";
        abstring += ab.basesort + "|";
    }

    if (ab.iscategory)
    {
        abstring += WebLibSimple.padNum(ab.product.storesort,8) + "|";
        abstring += ab.product.storename + "|";
        abstring += ab.product.text + "|";
        abstring += ab.catinx + "|";
    }

    if (ab.isproduct)
    {
        abstring += WebLibSimple.padNum(ab.storesort,8) + "|";
        abstring += ab.storename + "|";
        abstring += ab.text + "|";
    }

    if (ab.isstore)
    {
        abstring += WebLibSimple.padNum(ab.sort,8) + "|";
        abstring += ab.store + "|";
    }

    return abstring;
}

shoppinglist.sortCompare = function(a, b)
{
    var astring = shoppinglist.sortCompareString(a);
    var bstring = shoppinglist.sortCompareString(b);

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

    if (item.isprice)
    {
        divOuter.divInner.style.backgroundColor = "#eeeeee";
        divSample.innerHTML = item.displaytext;
        divSample.style.lineHeight = "40px";
        divKeywords.innerHTML = item.displayprice;
        divKeywords.style.color = "#ff4444";
        divMore.imgMore.src = "arrow_pick_270x270.png";
        divMore.onTouchClick = shoppinglist.onClickPick;
        imgIcon.src = item.icon ? item.icon : "multi_320x320.png";
    }

    if (item.iscategory)
    {
        divOuter.divInner.style.backgroundColor = "#dddddd";
        divSample.innerHTML = item.text;
        divMore.imgMore.src = "search_300x300.png";
        divMore.onTouchClick = shoppinglist.onClickCategory;
        imgIcon.src = WebLibSimple.getNixPixImg();
    }

    if (item.isproduct)
    {
        divOuter.divInner.style.backgroundColor = "#cccccc";
        divMore.imgMore.src = "search_300x300.png";
        divMore.onTouchClick = shoppinglist.onClickSearch;
        divMore.onTouchLongClick = shoppinglist.onClickDelete;

        if (item.price)
        {
            divSample.innerHTML = item.displayquant + " " + item.price.displaytext;
            divSample.style.lineHeight = "40px";
            divKeywords.innerHTML = item.price.displayprice;
            divKeywords.style.color = "#ff4444";
            imgIcon.src = item.price.icon ? item.price.icon : "multi_320x320.png";
        }
        else
        {
            divSample.innerHTML = item.displayquant + " " + item.text;
            imgIcon.src =  "multi_320x320.png";
        }
   }

    if (item.isstore)
    {
        divOuter.divInner.style.backgroundColor = "#aaaaaa";
        divSample.style.fontSize = "32px";
        divSample.innerHTML = item.store;
        divMore.imgMore.src = "arrow_less_270x270.png";
        divMore.onTouchClick = shoppinglist.onClickMore;
        imgIcon.src = item.logo ? item.logo : WebLibSimple.getNixPixImg();
    }

    divOuter.imgIcon = imgIcon;
    divOuter.divMore = divMore;
    divOuter.divSample = divSample;
    divOuter.divKeywords = divKeywords;

    divOuter.item = item;

    return divOuter;
}

shoppinglist.createStore = function(storename)
{
    var stores = WebLibStrings.strings[ "stores.defines" ];

    if (stores)
    {
        for (var stinx in stores)
        {
            if (stores[ stinx ].store == storename)
            {
                if (! stores[ stinx ].itemDiv)
                {
                    stores[ stinx ].isstore = true;
                    stores[ stinx ].itemDiv = shoppinglist.createItemDiv(stores[ stinx ]);
                }

                return stores[ stinx ];
            }
        }
    }

    return null;
}

shoppinglist.updateitemlist = function()
{
    var sl = shoppinglist;

    sl.itemlist.sort(shoppinglist.sortCompare);

    sl.listDiv.innerHTML = "";

    sl.storage = {};
    sl.storage.items = [];

    var laststore = null;

    for (var pinx = 0; pinx < sl.itemlist.length; pinx++)
    {
        item = sl.itemlist[ pinx ];

        if (item.isproduct)
        {
            if (item.storename != laststore)
            {
                var storeobj = shoppinglist.createStore(item.storename);
                sl.listDiv.appendChild(storeobj.itemDiv);
                sl.itemlist.splice(pinx++, 0, storeobj);
            }

            laststore = item.storename;
        }

        if (item.isstore)
        {
            laststore = item.store;
        }

        if (! item.itemDiv) item.itemDiv = shoppinglist.createItemDiv(item);
        sl.listDiv.appendChild(item.itemDiv);
    }

    var displayheight = sl.listDiv.parentNode.clientHeight;
    var scrollheight = sl.listDiv.clientHeight;

    if (scrollheight < displayheight)
    {
        //
        // Reset list div offset to 0px.
        //

        sl.listDiv.style.top = "0px";
    }
}

shoppinglist.cloneItem = function(item)
{
    var ni = {};

    for (var property in item)
    {
        if (property == "itemDiv") continue;
        if (item[ property ] == null) continue;
        if (typeof item[ property ] === 'object') continue;

        ni[ property ] = item[ property ];
    }

    return ni;
}

shoppinglist.loadItems = function()
{
    var sl = shoppinglist;

    var storage = JSON.parse(WebAppStorage.getAppStorage("list"));

    if (storage.items)
    {
        for (var inx in storage.items)
        {
            var item = storage.items[ inx ];

            if (item.priceline)
            {
                item.price = shoppinglist.parseRealProduct(item, item.priceline);
            }

            sl.itemlist.push(item);
        }
    }

    shoppinglist.updateitemlist();
}

shoppinglist.saveItems = function()
{
    var sl = shoppinglist;

    var storage = {};
    storage.items = [];

    for (var pinx = 0; pinx < sl.itemlist.length; pinx++)
    {
        item = sl.itemlist[ pinx ];

        if (! item.isproduct) continue;

        var si = shoppinglist.cloneItem(item);

        storage.items.push(si);
    }

    console.log("shoppinglist.saveItems: " + WebAppUtility.getPrettyJson(JSON.stringify(storage)));

    WebAppStorage.putAppStorage("list", JSON.stringify(storage));
}

shoppinglist.addItem = function(item)
{
    var sl = shoppinglist;

    sl.itemlist.push(item);

    shoppinglist.updateitemlist();
}

shoppinglist.onClickPick = function(ctarget, target)
{
    WebAppUtility.makeClick();

    var price = shoppinglist.getTargetItem(ctarget);
    if (! price) return;

    var product = price.product;
    var itemDiv = product.itemDiv;

    product.price = price;
    product.priceline = price.line;

    //
    // Delete recusive element used for picking.
    //

    delete price.product;

    itemDiv.divSample.innerHTML = product.displayquant + " " + product.price.displaytext;
    itemDiv.divSample.style.lineHeight = "40px";
    itemDiv.divKeywords.innerHTML = product.price.displayprice;
    itemDiv.divKeywords.style.color = "#ff4444";
    itemDiv.imgIcon.src = product.price.icon ? product.price.icon : "multi_320x320.png";

    shoppinglist.onClickNukeSearches(product.itemDiv.divMore);

    shoppinglist.saveItems();
}

shoppinglist.onClickCategory = function(ctarget, target)
{
    WebAppUtility.makeClick();

    var category = shoppinglist.getTargetItem(ctarget);
    if (! category) return;

    var categories = [];

    categories.push(category);

    var itemsfound = shoppinglist.searchCategories(category.product, categories);

    if (itemsfound == 0)
    {
        WebAppSpeak.speak("Ich habe keine Artikel gefunden");
    }
    else
    {
        target.imgMore.src = "arrow_less_270x270.png";
        target.onTouchClick = shoppinglist.onClickNukePrices;
    }
}

shoppinglist.evaluteBestCategories = function(product, results)
{
    //
    // Evaluate best categories.
    //

    var tempcats = [];

    for (var inx = 0; inx < results.length; inx++)
    {
        tempcats.push(shoppinglist.parseCategory(product, results[ inx ]));
    }

    var nummatches = 0;
    var targettext = product.text.toLowerCase();

    //
    // Collect and check category setup.
    //

    var categories = [];

    for (var inx = 0; inx < tempcats.length; inx++)
    {
        var text = tempcats[ inx ].text;
        var path = tempcats[ inx ].path;
        var topcat = false;

        for (var cnt = 0; cnt < tempcats.length; cnt++)
        {
            if (inx == cnt) continue;

            if (tempcats[ cnt ].path.substring(0, path.length) == path)
            {
                topcat = true;
                break;
            }
        }

        if (topcat) continue;

        if (text.toLowerCase().indexOf(targettext) >= 0)
        {
            //
            // The category name contains the target word.
            //

            nummatches++;
        }

        categories.push(tempcats[ inx ]);
    }

    if (nummatches > 0)
    {
        //
        // Redo category selection now and accept only
        // categories with matches in name.
        //

        categories = [];

        for (var inx = 0; inx < tempcats.length; inx++)
        {
            var text = tempcats[ inx ].text;
            var path = tempcats[ inx ].path;
            var topcat = false;

            for (var cnt = 0; cnt < tempcats.length; cnt++)
            {
                if (inx == cnt) continue;

                if (tempcats[ cnt ].path.substring(0, path.length) == path)
                {
                    topcat = true;
                    break;
                }
            }

            if (topcat || (text.toLowerCase().indexOf(targettext) < 0)) continue;

            categories.push(tempcats[ inx ]);
        }
    }

    return categories;
}

shoppinglist.searchCategories = function(product, categories)
{
    var itemsfound = 0;

    var catquery = [];

    for (var inx = 0; inx < categories.length; inx++)
    {
        catquery.push(categories[ inx ].catinx)
    }

    console.log(JSON.stringify(catquery));

    var results = JSON.parse(WebAppPrices.getProductsFromCategories(JSON.stringify(catquery)));

    if (results)
    {
        for (var inx = 0; inx < results.length; inx++)
        {
            var price = shoppinglist.parseRealProduct(product, results[ inx ]);
            price.product = product;

            shoppinglist.addItem(price);
            itemsfound++;
        }
    }

    return itemsfound;
}

shoppinglist.onClickNukePrices = function(ctarget, target)
{
    //
    // Remove all open search releated entries.
    //

    WebAppUtility.makeClick();

    var sl = shoppinglist;

    for (var inx = 0; inx < sl.itemlist.length; inx++)
    {
        item = sl.itemlist[ inx ];

        if (item.isprice)
        {
            sl.itemlist.splice(inx--, 1);
        }
    }

    shoppinglist.updateitemlist();

    ctarget.imgMore.src = "search_300x300.png";
    ctarget.onTouchClick = shoppinglist.onClickCategory;
}

shoppinglist.onClickNukeSearches = function(ctarget, target)
{
    //
    // Remove all open search releated entries.
    //

    if (target) WebAppUtility.makeClick();

    var sl = shoppinglist;

    for (var inx = 0; inx < sl.itemlist.length; inx++)
    {
        item = sl.itemlist[ inx ];

        if (item.isprice || item.iscategory)
        {
            sl.itemlist.splice(inx--, 1);
        }
    }

    shoppinglist.updateitemlist();

    ctarget.imgMore.src = "search_300x300.png";
    ctarget.onTouchClick = shoppinglist.onClickSearch;
}

shoppinglist.getTargetItem = function(target)
{
    while (target && ! target.item)
    {
        target = target.parentNode;
    }

    return target ? target.item : null;
}

shoppinglist.onClickDelete = function(ctarget, target)
{
    WebAppUtility.makeClick();

    var item = shoppinglist.getTargetItem(ctarget);
    if (! item) return;

    var sl = shoppinglist;

    for (var inx = 0; inx < sl.itemlist.length; inx++)
    {
        if (sl.itemlist[ inx ] == item)
        {
            sl.itemlist.splice(inx, 1);

            //
            // Nuke open categories and prices.
            //

            while (inx < sl.itemlist.length)
            {
                if (! (sl.itemlist[ inx ].iscategory || sl.itemlist[ inx ].isprice))
                {
                    break;
                }

                sl.itemlist.splice(inx--, 1);
            }

            //
            // Check if last in store list.
            //

            if (((inx > 0) && (sl.itemlist[ inx - 1 ].isstore)) &&
                ((inx == sl.itemlist.length) || sl.itemlist[ inx ].isstore))
            {
                //
                // Empty store, nuke.
                //

                sl.itemlist.splice(inx - 1, 1);
            }

            break;
        }
    }

    shoppinglist.updateitemlist();
}

shoppinglist.onClickSearch = function(ctarget, target)
{
    WebAppUtility.makeClick();

    var product = shoppinglist.getTargetItem(ctarget);
    if (! product) return;

    var itemsfound = 0;

    var results = JSON.parse(WebAppPrices.getCategories(product.text));

    if (results.length == 0)
    {
        results = JSON.parse(WebAppPrices.getProducts(product.text));

        if (results.length > 0)
        {
            for (var inx = 0; inx < results.length; inx++)
            {
                var price = shoppinglist.parseRealProduct(product, results[ inx ]);
                price.product = product;

                shoppinglist.addItem(price);
                itemsfound++;
            }
        }
    }
    else
    {
        var categories = shoppinglist.evaluteBestCategories(product, results);

        if (categories.length == 1)
        {
            itemsfound = shoppinglist.searchCategories(product, categories);
        }
        else
        {
            for (var inx = 0; inx < categories.length; inx++)
            {
                shoppinglist.addItem(categories[ inx ]);
                itemsfound++;
            }
        }
    }

    if (itemsfound == 0)
    {
        WebAppSpeak.speak("Ich habe keine Artikel gefunden");
    }
    else
    {
        target.imgMore.src = "arrow_less_270x270.png";
        target.onTouchClick = shoppinglist.onClickNukeSearches;
    }
}

shoppinglist.onClickMore = function(ctarget, target)
{
    WebAppUtility.makeClick();

    target.imgMore.src = "arrow_less_270x270.png";
    target.onTouchClick = shoppinglist.onClickLess;
}

shoppinglist.onClickLess = function(ctarget, target)
{
    WebAppUtility.makeClick();

    target.imgMore.src = "arrow_more_270x270.png";
    target.onTouchClick = shoppinglist.onClickMore;
}

shoppinglist.onClickAdd = function(ctarget, target)
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
        shoppinglist.saveItems();
    }
}

shoppinglist.createFrame();
shoppinglist.loadItems();

//shoppinglist.addItem(shoppinglist.parseProduct("Milch aus dem Aldi"));
//shoppinglist.addItem(shoppinglist.parseProduct("Kartoffeln aus dem Aldi"));
//shoppinglist.addItem(shoppinglist.parseProduct("Klopapier aus dem Aldi"));

//shoppinglist.addItem(shoppinglist.parseProduct("Batterien aus dem Aldi"));
//shoppinglist.addItem(shoppinglist.parseProduct("Butter aus dem Penny"));