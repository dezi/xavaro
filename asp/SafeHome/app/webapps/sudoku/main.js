sudoku.createFrame = function()
{
    var xx = sudoku;

    xx.level = 1;
    xx.hintLevel = 0;

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    //
    // Game panel.
    //

    xx.gamesize  = Math.min(wid, hei) - 100;
    xx.gamesize -= Math.floor(xx.gamesize % 9);
    xx.fieldsize = Math.floor(xx.gamesize / 9);

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

    WebLibSimple.setBGColor(xx.gamePanel, "#ffeeee");

    //
    // Digits enter panel.
    //

    xx.entersize  = Math.min(wid, hei) >> 1;
    xx.entersize -= Math.floor(xx.entersize % 9);
    xx.digitsize  = Math.floor(xx.entersize / 9);

    xx.enterDimm = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "enterDimm", xx.centerDiv);

    WebLibSimple.setBGColor(xx.enterDimm, "#88888888");
    xx.enterDimm.style.display = "none";
    xx.enterDimm.onTouchClick = xx.onTouchEnterCancel;

    xx.enterPanel = WebLibSimple.createDivWidHei(
        -xx.entersize / 2, -xx.entersize / 2,
        xx.entersize, xx.entersize,
        "enterPanel", xx.centerDiv);

    WebLibSimple.setBGColor(xx.enterPanel, "#ffffff");
    WebLibSimple.setFontSpecs(xx.enterPanel, 64, "bold");
    xx.enterPanel.style.border = "8px solid grey";
    xx.enterPanel.style.borderRadius = "16px";
    xx.enterPanel.style.display = "none";

    xx.enterTable = WebLibSimple.createAnyAppend("table", xx.enterPanel);
    xx.enterTable.style.width  = "100%";
    xx.enterTable.style.height = "100%";
    xx.enterTable.cellPadding  = "0";
    xx.enterTable.cellSpacing  = "0";
    xx.enterTable.border = "0";

    var digit = 1;

    for (var row = 0; row < 3; row++)
    {
        var tableRow = WebLibSimple.createAnyAppend("tr", xx.enterTable);

        for (var col = 0; col < 3; col++)
        {
            var tableData = WebLibSimple.createAnyAppend("td", tableRow);
            tableData.style.width  = xx.digitsize + "px";
            tableData.style.height = xx.digitsize + "px";
            tableData.style.textAlign = "center";
            tableData.style.verticalAlign = "middle";
            tableData.innerHTML = digit;

            tableData.onTouchClick = xx.onTouchEnterDigit;
            tableData.digitValue = digit;

            digit++;
        }
    }

    //
    // Buttons.
    //

    xx.buttonTop1div = sudoku.createButton("–");
    xx.buttonTop2div = sudoku.createButton(xx.level);
    xx.buttonTop3div = sudoku.createButton("+");

    xx.buttonBot1div = sudoku.createButton("!");
    xx.buttonBot2div = sudoku.createButton("*");
    xx.buttonBot3div = sudoku.createButton("?");

    xx.buttonTop1div.onTouchClick = sudoku.onButtonMinus;
    xx.buttonTop3div.onTouchClick = sudoku.onButtonPlus;
    xx.buttonBot1div.onTouchClick = sudoku.onLoadNewGame;
    xx.buttonBot2div.onTouchClick = sudoku.onSolveStep;
    xx.buttonBot3div.onTouchClick = sudoku.onHintPlayer;

    xx.audio = WebLibSimple.createAnyAppend("audio", null);
    xx.audio.src = "/webapps/sudoku/sounds/finish_level_1.wav";
    xx.audio.preload = "auto";

    xx.onWindowResize();

    addEventListener("resize", sudoku.onWindowResize);
}

sudoku.onTouchEnterDigit = function(target, ctarget)
{
    var xx = sudoku;

    WebAppUtility.makeClick();

    xx.game[ xx.enterPosition ] = target.digitValue;

    xx.enterDimm.style.display = "none";
    xx.enterPanel.style.display = "none";

    xx.displayGame();

    if (xx.hintLevel > 1)  xx.hintLevel = 1;

    var solutions = xx.getNumberSolutions();
    xx.gameCells[ xx.enterPosition ].style.color = (solutions == 1) ? "#000000" : "#ff0000";

    xx.displayHints();

    if (xx.issolved)
    {
        xx.audio.play();

        setTimeout(xx.loadNewGame, 1000);
        setTimeout(xx.audio.load, 2000);
    }
}

sudoku.onTouchEnterField = function(target, ctarget)
{
    var xx = sudoku;

    WebAppUtility.makeClick();

    xx.enterDimm.style.display = "block";
    xx.enterPanel.style.display = "block";

    xx.enterPosition = target.gamePosition;
}

