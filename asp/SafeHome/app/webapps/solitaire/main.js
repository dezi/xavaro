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

    xx.gamesize    = Math.floor(Math.min(wid, hei) * 95 / 100);
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
    xx.buttonBot2div.onTouchClick = solitaire.onSolveStep;
    xx.buttonBot3div.onTouchClick = solitaire.onHintPlayer;
    */

    xx.buttonBot1div.onTouchClick = solitaire.onRedealOpen;
    xx.buttonBot3div.onTouchClick = solitaire.onHintPlayer;

    //
    // Create cards.
    //

    WebLibCards.newDeck(52);

    xx.doneHeaps = [];

    xx.doneHeaps[ 0 ] = [];
    xx.doneHeaps[ 1 ] = [];
    xx.doneHeaps[ 2 ] = [];
    xx.doneHeaps[ 3 ] = [];

    xx.doneHeaps[ 3 ][ 0 ] = solitaire.createCard(-51, false);
    xx.doneHeaps[ 3 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 3 ][ 0 ].style.left = (xx.fieldwidth  * 3) + "px";

    xx.doneHeaps[ 2 ][ 0 ] = solitaire.createCard(-50, false);
    xx.doneHeaps[ 2 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 2 ][ 0 ].style.left = (xx.fieldwidth  * 4) + "px";

    xx.doneHeaps[ 1 ][ 0 ] = solitaire.createCard(-49, false);
    xx.doneHeaps[ 1 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 1 ][ 0 ].style.left = (xx.fieldwidth  * 5) + "px";

    xx.doneHeaps[ 0 ][ 0 ] = solitaire.createCard(-48, false);
    xx.doneHeaps[ 0 ][ 0 ].style.top  = (xx.fieldheight * 0) + "px";
    xx.doneHeaps[ 0 ][ 0 ].style.left = (xx.fieldwidth  * 6) + "px";

    xx.playHeaps = [];

    for (var inx = 0; inx < 7; inx++)
    {
        xx.playHeaps[ inx ] = [];

        var ph = xx.playHeaps[ inx ];

        for (var cnt = 0; cnt <= inx; cnt++)
        {
            ph[ cnt ] = solitaire.createCard(WebLibCards.getCard(), (cnt != inx));
            ph[ cnt ].style.top  = (xx.fieldheight * (1 + (0.2 * cnt))) + "px";
            ph[ cnt ].style.left = (xx.fieldwidth  * inx) + "px";
        }
   }

    //
    // Heap cards.
    //

    xx.heapCards = [];

    while (WebLibCards.getCount())
    {
        var heapCard = solitaire.createCard(WebLibCards.getCard(), true);
        heapCard.style.top  = (xx.fieldheight * 0) + "px";
        heapCard.style.left = (xx.fieldwidth  * 0) + "px";

        xx.heapCards.push(heapCard);
    }

    xx.openCards = [];

    xx.redealTimeout = setTimeout(xx.dealOpen, 150);

    addEventListener("resize", solitaire.onWindowResize);
}

solitaire.removeOpen = function()
{
    var xx = solitaire;

    xx.redealTimeout = null;

    if (xx.openCards.length > 0)
    {
        var cardDiv = xx.openCards.pop();

        cardDiv.style.top  = (xx.fieldheight * 0) + "px";
        cardDiv.style.left = (xx.fieldwidth  * 0) + "px";

        xx.showCard(cardDiv, false);

        xx.heapCards.unshift(cardDiv);

        if (xx.openCards.length > 0)
        {
            xx.redealTimeout = setTimeout(xx.removeOpen, 150);

            return;
        }
    }

    xx.redealTimeout = setTimeout(xx.dealOpen, 150);
}

solitaire.dealOpen = function()
{
    var xx = solitaire;

    xx.redealTimeout = null;

    if ((xx.openCards.length < 3) && (xx.heapCards.length > 0))
    {
        var cardDiv = xx.heapCards.pop();

        cardDiv.style.top    = (xx.fieldheight * 0) + "px";
        cardDiv.style.left   = (xx.fieldwidth  * (1 + (xx.openCards.length * 0.3))) + "px";
        cardDiv.style.zIndex = "" + (xx.openCards.length + 1);

        xx.showCard(cardDiv, true);

        xx.openCards.push(cardDiv);

        xx.redealTimeout = setTimeout(xx.dealOpen, 150);
    }
}

