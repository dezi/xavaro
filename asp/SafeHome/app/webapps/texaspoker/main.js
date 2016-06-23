texaspoker.createFrame = function()
{
    var xx = texaspoker;

    xx.topdiv = WebLibSimple.createDiv(0, 0, 0, 0, "topdiv", document.body);
    WebLibSimple.setFontSpecs(xx.topdiv, 32, "bold");
    WebLibSimple.setBGColor(xx.topdiv, "#dddddd");
    xx.topdiv.style.overflow = "hidden";
    xx.topdiv.style.backgroundImage = "url('walls/green-wool.jpg')";;

    xx.centerDiv = WebLibSimple.createDivWidHei("50%", "50%", 1, 1, "centerDiv", xx.topdiv);

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    //
    // Game panel.
    //

    xx.gamesize    = Math.floor(Math.min(wid, hei) * 85 / 100);
    xx.fieldwidth  = xx.gamesize / 6;
    xx.fieldheight = xx.gamesize / 3;

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

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

    xx.buttonBot3div.onTouchClick = texaspoker.onHintPlayer;

    //
    // Create cards.
    //

    xx.computCard1 = texaspoker.createCard();
    xx.computCard1.style.top  = (xx.fieldheight * 0) + "px";
    xx.computCard1.style.left = (xx.fieldwidth  * 0) + "px";

    xx.computCard2 = texaspoker.createCard();
    xx.computCard2.style.top  = (xx.fieldheight * 0) + "px";
    xx.computCard2.style.left = (xx.fieldwidth  * 1) + "px";

    xx.playerCard1 = texaspoker.createCard();
    xx.playerCard1.style.top  = (xx.fieldheight * 2) + "px";
    xx.playerCard1.style.left = (xx.fieldwidth  * 0) + "px";

    xx.playerCard2 = texaspoker.createCard();
    xx.playerCard2.style.top  = (xx.fieldheight * 2) + "0px";
    xx.playerCard2.style.left = (xx.fieldwidth  * 1) + "px";

    xx.flopCard1 = texaspoker.createCard();
    xx.flopCard1.style.top  = (xx.fieldheight * 1) + "px";
    xx.flopCard1.style.left = (xx.fieldwidth  * 0) + "px";

    xx.flopCard2 = texaspoker.createCard();
    xx.flopCard2.style.top  = (xx.fieldheight * 1) + "px";
    xx.flopCard2.style.left = (xx.fieldwidth  * 1) + "px";

    xx.flopCard3 = texaspoker.createCard();
    xx.flopCard3.style.top  = (xx.fieldheight * 1) + "px";
    xx.flopCard3.style.left = (xx.fieldwidth  * 2) + "px";

    xx.turnCard = texaspoker.createCard();
    xx.turnCard.style.top  = (xx.fieldheight * 1) + "px";
    xx.turnCard.style.left = (xx.fieldwidth  * 3.5) + "px";

    xx.riverCard = texaspoker.createCard();
    xx.riverCard.style.top  = (xx.fieldheight * 1) + "px";
    xx.riverCard.style.left = (xx.fieldwidth  * 5) + "px";

    //
    // Player score.
    //

    xx.computScore = texaspoker.createScore();
    xx.computScore.style.top  = (xx.fieldheight * 0.5) + "px";
    xx.computScore.style.left = (xx.fieldwidth  * 2.2) + "px";
    xx.computScore.innerHTML  = "--.-- %";

    xx.playerScore = texaspoker.createScore();
    xx.playerScore.style.top  = (xx.fieldheight * 2.5) + "px";
    xx.playerScore.style.left = (xx.fieldwidth  * 2.2) + "px";
    xx.playerScore.innerHTML  = "--.-- %";

    addEventListener("resize", texaspoker.onWindowResize);
}

texaspoker.createScore = function()
{
    var xx = texaspoker;

    var scoreDiv = WebLibSimple.createAnyAppend("div", xx.gamePanel);
    scoreDiv.style.position = "absolute";
    scoreDiv.style.width    = (xx.fieldwidth  * 4) + "px";
    scoreDiv.style.height   = (xx.fieldheight / 3) + "px";
    scoreDiv.style.color    = "#ffffff";

    WebLibSimple.setFontSpecs(scoreDiv, (xx.fieldheight / 5), "bold");

    return scoreDiv;
}

