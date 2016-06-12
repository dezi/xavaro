sudoku.createFrame = function()
{
    var xx = sudoku;

    xx.level = 1;

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    xx.gamesize  = Math.min(wid, hei) - 100;
    xx.gamesize -= Math.floor(xx.gamesize % 9);
    xx.fieldsize = Math.floor(xx.gamesize / 9);

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

    WebLibSimple.setBGColor(xx.gamePanel, "#ffdddd");

    xx.buttonTop1div = sudoku.createButton("–");
    xx.buttonTop2div = sudoku.createButton(" ");
    xx.buttonTop3div = sudoku.createButton("+");

    xx.buttonBot1div = sudoku.createButton("!");
    xx.buttonBot2div = sudoku.createButton(" ");
    xx.buttonBot3div = sudoku.createButton("?");

    xx.buttonTop1div.onTouchClick = sudoku.onButtonMinus;
    xx.buttonTop3div.onTouchClick = sudoku.onButtonPlus;
    xx.buttonBot1div.onTouchClick = sudoku.onLoadNewGame;
    xx.buttonBot3div.onTouchClick = sudoku.onHintPlayer;

    xx.onWindowResize();

    addEventListener("resize", sudoku.onWindowResize);
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
    xx.panelTable.style.width  = "100%";
    xx.panelTable.style.height = "100%";
    xx.panelTable.cellPadding  = "0";
    xx.panelTable.cellSpacing  = "0";
    xx.panelTable.border = "1";

    for (var row = 0; row < 9; row++)
    {
        var tableRow = WebLibSimple.createAnyAppend("tr", xx.panelTable);

        for (var col = 0; col < 9; col++)
        {
            var tableData = WebLibSimple.createAnyAppend("td", tableRow);
            tableData.style.width  = (xx.fieldsize - 0) + "px";
            tableData.style.height = (xx.fieldsize - 2) + "px";
            tableData.style.textAlign = "center";
            tableData.style.verticalAlign = "middle";
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

sudoku.displayHints = function()
{
    var xx = sudoku;

    for (var pos = 0; pos < 81; pos++)
    {
        if (xx.game[ pos ] > 0) continue;

        var gameCell = xx.gameCells[ pos ];

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
                gameCell.hintCells[ dig - 1 ].innerHTML = dig;
            }
        }
    }
}

sudoku.loadNewGameLevel = function()
{
    sudoku.newLevelTimeout = null;
    sudoku.buildGame();
}

sudoku.onLoadNewGame = function(target, ctarget)
{
    WebAppUtility.makeClick();
    sudoku.buildGame();
}

sudoku.onHintPlayer = function(target, ctarget)
{
    WebAppUtility.makeClick();
}

sudoku.onButtonMinus = function(target, ctarget)
{
    var xx = sudoku;

    WebAppUtility.makeClick();

    if (xx.level > 1) xx.buttonTop2div.buttonCen.innerHTML = --xx.level;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGameLevel, 1000);
}

sudoku.onButtonPlus = function(target, ctarget)
{
    var xx = sudoku;

    WebAppUtility.makeClick();

    if (xx.level < 10) xx.buttonTop2div.buttonCen.innerHTML = ++xx.level;

    if (xx.newLevelTimeout) clearTimeout(xx.newLevelTimeout);
    xx.newLevelTimeout = setTimeout(xx.loadNewGameLevel, 1000);
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
sudoku.buildGame();