sudoku.onTouchEnterCancel = function(target, ctarget)
{
    var xx = sudoku;

    WebAppUtility.makeClick();

    xx.enterDimm.style.display = "none";
    xx.enterPanel.style.display = "none";
}

sudoku.createButton = function(text)
{
    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, 128, 128, null, sudoku.topdiv);

    buttonImg = WebLibSimple.createAnyAppend("img", buttonDiv);
    buttonImg.style.width  = "100%";
    buttonImg.style.height = "100%";
    buttonImg.src = "black_button_256x256.png";

    buttonTab = WebLibSimple.createDivWidHei(0, 0, "100%", "100%", null, buttonDiv);
    buttonTab.style.display = "table";

    buttonCen = WebLibSimple.createAnyAppend("div", buttonTab);
    WebLibSimple.setFontSpecs(buttonCen, 64, "bold");
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

sudoku.createPanel = function()
{
    var xx = sudoku;

    xx.gameCells = [];

    xx.panelTable = WebLibSimple.createAnyAppend("table", xx.gamePanel);
    xx.panelTable.style.marginLeft = "2px";
    xx.panelTable.style.marginTop  = "2px";
    xx.panelTable.style.width  = "100%";
    xx.panelTable.style.height = "100%";
    xx.panelTable.cellPadding  = "0";
    xx.panelTable.cellSpacing  = "0";
    xx.panelTable.border = "0";

    var pos = 0;

    for (var row = 0; row < 9; row++)
    {
        var tableRow = WebLibSimple.createAnyAppend("tr", xx.panelTable);

        for (var col = 0; col < 9; col++)
        {
            var tableData = WebLibSimple.createAnyAppend("td", tableRow);
            tableData.style.width  = xx.fieldsize + "px";
            tableData.style.height = xx.fieldsize + "px";
            tableData.style.textAlign = "center";
            tableData.style.verticalAlign = "middle";
            tableData.onTouchClick = xx.onTouchEnterField;
            tableData.gamePosition = pos++;

            xx.gameCells.push(tableData);

            var solveTable = WebLibSimple.createAnyAppend("table", tableData);
            solveTable.style.width  = "100%";
            solveTable.style.height = "100%";
            solveTable.cellPadding  = "0";
            solveTable.cellSpacing  = "0";

            tableData.hintTable = solveTable;
            tableData.hintCells = [];

            for (var irow = 0; irow < 3; irow++)
            {
                var solveRow = WebLibSimple.createAnyAppend("tr", solveTable);

                for (var icol = 0; icol < 3; icol++)
                {
                    var solveData = WebLibSimple.createAnyAppend("td", solveRow);
                    solveData.style.fontSize = "16px";
                    solveData.style.fontWeight = "normal";
                    solveData.style.textAlign = "center";
                    solveData.style.verticalAlign = "middle";

                    solveData.innerHTML = "&ensp;";

                    tableData.hintCells.push(solveData);
                }
            }
        }
    }

    //
    // Make raster.
    //

    for (var inx = 0; inx <= 9; inx++)
    {
        var wid = (inx % 3) ? 2 : 3;

        var bar = WebLibSimple.createDivWidth(xx.fieldsize * inx, 0, wid, -wid, null, xx.gamePanel);
        WebLibSimple.setBGColor(bar, "#0000ff");

        var bar = WebLibSimple.createDivHeight(0, xx.fieldsize * inx, -wid, wid, null, xx.gamePanel);
        WebLibSimple.setBGColor(bar, "#0000ff");
    }
}

sudoku.displayGame = function()
{
    var xx = sudoku;

    for (var pos = 0; pos < 81; pos++)
    {
        xx.gameCells[ pos ].innerHTML = null;

        if (xx.game[ pos ] > 0)
        {
            xx.gameCells[ pos ].innerHTML = xx.game[ pos ];
        }
        else
        {
            xx.gameCells[ pos ].appendChild(xx.gameCells[ pos ].hintTable);
        }
    }
}

sudoku.findSingle = function(positions)
{
    var xx = sudoku;

    for (var dig = 1; dig <= 9; dig++)
    {
        var usable = [];
        var usedig = -1;

        for (var inx = 0; inx < positions.length; inx++)
        {
            var pos = positions[ inx ];

            if (xx.game[ pos ] > 0) continue;

            if (xx.horzhave[ xx.horz[ pos ] ][ dig ] ||
                xx.verthave[ xx.vert[ pos ] ][ dig ] ||
                xx.cellhave[ xx.cell[ pos ] ][ dig ])
            {
                continue;
            }

            usable.push(pos);
            usedig = dig;
        }

        if (usable.length == 1)
        {
            //
            // One cell, horz or vert with only one choice.
            //

            for (var inx = 0; inx < positions.length; inx++)
            {
                var pos = positions[ inx ];
                var gameCell = xx.gameCells[ pos ];
                WebLibSimple.setBGColor(gameCell, "#ffccff");
            }

            var pos = usable[ 0 ];
            var gameCell = xx.gameCells[ pos ];
            WebLibSimple.setBGColor(gameCell, "#ff88ff");

            xx.onlyone = pos;
            xx.onlydig = usedig;

            return pos;
        }
    }

    return -1;
}

