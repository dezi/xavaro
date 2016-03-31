findit.defs =
{
    "margin" : 40,
    "border" :  4,
    "radius" :  8,

    "cells"  : 12,
    "words"  : 10,
}

findit.readConfigLists = function()
{
    //
    // Get locale string from Android system.
    //

    var localelists = "lists." + WebAppUtility.getLocale();

    //
    // Load default lists as initial set.
    //

    findit.configLists = JSON.parse(WebAppRequest.loadSync("lists.json"));

    //
    // Load locale lists if present.
    //

    for (var inx in findit.configLists.locales)
    {
        if (findit.configLists.locales[ inx ] == localelists)
        {
            //
            // We have found our locale list. Read it into memory.
            //

            var localedata = JSON.parse(WebAppRequest.loadSync(localelists + ".json"));

            //
            // Merge entries into base list.
            //

            for (var key in localedata) findit.configLists[ key ] = localedata[ key ];
        }
    }
}

findit.onClickGame = function(event)
{
    //
    // Stop event from beeing handed to other elements.
    //

    event.stopPropagation();

    //
    // Identify the launch item for this click.
    //

    var target = WebLibSimple.findTarget(event.target, "launchItem");
    if (! (target && target.config)) return;

    //
    // Give haptic feedback for click received.
    //

    WebAppUtility.makeClick();

    //
    // Create or open game page.
    //

    findit.currentGameKey = target.config.gameKey;

    findit.createGameScreen();
}

findit.onResize = function(elem)
{
    //
    // Retrieve current dimensions of screen after resize.
    //

    var wid = WebLibLaunch.getWidth();
    var hei = WebLibLaunch.getHeight();

    //
    // Compute preliminary size.
    //

    var margin = findit.defs.margin;
    var charsSize = (wid < hei) ? (wid - margin * 2) : (hei - margin * 2);
    charsSize -= findit.defs.border * 2;

    //
    // Compute rounded difference.
    //

    var cellSize  = Math.floor(charsSize / findit.defs.cells);
    var innerRest = charsSize - (cellSize * findit.defs.cells);

    //
    // Distribute the rest of pixels into margins.
    //

    charsSize -= innerRest;
    margin += Math.floor(innerRest / 2);

    //
    // Size setup.
    //

    findit.charsDiv.style.left   = WebLibSimple.addPixel(margin);
    findit.charsDiv.style.top    = WebLibSimple.addPixel(margin);
    findit.charsDiv.style.width  = WebLibSimple.addPixel(charsSize);
    findit.charsDiv.style.height = WebLibSimple.addPixel(charsSize);
}

findit.createGameScreen = function()
{
    //
    // Create top game screen and attach to launcher.
    //

    findit.gameScreen = WebLibSimple.createDiv(0, 0, 0, 0, "gameScreen");
    WebLibSimple.setDefaultBGColor(findit.gameScreen);
    WebLibLaunch.addTopScreen(findit.gameScreen);

    //
    // Register resize callback on orientation change.
    //

    findit.gameScreen.onresize = findit.onResize;

    //
    // Create characters game area.
    //

    findit.charsDiv = WebLibSimple.createDivWidHei(0, 0, 0, 0, "charsDiv", findit.gameScreen);
    findit.charsDiv.style.border = findit.defs.border + "px solid grey";
    findit.charsDiv.style.borderRadius = findit.defs.radius + "px";
    findit.charsDiv.style.borderRadius = findit.defs.radius + "px";

    //
    // Adjust to current screen dimensions.
    //

    findit.onResize(findit.gameScreen);

    //
    // Computer cell dimensions after resize.
    //

    var innerSize = findit.charsDiv.clientWidth;
    var cellSize  = (innerSize / findit.defs.cells);
    var cellInner = cellSize - 4;
    var fontSize  = cellInner - 4;

    console.log("Innersize=" + innerSize + ":" + cellSize);

    //
    // Set font specs to master div.
    //

    findit.charsDiv.style.textAlign = "center";
    findit.charsDiv.style.lineHeight = WebLibSimple.addPixel(cellSize);
    WebLibSimple.setFontSpecs(findit.charsDiv, fontSize, "bold", "#999999")

    //
    // Create character elements.
    //

    for (var row = 0; row < findit.defs.cells; row++)
    {
        for (var col = 0; col < findit.defs.cells; col++)
        {
            var left = col * cellSize;
            var top  = row * cellSize;

            var cccDiv = WebLibSimple.createDivWidHei(left, top, cellSize, cellSize, null, findit.charsDiv);
            cccDiv.style.border = "1px solid grey";
            cccDiv.innerHTML = "C";
        }
    }

    findit.computeRandomWords();
}

