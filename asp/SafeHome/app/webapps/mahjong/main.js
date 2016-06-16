mahjong.createFrame = function()
{
    var xx = mahjong;

    xx.boardIndex = 1;
    xx.hintLevel  = 0;
    xx.resetTiles = [];

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";
    xx.topdiv.style.backgroundImage = "url('walls/wood.jpg')";;

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);
    xx.gamePanel = WebLibSimple.createDivWidHei(0, 0, 0, 0, "gamePanel", xx.centerDiv);

    //
    // Buttons.
    //

    xx.buttonTop1div = mahjong.createButton("–");
    xx.buttonTop2div = mahjong.createButton(xx.boardIndex);
    xx.buttonTop3div = mahjong.createButton("+");

    xx.buttonBot1div = mahjong.createButton("!");
    xx.buttonBot2div = mahjong.createButton("=");
    xx.buttonBot3div = mahjong.createButton("?");

    xx.buttonTop1div.onTouchClick = mahjong.onButtonMinus;
    xx.buttonTop3div.onTouchClick = mahjong.onButtonPlus;
    xx.buttonBot1div.onTouchClick = mahjong.onLoadNewGame;
    xx.buttonBot2div.onTouchClick = mahjong.onSolveStep;
    xx.buttonBot3div.onTouchClick = mahjong.onHintPlayer;

    xx.audioInvalid = WebLibSimple.createAnyAppend("audio", null);
    xx.audioInvalid.src = "/webapps/mahjong/sounds/invalid.wav";
    xx.audioInvalid.preload = "auto";

    xx.audioTadaaa = WebLibSimple.createAnyAppend("audio", null);
    xx.audioTadaaa.src = "/webapps/mahjong/sounds/tadaaa.wav";
    xx.audioTadaaa.preload = "auto";

    xx.audioNomoves = WebLibSimple.createAnyAppend("audio", null);
    xx.audioNomoves.src = "/webapps/mahjong/sounds/nomoves.wav";
    xx.audioNomoves.preload = "auto";

    addEventListener("resize", mahjong.onWindowResize);
}

mahjong.createButton = function(text)
{
    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, 0, 0, null, mahjong.topdiv);
    WebLibSimple.setFontSpecs(buttonDiv, 64, "bold");

    buttonImg = WebLibSimple.createAnyAppend("img", buttonDiv);
    buttonImg.style.width  = "100%";
    buttonImg.style.height = "100%";
    buttonImg.src = "black_button_256x256.png";

    buttonTab = WebLibSimple.createDivWidHei(0, 0, "100%", "100%", null, buttonDiv);
    buttonTab.style.display = "table";

    buttonCen = WebLibSimple.createAnyAppend("div", buttonTab);
    buttonCen.style.display = "table-cell";
    buttonCen.style.width   = "100%";
    buttonCen.style.height  = "100%";
    buttonCen.style.color   = "#ffffff";
    buttonCen.style.verticalAlign = "middle";
    buttonCen.style.textAlign     = "center";
    buttonCen.innerHTML = text;

    buttonDiv.buttonCen = buttonCen;

    return buttonDiv;
}