sudoku.displayHints = function(solvestep)
{
    var xx = sudoku;

    xx.onlyone = -1;
    xx.onlydig = -1;

    xx.issolved = true;

    for (var pos = 0; pos < 81; pos++)
    {
        var gameCell = xx.gameCells[ pos ];
        WebLibSimple.setBGColor(gameCell, null);

        if (xx.game[ pos ] > 0) continue;

        xx.issolved = false;

        var numvalid = 0;
        var validdig = 0;

        for (var dig = 1; dig <= 9; dig++)
        {
            if (xx.horzhave[ xx.horz[ pos ] ][ dig ] ||
                xx.verthave[ xx.vert[ pos ] ][ dig ] ||
                xx.cellhave[ xx.cell[ pos ] ][ dig ])
            {
                gameCell.hintCells[ dig - 1 ].innerHTML = "&ensp;";
            }
            else
            {
                gameCell.hintCells[ dig - 1 ].innerHTML = (xx.hintLevel > 0) ? dig : "&ensp;";
                validdig = dig;
                numvalid++;
            }
        }

        if (numvalid == 1)
        {
            xx.onlyone = pos;
            xx.onlydig = validdig;
        }
    }

    if (xx.hintLevel < 2) return;

    //
    // Give algorithmic hints.
    //

    if (xx.onlyone >= 0)
    {
        //
        // One field with only one choice.
        //

        var gameCell = xx.gameCells[ xx.onlyone ];
        WebLibSimple.setBGColor(gameCell, "#ff8888");
    }
    else
    {
        for (var inx = 0; inx < 9; inx++)
        {
            var pos = xx.findSingle(xx.horzpositions[ inx ]);
            if (pos >= 0) break;

            var pos = xx.findSingle(xx.vertpositions[ inx ]);
            if (pos >= 0) break;

            var pos = xx.findSingle(xx.cellpositions[ inx ]);
            if (pos >= 0) break;
        }
    }

    if (! solvestep) return;

    //
    // Solve next digit.
    //

    xx.hintLevel = 1;

    if (xx.onlyone >= 0)
    {
        xx.game[ xx.onlyone ] = xx.onlydig;
    }

    xx.displayGame();
    xx.buildHints();
    xx.displayHints();

    if (xx.issolved)
    {
        xx.audio.play();

        setTimeout(xx.loadNewGame, 1000);
        setTimeout(xx.audio.load, 2000);
    }
}

sudoku.loadNewGame = function()
{
    var xx = sudoku;

    xx.newLevelTimeout = null;

    xx.hintLevel = 0;

    for (var pos = 0; pos < 81; pos++)
    {
        xx.gameCells[ pos ].innerHTML = null;
    }

    setTimeout(xx.buildGame, 0);
}

sudoku.onLoadNewGame = function(target, ctarget)
{
    WebAppUtility.makeClick();

    sudoku.loadNewGame();
}

sudoku.onHintPlayer = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = sudoku;

    xx.hintLevel = (xx.hintLevel + 1) % 3;

    xx.displayHints();
}

sudoku.onSolveStep = function(target, ctarget)
{
    WebAppUtility.makeClick();

    sudoku.displayHints(true);
}

sudoku.onButtonMinus = function(target, ctarget)
{
    var xx = sudoku;

    WebAppUtility.makeClick();

    if (xx.level > 1) xx.buttonTop2div.buttonCen.innerHTML = --xx.level;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGame, 1000);
}

sudoku.onButtonPlus = function(target, ctarget)
{
    var xx = sudoku;

    WebAppUtility.makeClick();

    if (xx.level < 9) xx.buttonTop2div.buttonCen.innerHTML = ++xx.level;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGame, 1000);
}

sudoku.onWindowResize = function()
{
    var xx = sudoku;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    var spacehorz = (wid - xx.gamesize) >> 1;
    var spacevert = (hei - xx.gamesize) >> 1;

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

sudoku.buildGame = function()
{
    var xx = sudoku;

    xx.generateGame();
    xx.displayGame();
    xx.buildHints();
    xx.displayHints();
}

sudoku.createFrame();
sudoku.createPanel();

setTimeout(sudoku.buildGame, 0);