findit.cleanupTargetWord = function(clean, name)
{
    //
    // Enable clean rules.
    //

    var normal = false;
    var prefix = false;
    var hyphen = false;
    var multis = false;

    for (var inx in clean)
    {
        var rule = clean[ inx ];

        if (rule == "normal") normal = true;
        if (rule == "prefix") prefix = true;
        if (rule == "hyphen") hyphen = true;
        if (rule == "multis") multis = true;
    }

    //
    // Normal rule: "Echter Steinklee (gelb)" => "Echter Steinklee"
    //

    if (normal)
    {
        name = name.split("(");
        name = name[ 0 ];
    }

    //
    // Multis rule: "Hannah / Hanna" => "Hannah"
    //

    if (multis)
    {
        name = name.split(" / ");
        name = name[ 0 ];
    }

    //
    // Prefix rule: "Afrikanischer Habichtsadler" => "Habichtsadler"
    //

    if (prefix)
    {
        name = name.split(" ");
        name = name[ name.length - 1 ];
    }

    //
    // Hyphen rule: "Afrika-Waldkauz" => "Waldkauz"
    //

    if (hyphen)
    {
        name = name.split("-");
        name = name[ name.length - 1 ];
    }

    //
    // Normal rule rest.
    //

    if (normal)
    {
        name = name.toUpperCase();

        name = name.replace(/ /g,"");
        name = name.replace(/-/g,"");
        name = name.replace(/’/g,"");
        name = name.replace(/,/g,"");
        name = name.replace(/\./g,"");
        name = name.replace(/ß/g,"ss");

        name = name.replace(/Ä/g,"AE");
        name = name.replace(/Ö/g,"OE");
        name = name.replace(/Ü/g,"UE");

        name = name.replace(/Á/g,"A");
        name = name.replace(/Â/g,"A");
        name = name.replace(/Ă/g,"A");
        name = name.replace(/Ã/g,"A");
        name = name.replace(/É/g,"E");
        name = name.replace(/È/g,"E");
        name = name.replace(/Ë/g,"E");
        name = name.replace(/Í/g,"I");
        name = name.replace(/Ō/g,"O");
        name = name.replace(/Ó/g,"O");
        name = name.replace(/Ú/g,"U");
        name = name.replace(/Š/g,"S");
        name = name.replace(/Ș/g,"S");
    }

    return name;
}