mahjong.onWindowResize = function()
{
    var xx = mahjong;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    //
    // Scale game panel to best fit.
    //

    xx.panelActWid = Math.floor(wid * 85 / 100);
    xx.panelActHei = Math.floor(xx.panelRealHei * xx.panelActWid / xx.panelRealWid);

    if (xx.panelActHei > (hei - 200))
    {
        xx.panelActHei = (hei - 200);
        xx.panelActWid = Math.floor(xx.panelRealWid * xx.panelActHei / xx.panelRealHei);
    }

    xx.gamePanel.style.left   = -(xx.panelActWid >> 1) + "px";
    xx.gamePanel.style.top    = -(xx.panelActHei >> 1) + "px";
    xx.gamePanel.style.width  = xx.panelActWid + "px";
    xx.gamePanel.style.height = xx.panelActHei + "px";

    //
    // Adjust tile sizes and positions.
    //

    xx.tileActWid = xx.panelActWid / (xx.xSize >> 1);
    xx.tileActHei = xx.panelActHei / (xx.ySize >> 1);

    xx.tileTotWid = xx.tileRealWid * xx.tileActWid / xx.tilePackWid;
    xx.tileTotHei = xx.tileRealHei * xx.tileActHei / xx.tilePackHei;

    xx.tileSkewWid = Math.floor(xx.tileTotWid - xx.tileActWid);
    xx.tileSkewHei = Math.floor(xx.tileTotHei - xx.tileActHei);

    for (var zinx = 0; zinx < xx.zSize; zinx++)
    {
        var skewWid = Math.floor(xx.tileSkewWid * zinx);
        var skewHei = Math.floor(xx.tileSkewHei * zinx);

        for (var yinx = 0; yinx < xx.ySize; yinx++)
        {
            for (var xinx = 0; xinx < xx.xSize; xinx++)
            {
                var div = xx.matrix[ zinx ][ yinx ][ xinx ];
                if (! div) continue;

                div.style.left   = (Math.round(xx.tileActWid * xinx / 2) - skewWid) + "px";
                div.style.top    = (Math.round(xx.tileActHei * yinx / 2) - skewHei) + "px";
                div.style.width  = xx.tileTotWid + "px";
                div.style.height = xx.tileTotHei + "px";
            }
        }
    }

    //
    // Adjust button positions.
    //

    var spacehorz = (wid - xx.panelActWid) >> 1;
    var spacevert = (hei - xx.panelActHei) >> 1;

    var butsiz = Math.floor(Math.max(spacehorz, spacevert) * 60 / 100);
    var fntsiz = Math.floor(butsiz * 75 / 100);

    xx.buttonTop1div.style.width  = butsiz + "px";
    xx.buttonTop1div.style.height = butsiz + "px";
    xx.buttonTop2div.style.width  = butsiz + "px";
    xx.buttonTop2div.style.height = butsiz + "px";
    xx.buttonTop3div.style.width  = butsiz + "px";
    xx.buttonTop3div.style.height = butsiz + "px";

    xx.buttonBot1div.style.width  = butsiz + "px";
    xx.buttonBot1div.style.height = butsiz + "px";
    xx.buttonBot2div.style.width  = butsiz + "px";
    xx.buttonBot2div.style.height = butsiz + "px";
    xx.buttonBot3div.style.width  = butsiz + "px";
    xx.buttonBot3div.style.height = butsiz + "px";

    WebLibSimple.setFontSpecs(xx.buttonTop1div, fntsiz, "bold");
    WebLibSimple.setFontSpecs(xx.buttonTop2div, fntsiz, "bold");
    WebLibSimple.setFontSpecs(xx.buttonTop3div, fntsiz, "bold");
    WebLibSimple.setFontSpecs(xx.buttonBot1div, fntsiz, "bold");
    WebLibSimple.setFontSpecs(xx.buttonBot2div, fntsiz, "bold");
    WebLibSimple.setFontSpecs(xx.buttonBot3div, fntsiz, "bold");

    if (spacehorz < spacevert)
    {
        var spacevert = (spacevert - xx.buttonTop1div.clientHeight) >> 1;
        var spacemiddle = (wid >> 1) - (xx.buttonTop1div.clientWidth >> 1);

        xx.buttonTop1div.style.left   = spacehorz + "px";
        xx.buttonTop1div.style.top    = spacevert + "px";
        xx.buttonTop1div.style.right  = null;
        xx.buttonTop1div.style.bottom = null;

        xx.buttonTop2div.style.left   = spacemiddle + "px";
        xx.buttonTop2div.style.top    = spacevert + "px";
        xx.buttonTop2div.style.right  = null;
        xx.buttonTop2div.style.bottom = null;

        xx.buttonTop3div.style.left   = null;
        xx.buttonTop3div.style.top    = spacevert + "px";
        xx.buttonTop3div.style.right  = spacehorz + "px";
        xx.buttonTop3div.style.bottom = null;

        xx.buttonBot1div.style.left   = spacehorz + "px";
        xx.buttonBot1div.style.top    = null;
        xx.buttonBot1div.style.right  = null;
        xx.buttonBot1div.style.bottom = spacevert + "px";

        xx.buttonBot2div.style.left   = spacemiddle + "px";
        xx.buttonBot2div.style.top    = null;
        xx.buttonBot2div.style.right  = null;
        xx.buttonBot2div.style.bottom = spacevert + "px";

        xx.buttonBot3div.style.left   = null;
        xx.buttonBot3div.style.top    = null;
        xx.buttonBot3div.style.right  = spacehorz + "px";
        xx.buttonBot3div.style.bottom = spacevert + "px";
    }
    else
    {
        var spacehorz = (spacehorz - xx.buttonTop1div.clientWidth) >> 1;
        var spacemiddle = (hei >> 1) - (xx.buttonTop1div.clientHeight >> 1);

        xx.buttonTop1div.style.left   = spacehorz + "px";
        xx.buttonTop1div.style.top    = spacevert + "px";
        xx.buttonTop1div.style.right  = null;
        xx.buttonTop1div.style.bottom = null;

        xx.buttonTop2div.style.left   = spacehorz + "px";
        xx.buttonTop2div.style.top    = spacemiddle + "px";
        xx.buttonTop2div.style.right  = null;
        xx.buttonTop2div.style.bottom = null;

        xx.buttonTop3div.style.left   = spacehorz + "px";
        xx.buttonTop3div.style.top    = null;
        xx.buttonTop3div.style.right  = null;
        xx.buttonTop3div.style.bottom = spacevert + "px";

        xx.buttonBot1div.style.left   = null;
        xx.buttonBot1div.style.top    = spacevert + "px";
        xx.buttonBot1div.style.right  = spacehorz + "px";
        xx.buttonBot1div.style.bottom = null;

        xx.buttonBot2div.style.left   = null;
        xx.buttonBot2div.style.top    = spacemiddle + "px";
        xx.buttonBot2div.style.right  = spacehorz + "px";
        xx.buttonBot2div.style.bottom = null;

        xx.buttonBot3div.style.left   = null;
        xx.buttonBot3div.style.top    = null;
        xx.buttonBot3div.style.right  = spacehorz + "px";
        xx.buttonBot3div.style.bottom = spacevert + "px";
    }
}