solitaire.createCard = function(card, back)
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

    cardDiv.cardValue = Math.abs(card);
    cardDiv.isToken   = (card < 0);
    cardDiv.isBack    = back;

    cardDiv.onTouchClick = solitaire.onClickCard;

    cardDiv.backImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.backImg.style.position = "absolute";
    cardDiv.backImg.style.left     = "0px";
    cardDiv.backImg.style.top      = "0px";
    cardDiv.backImg.style.width    = "auto";
    cardDiv.backImg.style.height   = "100%";
    cardDiv.backImg.src = cardDiv.isToken
        ? WebLibCards.getCardDimmUrl()
        : WebLibCards.getCardBackgroundUrl(false);

    cardDiv.cardImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.cardImg.style.position = "absolute";
    cardDiv.cardImg.style.left     = "0px";
    cardDiv.cardImg.style.top      = "0px";
    cardDiv.cardImg.style.width    = "auto";
    cardDiv.cardImg.style.height   = "100%";
    cardDiv.cardImg.src = WebLibCards.getCardImageUrl(cardDiv.cardValue);

    cardDiv.flipImg = WebLibSimple.createAnyAppend("img", cardDiv);
    cardDiv.flipImg.style.display  = "none";
    cardDiv.flipImg.style.position = "absolute";
    cardDiv.flipImg.style.left     = "0px";
    cardDiv.flipImg.style.top      = "0px";
    cardDiv.flipImg.style.width    = "auto";
    cardDiv.flipImg.style.height   = "100%";
    cardDiv.flipImg.src = WebLibCards.getCardBacksideUrl();

    if (cardDiv.isBack)
    {
        cardDiv.cardImg.style.display = "none";
        cardDiv.flipImg.style.display = "block";
    }

    return cardDiv;
}

solitaire.showCard = function(cardDiv, show)
{
    if (show)
    {
        cardDiv.cardImg.style.display = "block";
        cardDiv.flipImg.style.display = "none";
        cardDiv.isBack = false;
    }
    else
    {
        cardDiv.cardImg.style.display = "none";
        cardDiv.flipImg.style.display = "block";
        cardDiv.isBack = true;
    }
}

solitaire.checkValidSelect = function(cardDiv, select)
{
    var xx = solitaire;

    xx.checkTimeout = null;

    //
    // Check open cards heap.
    //

    for (var inx = 0; inx < xx.openCards.length - 1; inx++)
    {
        if (xx.openCards[ inx ].select)
        {
            xx.selectCard(xx.openCards[ inx ], false);
        }
    }
}

solitaire.selectCard = function(cardDiv, select)
{
    var xx = solitaire;

    if (! cardDiv.isBack)
    {
        if (cardDiv.isToken && ! select)
        {
            cardDiv.backImg.src = WebLibCards.getCardDimmUrl();
        }
        else
        {
            cardDiv.backImg.src = WebLibCards.getCardBackgroundUrl(select);
        }

        cardDiv.select = select;

        if (cardDiv.select)
        {
            if (xx.checkTimeout) removeTimeout(xx.checkTimeout);
            xx.checkTimeout = setTimeout(xx.checkValidSelect, 500);
        }
    }
}

solitaire.toggleCard = function(cardDiv)
{
    if (! cardDiv.isBack)
    {
        solitaire.selectCard(cardDiv, ! cardDiv.select);
    }
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

solitaire.onRedealOpen = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = solitaire;

    if (! xx.redealTimeout) xx.removeOpen();
}

solitaire.onClickCard = function(target, ctarget)
{
    WebAppUtility.makeClick();

    var xx = solitaire;

    //
    // Check if heap card.
    //

    for (var inx = 0; inx < xx.heapCards.length; inx++)
    {
        if (target == xx.heapCards[ inx ])
        {
            if (! xx.redealTimeout) xx.removeOpen();

            return;
        }
    }

    xx.toggleCard(target);
}

solitaire.createFrame();
solitaire.onWindowResize();