findit.computeRandomWords = function()
{
    console.log("findit.currentGameKey:" + findit.currentGameKey);

    //
    // Lookup game key in index and derived key for list.
    //

    var listKeys = findit.configLists[ "list.index.keys" ];
    var gameListKey = null;

    for (var inx in listKeys)
    {
        if (listKeys[ inx ] == findit.currentGameKey)
        {
            gameListKey = findit.configLists[ "list.index.vals" ][ inx ];
            break;
        }
    }

    //
    // Check result.
    //

    if (! (gameListKey
            && findit.configLists[ gameListKey ]
            && findit.configLists[ gameListKey + ".tabs" ]
            && findit.configLists[ gameListKey + ".clean" ]))
    {
        //
        // Misconfiguration.
        //

        console.log("findit.computeRandomWords: not found:" + findit.currentGameKey);

        return;
    }

    //
    // Register our current word list.
    //

    findit.currentGameList = findit.configLists[ gameListKey ];
    findit.currentGameListTabs = findit.configLists[ gameListKey + ".tabs" ];
    findit.currentGameListClean = findit.configLists[ gameListKey + ".clean" ];

    console.log("findit.computeRandomWords: list.length=" + findit.currentGameList.length);

    //
    // Pick a number of random words.
    //

    findit.currentGameWords = [];

    var used = {};

    while (findit.currentGameWords.length < findit.defs.words)
    {
        var rand = Math.floor(Math.random() * findit.currentGameList.length);
        var entry = findit.currentGameList[ rand ];

        //
        // Format entry into word object using the column tabs names.
        //

        var word = {};

        for (var inx in findit.currentGameListTabs)
        {
            var tabname  = findit.currentGameListTabs[ inx ];
            var tabvalue = entry[ inx ];

            word[ tabname ] = tabvalue;
        }

        //
        // Cleanup name for display.
        //

        word.clean = findit.cleanupTargetWord(findit.currentGameListClean, word.name);

        //
        // Check for duplicate.
        //

        if (used[ word.clean ]) continue;
        used[ word.clean ] = true;

        //
        // Add word to game words.
        //

        findit.currentGameWords.push(word);
    }

    for (var inx in findit.currentGameWords)
    {
        var word = findit.currentGameWords[ inx ];
        console.log("findit.computeRandomWords: " + JSON.stringify(word));
    }
}

findit.checkAllWords = function()
{
    //
    // Evaluate all words of all lists and find bad words.
    //

    var listKeys = findit.configLists[ "list.index.keys" ];

    for (var inx in listKeys)
    {
        gameListKey = findit.configLists[ "list.index.vals" ][ inx ];

        var list  = findit.configLists[ gameListKey ];
        var tabs  = findit.configLists[ gameListKey + ".tabs" ];
        var clean = findit.configLists[ gameListKey + ".clean" ];

        if (! (list && tabs && clean))
        {
            //
            // Misconfiguration.
            //

            if (! list) console.log("findit.checkAllWords: list missing:" + gameListKey);
            if (! tabs) console.log("findit.checkAllWords: tabs missing:" + gameListKey);
            if (! clean) console.log("findit.checkAllWords: clean missing:" + gameListKey);

            continue;
        }

        for (var cnt in list)
        {
            var entry = list[ cnt ];

            var word = {};

            for (var fnz in tabs)
            {
                var tabname  = tabs[ fnz ];
                var tabvalue = entry[ fnz ];

                word[ tabname ] = tabvalue;
            }

            word.clean = findit.cleanupTargetWord(clean, word.name);
            var res = word.clean.match(/[^A-Z]/g);

            if (res) console.log("findit.checkAllWords: " + word.clean);
        }
    }
}

findit.createLaunchItems = function()
{
    //
    // Shortcuts to config lists.
    //

    var gamesListKeys  = findit.configLists[ "list.index.keys"  ];
    var gamesListVals  = findit.configLists[ "list.index.vals"  ];
    var gamesListIcons = findit.configLists[ "list.index.icons" ];

    //
    // Empty list of launch items.
    //

    findit.lauchItems = [];

    //
    // Create a launch item for each different game.
    //

    for (var inx in gamesListKeys)
    {
        //
        // Derive required data.
        //

        var gameKey   = gamesListKeys [ inx ];
        var gameIcon  = gamesListIcons[ inx ];

        //
        // Translate the game key into locale language name.
        //

        var gameLabel = WebLibStrings.getTransTrans("list.index.keys", gameKey);

        //
        // Configure launch item.
        //

        var config = {};

        config.icon = gameIcon;
        config.label = gameLabel;
        config.gameKey = gameKey;
        config.onclick = findit.onClickGame;

        //
        // Create launch item and push to list.
        //

        findit.lauchItems.push = WebLibLaunch.createLaunchItem(config);
    }
}

WebLibLaunch.createFrame();

findit.readConfigLists();
findit.createLaunchItems();
