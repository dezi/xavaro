solitaire.createFrame = function()
{
    var xx = solitaire;

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
    xx.fieldwidth  = xx.gamesize / 7;
    xx.fieldheight = xx.gamesize / 4;

    xx.gamePanel = WebLibSimple.createDivWidHei(
        -xx.gamesize / 2, -xx.gamesize / 2,
        xx.gamesize, xx.gamesize,
        "gamePanel", xx.centerDiv);

    //
    // Buttons.
    //

    xx.buttonTop1div = solitaire.createButton("–");
    xx.buttonTop2div = solitaire.createButton("$");
    xx.buttonTop3div = solitaire.createButton("+");

    xx.buttonBot1div = solitaire.createButton("!");
    xx.buttonBot2div = solitaire.createButton("=");
    xx.buttonBot3div = solitaire.createButton("?");

    /*
    xx.buttonTop1div.onTouchClick = solitaire.onButtonMinus;
    xx.buttonTop3div.onTouchClick = solitaire.onButtonPlus;
    xx.buttonBot1div.onTouchClick = solitaire.onLoadNewGame;
    xx.buttonBot2div.onTouchClick = solitaire.onSolveStep;
    xx.buttonBot3div.onTouchClick = solitaire.onHintPlayer;
    */

    xx.buttonBot3div.onTouchClick = solitaire.onHintPlayer;

    //
    // Create cards.
    //

    xx.heapCard = solitaire.createCard();
    xx.heapCard.style.top  = (xx.fieldheight * 0) + "px";
    xx.heapCard.style.left = (xx.fieldwidth  * 0) + "px";
    xx.heapCard.cardImg.src = WebLibCards.getCardBacksideUrl();

    WebLibCards.newDeck(52);

    xx.openCards = [];

    xx.openCards[ 0 ] = solitaire.createCard();
    xx.openCards[ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.openCards[ 0 ].style.left = (xx.fieldwidth  * 1) + "px";
    xx.openCards[ 0 ].cardValue = WebLibCards.getCard();
    xx.openCards[ 0 ].cardImg.src = WebLibCards.getCardImageUrl(xx.openCards[ 0 ].cardValue);

    xx.openCards[ 1 ] = solitaire.createCard();
    xx.openCards[ 1 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.openCards[ 1 ].style.left = (xx.fieldwidth  * 1.3) + "px";
    xx.openCards[ 1 ].cardValue = WebLibCards.getCard();
    xx.openCards[ 1 ].cardImg.src = WebLibCards.getCardImageUrl(xx.openCards[ 1 ].cardValue);

    xx.openCards[ 2 ] = solitaire.createCard();
    xx.openCards[ 2 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.openCards[ 2 ].style.left = (xx.fieldwidth  * 1.6) + "px";
    xx.openCards[ 2 ].cardValue = WebLibCards.getCard();
    xx.openCards[ 2 ].cardImg.src = WebLibCards.getCardImageUrl(xx.openCards[ 2 ].cardValue);

    xx.doneHeaps = [];

    xx.doneHeaps[ 3 ] = solitaire.createCard();
    xx.doneHeaps[ 3 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 3 ].style.left = (xx.fieldwidth  * 3) + "px";

    xx.doneHeaps[ 2 ] = solitaire.createCard();
    xx.doneHeaps[ 2 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 2 ].style.left = (xx.fieldwidth  * 4) + "px";

    xx.doneHeaps[ 1 ] = solitaire.createCard();
    xx.doneHeaps[ 1 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 1 ].style.left = (xx.fieldwidth  * 5) + "px";

    xx.doneHeaps[ 0 ] = solitaire.createCard();
    xx.doneHeaps[ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 0 ].style.left = (xx.fieldwidth  * 6) + "px";

    xx.doneHeaps[ 3 ].cardValue = 51;
    xx.doneHeaps[ 2 ].cardValue = 50;
    xx.doneHeaps[ 1 ].cardValue = 49;
    xx.doneHeaps[ 0 ].cardValue = 48;

    xx.doneHeaps[ 3 ].cardImg.src = WebLibCards.getCardImageUrl(xx.doneHeaps[ 3 ].cardValue);
    xx.doneHeaps[ 2 ].cardImg.src = WebLibCards.getCardImageUrl(xx.doneHeaps[ 2 ].cardValue);
    xx.doneHeaps[ 1 ].cardImg.src = WebLibCards.getCardImageUrl(xx.doneHeaps[ 1 ].cardValue);
    xx.doneHeaps[ 0 ].cardImg.src = WebLibCards.getCardImageUrl(xx.doneHeaps[ 0 ].cardValue);

    addEventListener("resize", solitaire.onWindowResize);
}

solitaire.createCard = function()
{
    var xx = solitaire;

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

    cardDiv.cardImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.cardImg.style.width  = "auto";
    cardDiv.cardImg.style.height = "100%";

    return cardDiv;
}

solitaire.createButton = function(text)
{
    var xx = solitaire;

    var wid = xx.topdiv.clientWidth;
    var hei = xx.topdiv.clientHeight;

    var butsiz = Math.floor((Math.max(wid, hei) - xx.gamesize) * 75 / 200);
    var fntsiz = Math.floor(butsiz * 70 / 100);

    var buttonDiv;
    var buttonImg;

    buttonDiv = WebLibSimple.createDivWidHei(null, null, butsiz, butsiz, null, solitaire.topdiv);
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

solitaire.onWindowResize = function()
{
    var xx = solitaire;

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

solitaire.onHintPlayer = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = solitaire;

    WebLibCards.newDeck(52);
}

solitaire.createFrame();
solitaire.onWindowResize();
