findit.defs =
{
    "margin" : 40,
    "border" :  4,
    "radius" :  8,

    "cells"  : 12,
    "words"  : 10,
}

WebAppRequest.onBackkeyPressed = function()
{
    if (findit.gameScreen)
    {
        WebLibLaunch.removeTopScreen(findit.gameScreen);
        findit.gameScreen = null;

        WebAppRequest.haveBackkeyPressed(true);

        return;
    }

    WebAppRequest.haveBackkeyPressed(false);
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

findit.onClickGame = function(target)
{
    //
    // Identify the launch item for this click.
    //

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

    findit.puzzleDiv.style.left   = WebLibSimple.addPixel(margin);
    findit.puzzleDiv.style.top    = WebLibSimple.addPixel(margin);
    findit.puzzleDiv.style.width  = WebLibSimple.addPixel(charsSize);
    findit.puzzleDiv.style.height = WebLibSimple.addPixel(charsSize);

    //
    // Re-arrange solution div.
    //

    if (findit.solutionDiv)
    {
        var wcount = findit.solutionWordDivs.length;

        if (wid > hei)
        {
            //
            // Landscape orientation.
            //

            var solhei = charsSize;
            var solwid = wid - charsSize - margin * 2;

            findit.solutionDiv.style.right  = WebLibSimple.addPixel(margin / 4);
            findit.solutionDiv.style.bottom = WebLibSimple.addPixel(margin);

            findit.solutionDiv.style.width  = WebLibSimple.addPixel(solwid);
            findit.solutionDiv.style.height = WebLibSimple.addPixel(solhei);

            var hoff = findit.solutionDiv.clientWidth;
            var voff = Math.floor(findit.solutionDiv.clientHeight / wcount);
            var fsiz = Math.floor(voff / 2);

            WebLibSimple.setFontSpecs(findit.solutionDiv, fsiz, "bold")

            for (var inx in findit.solutionWordDivs)
            {
                var solDiv = findit.solutionWordDivs[ inx ];

                solDiv.style.left       = WebLibSimple.addPixel(0);
                solDiv.style.top        = WebLibSimple.addPixel(inx * voff);
                solDiv.style.width      = WebLibSimple.addPixel(hoff);
                solDiv.style.height     = WebLibSimple.addPixel(voff);
            }
       }
        else
        {
            //
            // Portrait orientation.
            //

            var solwid = charsSize;
            var solhei = hei - charsSize - margin * 2;

            findit.solutionDiv.style.right  = WebLibSimple.addPixel(margin);
            findit.solutionDiv.style.bottom = WebLibSimple.addPixel(margin / 4);

            findit.solutionDiv.style.width  = WebLibSimple.addPixel(solwid);
            findit.solutionDiv.style.height = WebLibSimple.addPixel(solhei);

            var hoff = Math.floor(findit.solutionDiv.clientWidth / 2);
            var voff = Math.floor(findit.solutionDiv.clientHeight / Math.floor(wcount / 2));
            var fsiz = Math.floor(voff / 2);

            WebLibSimple.setFontSpecs(findit.solutionDiv, fsiz, "bold")

            for (var inx in findit.solutionWordDivs)
            {
                var solDiv = findit.solutionWordDivs[ inx ];

                solDiv.style.left       = WebLibSimple.addPixel(((inx % 2) == 0) ? 0 : hoff);
                solDiv.style.top        = WebLibSimple.addPixel(Math.floor(inx / 2) * voff);
                solDiv.style.width      = WebLibSimple.addPixel(hoff);
                solDiv.style.height     = WebLibSimple.addPixel(voff);
            }
        }
    }
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
    // Create game elements.
    //

    findit.createPuzzle();
    findit.createSolution();
}

findit.onWordClick = function(target)
{
    if (target.style.backgroundImage == null)
    {
        //
        // Open wikipedia.
        //
    }
}

findit.onWordLongClick = function(target)
{
    if (target.style.backgroundImage != null)
    {
        target.style.backgroundImage = null;
        target.style.color = "#ff0000";
    }
}

findit.createSolution = function()
{
    //
    // Create solution game area.
    //

    findit.solutionDiv = WebLibSimple.createDivWidHei(null, null, 0, 0, "solutionDiv", findit.gameScreen);
    findit.solutionDiv.style.color = WebLibSimple.getRGBColor("#60000000");
    findit.solutionDiv.style.padding = findit.defs.border + "px";

    //
    // Create the solution word divs.
    //

    findit.solutionWordDivs = [];
    findit.solutionWordSpans = [];

    for (var inx in findit.currentGameWords)
    {
        var solDiv = WebLibSimple.createDivWidHei(null, null, 0, 0, "solWordDiv", findit.solutionDiv);
        solDiv.style.textAlign = "center";

        var solSpan = WebLibSimple.createAnyAppend("span", solDiv)
        solSpan.style.paddingTop      = "8px";
        solSpan.style.paddingBottom   = "8px";
        solSpan.style.paddingLeft     = "14px";
        solSpan.style.paddingRight    = "14px";
        solSpan.style.backgroundSize  = "100% 100%";
        solSpan.style.backgroundImage = "url('wipeout_black.png')";

        solSpan.onTouchClick = findit.onWordClick;
        solSpan.onTouchLongClick = findit.onWordLongClick;

        var solText = WebLibSimple.createAnyAppend("span", solSpan)
        solText.innerHTML = findit.currentGameWords[ inx ].clean;

        findit.solutionWordSpans.push(solSpan);
        findit.solutionWordDivs.push(solDiv);
    }

    //
    // Adjust to current screen dimensions.
    //

    findit.onResize(findit.gameScreen);
}

findit.onPuzzleClick = function(target, clickelem)
{
    var target = clickelem;

    while (target && ! target.puzzleChar)
    {
        target = target.parentElement;
    }

    if (! target) return;

    var row = target.puzzleRow;
    var col = target.puzzleCol;

    //
    // Deselect other characters if not continous.
    //

    var cells = findit.defs.cells;
    var mdivs = findit.matrixDivs;

    //
    // Check straight directions.
    //

    var cont = 0;

    if ((row > 0) && mdivs[ row - 1 ][ col ].puzzleSel) cont++;
    if ((col > 0) && mdivs[ row ][ col - 1 ].puzzleSel) cont++;
    if (((row + 1) < cells) && mdivs[ row + 1 ][ col ].puzzleSel) cont++;
    if (((col + 1) < cells) && mdivs[ row ][ col + 1 ].puzzleSel) cont++;

    //
    // Check diagonales.
    //

    var diag = 0;
    if ((row > 0) && (col > 0) && mdivs[ row - 1 ][ col - 1 ].puzzleSel) diag++;
    if (((row + 1) < cells) && (col > 0) && mdivs[ row + 1 ][ col - 1 ].puzzleSel) diag++;
    if ((row > 0) && ((col + 1) < cells) && mdivs[ row - 1 ][ col + 1 ].puzzleSel) diag++;
    if (((row + 1) < cells) && ((col + 1) < cells) && mdivs[ row + 1 ][ col + 1 ].puzzleSel) diag++;

    //
    // Reset all fields if required.
    //

    if (diag || ! cont)
    {
        for (var row = 0; row < cells; row++)
        {
            for (var col = 0; col < cells; col++)
            {
                mdivs[ row ][ col ].puzzleSel = false;
                mdivs[ row ][ col ].style.backgroundColor = null;
            }
        }
    }

    //
    // Select current field.
    //

    target.puzzleSel = true;
    target.style.backgroundColor = "#ffffbb";

    //
    // Check for a horizontal solution.
    //

    for (var row = 0; row < cells; row++)
    {
        var word = "";

        for (var col = 0; col < cells; col++)
        {
            if (mdivs[ row ][ col ].puzzleSel) word += findit.matrixChars[ row ][ col ];
        }

        if (findit.checkGameWord(word))
        {
            for (var col = 0; col < cells; col++)
            {
                if (mdivs[ row ][ col ].puzzleSel)
                {
                    mdivs[ row ][ col ].puzzleSel = false;
                    mdivs[ row ][ col ].style.backgroundColor = null;
                    mdivs[ row ][ col ].style.color = "#ff0000"
                }
            }
        }
    }

    //
    // Check for a vertical solution.
    //

    for (var col = 0; col < cells; col++)
    {
        var word = "";

        for (var row = 0; row < cells; row++)
        {
            if (mdivs[ row ][ col ].puzzleSel) word += findit.matrixChars[ row ][ col ];
        }

        if (findit.checkGameWord(word))
        {
            for (var row = 0; row < cells; row++)
            {
                if (mdivs[ row ][ col ].puzzleSel)
                {
                    mdivs[ row ][ col ].puzzleSel = false;
                    mdivs[ row ][ col ].style.backgroundColor = null;
                    mdivs[ row ][ col ].style.color = "#ff0000"
                }
            }
        }
    }
}

findit.gameSolved = function()
{
    var gameLabel = WebLibStrings.getTransTrans("list.index.keys", findit.currentGameKey);

    WebAppSpeak.speak(WebLibStrings.getTrans("game.solved", gameLabel));

    WebAppActivity.recordActivity(WebLibStrings.getTrans("activity.game.solved", gameLabel));
}

findit.checkGameWord = function(word)
{
    var ok = false;

    if (word.length > 0)
    {
        var allok = true;

        for (var inx in findit.currentGameWords)
        {
            if (word == findit.currentGameWords[ inx ].clean)
            {
                //
                // Valid game word.
                //

                var solSpan = findit.solutionWordSpans[ inx ];
                solSpan.style.color = WebLibSimple.getRGBColor("#60000000");
                solSpan.style.backgroundImage = null;

                findit.currentGameWords[ inx ].found = ok = true;
            }

            allok = allok && findit.currentGameWords[ inx ].found;
        }

        if (allok) setTimeout(findit.gameSolved, 1000);
    }

    return ok;
}

findit.createPuzzle = function()
{
    //
    // Create puzzle game area.
    //

    findit.puzzleDiv = WebLibSimple.createDivWidHei(0, 0, 0, 0, "puzzleDiv", findit.gameScreen);
    findit.puzzleDiv.style.border = findit.defs.border + "px solid grey";
    findit.puzzleDiv.style.borderRadius = findit.defs.radius + "px";
    findit.puzzleDiv.onTouchClick = findit.onPuzzleClick;

    //
    // Adjust to current screen dimensions.
    //

    findit.onResize(findit.gameScreen);

    //
    // Computer cell dimensions after resize.
    //

    var innerSize = findit.puzzleDiv.clientWidth;
    var cellSize  = (innerSize / findit.defs.cells);
    var cellInner = cellSize - 4;
    var fontSize  = cellInner - 4;

    console.log("Innersize=" + innerSize + ":" + cellSize);

    //
    // Set font specs to master div.
    //

    findit.puzzleDiv.style.textAlign = "center";
    findit.puzzleDiv.style.lineHeight = WebLibSimple.addPixel(cellSize);
    WebLibSimple.setFontSpecs(findit.puzzleDiv, fontSize, "bold", "#999999")

    //
    // Create target words.
    //

    while (true)
    {
        findit.computeRandomWords();

        if (findit.positionGameWords())
        {
            //
            // All words could be positoned.
            //

            if (findit.verifyGameWords())
            {
                //
                // No other words are present.
                //

                break;
            }
        }
    }

    //
    // Create character elements.
    //

    findit.matrixDivs = [];

    for (var row = 0; row < findit.defs.cells; row++)
    {
        var rowDivs = [];

        for (var col = 0; col < findit.defs.cells; col++)
        {
            var left = col * cellSize;
            var top  = row * cellSize;

            var cccDiv = WebLibSimple.createDivWidHei(left, top, cellSize, cellSize, null, findit.puzzleDiv);
            cccDiv.style.border = "1px solid grey";
            cccDiv.innerHTML = findit.matrixChars[ row ][ col ];
            cccDiv.puzzleChar = true;
            cccDiv.puzzleCol = col;
            cccDiv.puzzleRow = row;

            rowDivs.push(cccDiv);
        }

        findit.matrixDivs.push(rowDivs);
    }
}

findit.setupCleanRules = function(clean)
{
    //
    // Enable clean rules.
    //

    findit.cleanNormal = false;
    findit.cleanPrefix = false;
    findit.cleanHyphen = false;
    findit.cleanMultis = false;

    for (var inx in clean)
    {
        var rule = clean[ inx ];

        if (rule == "normal") findit.cleanNormal = true;
        if (rule == "prefix") findit.cleanPrefix = true;
        if (rule == "hyphen") findit.cleanHyphen = true;
        if (rule == "multis") findit.cleanMultis = true;
    }
}

findit.cleanupTargetWord = function(name)
{
    //
    // Normal rule: "Echter Steinklee (gelb)" => "Echter Steinklee"
    //

    if (findit.cleanNormal)
    {
        name = name.split("(");
        name = name[ 0 ].trim();

        var temp = name.split(", ");
        if (temp.length == 2) name = temp[ 1 ] + " " + temp[ 0 ];
    }

    //
    // Multis rule: "Hannah / Hanna" => "Hannah"
    //

    if (findit.cleanMultis)
    {
        name = name.split(" / ");
        name = name[ 0 ];
    }

    //
    // Prefix rule: "Afrikanischer Habichtsadler" => "Habichtsadler"
    //

    if (findit.cleanPrefix)
    {
        name = name.split(" ");
        name = name[ name.length - 1 ];
    }

    //
    // Hyphen rule: "Afrika-Waldkauz" => "Waldkauz"
    //

    if (findit.cleanHyphen)
    {
        name = name.split("-");
        name = name[ name.length - 1 ];
    }

    //
    // Normal rule rest.
    //

    if (findit.cleanNormal)
    {
        name = name.toUpperCase();

        name = name.replace(/ /g,"");
        name = name.replace(/-/g,"");
        name = name.replace(/’/g,"");
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

findit.positionGameWords = function()
{
    //
    // Setup empty matrix.
    //

    var cells = findit.defs.cells;

    findit.matrixChars = [];

    for (var hei = 0; hei < cells; hei++)
    {
        var row = [];

        for (var wid = 0; wid < cells; wid++)
        {
            row.push(" ");
        }

        findit.matrixChars.push(row);
    }

    //
    // Position all words by random.
    //

    var conflict = false;

    for (var inx in findit.currentGameWords)
    {
        var word = findit.currentGameWords[ inx ].clean;
        var wlen = word.length;
        var maxpos = cells - wlen;

        for (var retry = 0; retry < 100; retry++)
        {
            conflict = false;

            var horz = ((inx % 2) == 0); //(Math.random() >= 0.5);
            var maxstart = Math.floor(Math.random() * maxpos);
            var posstart = Math.floor(Math.random() * cells);

            if (horz)
            {
                var row = posstart;
                var col = maxstart;

                for (var fnz = 0; fnz < wlen; fnz++)
                {
                    var ccc = findit.matrixChars[ row ][ col++ ];

                    if ((ccc != " ") && (ccc != word.charAt(fnz)))
                    {
                        conflict = true;
                        break;
                    }
                }

                if (conflict) continue;

                var row = posstart;
                var col = maxstart;

                for (var fnz = 0; fnz < wlen; fnz++)
                {
                    findit.matrixChars[ row ][ col++ ] = word.charAt(fnz);
                }
            }
            else
            {
                var row = maxstart;
                var col = posstart;

                for (var fnz = 0; fnz < wlen; fnz++)
                {
                    var ccc = findit.matrixChars[ row++ ][ col ];

                    if ((ccc != " ") && (ccc != word.charAt(fnz)))
                    {
                        conflict = true;
                        break;
                    }
                }

                if (conflict) continue;

                var row = maxstart;
                var col = posstart;

                for (var fnz = 0; fnz < wlen; fnz++)
                {
                    findit.matrixChars[ row++ ][ col ] = word.charAt(fnz);
                }
            }

            break;
        }

        if (conflict) break;
    }

    if (conflict) return false;

    //
    // Fill in junk characters.
    //

    var alpha = "abcdefghijkmlnopqrstuvwxyz";

    for (var row = 0; row < cells; row++)
    {
        for (var col = 0; col < cells; col++)
        {
            if (findit.matrixChars[ row ][ col ] == " ")
            {
                var rnd = Math.floor(Math.random() * alpha.length);
                findit.matrixChars[ row ][ col ] = alpha.charAt(rnd);
            }
        }
    }

    return true;
}

findit.verifyGameWords = function()
{
    //
    // Verify current game not to contain any other
    // word from wordlist by coincidence.
    //

    var cells = findit.defs.cells;

    var current = {};

    for (var inx in findit.currentGameWords)
    {
        current[ findit.currentGameWords[ inx ].clean ] = true;
    }

    //
    // Build all row and col strings.
    //

    var allstrings = [];

    for (var row = 0; row < cells; row++)
    {
        var string = "";

        for (var col = 0; col < cells; col++)
        {
            string += findit.matrixChars[ row ][ col ];
        }

        allstrings.push(string);
    }

    for (var col = 0; col < cells; col++)
    {
        var string = "";

        for (var row = 0; row < cells; row++)
        {
            string += findit.matrixChars[ row ][ col ];
        }

        allstrings.push(string);
    }

    //
    // Derive tab index of the "name".
    //

    var tabinx = -1;

    for (tabinx in findit.currentGameListTabs)
    {
        if (findit.currentGameListTabs[ tabinx ] == "name") break;
    }

    if (tabinx < 0)
    {
        //
        // Misconfiguration.
        //

        console.log("findit.verifyGameWords: no name tab:" + findit.currentGameKey);

        return false;
    }

    //
    // Build all other strings.
    //

    for (var inx in findit.currentGameList)
    {
        var entry = findit.currentGameList[ inx ];
        var name = entry[ tabinx ];
        var clean = findit.cleanupTargetWord(name);

        if (current[ clean ]) continue;

        for (var cnt in allstrings)
        {
            if (allstrings[ cnt ].indexOf(clean) >= 0)
            {
                console.log("findit.verifyGameWords: found:" + clean);

                var bad = true;

                for (var fnz in current)
                {
                    if (fnz.indexOf(clean) >= 0)
                    {
                        console.log("findit.verifyGameWords: inside:" + fnz);
                        bad = false;
                    }
                }

                if (bad) return false;
            }
        }
    }

    return true;
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
    // Setup word cleaning rules.
    //

    findit.setupCleanRules(findit.currentGameListClean);

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
        // Discard disabled names.
        //

        if (word.name.charAt(0) == "#") continue;

        //
        // Discard names w/o wikipedia entry.
        //

        if (! word.wiki) continue;

        //
        // Cleanup name for display.
        //

        word.clean = findit.cleanupTargetWord(word.name);

        //
        // Check word length.
        //

        if (word.clean.length > findit.defs.cells) continue;

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

        findit.setupCleanRules(clean);

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

            word.clean = findit.cleanupTargetWord(word.name);
            if (word.clean.length == 0) console.log("findit.checkAllWords: " + JSON.stringify(word));

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
        config.onTouchClick = findit.onClickGame;

        //
        // Create launch item and push to list.
        //

        findit.lauchItems.push = WebLibLaunch.createLaunchItem(config);
    }
}

WebLibLaunch.createFrame();

findit.readConfigLists();
//findit.checkAllWords();
findit.createLaunchItems();
