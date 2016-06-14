texaspoker.createFrame = function()
{
    var xx = texaspoker;

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

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

    WebLibSimple.setBGColor(xx.gamePanel, "#ffeeee");

    //
    // Buttons.
    //

    xx.buttonTop1div = texaspoker.createButton("–");
    xx.buttonTop2div = texaspoker.createButton("...");
    xx.buttonTop3div = texaspoker.createButton("+");

    xx.buttonBot1div = texaspoker.createButton("!");
    xx.buttonBot2div = texaspoker.createButton("=");
    xx.buttonBot3div = texaspoker.createButton("?");

    /*
    xx.buttonTop1div.onTouchClick = texaspoker.onButtonMinus;
    xx.buttonTop3div.onTouchClick = texaspoker.onButtonPlus;
    xx.buttonBot1div.onTouchClick = texaspoker.onLoadNewGame;
    xx.buttonBot2div.onTouchClick = texaspoker.onSolveStep;
    xx.buttonBot3div.onTouchClick = texaspoker.onHintPlayer;
    */

    addEventListener("resize", texaspoker.onWindowResize);
}

texaspoker.createButton = function(text)
{
    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, 128, 128, null, texaspoker.topdiv);

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

texaspoker.onWindowResize = function()
{
    var xx = texaspoker;

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

texaspoker.createGame = function()
{
    var xx = texaspoker;

    var scoredata = WebAppRequest.loadSync("scoretable.txt");
    scoredata = scoredata.trim().split("\n");

    console.log("blockgame.readLevel: hands=" + scoredata.length);

    xx.totalodds = 0;
    xx.scoretable = {};

    for (var inx = 0; inx < scoredata.length; inx++)
    {
        var parts = scoredata[ inx ].split("=");
        if (parts.length != 2) continue;

        var scoreval = parseInt(parts[ 1 ]);

        xx.scoretable[ parts[ 0 ] ] = scoreval;
        xx.totalodds += scoreval;
    }

    var wingames = 0;

    for (var scorekey in xx.scoretable)
    {
        var thisodd = xx.scoretable[ scorekey ];

        xx.scoretable[ scorekey ] = wingames / xx.totalodds;

        wingames += thisodd;
    }
}

texaspoker.createFrame();
texaspoker.createGame();
texaspoker.onWindowResize();
