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

    xx.gamesize  = Math.floor(Math.min(wid, hei) * 85 / 100);

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

    WebLibSimple.setBGColor(xx.gamePanel, "#ffeeee");

    //
    // Buttons.
    //

    xx.buttonTop1div = texaspoker.createButton("–");
    xx.buttonTop2div = texaspoker.createButton("$");
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
    var xx = texaspoker;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    var butsiz = Math.floor((Math.max(wid, hei) - xx.gamesize) * 75 / 200);
    var fntsiz = Math.floor(butsiz * 70 / 100);

    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, butsiz, butsiz, null, texaspoker.topdiv);
    WebLibSimple.setFontSpecs(buttonDiv, fntsiz, "bold");

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

    //
    // Initialize global score lookup.
    //

    var scoredata = WebAppRequest.loadSync("scoretable.txt");
    scoredata = scoredata.split("\n");
    if (scoredata[ scoredata.length - 1 ] == "") scoredata.pop();

    console.log("texaspoker.createGame: scores=" + scoredata.length);

    xx.totalodds = 0;
    xx.scoretable = {};

    for (var inx = 0; inx < scoredata.length; inx++)
    {
        var parts = scoredata[ inx ].split("=");
        if (parts.length != 3) continue;

        var winpercent = parseFloat(parts[ 1 ]);
        var scoreodds = parseInt(parts[ 2 ]);

        xx.scoretable[ parts[ 0 ] ] = { odd : scoreodds, win : winpercent };
        xx.totalodds += scoreodds;
    }

    console.log("texaspoker.createGame: total=" + xx.totalodds);

    if (xx.totalodds != 133784560)
    {
        alert("Datenbank stimmt nicht...")
    }

    //
    // Initialize start hands.
    //

    var handsdata = WebAppRequest.loadSync("handstable.txt");
    handsdata = handsdata.split("\n");
    if (handsdata[ handsdata.length - 1 ] == "") handsdata.pop();

    xx.totalhands = parseInt(handsdata.shift());
    xx.starthands = {};
    xx.starthand  = {};

    console.log("texaspoker.createGame: totalhands=" + xx.totalhands);

    for (var inx = 0; inx < handsdata.length; inx++)
    {
        var parts = handsdata[ inx ].split(",");
        if (parts.length != 3) continue;

        var hands = parts[ 0 ];
        var win = parseInt(parts[ 1 ]);
        var split = parseInt(parts[ 2 ]);
        var loose = xx.totalhands - win - split;

        xx.starthands[ hands ] = [ win, split ];

        var handa = hands[ 0 ] + hands[ 1 ];
        var handb;

        if (hands[ 2 ] == "s")
        {
            handa = handa + hands[ 2 ];
            handb = hands[ 3 ] + hands[ 4 ];

            if (hands.length == 6) handb = handb + hands[ 5 ];
        }
        else
        {
            handb = hands[ 2 ] + hands[ 3 ];
            if (hands.length == 5) handb = handb + hands[ 4 ];
        }

        if (! xx.starthand[ handa ]) xx.starthand[ handa ] = [ 0, 0, 0 ];
        if (! xx.starthand[ handb ]) xx.starthand[ handb ] = [ 0, 0, 0 ];

        xx.starthand[ handa ][ 0 ] += win;
        xx.starthand[ handa ][ 1 ] += loose;
        xx.starthand[ handa ][ 2 ] += split;

        if (handa != handb)
        {
            xx.starthand[ handb ][ 0 ] += loose;
            xx.starthand[ handb ][ 1 ] += win;
            xx.starthand[ handb ][ 2 ] += split;
        }
    }

    for (var hand in xx.starthand)
    {
        var wins   = xx.starthand[ hand ][ 0 ];
        var looses = xx.starthand[ hand ][ 1 ];
        var splits = xx.starthand[ hand ][ 2 ];

        var strength = Math.floor(wins / (wins + looses + split) * 10000) / 100.0;

        console.log("texaspoker.createGame:"
            + " hand=" + hand
            + " strength=" + strength
            + " wins="   + wins
            + " looses=" + looses
            + " splits=" + splits
            );
    }

    //
    // Initialize tables.
    //

    xx.card2val = [];
    xx.card2col = [];

    for (var inx = 0; inx < 52; inx++)
    {
        xx.card2val[ inx ] = inx / 4;
        xx.card2col[ inx ] = inx % 4;
    }

    xx.vals2hex = [];

    for (var inx = 0; inx < 13; inx++)
    {
        xx.vals2hex[ inx ] = String.fromCharCode(inx + (inx < 10 ? 48 : 87));
    }

    xx.wincards =
    [
        5, // High card.
        2, // Two of a kind.
        4, // Two pairs.
        3, // Three of a kind.
        5, // Straight.
        5, // Flush.
        5, // Full house.
        4, // Four of a kind.
        5, // Straight flush.
        5, // Royal flush.
    ];

    xx.winsuited =
    [
        false, // High card.
        false, // Two of a kind.
        false, // Two pairs.
        false, // Three of a kind.
        false, // Straight.
        true,  // Flush.
        false, // Full house.
        false, // Four of a kind.
        true,  // Straight flush.
        true,  // Royal flush.
    ];
}

texaspoker.createFrame();
texaspoker.createGame();
texaspoker.onWindowResize();