mahjong.resetSelect = function()
{
    var xx = mahjong;

    while (xx.resetTiles.length > 0)
    {
        var target = xx.resetTiles.shift();

        if (target.isBorder && (xx.hintLevel > 0))
        {
            target.tileBack.src = "tile_border_89x117.png";
        }
        else
        {
            target.tileBack.src = "tile_neutral_89x117.png";
        }

        target.tileSelected = false;
    }
}

mahjong.removeTile = function(tile)
{
    var xx = mahjong;

    WebLibSimple.detachElement(tile);

    var pos = tile.tilePosition;
    xx.matrix[ pos.z ][ pos.y ][ pos.x ] = null;
}

mahjong.checkMove = function()
{
    var xx = mahjong;

    if (xx.selectedTile1 && xx.selectedTile2)
    {
        if (xx.canBePairedWith(xx.selectedTile1.tileName, xx.selectedTile2.tileName))
        {
            xx.removeTile(xx.selectedTile1);
            xx.removeTile(xx.selectedTile2);

            xx.computeBorder();

            if (xx.combinations.length == 0)
            {
                //
                // Game finished.
                //

                if (xx.tilesLeft == 0)
                {
                    xx.audioTadaaa.play();
                    setTimeout(xx.audioTadaaa.load, 2000);
                }
                else
                {
                    xx.audioNomoves.play();
                    setTimeout(xx.audioNomoves.load, 2000);
                }

                setTimeout(mahjong.loadNewGame, 2000);
            }
        }
        else
        {
            xx.resetTiles.push(xx.selectedTile1);
            xx.resetTiles.push(xx.selectedTile2);

            xx.resetSelect();

            xx.audioInvalid.play();
            setTimeout(xx.audioInvalid.load, 2000);
        }

        xx.selectedTile1 = null;
        xx.selectedTile2 = null;
    }
}

