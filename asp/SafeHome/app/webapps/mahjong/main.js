mahjong.createFrame = function()
{
    var xx = mahjong;

    xx.boardIndex = 1;

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);
    xx.gamePanel = WebLibSimple.createDivWidHei(0, 0, 0, 0, "gamePanel", xx.centerDiv);

    WebLibSimple.setBGColor(xx.gamePanel, "#ffeeee");

    //
    // Buttons.
    //

    xx.buttonTop1div = mahjong.createButton("–");
    xx.buttonTop2div = mahjong.createButton(xx.boardIndex);
    xx.buttonTop3div = mahjong.createButton("+");

    xx.buttonBot1div = mahjong.createButton("!");
    xx.buttonBot2div = mahjong.createButton("*");
    xx.buttonBot3div = mahjong.createButton("?");

    /*
    xx.buttonTop1div.onTouchClick = mahjong.onButtonMinus;
    xx.buttonTop3div.onTouchClick = mahjong.onButtonPlus;
    xx.buttonBot1div.onTouchClick = mahjong.onLoadNewGame;
    xx.buttonBot2div.onTouchClick = mahjong.onSolveStep;
    xx.buttonBot3div.onTouchClick = mahjong.onHintPlayer;
    */

    addEventListener("resize", mahjong.onWindowResize);
}

mahjong.createButton = function(text)
{
    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, 128, 128, null, mahjong.topdiv);

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

mahjong.onWindowResize = function()
{
    var xx = mahjong;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    xx.panelActWid = (wid - 100);
    xx.panelActHei = Math.floor(xx.panelRealHei * xx.panelActWid / xx.panelRealWid);

    if (xx.panelActHei > (hei - 150))
    {
        xx.panelActHei = (hei - 150);
        xx.panelActWid = Math.floor(xx.panelRealWid * xx.panelActHei / xx.panelRealHei);
    }

    xx.gamePanel.style.left   = -(xx.panelActWid >> 1) + "px";
    xx.gamePanel.style.top    = -(xx.panelActHei >> 1) + "px";
    xx.gamePanel.style.width  = xx.panelActWid + "px";
    xx.gamePanel.style.height = xx.panelActHei + "px";

    var spacehorz = (wid - xx.panelActWid) >> 1;
    var spacevert = (hei - xx.panelActHei) >> 1;

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

mahjong.createFrame();
mahjong.createBoard();
mahjong.onWindowResize();
