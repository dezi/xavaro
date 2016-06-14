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

    console.log("texaspoker.createGame: total=" + xx.totalodds);

    var wingames = 0;
    var logcnt = 0;

    for (var scorekey in xx.scoretable)
    {
        var thisodd = xx.scoretable[ scorekey ];
        var thiswin = wingames / xx.totalodds;

        xx.scoretable[ scorekey ] = { odd : thisodd, win : thiswin };

        wingames += thisodd;

        //if ((logcnt++ % 100) == 0) console.log(scorekey + "=" + thiswin);
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
    ]
}

texaspoker.dumpPreflopOdds = function()
{
    var xx = texaspoker;

    for (var c1 = 12; c1 >= 0; c1--)
    {
        var hex1 = xx.vals2hex[ c1 ];

        for (var c2 = c1; c2 >= 0; c2--)
        {
            var hex2 = xx.vals2hex[ c2 ];

            var wins = 0;
            var hands = 0;

            for (var scorekey in xx.scoretable)
            {
                //
                // Relevant cards per win type.
                //

                var wincards = xx.wincards[ scorekey[ 0 ] ] + 1;

                var match = 0;

                for (var cnt = 1; cnt < wincards; cnt++)
                {
                    if (match == 0)
                    {
                        if (scorekey[ cnt ] == hex1) match++;
                    }
                    else
                    {
                        if (scorekey[ cnt ] == hex2)
                        {
                            wins += xx.scoretable[ scorekey ].win;
                            hands++;

                            //console.log("texaspoker.dumpPreflopOdds: hands=" + hands + " " + scorekey + " " + xx.scoretable[ scorekey ].win);

                            break;
                        }
                    }
                }
            }

            wins /= hands;

            console.log("texaspoker.dumpPreflopOdds: hand=" + hex1 + hex2 + " wins=" + wins);
            //console.log("------------------------------------------------");
        }

        break;
    }
}

texaspoker.createFrame();
texaspoker.createGame();
texaspoker.onWindowResize();

texaspoker.dumpPreflopOdds();