texaspoker.createCard = function()
{
    var xx = texaspoker;

    var marginx = Math.floor(xx.fieldwidth  * 0.1);
    var marginy = Math.floor(xx.fieldheight * 0.175);

    var wid = xx.fieldwidth  - marginx * 2;
    var hei = xx.fieldheight - marginy * 2;

    cardDiv = WebLibSimple.createAnyAppend("center", xx.gamePanel);
    cardDiv.style.position = "absolute";
    cardDiv.style.width    = wid + "px";
    cardDiv.style.height   = hei + "px";
    cardDiv.style.marginLeft   = marginx + "px";
    cardDiv.style.marginRight  = marginx + "px";
    cardDiv.style.marginTop    = marginy + "px";
    cardDiv.style.marginBottom = marginy + "px";

    cardDiv.backImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.backImg.style.position = "absolute";
    cardDiv.backImg.style.left     = "0px";
    cardDiv.backImg.style.top      = "0px";
    cardDiv.backImg.style.width    = "auto";
    cardDiv.backImg.style.height   = "100%";
    cardDiv.backImg.src = WebLibCards.getCardBackgroundUrl(false);

    cardDiv.cardImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.cardImg.style.position = "absolute";
    cardDiv.cardImg.style.left     = "0px";
    cardDiv.cardImg.style.top      = "0px";
    cardDiv.cardImg.style.width    = "auto";
    cardDiv.cardImg.style.height   = "100%";

    return cardDiv;
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

        var strength = Math.floor(wins / (wins + looses + splits) * 10000) / 100.0;

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
        xx.card2val[ inx ] = inx >> 2;
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

texaspoker.getHeadsUpPercent = function(hand1, hand2)
{
    var xx = texaspoker;

    var wins = {};

    if (hand1 > hand2)
    {
        var vals = xx.starthands[ hand1 + hand2 ];

        wins.hand1 = vals[ 0 ];
        wins.split = vals[ 1 ];
        wins.hand2 = xx.totalhands - wins.hand1 - wins.split;
    }
    else
    {
        var vals = xx.starthands[ hand2 + hand1 ];

        wins.hand2 = vals[ 0 ];
        wins.split = vals[ 1 ];
        wins.hand1 = xx.totalhands - wins.hand2 - wins.split;
    }

    wins.percent1 = wins.hand1 / (wins.hand1 + wins.split + wins.hand2);
    wins.percent2 = wins.hand2 / (wins.hand1 + wins.split + wins.hand2);

    wins.percent1 = Math.floor(wins.percent1 * 10000) / 100;
    wins.percent2 = Math.floor(wins.percent2 * 10000) / 100;

    return wins;
}

texaspoker.getHandPercent = function(hand)
{
    var xx = texaspoker;

    if (! xx.starthand[ hand ]) console.log("==========================>>>>hand=" + hand);

    var wins   = xx.starthand[ hand ][ 0 ];
    var looses = xx.starthand[ hand ][ 1 ];
    var splits = xx.starthand[ hand ][ 2 ];

    var strength = Math.floor(wins / (wins + looses + splits) * 10000) / 100.0;

    return strength;
}

texaspoker.onHintPlayer = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = texaspoker;

    WebLibCards.newDeck(52);

    xx.computCard1.cardValue = WebLibCards.getCard();
    xx.computCard2.cardValue = WebLibCards.getCard();

    if (xx.computCard2.cardValue > xx.computCard1.cardValue)
    {
        var tmp = xx.computCard2.cardValue;
        xx.computCard2.cardValue = xx.computCard1.cardValue;
        xx.computCard1.cardValue = tmp;
    }

    xx.computCard1.cardImg.src = WebLibCards.getCardImageUrl(xx.computCard1.cardValue);
    xx.computCard2.cardImg.src = WebLibCards.getCardImageUrl(xx.computCard2.cardValue);

    xx.playerCard1.cardValue = WebLibCards.getCard();
    xx.playerCard2.cardValue = WebLibCards.getCard();

    if (xx.playerCard2.cardValue > xx.playerCard1.cardValue)
    {
        var tmp = xx.playerCard2.cardValue;
        xx.playerCard2.cardValue = xx.playerCard1.cardValue;
        xx.playerCard1.cardValue = tmp;
    }

    xx.playerCard1.cardImg.src = WebLibCards.getCardImageUrl(xx.playerCard1.cardValue);
    xx.playerCard2.cardImg.src = WebLibCards.getCardImageUrl(xx.playerCard2.cardValue);

    xx.flopCard1.cardValue = WebLibCards.getCard();
    xx.flopCard2.cardValue = WebLibCards.getCard();
    xx.flopCard3.cardValue = WebLibCards.getCard();

    xx.flopCard1.cardImg.src = WebLibCards.getCardImageUrl(xx.flopCard1.cardValue);
    xx.flopCard2.cardImg.src = WebLibCards.getCardImageUrl(xx.flopCard2.cardValue);
    xx.flopCard3.cardImg.src = WebLibCards.getCardImageUrl(xx.flopCard3.cardValue);

    xx.turnCard.cardValue = WebLibCards.getCard();
    xx.turnCard.cardImg.src = WebLibCards.getCardImageUrl(xx.turnCard.cardValue);

    xx.riverCard.cardValue = WebLibCards.getCard();
    xx.riverCard.cardImg.src = WebLibCards.getCardImageUrl(xx.riverCard.cardValue);

    var c1 = xx.card2val[ xx.computCard1.cardValue ];
    var c2 = xx.card2val[ xx.computCard2.cardValue ];
    var cs = (xx.card2col[ xx.computCard1.cardValue ] == xx.card2col[ xx.computCard2.cardValue ]);
    var chand = xx.vals2hex[ c1 ] + xx.vals2hex[ c2 ] + (cs ? "s" : "");

    xx.computScore.innerHTML = xx.getHandPercent(chand) + " %";

    var p1 = xx.card2val[ xx.playerCard1.cardValue ];
    var p2 = xx.card2val[ xx.playerCard2.cardValue ];
    var ps = (xx.card2col[ xx.playerCard1.cardValue ] == xx.card2col[ xx.playerCard2.cardValue ]);
    var phand = xx.vals2hex[ p1 ] + xx.vals2hex[ p2 ] + (ps ? "s" : "");

    xx.playerScore.innerHTML = xx.getHandPercent(phand) + " %";

    var headsup = texaspoker.getHeadsUpPercent(chand, phand);

    console.log("=====================>:::::" + headsup.hand1 + " " + headsup.split + " " + headsup.hand2);
    console.log("=====================>:::::" + headsup.percent1 + " " + headsup.percent2);

    xx.computScore.innerHTML += " – " + headsup.percent1 + " %";
    xx.playerScore.innerHTML += " – " + headsup.percent2 + " %";
}

texaspoker.createFrame();
texaspoker.createGame();
texaspoker.onWindowResize();
texaspoker.onHintPlayer();