mahjong.onTileClick = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = mahjong;

    if (target.tileSelected)
    {
        target.tileBack.src = "tile_neutral_89x117.png";
        target.tileSelected = false;

        if (xx.selectedTile1 == target) xx.selectedTile1 = null;
        if (xx.selectedTile2 == target) xx.selectedTile2 = null;
    }
    else
    {
        if (xx.selectedTile1 && xx.selectedTile2)
        {
            //
            // User clicked again before match evaluation.
            //

            return;
        }

        target.tileBack.src = "tile_selected_89x117.png";

        if (xx.border[ target.tileKey ])
        {
            target.tileSelected = true;

            if (xx.selectedTile1 == null)
            {
                xx.selectedTile1 = target;
            }
            else
            {
                xx.selectedTile2 = target;
            }

            if (xx.selectedTile1 && xx.selectedTile2)
            {
                setTimeout(xx.checkMove, 100);
            }
        }
        else
        {
            xx.resetTiles.push(target);
            setTimeout(xx.resetSelect, 250);
        }
    }
}

mahjong.loadNewGame = function()
{
    var xx = mahjong;

    xx.newLevelTimeout = null;
    xx.hintLevel = 0;

    xx.createBoard();
    xx.onWindowResize();
}

mahjong.onLoadNewGame = function(target, ctarget)
{
    WebAppUtility.makeClick();

    mahjong.loadNewGame();
}

mahjong.onHintPlayer = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = mahjong;

    xx.hintLevel = (xx.hintLevel + 1) % 3;

    xx.computeBorder();

    if (xx.hintLevel < 2) return;
    if (xx.combinations.length == 0) return;

    var rnd = -1;
    var lev = -1;

    for (var inx = 0; inx < xx.combinations.length; inx++)
    {
        var combi = xx.combinations[ inx ];

        if ((rnd < 0) || ((combi.tile1.tilePosition.z * combi.tile2.tilePosition.z) > lev))
        {
            rnd = inx;
            lev = combi.tile1.tilePosition.z * combi.tile2.tilePosition.z;
        }
    }

    if (xx.selectedTile1) xx.resetTiles.push(xx.selectedTile1);
    if (xx.selectedTile2) xx.resetTiles.push(xx.selectedTile2);

    xx.selectedTile1 = xx.combinations[ rnd ].tile1;
    xx.selectedTile2 = xx.combinations[ rnd ].tile2;

    xx.selectedTile1.tileBack.src = "tile_selected_89x117.png";
    xx.selectedTile1.tileSelected = true;

    xx.selectedTile2.tileBack.src = "tile_selected_89x117.png";
    xx.selectedTile2.tileSelected = true;
}

mahjong.onSolveStep = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = mahjong;

    if (xx.hintLevel < 2) return;

    if (xx.selectedTile1 && xx.selectedTile2)
    {
        setTimeout(xx.checkMove, 100);
    }

    xx.hintLevel = 1;
}

mahjong.onButtonMinus = function(target, ctarget)
{
    var xx = mahjong;

    WebAppUtility.makeClick();

    if (xx.boardIndex > 1) xx.buttonTop2div.buttonCen.innerHTML = --xx.boardIndex;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGame, 1000);
}

mahjong.onButtonPlus = function(target, ctarget)
{
    var xx = mahjong;

    WebAppUtility.makeClick();

    if (xx.boardIndex < xx.boards.length) xx.buttonTop2div.buttonCen.innerHTML = ++xx.boardIndex;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGame, 1000);
}

mahjong.createFrame();
mahjong.createBoard();
mahjong.onWindowResize();
